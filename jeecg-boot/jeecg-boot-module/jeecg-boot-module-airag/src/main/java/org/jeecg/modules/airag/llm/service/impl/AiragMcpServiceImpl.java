package org.jeecg.modules.airag.llm.service.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.request.json.*;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.AiRagConfigBean;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import org.jeecg.modules.airag.llm.handler.CommandExecUtil;
import org.jeecg.modules.airag.llm.mapper.AiragMcpMapper;
import org.jeecg.modules.airag.llm.service.IAiragMcpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: MCP
 * @Author: jeecg-boot
 * @Date: 2025-10-20
 * @Version: V1.0
 */
@Slf4j
@SuppressWarnings("removal")
@Service("airagMcpServiceImpl")
public class AiragMcpServiceImpl extends ServiceImpl<AiragMcpMapper, AiragMcp> implements IAiragMcpService {

    @Autowired
    private ObjectMapper objectMapper; // Sử dụng cấu hình toàn cục Jackson ObjectMapper
    @Autowired
    private AiRagConfigBean aiRagConfigBean;

    /**
     * Thêm mới hoặc chỉnh sửa Mcpserver
     *
     * @param airagMcp MCP Đối tượng
     * @return Trả về đối tượng MCP sau khi lưu
     * @author chenrui
     * @date 2025/10/21
     */
    @Override
    public Result<String> edit(AiragMcp airagMcp) {
        // Kiểm chứng các mục bắt buộc
        if (airagMcp.getName() == null || airagMcp.getName().trim().isEmpty()) {
            return Result.error("Tên không được trống");
        }
        //update-begin---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------
        // Đặt mặc định category
        if (oConvertUtils.isEmpty(airagMcp.getCategory())) {
            airagMcp.setCategory("mcp");
        }
        // Đối với loại MCP, cần kiểm chứng type và endpoint
        if ("mcp".equalsIgnoreCase(airagMcp.getCategory())) {
            if (airagMcp.getType() == null || airagMcp.getType().trim().isEmpty()) {
                return Result.error("MCP Loại không được trống");
            }
            if (airagMcp.getEndpoint() == null || airagMcp.getEndpoint().trim().isEmpty()) {
                return Result.error("Endpoint phía máy chủ không được trống");
            }
        } else if ("plugin".equalsIgnoreCase(airagMcp.getCategory())) {
            // Đối với loại plugin, BaseURL là tùy chọn; nếu không điền sẽ dùng địa chỉ hệ thống hiện tại
            // Không còn kiểm chứng endpoint có trống hay không
        } else {
            // Loại không xác định, mặc định là MCP và kiểm chứng
            if (airagMcp.getEndpoint() == null || airagMcp.getEndpoint().trim().isEmpty()) {
                return Result.error("Endpoint phía máy chủ không được trống");
            }
        }
        //update-end---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------

        if (airagMcp.getId() == null || airagMcp.getId().trim().isEmpty()) {
            // Đặt giá trị mặc định
            airagMcp.setStatus("enable");
            //update-begin---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------
            // Chỉ loại MCP mới đặt trường synced; loại plugin không cần đồng bộ, mặc định là đã đồng bộ
            if ("mcp".equalsIgnoreCase(airagMcp.getCategory())) {
                airagMcp.setSynced(CommonConstant.STATUS_0_INT);
            } else {
                airagMcp.setSynced(CommonConstant.STATUS_1_INT);
            }
            //update-end---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------
            // Thêm mới
            this.save(airagMcp);
        } else {
            // Chỉnh sửa
            this.updateById(airagMcp);
        }
        return Result.OK("Lưu thành công");
    }

    /**
     * Đồng bộ danh sách công cụ của MCP
     *
     * @param id mcp Khóa chính
     * @return Danh sách công cụ
     * @author chenrui
     * @date 2025/10/21
     */
    @Override
    public Result<?> sync(String id) {
        AiragMcp mcp = this.getById(id);
        if (mcp == null) {
            return Result.error("Không tìm thấy đối tượng MCP tương ứng");
        }
        //update-begin---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------
        // Chỉ loại MCP mới hỗ trợ đồng bộ, loại plugin không hỗ trợ
        String category = mcp.getCategory();
        if (oConvertUtils.isEmpty(category)) {
            category = "mcp"; // Tương thích dữ liệu cũ
        }
        if (!"mcp".equalsIgnoreCase(category)) {
            return Result.error("Chỉ loại MCP mới hỗ trợ thao tác đồng bộ");
        }
        //update-end---author:chenrui ---date:20251031  for：[QQYUN-12453]【AI】Hỗ trợ plugin------------
        String type = mcp.getType();
        String endpoint = mcp.getEndpoint();
        Map<String, String> headers = null;
        if (oConvertUtils.isNotEmpty(mcp.getHeaders())) {
            try {
                headers = JSONObject.parseObject(mcp.getHeaders(), new com.alibaba.fastjson.TypeReference<Map<String, String>>() {});
            } catch (JSONException e) {
                headers = null;
            }
        }
        if (type == null || endpoint == null) {
            return Result.error("MCP Loại hoặc endpoint trống");
        }
        McpClient mcpClient = null;
        try {
            if ("sse".equalsIgnoreCase(type)) {
                //TODO 1.4.0-beta10 Đã bị loại bỏ,Khuyến nghị sử dụng http
                log.info("[MCP]Sử dụng giao thức SSE(HttpMcpTransport), endpoint:{}", endpoint);
                HttpMcpTransport.Builder builder = HttpMcpTransport.builder()
                        .sseUrl(endpoint)
                        .logRequests(true)
                        .logResponses(true);
                if (headers != null && !headers.isEmpty()) {
                    builder.customHeaders(headers);
                }
                mcpClient = new DefaultMcpClient.Builder().transport(builder.build()).build();
            } else if ("stdio".equalsIgnoreCase(type)) {
                //update-begin---author:wangshuai---date:2025-12-18---for:【QQYUN-14242】【AI】Thêm tham số điều khiển có bật hay không, mặc định vô hiệu hóa stdio gọi thực thi lệnh---
                String openSafe = aiRagConfigBean.getAllowSensitiveNodes();
                if(oConvertUtils.isNotEmpty(openSafe) && openSafe.toLowerCase().contains("stdio")) {
                    log.info("[MCP]Sử dụng giao thức STDIO(StdioMcpTransport), endpoint:{}", endpoint);
                    //update-begin---author:wangshuai---date:2026-10-21---for:【GHSA-p3r5-xh32-m66j】/【GHSA-vj3m-vf8p-cvgc】Tăng cường chống injection lệnh stdio: endpoint sẽ bị sh -c/cmd.exe /c thực thi, kiểm tra và từ chối trước khi thực thi & | ; < > ` $ và các ký tự đặc biệt của shell khác, chống injection lệnh(CWE-78) ---
                    CommandExecUtil.validateArg(endpoint.trim());
                    //update-end---author:wangshuai---date:2026-10-21---for:【GHSA-p3r5-xh32-m66j】/【GHSA-vj3m-vf8p-cvgc】Tăng cường chống injection lệnh stdio: endpoint sẽ bị sh -c/cmd.exe /c thực thi, kiểm tra và từ chối trước khi thực thi & | ; < > ` $ và các ký tự đặc biệt của shell khác, chống injection lệnh(CWE-78)---
                    // stdio Loại: endpoint có thể là một dòng lệnh
                    // Windows cần thông qua cmd.exe /c để thực thi lệnh, nếu không sẽ không tìm thấy các chương trình như npx
                    List<String> cmdParts;
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        // Windows: Sử dụng cmd.exe /c Thực thi
                        cmdParts = new ArrayList<>();
                        cmdParts.add("cmd.exe");
                        cmdParts.add("/c");
                        cmdParts.add(endpoint.trim());
                    } else {
                        // Linux/Mac: Sử dụng sh -c Thực thi
                        cmdParts = new ArrayList<>();
                        cmdParts.add("sh");
                        cmdParts.add("-c");
                        cmdParts.add(endpoint.trim());
                    }
                    log.info("[MCP]Thực thi lệnh stdio: {}", cmdParts);
                    StdioMcpTransport.Builder builder = new StdioMcpTransport.Builder()
                            .command(cmdParts)
                            .environment(headers);
                    mcpClient = new DefaultMcpClient.Builder().transport(builder.build()).build();
                } else {
                    String disabledMsg = "stdio Tính năng đã bị vô hiệu hóa. Nếu cần bật, hãy thêm stdio vào jeecg.airag.allow-sensitive-nodes trong yml.";
                    log.warn("[MCP]{}", disabledMsg);
                    return Result.error(disabledMsg);
                }
                //update-end---author:wangshuai---date:2025-12-19---for:【QQYUN-14242】【AI】Thêm tham số điều khiển có bật hay không, mặc định vô hiệu hóa stdio gọi thực thi lệnh---
            }else if("http".equalsIgnoreCase(type)){
                log.info("[MCP]Sử dụng giao thức HTTP(StreamableHttpMcpTransport), endpoint:{}", endpoint);
                //Thêm tùy chọn http
                mcpClient = mcpHttpCreate(endpoint,headers);
            } else {
                return Result.error("Loại MCP không được hỗ trợ:" + type);
            }
            List<ToolSpecification> toolSpecifications = mcpClient.listTools();
            // Thử sử dụng ObjectMapper để tuần tự hóa trực tiếp trước, nếu kết quả là {} thì quay lại dùng reflection Map
            List<Map<String, Object>> specMaps = toolSpecifications.stream()
                    .map(spec -> {
                        try {
                            String raw = objectMapper.writeValueAsString(spec);
                            if (raw != null && raw.length() > 2) {
                                // Giải tuần tự hóa trực tiếp thành Map, giữ lại các trường Jackson nhận ra
                                return objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {
                                });
                            }
                        } catch (Exception ignore) {
                        }
                        return convertToolSpec(spec);
                    })
                    .collect(Collectors.toList());
            String jsonList;
            try {
                jsonList = objectMapper.writeValueAsString(specMaps);
            } catch (JsonProcessingException e) {
                jsonList = JSONObject.toJSONString(specMaps);
            }
            String firstJson = specMaps.isEmpty() ? "null" : safeWriteJson(specMaps.get(0));
            log.info("MCP Danh sách công cụ id={}, size={}, first={}", id, toolSpecifications.size(), firstJson);
            mcp.setTools(jsonList);
            mcp.setSynced(1);

            Map<String,Object> metadata = new HashMap<>();
            metadata.put("tool_count", toolSpecifications.size());
            mcp.setMetadata(objectMapper.writeValueAsString(metadata));
            this.updateById(mcp);
            return Result.OK(specMaps);
        } catch (Exception e) {
            String message = e.getMessage();
            if (e instanceof IllegalArgumentException) {
                message = ", lỗi tham số MCP client";
            }
            log.error("Đồng bộ công cụ MCP thất bại id={}, error={}", id, message, e);
            return Result.error("Đồng bộ thất bại" + message);
        } finally {
            if (mcpClient != null) {
                try {
                    Method closeMethod = mcpClient.getClass().getMethod("close");
                    closeMethod.invoke(mcpClient);
                } catch (NoSuchMethodException ignore) {
                } catch (Exception ex) {
                    log.warn("Đóng MCP client thất bại id={}, error={}", id, ex.getMessage());
                }
            }
        }
    }

    /**
     * mcp Tạo plugin http
     * 
     * @param endpoint
     * @param headers
     * @return
     */
    private McpClient mcpHttpCreate(String endpoint, Map<String, String> headers) {
        StreamableHttpMcpTransport.Builder builder = new StreamableHttpMcpTransport.Builder()
                .url(endpoint)
                .timeout(Duration.ofMinutes(60))
                .logRequests(true)
                .logResponses(true);

        if (headers != null && !headers.isEmpty()) {
            builder.customHeaders(headers);
        }

        return new DefaultMcpClient.Builder()
                .transport(builder.build())
                .build();
    }

    // Tuần tự hóa an toàn một đối tượng thành chuỗi JSON
    private String safeWriteJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    // Dùng reflection để chuyển ToolSpecification thành Map, tương thích record/trường private/chỉ các trường hợp dùng annotation Jackson -> đổi thành gọi trực tiếp accessor
    private Map<String, Object> convertToolSpec(ToolSpecification spec) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (spec == null) {
            return map;
        }
        map.put("name", spec.name());
        map.put("description", spec.description());
        try {
            Object params = spec.parameters();
            if (params != null) {
                JsonObjectSchema obj = (JsonObjectSchema) params;
                List<Map<String, Object>> fields = new ArrayList<>();
                if (obj.properties() != null) {
                    obj.properties().forEach((fieldName, fieldSchema) -> {
                        Map<String, Object> fieldMap = new LinkedHashMap<>();
                        fieldMap.put("name", fieldName);
                        fieldMap.put("description", extractDescription(fieldSchema));
                        // Nếu cần đánh dấu bắt buộc
                        if (obj.required() != null && obj.required().contains(fieldName)) {
                            fieldMap.put("required", true);
                        }
                        fields.add(fieldMap);
                    });
                }
                map.put("parameters", fields);
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    // Trích xuất mô tả schema của từng loại
    private String extractDescription(Object schema) {
        if (schema == null) return null;
        try {
            if (schema instanceof JsonStringSchema) return ((JsonStringSchema) schema).description();
            if (schema instanceof JsonNumberSchema) return ((JsonNumberSchema) schema).description();
            if (schema instanceof JsonBooleanSchema) return ((JsonBooleanSchema) schema).description();
            if (schema instanceof JsonArraySchema) return ((JsonArraySchema) schema).description();
            if (schema instanceof JsonEnumSchema) return ((JsonEnumSchema) schema).description();
            if (schema instanceof JsonObjectSchema) return ((JsonObjectSchema) schema).description();
        } catch (Exception ignored) {
        }
        return schema.toString();
    }

    /**
     * Sửa trạng thái
     *
     * @param id     MCP Khóa chính
     * @param action Thao tác (enable/disable）
     * @return Kết quả thao tác
     * @author chenrui
     * @date 2025/10/21 11:00
     */
    @Override
    public Result<?> toggleStatus(String id, String action) {
        if (oConvertUtils.isEmpty(id)) {
            return Result.error("id Không được trống");
        }
        if (oConvertUtils.isEmpty(action)) {
            return Result.error("action Không được trống");
        }
        String normalized = action.toLowerCase();
        if (!"enable".equals(normalized) && !"disable".equals(normalized)) {
            return Result.error("action chỉ có thể là enable hoặc disable");
        }
        AiragMcp mcp = this.getById(id);
        if (mcp == null) {
            return Result.error("Không tìm thấy dịch vụ MCP tương ứng");
        }
        if (normalized.equalsIgnoreCase(mcp.getStatus())) {
            return Result.OK("Thao tác thành công");
        }
        mcp.setStatus(normalized);
        this.updateById(mcp);
        return Result.OK("Thao tác thành công");
    }

    /**
     * Lưu công cụ plugin (chỉ cập nhật trường tools)
     * for [QQYUN-12453]【AI】Hỗ trợ plugin
     * @param id Plugin ID
     * @param tools Chuỗi JSON danh sách công cụ
     * @return Kết quả thao tác
     * @author chenrui
     * @date 2025/10/30
     */
    @Override
    public Result<String> saveTools(String id, String tools) {
        if (oConvertUtils.isEmpty(id)) {
            return Result.error("ID plugin không được trống");
        }
        AiragMcp mcp = this.getById(id);
        if (mcp == null) {
            return Result.error("Không tìm thấy plugin tương ứng");
        }
        // Xác minh có phải loại plugin không
        String category = mcp.getCategory();
        if (oConvertUtils.isEmpty(category)) {
            category = "mcp"; // Tương thích dữ liệu cũ
        }
        if (!"plugin".equalsIgnoreCase(category)) {
            return Result.error("Chỉ loại plugin mới có thể lưu công cụ");
        }
        // Cập nhật trường tools
        mcp.setTools(tools);
        
        // Cập nhật trong metadata tool_count
        try {
            com.alibaba.fastjson.JSONArray toolsArray = com.alibaba.fastjson.JSONArray.parseArray(tools);
            int toolCount = toolsArray != null ? toolsArray.size() : 0;
            
            // Phân tích các metadata
            JSONObject metadata = new JSONObject();
            if (oConvertUtils.isNotEmpty(mcp.getMetadata())) {
                try {
                    JSONObject metadataJson = JSONObject.parseObject(mcp.getMetadata());
                    if (metadataJson != null) {
                        metadata.putAll(metadataJson);
                    }
                } catch (Exception e) {
                    log.warn("Phân tích metadata thất bại, sẽ tạo lại: {}", mcp.getMetadata());
                }
            }
            
            // Cập nhật tool_count
            metadata.put("tool_count", toolCount);
            
            // Lưu metadata
            mcp.setMetadata(metadata.toJSONString());
        } catch (Exception e) {
            log.warn("Cập nhật số lượng công cụ thất bại: {}", e.getMessage());
            // Ngay cả khi cập nhật tool_count thất bại cũng không ảnh hưởng đến việc lưu tools
        }
        
        this.updateById(mcp);
        return Result.OK("Lưu thành công");
    }
}
