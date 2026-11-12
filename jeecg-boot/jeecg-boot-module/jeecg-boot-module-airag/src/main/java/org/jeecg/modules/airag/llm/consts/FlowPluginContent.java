package org.jeecg.modules.airag.llm.consts;

/**
* @Description: Hằng số plugin quy trình
*
* @author: wangshuai
* @date: 2025/12/23 19:37
*/
public interface FlowPluginContent {
    /**
     * Tên
     */
    String NAME = "name";

    /**
     * Mô tả
     */
    String DESCRIPTION = "description";

    /**
     * Phản hồi
     */
    String RESPONSES = "responses";

    /**
     * Loại
     */
    String TYPE = "type";

    /**
     * Tham số
     */
    String PARAMETERS = "parameters";

    /**
     * Có bắt buộc hay không
     */
    String REQUIRED = "required";

    /**
     * Giá trị mặc định
     */
    String DEFAULT_VALUE = "defaultValue";

    /**
     * Đường dẫn
     */
    String PATH = "path";

    /**
     * Phương thức
     */
    String METHOD = "method";

    /**
     * Vị trí
     */
    String LOCATION = "location";

    /**
     * Loại xác thực
     */
    String AUTH_TYPE = "authType";

    /**
     * token Tên tham số
     */
    String TOKEN_PARAM_NAME = "tokenParamName";

    /**
     * token Giá trị tham số
     */
    String TOKEN_PARAM_VALUE = "tokenParamValue";

    /**
     * token
     */
    String TOKEN = "token";

    /**
     * Path Vị trí
     */
    String LOCATION_PATH = "Path";

    /**
     * Header Vị trí
     */
    String LOCATION_HEADER = "Header";

    /**
     * Query Vị trí
     */
    String LOCATION_QUERY = "Query";

    /**
     * Body Vị trí
     */
    String LOCATION_BODY = "Body";

    /**
     * Form-Data Vị trí
     */
    String LOCATION_FORM_DATA = "Form-Data";

    /**
     * String Loại
     */
    String TYPE_STRING = "String";

    /**
     * string Loại
     */
    String TYPE_STRING_LOWER = "string";

    /**
     * Number Loại
     */
    String TYPE_NUMBER = "Number";

    /**
     * number Loại
     */
    String TYPE_NUMBER_LOWER = "number";

    /**
     * Integer Loại
     */
    String TYPE_INTEGER = "Integer";

    /**
     * integer Loại
     */
    String TYPE_INTEGER_LOWER = "integer";

    /**
     * Boolean Loại
     */
    String TYPE_BOOLEAN = "Boolean";

    /**
     * boolean Loại
     */
    String TYPE_BOOLEAN_LOWER = "boolean";

    /**
     * Số lượng công cụ
     */
    String TOOL_COUNT = "tool_count";

    /**
     * Có bật không
     */
    String ENABLED = "enabled";

    /**
     * Đầu vào
     */
    String INPUTS = "inputs";

    /**
     * Đầu ra
     */
    String OUTPUTS = "outputs";

    /**
     * POST Yêu cầu
     */
    String POST = "POST";

    /**
     * token Tên
     */
    String X_ACCESS_TOKEN = "X-Access-Token";

    /**
     * Tên plugin
     */
    String PLUGIN_NAME = "Gọi quy trình";

    /**
     * Mô tả plugin
     */
    String PLUGIN_DESC = "Gọi quy trình làm việc";

    /**
     * Tiền tố tên công cụ quy trình
     */
    String FLOW_TOOL_NAME_PREFIX = "flow_";

    /**
     * Địa chỉ yêu cầu plugin
     */
    String PLUGIN_REQUEST_URL = "/airag/flow/plugin/run/";

    /**
     * Tên plugin kho bộ nhớ
     */
    String PLUGIN_MEMORY_NAME = "Kho bộ nhớ";

    /**
     * Mô tả plugin kho bộ nhớ
     */
    String PLUGIN_MEMORY_DESC = "Dùng để ghi lại bộ nhớ dài hạn";
    
    /**
     * Đường dẫn thêm bộ nhớ
     */
    String PLUGIN_MEMORY_ADD_PATH = "/airag/knowledge/plugin/add";

    /**
     * Đường dẫn truy vấn bộ nhớ
     */
    String PLUGIN_MEMORY_QUERY_PATH = "/airag/knowledge/plugin/query";
}
