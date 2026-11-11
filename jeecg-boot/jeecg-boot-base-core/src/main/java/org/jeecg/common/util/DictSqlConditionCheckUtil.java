package org.jeecg.common.util;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Công cụ kiểm chứng cấu trúc SQL của điều kiện từ điển phía frontend.
 *
 * @author scott
 * @since 2026-08-25 issues/9840 Phòng chống SQL injection cho điều kiện lọc từ điển
 */
public final class DictSqlConditionCheckUtil {

	/**
	 * Tên trường điều kiện: chữ cái/Số//số/gạch dưới tạo thành định danh, có thể mang dấu backtick hoặc dấu ngoặc kép, có thể mang tiền tố bí danh bảng
	 */
	private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile(
			"[`\"]?[\\p{L}_][\\p{L}\\p{N}_]*[`\"]?(\\.[`\"]?[\\p{L}_][\\p{L}\\p{N}_]*[`\"]?)*");

	private DictSqlConditionCheckUtil() {
	}

	/**
	 * Kiểm chứng cú pháp điều kiện từ điển và trả về các trường được dùng trong điều kiện.
	 *
	 * @param value Điều kiện lọc từ điển
	 * @return Tập hợp trường điều kiện
	 * @throws IllegalArgumentException Điều kiện chứa cấu trúc SQL không được hỗ trợ hoặc không an toàn
	 */
	public static Set<String> checkAndGetFields(String value) {
		if (value == null || value.trim().isEmpty()) {
			return Set.of();
		}
		String trimmed = value.trim();
		Set<String> conditionFields = new LinkedHashSet<>();
		try {
			//update-begin---author:scott ---date:20261027  for：【GHSA-jgwm-2xxm-mmm4/GHSA-xmw4-rx44-4jcp】Cấm dấu gạch chéo ngược, JSqlParser không coi\là ký tự escape nhưng MySQL thì có, hai bên hiểu ranh giới chuỗi không nhất quán nên có thể bypass kiểm chứng-----------
			if (trimmed.indexOf('\\') >= 0) {
				throw new IllegalArgumentException("Điều kiện lọc từ điển không được chứa dấu gạch chéo ngược");
			}
			//update-end---author:scott ---date:20261027  for：【GHSA-jgwm-2xxm-mmm4/GHSA-xmw4-rx44-4jcp】Cấm dấu gạch chéo ngược, JSqlParser không coi\là ký tự escape nhưng MySQL thì có, hai bên hiểu ranh giới chuỗi không nhất quán nên có thể bypass kiểm chứng-----------
			if (containsSqlComment(trimmed)) {
				throw new IllegalArgumentException("Điều kiện lọc từ điển không được chứa chú thích SQL");
			}
			int orderByIndex = findTopLevelOrderBy(trimmed);
			String conditionSql = orderByIndex < 0 ? trimmed : trimmed.substring(0, orderByIndex).trim();
			String orderBySql = orderByIndex < 0 ? null
					: trimmed.substring(orderByIndex).replaceFirst("(?i)^order\\s+by\\s+", "").trim();
			if ((!conditionSql.isEmpty() && !validateCondition(conditionSql, conditionFields))
					|| (orderBySql != null && !validateOrderBy(orderBySql, conditionFields))) {
				throw new IllegalArgumentException("Điều kiện lọc từ điển không được hỗ trợ");
			}
			return conditionFields;
		} catch (Exception e) {
			throw new IllegalArgumentException("Điều kiện lọc từ điển không được hỗ trợ", e);
		}
	}

	private static boolean validateCondition(String value, Set<String> conditionFields) throws Exception {
		CCJSqlParser parser = CCJSqlParserUtil.newParser(value);
		Expression expression = parser.Expression();
		return parser.getNextToken().kind == CCJSqlParserConstants.EOF
				&& validateExpression(expression, conditionFields);
	}

	private static boolean validateOrderBy(String value, Set<String> conditionFields) throws Exception {
		if (value.isEmpty()) {
			return false;
		}
		for (String orderItem : value.split(",")) {
			String[] parts = orderItem.trim().split("\\s+");
			if (parts.length == 0 || parts.length > 2
					|| (parts.length == 2 && !"ASC".equalsIgnoreCase(parts[1]) && !"DESC".equalsIgnoreCase(parts[1]))) {
				return false;
			}
			CCJSqlParser parser = CCJSqlParserUtil.newParser(parts[0]);
			Expression expression = parser.Expression();
			if (parser.getNextToken().kind != CCJSqlParserConstants.EOF
					|| !addConditionField(expression, conditionFields)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Định vị ORDER BY nằm ngoài chuỗi và dấu ngoặc, tránh nhận nhầm văn bản thông thường trong giá trị điều kiện.
	 */
	private static int findTopLevelOrderBy(String value) {
		char quote = 0;
		int depth = 0;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (quote != 0) {
				if (current == '\\' && i + 1 < value.length()) {
					i++;
				} else if (current == quote) {
					if (i + 1 < value.length() && value.charAt(i + 1) == quote) {
						i++;
					} else {
						quote = 0;
					}
				}
				continue;
			}
			if (current == '\'' || current == '"' || current == '`') {
				quote = current;
			} else if (current == '(') {
				depth++;
			} else if (current == ')') {
				depth--;
			} else if (depth == 0 && isKeyword(value, i, "order")) {
				int byIndex = i + "order".length();
				if (byIndex < value.length() && Character.isWhitespace(value.charAt(byIndex))) {
					while (byIndex < value.length() && Character.isWhitespace(value.charAt(byIndex))) {
						byIndex++;
					}
					if (isKeyword(value, byIndex, "by")) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	private static boolean isKeyword(String value, int start, String keyword) {
		if (start < 0 || start + keyword.length() > value.length()
				|| !value.regionMatches(true, start, keyword, 0, keyword.length())) {
			return false;
		}
		boolean leftBoundary = start == 0 || !Character.isJavaIdentifierPart(value.charAt(start - 1));
		int end = start + keyword.length();
		boolean rightBoundary = end == value.length() || !Character.isJavaIdentifierPart(value.charAt(end));
		return leftBoundary && rightBoundary;
	}

	/**
	 * Chỉ nhận dạng chú thích SQL nằm ngoài chuỗi và định danh được trích dẫn, tránh nhận nhầm ký hiệu chú thích trong văn bản thông thường.
	 */
	private static boolean containsSqlComment(String value) {
		char quote = 0;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (quote != 0) {
				if (current == '\\' && i + 1 < value.length()) {
					i++;
				} else if (current == quote) {
					if (i + 1 < value.length() && value.charAt(i + 1) == quote) {
						i++;
					} else {
						quote = 0;
					}
				}
				continue;
			}
			if (current == '\'' || current == '"' || current == '`') {
				quote = current;
				continue;
			}
			//update-begin---author:scott ---date:20261027  for：【GHSA-xmw4-rx44-4jcp】Dấu # của MySQL#cũng là chú thích dòng-----------
			if (current == '#') {
				return true;
			}
			//update-end---author:scott ---date:20261027  for：【GHSA-xmw4-rx44-4jcp】Dấu # của MySQL#cũng là chú thích dòng-----------
			if (i + 1 < value.length()
					&& ((current == '-' && value.charAt(i + 1) == '-')
					|| (current == '/' && value.charAt(i + 1) == '*'))) {
				return true;
			}
		}
		return false;
	}

	private static boolean validateExpression(Expression expression, Set<String> conditionFields) {
		if (expression instanceof ParenthesedExpressionList<?> expressionList) {
			return expressionList.size() == 1 && validateExpression(expressionList.get(0), conditionFields);
		}
		if (expression instanceof AndExpression || expression instanceof OrExpression) {
			BinaryExpression logicalExpression = (BinaryExpression) expression;
			return validateExpression(logicalExpression.getLeftExpression(), conditionFields)
					&& validateExpression(logicalExpression.getRightExpression(), conditionFields);
		}
		if (isComparisonExpression(expression)) {
			BinaryExpression comparison = (BinaryExpression) expression;
			return validateComparisonExpression(comparison, conditionFields);
		}
		if (expression instanceof LikeExpression likeExpression) {
			return addConditionField(likeExpression.getLeftExpression(), conditionFields)
					&& likeExpression.getRightExpression() instanceof StringValue
					&& (likeExpression.getEscape() == null || likeExpression.getEscape() instanceof StringValue);
		}
		if (expression instanceof InExpression inExpression) {
			return addConditionField(inExpression.getLeftExpression(), conditionFields)
					&& isLiteralList(inExpression.getRightExpression());
		}
		if (expression instanceof Between between) {
			return addConditionField(between.getLeftExpression(), conditionFields)
					&& isLiteral(between.getBetweenExpressionStart())
					&& isLiteral(between.getBetweenExpressionEnd());
		}
		if (expression instanceof IsNullExpression isNullExpression) {
			return addConditionField(isNullExpression.getLeftExpression(), conditionFields);
		}
		return false;
	}

	private static boolean validateComparisonExpression(BinaryExpression comparison, Set<String> conditionFields) {
		Expression leftExpression = comparison.getLeftExpression();
		Expression rightExpression = comparison.getRightExpression();
		//update-begin---author:scott ---date:20260831  for：【issues/9840】Tương thích 1=1 Điều kiện giữ chỗ của từ điển-----------
		if (comparison instanceof EqualsTo && isLiteral(leftExpression) && isLiteral(rightExpression)) {
			return true;
		}
		//update-end---author:scott ---date:20260831  for：【issues/9840】Tương thích 1=1 Điều kiện giữ chỗ của từ điển-----------
		return addConditionField(leftExpression, conditionFields) && isLiteral(rightExpression);
	}

	private static boolean isComparisonExpression(Expression expression) {
		return expression instanceof EqualsTo
				|| expression instanceof NotEqualsTo
				|| expression instanceof GreaterThan
				|| expression instanceof GreaterThanEquals
				|| expression instanceof MinorThan
				|| expression instanceof MinorThanEquals;
	}

	private static boolean addConditionField(Expression expression, Set<String> conditionFields) {
		if (!(expression instanceof Column column)) {
			return false;
		}
		//update-begin---author:scott ---date:20261027  for：【GHSA-xmw4-rx44-4jcp】Tên trường chỉ cho phép định danh thông thường, ngăn password#các trường hợp tương tự bypass kiểm tra trường nhạy cảm-----------
		if (!COLUMN_NAME_PATTERN.matcher(column.getFullyQualifiedName()).matches()) {
			return false;
		}
		//update-end---author:scott ---date:20261027  for：【GHSA-xmw4-rx44-4jcp】Tên trường chỉ cho phép định danh thông thường, ngăn password#các trường hợp tương tự bypass kiểm tra trường nhạy cảm-----------
		conditionFields.add(column.getFullyQualifiedName());
		return true;
	}

	private static boolean isLiteralList(Expression expression) {
		if (!(expression instanceof ExpressionList<?> expressionList) || expressionList.isEmpty()) {
			return false;
		}
		return expressionList.stream().allMatch(DictSqlConditionCheckUtil::isLiteral);
	}

	private static boolean isLiteral(Expression expression) {
		if (expression instanceof LongValue || expression instanceof DoubleValue || expression instanceof StringValue) {
			return true;
		}
		if (expression instanceof SignedExpression signedExpression) {
			char sign = signedExpression.getSign();
			Expression number = signedExpression.getExpression();
			return (sign == '-' || sign == '+') && (number instanceof LongValue || number instanceof DoubleValue);
		}
		return false;
	}
}
