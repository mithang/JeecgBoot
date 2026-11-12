package org.jeecg.modules.airag.llm.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.flow.component.ToolsNode;
import org.jeecg.modules.airag.llm.consts.FlowPluginContent;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Bộ xây dựng công cụ plugin
 * Theo cấu hình plugin, xây dựng ToolSpecification và ToolExecutor
 * for [QQYUN-12453]【AI】Hỗ trợ plugin
 *
 * @author chenrui
 * @date 2025/10/30
 */
@Slf4j
public class PluginToolBuilder {

	private static final String PLUGIN_UNAVAILABLE_MESSAGE = "Plugin đã bị vô hiệu hóa hoặc xóa, lần này sẽ không gọi công cụ này nữa. Vui lòng tiếp tục hoàn thành các nhiệm vụ còn lại.";

    /**
     * Xây dựng công cụ từ cấu hình plugin Map
     *
     * @param airagMcp Cấu hình plugin
     * @return Map<ToolSpecification, ToolExecutor>
     */
    public static Map<ToolSpecification, ToolExecutor> buildTools(AiragMcp airagMcp, HttpServletRequest currentHttpRequest) {
		return buildTools(airagMcp, currentHttpRequest, null);
	}

	/**
	 * Xây dựng Map công cụ từ cấu hình plugin, và kiểm tra plugin còn khả dụng không trước khi thực thi yêu cầu HTTP.
	 *
	 * @param airagMcp Cấu hình plugin
	 * @param currentHttpRequest Yêu cầu hiện tại
	 * @param availableChecker Bộ kiểm tra trạng thái khả dụng theo thời gian thực của plugin, không kiểm tra khi trống
	 * @return Map&lt;ToolSpecification, ToolExecutor&gt;
	 * @author scott
	 * @since 2026-08-10 Ngay sau khi plugin bị vô hiệu hóa, chặn phiên đã tải tiếp tục gọi
	 */
	public static Map<ToolSpecification, ToolExecutor> buildTools(AiragMcp airagMcp, HttpServletRequest currentHttpRequest,
			BooleanSupplier availableChecker) {
        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();
        if (airagMcp == null || oConvertUtils.isEmpty(airagMcp.getTools())) {
            return tools;
        }

        try {
            JSONArray toolsArray = JSONArray.parseArray(airagMcp.getTools());
            if (toolsArray == null || toolsArray.isEmpty()) {
                return tools;
            }

            String baseUrl = airagMcp.getEndpoint();
            boolean isEmptyBaseUrl = oConvertUtils.isEmpty(baseUrl);
            // Nếu baseUrl trống, sử dụng địa chỉ hệ thống hiện tại
            if (isEmptyBaseUrl) {
                if (currentHttpRequest != null) {
                    baseUrl = CommonUtils.getBaseUrl(currentHttpRequest);
                    log.info("Plugin[{}]có BaseURL trống, sử dụng địa chỉ hệ thống: {}", airagMcp.getName(), baseUrl);
                } else {
                    log.warn("Plugin[{}]có BaseURL trống và không lấy được địa chỉ hệ thống, bỏ qua việc xây dựng công cụ", airagMcp.getName());
                    return tools;
                }
            }

            // Phân tích cú pháp headers
            Map<String, String> headersMap = parseHeaders(airagMcp.getHeaders());

            // Xác định có cần ký không
            boolean isNeedSign = isEmptyBaseUrl && ToolsNode.Helper.checkNeedSign(headersMap);

            // Phân tích và áp dụng cấu hình ủy quyền (đọc từ metadata)
            applyAuthConfig(headersMap, airagMcp.getMetadata(), currentHttpRequest);

            for (int i = 0; i < toolsArray.size(); i++) {
                JSONObject toolConfig = toolsArray.getJSONObject(i);
                if (toolConfig == null) {
                    continue;
                }
                try {
                    ToolSpecification spec = buildToolSpecification(toolConfig);
                    ToolExecutor executor = buildToolExecutor(toolConfig, baseUrl, headersMap, isNeedSign, airagMcp.getName(), availableChecker);
                    if (spec != null && executor != null) {
                        tools.put(spec, executor);
                    }
                } catch (Exception e) {
                    log.error("Xây dựng công cụ plugin thất bại, cấu hình công cụ: {}", toolConfig.toJSONString(), e);
                }
            }
        } catch (Exception e) {
            log.error("Phân tích cấu hình công cụ plugin thất bại, plugin: {}", airagMcp.getName(), e);
        }

        return tools;
    }

    /**
     * Xây dựng ToolSpecification
     */
    private static ToolSpecification buildToolSpecification(JSONObject toolConfig) {
        String name = toolConfig.getString("name");
        String description = toolConfig.getString("description");

        if (oConvertUtils.isEmpty(name) || oConvertUtils.isEmpty(description)) {
            log.warn("Cấu hình công cụ thiếu trường name hoặc description");
            return null;
        }

        // Xây dựng thông tin mô tả đầy đủ (bao gồm cấu hình tham số phản hồi)
        StringBuilder fullDescription = new StringBuilder(description);
        
        // Phân tích tham số phản hồi và nối vào mô tả
        JSONArray responses = toolConfig.getJSONArray("responses");
        if (responses != null && !responses.isEmpty()) {
            fullDescription.append("\n\n Mô tả giá trị trả về:");
            for (int i = 0; i < responses.size(); i++) {
                JSONObject responseParam = responses.getJSONObject(i);
                if (responseParam == null) {
                    continue;
                }
                String paramName = responseParam.getString("name");
                String paramDesc = responseParam.getString("description");
                String paramType = responseParam.getString("type");

                if (oConvertUtils.isEmpty(paramName)) {
                    continue;
                }

                fullDescription.append("\n- ").append(paramName);
                if (oConvertUtils.isNotEmpty(paramType)) {
                    fullDescription.append(" (").append(paramType).append(")");
                }
                if (oConvertUtils.isNotEmpty(paramDesc)) {
                    fullDescription.append(": ").append(paramDesc);
                }
            }
        }

        JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();

        // Phân tích tham số yêu cầu
        JSONArray parameters = toolConfig.getJSONArray("parameters");
        if (parameters != null && !parameters.isEmpty()) {
            List<String> requiredParams = new ArrayList<>();
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                if (param == null) {
                    continue;
                }
                //update-begin---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
                // Tham số cố định phía máy chủ không expose cho mô hình, bộ thực thi công cụ sẽ tự động chèn theo defaultValue
                if (Boolean.TRUE.equals(param.getBoolean("hidden"))) {
                    continue;
                }
                //update-end---author:wangshuai ---date:20260804  for：【LHZP-1591] Mô hình Zhipu không thể ghi thông tin ứng dụng AI vào kho bộ nhớ-----------
                String paramName = param.getString("name");
                String paramDesc = param.getString("description");
                String paramType = param.getString("type");

                if (oConvertUtils.isEmpty(paramName)) {
                    continue;
                }

                // Thêm thuộc tính theo loại tham số
                if ("String".equalsIgnoreCase(paramType) || "string".equalsIgnoreCase(paramType)) {
                    schemaBuilder.addStringProperty(paramName, paramDesc != null ? paramDesc : "");
                } else if ("Number".equalsIgnoreCase(paramType) || "number".equalsIgnoreCase(paramType)
                        || "Integer".equalsIgnoreCase(paramType) || "integer".equalsIgnoreCase(paramType)) {
                    schemaBuilder.addNumberProperty(paramName, paramDesc != null ? paramDesc : "");
                } else if ("Boolean".equalsIgnoreCase(paramType) || "boolean".equalsIgnoreCase(paramType)) {
                    schemaBuilder.addBooleanProperty(paramName, paramDesc != null ? paramDesc : "");
                } else {
                    // Mặc định xử lý dưới dạng String
                    schemaBuilder.addStringProperty(paramName, paramDesc != null ? paramDesc : "");
                }

                // Kiểm tra có bắt buộc không
                Boolean required = param.getBooleanValue("required");
                if (required != null && required) {
                    requiredParams.add(paramName);
                }
            }

            if (!requiredParams.isEmpty()) {
                schemaBuilder.required(requiredParams.toArray(new String[0]));
            }
        }

        return ToolSpecification.builder()
                .name(name)
                .description(fullDescription.toString())
                .parameters(schemaBuilder.build())
                .build();
    }

    /**
     * Xây dựng ToolExecutor
     */
    private static ToolExecutor buildToolExecutor(JSONObject toolConfig, String baseUrl, Map<String, String> defaultHeaders, boolean isNeedSign,
			String pluginName, BooleanSupplier availableChecker) {
        String path = toolConfig.getString("path");
        String method = toolConfig.getString("method");
        String toolName = toolConfig.getString("name");
        JSONArray parameters = toolConfig.getJSONArray("parameters");

        if (oConvertUtils.isEmpty(path) || oConvertUtils.isEmpty(method)) {
            log.warn("Cấu hình công cụ thiếu trường path hoặc method");
            return null;
        }

        return (toolExecutionRequest, memoryId) -> {
            try {
				//update-begin---author:scott ---date:20260810  for: Sau khi vô hiệu hóa plugin, chặn ngay phiên đã tải tiếp tục gọi---
				if (availableChecker != null && !availableChecker.getAsBoolean()) {
					log.warn("Plugin[{}]Đã bị vô hiệu hóa hoặc xóa, dừng công cụ[{}]Gọi", pluginName, toolName);
					return PLUGIN_UNAVAILABLE_MESSAGE;
				}
				//update-end---author:scott ---date:20260810  for: Sau khi vô hiệu hóa plugin, chặn ngay phiên đã tải tiếp tục gọi---
                // Phân tích tham số AI truyền vào
                JSONObject args = JSONObject.parseObject(toolExecutionRequest.arguments());

                // Xây dựng đầy đủ URL
                String url = buildUrl(baseUrl, path, parameters, args);

                // Xây dựng phương thức yêu cầu
                HttpMethod httpMethod = parseHttpMethod(method);

                // Xây dựng request header
                HttpHeaders httpHeaders = buildHttpHeaders(parameters, args, defaultHeaders);

                // Xây dựng tham số yêu cầu
                JSONObject urlVariables = buildUrlVariables(parameters, args);
                Object body = buildRequestBody(parameters, args, httpHeaders);

                if (isNeedSign) {
                    // Ký trước khi gửi yêu cầu
                    ToolsNode.Helper.applySignature(url, httpHeaders, urlVariables, body);
                }

                // Gửi yêu cầu HTTP,Tăng thời gian timeout
                ResponseEntity<String> response = RestUtil.request(url, httpMethod, httpHeaders, urlVariables, body, String.class, AiragConsts.DEFAULT_TIMEOUT * 1000);

                // Trả về trực tiếp chuỗi phản hồi gốc, không phân tích
                return response.getBody() != null ? response.getBody() : "";
            } catch (HttpClientErrorException e) {
                log.error("Yêu cầu HTTP của công cụ plugin thất bại: {}", e.getMessage(), e);
                //update-begin---author:wangshuai---date:2026-01-16---for:【QQYUN-14577] Tìm kiếm hình ảnh thất bại sẽ khiến quá trình không thể tiếp tục---
                return "Gọi plugin thất bại (HTTP " + e.getStatusCode() + "）：" + e.getResponseBodyAsString()
                        + ". Vui lòng tiếp tục hoàn thành các nhiệm vụ còn lại.";
                //update-end---author:wangshuai---date:2026-01-16---for:【QQYUN-14577] Tìm kiếm hình ảnh thất bại sẽ khiến quá trình không thể tiếp tục---
            } catch (Exception e) {
                log.error("Thực thi công cụ plugin thất bại: {}", e.getMessage(), e);
                //update-begin---author:wangshuai---date:2026-01-16---for:【QQYUN-14577] Tìm kiếm hình ảnh thất bại sẽ khiến quá trình không thể tiếp tục---
                return "Thực thi công cụ plugin thất bại:" + e.getMessage()
                        + ". Vui lòng tiếp tục hoàn thành các nhiệm vụ còn lại.";
                //update-end---author:wangshuai---date:2026-01-16---for:【QQYUN-14577] Tìm kiếm hình ảnh thất bại sẽ khiến quá trình không thể tiếp tục---
            }
        };
    }

    /**
     * Xây dựng URL đầy đủ (xử lý tham số Path)
     */
    private static String buildUrl(String baseUrl, String path, JSONArray parameters, JSONObject args) {
        String fullPath = path;
        if (!path.startsWith("/")) {
            fullPath = "/" + path;
        }
        // Khi nối URL, tránh xuất hiện dấu gạch chéo kép
        if (baseUrl.endsWith("/") && fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }
        String url = baseUrl + fullPath;

        // Thay thế tham số Path
        if (parameters != null && args != null) {
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                if (param == null) {
                    continue;
                }
                String paramName = param.getString("name");
                String paramLocation = param.getString("location");

                if (!"Path".equalsIgnoreCase(paramLocation)) {
                    continue;
                }

                Object value = args.get(paramName);
                if (value != null) {
                    //update-begin---author:wangshuai---date:2026-03-30---for:【issues/9421】Sửa lỗ hổng path traversal của buildUrl---
                    String paramValue = value.toString();
                    // Ngăn chèn duyệt đường dẫn: từ chối các tham số chứa ..、/ 、\ tham số đường dẫn
                    if (paramValue.contains("..") || paramValue.contains("/") || paramValue.contains("\\")
                            || paramValue.toLowerCase().contains("%2e") || paramValue.toLowerCase().contains("%2f")) {
                        throw new IllegalArgumentException("Path Tham số chứa ký tự không hợp lệ: " + paramName);
                    }
                    url = url.replace("{" + paramName + "}", paramValue);
                    //update-end---author:wangshuai---date:2026-03-30---for:【issues/9421】Sửa lỗ hổng path traversal của buildUrl---
                }
            }
        }

        return url;
    }

    /**
     * Xây dựng request header
     */
    private static HttpHeaders buildHttpHeaders(JSONArray parameters, JSONObject args, Map<String, String> defaultHeaders) {
        HttpHeaders httpHeaders = new HttpHeaders();

        // Thêm header yêu cầu mặc định
        if (defaultHeaders != null) {
            defaultHeaders.forEach(httpHeaders::set);
        }

        // Thêm tham số loại Header
        if (parameters != null && args != null) {
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                if (param == null) {
                    continue;
                }
                String paramName = param.getString("name");
                String paramLocation = param.getString("location");

                if (!"Header".equalsIgnoreCase(paramLocation)) {
                    continue;
                }

                Object value = args.get(paramName);
                if (value != null) {
                    httpHeaders.set(paramName, value.toString());
                }
            }
        }

        // Nếu request body không trống và chưa đặt Content-Type, mặc định đặt thành application/json
        if (!httpHeaders.containsHeader(HttpHeaders.CONTENT_TYPE)) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        return httpHeaders;
    }

    /**
     * Xây dựng tham số truy vấn URL
     */
    private static JSONObject buildUrlVariables(JSONArray parameters, JSONObject args) {
        JSONObject urlVariables = new JSONObject();

        if (parameters == null || args == null) {
            return urlVariables;
        }

        for (int i = 0; i < parameters.size(); i++) {
            JSONObject param = parameters.getJSONObject(i);
            if (param == null) {
                continue;
            }
            String paramName = param.getString("name");
            String paramLocation = param.getString("location");

            String location = paramLocation != null ? paramLocation : "";
            // Chỉ định rõ loại Query, hoặc chưa chỉ định loại (mặc định là Query)
            boolean isQueryParam = "Query".equalsIgnoreCase(location);
            boolean isOtherType = "Body".equalsIgnoreCase(location) || "Form-Data".equalsIgnoreCase(location)
                    || "Header".equalsIgnoreCase(location) || "Path".equalsIgnoreCase(location);
            
            if (isQueryParam || !isOtherType) {
                Object value = args.get(paramName);
                if (value != null) {
                    //Nếu là id của kho tri thức thì gán giá trị mặc định
                    if ("knowledgeId".equalsIgnoreCase(paramName)) {
                        String defaultValue = param.getString("defaultValue");
                        urlVariables.put(paramName, defaultValue);
                    } else {
                        urlVariables.put(paramName, value);
                    }
                }
            }
        }

        return urlVariables.isEmpty() ? null : urlVariables;
    }

    /**
     * Xây dựng request body
     */
    private static Object buildRequestBody(JSONArray parameters, JSONObject args, HttpHeaders httpHeaders) {
        if (parameters == null || args == null) {
            return null;
        }

        boolean hasBody = false;
        boolean hasFormData = false;

        // Kiểm tra có tham số Body hoặc Form-Data loại tham số
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject param = parameters.getJSONObject(i);
            if (param == null) {
                continue;
            }
            String paramLocation = param.getString("location");

            if ("Body".equalsIgnoreCase(paramLocation)) {
                hasBody = true;
            } else if ("Form-Data".equalsIgnoreCase(paramLocation)) {
                hasFormData = true;
            }
        }

        // Body và Form-Data Loại trừ lẫn nhau
        if (hasBody && hasFormData) {
            log.warn("Cấu hình công cụ đồng thời chứa Body và Form-Data loại tham số, ưu tiên sử dụng Body");
            hasFormData = false;
        }

        if (hasBody) {
            // Body loại: xây dựng đối tượng JSON
            JSONObject body = new JSONObject();
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                if (param == null) {
                    continue;
                }
                String paramName = param.getString("name");
                String paramLocation = param.getString("location");

                if (!"Body".equalsIgnoreCase(paramLocation) ) {
                    continue;
                }

                Object value = args.get(paramName);
                if (value != null) {
                    //Nếu là id của kho tri thức thì gán giá trị mặc định
                    if ("knowledgeId".equalsIgnoreCase(paramName)) {
                        String defaultValue = param.getString("defaultValue");
                        body.put(paramName, defaultValue);
                    } else {
                        body.put(paramName, value);
                    }
                } else {
                    // Kiểm tra có giá trị mặc định không
                    // update-begin--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                    // Giá trị mặc định lấy theo kiểu gốc: giá trị mặc định của images trong plugin quy trình là mảng hình ảnh, như ["temp/a.png","temp/b.png"]，
                    // dùng getString sẽ thành chuỗi "[\"temp/a.png\",\"temp/b.png\"]", nút LLM của quy trình sẽ tách theo dấu phẩy thành đường dẫn hình ảnh sai
                    Object defaultValue = param.get(FlowPluginContent.DEFAULT_VALUE);
                    // update-end--author:wangshuai---date:20261012---for：【issues/9872] Trong tác tử thông minh, quy trình liên quan đến nhập hình ảnh không thể liên kết với hình ảnh đã tải lên trong cuộc trò chuyện
                    if (oConvertUtils.isNotEmpty(defaultValue)) {
                        body.put(paramName, defaultValue);
                    }
                }
            }
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            return body.isEmpty() ? null : body;
        } else if (hasFormData) {
            // Form-Data loại: xây dựng đối tượng JSON (RestUtil sẽ xử lý)
            JSONObject formData = new JSONObject();
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                if (param == null) {
                    continue;
                }
                String paramName = param.getString("name");
                String paramLocation = param.getString("location");

                if (!"Form-Data".equalsIgnoreCase(paramLocation)) {
                    continue;
                }

                Object value = args.get(paramName);
                if (value != null) {
                    formData.put(paramName, value);
                } else {
                    String defaultValue = param.getString("defaultValue");
                    if (oConvertUtils.isNotEmpty(defaultValue)) {
                        formData.put(paramName, defaultValue);
                    }
                }
            }
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            return formData.isEmpty() ? null : formData;
        }

        return null;
    }

    /**
     * Phân tích phương thức HTTP
     */
    private static HttpMethod parseHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Phương thức HTTP không hợp lệ: {}, sử dụng mặc định GET", method);
            return HttpMethod.GET;
        }
    }


    /**
     * Phân tích chuỗi JSON headers thành Map
     */
    private static Map<String, String> parseHeaders(String headersStr) {
        Map<String, String> headersMap = new HashMap<>();
        if (oConvertUtils.isEmpty(headersStr)) {
            return headersMap;
        }

        try {
            JSONObject headersJson = JSONObject.parseObject(headersStr);
            if (headersJson != null) {
                headersJson.forEach((key, value) -> {
                    if (value != null) {
                        headersMap.put(key, value.toString());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Phân tích headers thất bại: {}", headersStr);
        }

        return headersMap;
    }

    /**
     * Áp dụng cấu hình ủy quyền vào headers
     * Đọc cấu hình ủy quyền từ metadata, nếu là ủy quyền Token thì thêm vào headers
     * Nếu loại ủy quyền là token nhưng chưa đặt giá trị token, lấy từ TokenUtils token
     *
     * @param headersMap Request header Map
     * @param metadataStr Chuỗi JSON metadata
     */
    private static void applyAuthConfig(Map<String, String> headersMap, String metadataStr, HttpServletRequest currentHttpRequest) {
        if (oConvertUtils.isEmpty(metadataStr)) {
            return;
        }

        try {
            JSONObject metadata = JSONObject.parseObject(metadataStr);
            if (metadata == null) {
                return;
            }

            String authType = metadata.getString("authType");
            if (oConvertUtils.isEmpty(authType) || !"token".equalsIgnoreCase(authType)) {
                return;
            }

            // Token Phương thức ủy quyền: lấy cấu hình token từ metadata và thêm vào headers
            String tokenParamName = metadata.getString("tokenParamName");
            String tokenParamValue = metadata.getString("tokenParamValue");

            // Nếu tên tham số token tồn tại nhưng giá trị token chưa được đặt, thử lấy từ TokenUtils token
            if (oConvertUtils.isNotEmpty(tokenParamName) && oConvertUtils.isEmpty(tokenParamValue)) {
                try {
                    // Lưu ý: TokenUtils cần lấy request của luồng hiện tại, vì vậy phải sử dụng trong lời gọi đồng bộ
                    String currentToken = TokenUtils.getTokenByRequest();
                    if(oConvertUtils.isEmpty(currentToken) && currentHttpRequest != null) {
                        currentToken = TokenUtils.getTokenByRequest(currentHttpRequest);
                    }
                    if (oConvertUtils.isNotEmpty(currentToken)) {
                        tokenParamValue = currentToken;
                        log.debug("Lấy Token từ TokenUtils và thêm vào header yêu cầu: {} = {}", tokenParamName, 
                            currentToken.length() > 10 ? currentToken.substring(0, 10) + "..." : currentToken);
                    } else {
                        log.warn("Token tokenParamValue trong cấu hình ủy quyền trống và không thể lấy từ TokenUtils token");
                    }
                } catch (Exception e) {
                    log.warn("Lấy token từ TokenUtils thất bại: {}", e.getMessage());
                }
            }

            if (oConvertUtils.isNotEmpty(tokenParamName) && oConvertUtils.isNotEmpty(tokenParamValue)) {
                // Nếu trong headers đã tồn tại header cùng tên, ưu tiên dùng cấu hình trong metadata (ghi đè)
                headersMap.put(tokenParamName, tokenParamValue);
                // Trong nhật ký chỉ hiển thị vài ký tự đầu của token, tránh rò rỉ toàn bộ token
                String tokenPreview = tokenParamValue.length() > 10 
                    ? tokenParamValue.substring(0, 10) + "..." 
                    : tokenParamValue;
                log.debug("Thêm ủy quyền Token vào header yêu cầu: {} = {}", tokenParamName, tokenPreview);
            } else {
                log.warn("Token Cấu hình ủy quyền không đầy đủ: tokenParamName={}, tokenParamValue={}", tokenParamName, tokenParamValue != null ? "***" : null);
            }
        } catch (Exception e) {
            log.warn("Phân tích cấu hình ủy quyền thất bại: {}", metadataStr, e);
        }
    }
}

