package org.jeecg.modules.demo.mcp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.config.shiro.IgnoreAuth;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Server Ví dụ (Model Context Protocol)
 * 
 * Đây là một triển khai phía máy chủ tuân thủ giao thức MCP， hỗ trợ truyền tải SSE。
 * 
 * Địa chỉ kết nối: http://Máy chủ của bạn:8080/jeecg-boot/demo/mcp/sse
 * 
 * Các công cụ được cung cấp:
 * - hello: Công cụ chào hỏi
 * - get_time: Lấy thời gian hiện tại
 * - calculate: Máy tính đơn giản
 */
@Slf4j
@RestController
@RequestMapping("/demo/mcp")
@Tag(name = "MCP Server Ví dụ")
public class McpDemoController {

    // Lưu trữ kết nối SSE
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    // Định nghĩa danh sách công cụ
    private final List<Map<String, Object>> TOOLS = List.of(
            Map.of(
                    "name", "hello",
                    "description", "Công cụ chào hỏi， trả về lời chào",
                    "inputSchema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "name", Map.of("type", "string", "description", "Tên của bạn")
                            ),
                            "required", List.of("name")
                    )
            ),
            Map.of(
                    "name", "get_time",
                    "description", "Lấy thời gian hiện tại của máy chủ",
                    "inputSchema", Map.of(
                            "type", "object",
                            "properties", Map.of()
                    )
            ),
            Map.of(
                    "name", "calculate",
                    "description", "Máy tính đơn giản， hỗ trợ cộng， trừ， nhân， chia",
                    "inputSchema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "a", Map.of("type", "number", "description", "Số thứ nhất"),
                                    "b", Map.of("type", "number", "description", "Số thứ hai"),
                                    "operator", Map.of("type", "string", "description", "Toán tử: +, -, *, /")
                            ),
                            "required", List.of("a", "b", "operator")
                    )
            )
    );

    /**
     * MCP SSE endpoint - Client thiết lập kết nối SSE qua API này
     */
    @IgnoreAuth
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "MCP SSE Endpoint kết nối")
    public SseEmitter sse(HttpServletRequest request) {
        String clientId = UUID.randomUUID().toString();
        log.info("[MCP Server] Kết nối SSE của client mới: {}", clientId);
        
        SseEmitter emitter = new SseEmitter(0L); // Không timeout
        sseEmitters.put(clientId, emitter);
        
        emitter.onCompletion(() -> {
            log.info("[MCP Server] Client ngắt kết nối: {}", clientId);
            sseEmitters.remove(clientId);
        });
        emitter.onTimeout(() -> {
            log.info("[MCP Server] Client bị timeout: {}", clientId);
            sseEmitters.remove(clientId);
        });
        emitter.onError(e -> {
            log.error("[MCP Server] SSE Lỗi: {}", e.getMessage());
            sseEmitters.remove(clientId);
        });
        
        // Gửi sự kiện endpoint， báo cho client địa chỉ endpoint của tin nhắn
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String messageEndpoint = baseUrl + request.getContextPath() + "/demo/mcp/message?sessionId=" + clientId;
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data(messageEndpoint));
            log.info("[MCP Server] Gửi sự kiện endpoint: {}", messageEndpoint);
        } catch (IOException e) {
            log.error("[MCP Server] Gửi sự kiện endpoint thất bại", e);
        }
        
        return emitter;
    }

    /**
     * Streamable HTTP endpoint - Đồng thời hỗ trợ POST tới /sse của JSON-RPC Yêu cầu
     * Cursor Client sẽ thử cách này trước
     */
    @IgnoreAuth
    @PostMapping(value = "/sse")
    @Operation(summary = "MCP Streamable HTTP endpoint")
    public void ssePost(@RequestBody String body, HttpServletResponse response) throws IOException {
        log.info("[MCP Server] Streamable HTTP Yêu cầu: {}", body);
        handleJsonRpcRequest(body, response);
    }

    /**
     * MCP Endpoint xử lý tin nhắn - Xử lý JSON-RPC Yêu cầu
     * Ghi trực tiếp dữ liệu gốc JSON-RPC phản hồi， tránh framework bọc lại
     */
    @IgnoreAuth
    @PostMapping(value = "/message")
    @Operation(summary = "MCP Xử lý tin nhắn")
    public void handleMessage(@RequestParam(required = false) String sessionId,
                              @RequestBody String body,
                              HttpServletResponse response) throws IOException {
        log.info("[MCP Server] Đã nhận tin nhắn, sessionId: {}, body: {}", sessionId, body);
        handleJsonRpcRequest(body, response);
    }

    /**
     * Xử lý JSON-RPC Phương thức công khai của yêu cầu
     */
    private void handleJsonRpcRequest(String body, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        
        try {
            JSONObject request = JSON.parseObject(body);
            String method = request.getString("method");
            Object id = request.get("id");
            JSONObject params = request.getJSONObject("params");
            
            // Tin nhắn dạng thông báo（không có id）không cần phản hồi
            if (id == null) {
                log.info("[MCP Server] Đã nhận thông báo: {}", method);
                writer.write("{}");
                writer.flush();
                return;
            }
            
            // Xây dựng JSON-RPC 2.0 Phản hồi
            Map<String, Object> jsonRpcResponse = new LinkedHashMap<>();
            jsonRpcResponse.put("jsonrpc", "2.0");
            jsonRpcResponse.put("id", id);
            
            try {
                Object result = switch (method) {
                    case "initialize" -> handleInitialize(params);
                    case "initialized", "notifications/initialized" -> handleInitialized();
                    case "tools/list" -> handleToolsList();
                    case "tools/call" -> handleToolsCall(params);
                    case "ping" -> handlePing();
                    case "notifications/cancelled" -> handleCancelled(params);
                    default -> {
                        if (method != null && method.startsWith("notifications/")) {
                            log.info("[MCP Server] Bỏ qua thông báo không xác định: {}", method);
                            yield Map.of();
                        }
                        throw new RuntimeException("Phương thức không xác định: " + method);
                    }
                };
                jsonRpcResponse.put("result", result);
            } catch (Exception e) {
                log.error("[MCP Server] Xử lý yêu cầu thất bại", e);
                jsonRpcResponse.put("error", Map.of(
                        "code", -32603,
                        "message", e.getMessage()
                ));
            }
            
            String responseJson = JSON.toJSONString(jsonRpcResponse);
            log.info("[MCP Server] Trả về: {}", responseJson);
            writer.write(responseJson);
            
        } catch (Exception e) {
            log.error("[MCP Server] Phân tích cú pháp yêu cầu thất bại", e);
            writer.write("{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32700,\"message\":\"Parse error\"}}");
        }
        
        writer.flush();
    }

    /**
     * Xử lý yêu cầu initialize
     */
    private Map<String, Object> handleInitialize(JSONObject params) {
        log.info("[MCP Server] Yêu cầu khởi tạo: {}", params);
        return Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", Map.of()
                ),
                "serverInfo", Map.of(
                        "name", "jeecg-mcp-demo",
                        "version", "1.0.0"
                )
        );
    }
    
    /**
     * Xử lý thông báo initialized
     */
    private Map<String, Object> handleInitialized() {
        log.info("[MCP Server] Client đã hoàn tất khởi tạo");
        return Map.of();
    }
    
    /**
     * Xử lý yêu cầu ping
     */
    private Map<String, Object> handlePing() {
        log.info("[MCP Server] Ping");
        return Map.of();
    }
    
    /**
     * Xử lý notifications/cancelled Thông báo
     */
    private Map<String, Object> handleCancelled(JSONObject params) {
        log.info("[MCP Server] Yêu cầu đã bị hủy: {}", params);
        return Map.of();
    }

    /**
     * Xử lý tools/list Yêu cầu
     */
    private Map<String, Object> handleToolsList() {
        log.info("[MCP Server] Lấy danh sách công cụ");
        return Map.of("tools", TOOLS);
    }

    /**
     * Xử lý tools/call Yêu cầu
     */
    private Map<String, Object> handleToolsCall(JSONObject params) {
        String toolName = params.getString("name");
        JSONObject arguments = params.getJSONObject("arguments");
        if (arguments == null) {
            arguments = new JSONObject();
        }
        log.info("[MCP Server] Gọi công cụ: {}, Tham số: {}", toolName, arguments);
        
        String result = switch (toolName) {
            case "hello" -> {
                String name = arguments.getString("name");
                if (name == null || name.isEmpty()) {
                    name = "World";
                }
                yield "Xin chào, " + name + "! Chào mừng bạn sử dụng dịch vụ MCP JeecgBoot!";
            }
            case "get_time" -> {
                yield "Thời gian hiện tại: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            case "calculate" -> {
                double a = arguments.getDoubleValue("a");
                double b = arguments.getDoubleValue("b");
                String op = arguments.getString("operator");
                if (op == null) op = "+";
                double res = switch (op) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> b != 0 ? a / b : Double.NaN;
                    default -> throw new RuntimeException("Toán tử không được hỗ trợ: " + op);
                };
                yield String.format("%.2f %s %.2f = %.2f", a, op, b, res);
            }
            default -> throw new RuntimeException("Công cụ không xác định: " + toolName);
        };
        
        return Map.of(
                "content", List.of(Map.of(
                        "type", "text",
                        "text", result
                ))
        );
    }

    /**
     * Trang hướng dẫn sử dụng
     */
    @IgnoreAuth
    @GetMapping("/info")
    @Operation(summary = "MCP Server Hướng dẫn sử dụng")
    public Map<String, Object> info(HttpServletRequest request) {
        log.info("[MCP Server] Hello API đã được truy cập");
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return Map.of(
                "success", true,
                "message", "JeecgBoot MCP Server Ví dụ",
                "sseUrl", baseUrl + "/demo/mcp/sse",
                "tools", List.of(
                        Map.of("name", "hello", "description", "Công cụ chào hỏi", "params", "name: Tên của bạn"),
                        Map.of("name", "get_time", "description", "Lấy thời gian hiện tại", "params", "Không có"),
                        Map.of("name", "calculate", "description", "Máy tính đơn giản", "params", "a, b, operator(+,-,*,/)")
                ),
                "usage", "Trong Cursor/Claude và các MCP client khác để cấu hình SSE URL: " + baseUrl + "/demo/mcp/sse",
                "example", "Vui lòng gọi công cụ hello， điền tham số name \"Người dùng kiểm tra\""
        );
    }
}
