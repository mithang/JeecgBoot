package org.jeecg.common.es;

/**
 * Dùng để tạo queryString của ElasticSearch queryString
 *
 * @author sunjianlei
 */
public class QueryStringBuilder {

    StringBuilder builder;

    public QueryStringBuilder(String field, String str, boolean not, boolean addQuot) {
        builder = this.createBuilder(field, str, not, addQuot);
    }

    public QueryStringBuilder(String field, String str, boolean not) {
        builder = this.createBuilder(field, str, not, true);
    }

    /**
     * Tạo StringBuilder
     *
     * @param field
     * @param str
     * @param not     Có phải là không khớp không
     * @param addQuot Có thêm dấu ngoặc kép không
     * @return
     */
    public StringBuilder createBuilder(String field, String str, boolean not, boolean addQuot) {
        StringBuilder sb = new StringBuilder(field).append(":(");
        if (not) {
            sb.append(" NOT ");
        }
        this.addQuotEffect(sb, str, addQuot);
        return sb;
    }

    public QueryStringBuilder and(String str) {
        return this.and(str, true);
    }

    public QueryStringBuilder and(String str, boolean addQuot) {
        builder.append(" AND ");
        this.addQuot(str, addQuot);
        return this;
    }

    public QueryStringBuilder or(String str) {
        return this.or(str, true);
    }

    public QueryStringBuilder or(String str, boolean addQuot) {
        builder.append(" OR ");
        this.addQuot(str, addQuot);
        return this;
    }

    public QueryStringBuilder not(String str) {
        return this.not(str, true);
    }

    public QueryStringBuilder not(String str, boolean addQuot) {
        builder.append(" NOT ");
        this.addQuot(str, addQuot);
        return this;
    }

    /**
    * Thêm dấu ngoặc kép (tìm kiếm mờ, không được thêm dấu ngoặc kép)
    */
    private QueryStringBuilder addQuot(String str, boolean addQuot) {
        return this.addQuotEffect(this.builder, str, addQuot);
    }

    /**
     * Có thêm dấu ngoặc kép ở hai bên không
     * @param builder
     * @param str
     * @param addQuot
     * @return
     */
    private QueryStringBuilder addQuotEffect(StringBuilder builder, String str, boolean addQuot) {
        if (addQuot) {
            builder.append('"');
        }
        builder.append(str);
        if (addQuot) {
            builder.append('"');
        }
        return this;
    }

    @Override
    public String toString() {
        return builder.append(")").toString();
    }

}
