package org.jeecg.modules.openapi.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.openapi.entity.OpenApi;
import org.jeecg.modules.openapi.entity.OpenApiAuth;
import org.jeecg.modules.openapi.entity.OpenApiHeader;
import org.jeecg.modules.openapi.entity.OpenApiParam;
import org.jeecg.modules.openapi.generator.PathGenerator;
import org.jeecg.modules.openapi.service.OpenApiAuthService;
import org.jeecg.modules.openapi.service.OpenApiService;
import org.jeecg.modules.openapi.swagger.*;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @date 2024/12/10 9:11
 */
@RestController
@RequestMapping("/openapi")
public class OpenApiController extends JeecgController<OpenApi, OpenApiService> {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private OpenApiAuthService openApiAuthService;
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param openApi
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(OpenApi openApi, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<OpenApi> queryWrapper = QueryGenerator.initQueryWrapper(openApi, req.getParameterMap());
        Page<OpenApi> page = new Page<>(pageNo, pageSize);
        IPage<OpenApi> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Thêm
     *
     * @param openApi
     * @return
     */
    @RequiresPermissions("openapi:open_api:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody OpenApi openApi) {
        if (openApi == null) {
            return Result.error("Tham số yêu cầu không được để trống");
        }
        validOriginUrl(openApi.getOriginUrl());
        service.save(openApi);
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param openApi
     * @return
     */
    @RequiresPermissions("openapi:open_api:edit")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody OpenApi openApi) {
        if (openApi == null) {
            return Result.error("Tham số yêu cầu không được để trống");
        }
        validOriginUrl(openApi.getOriginUrl());
        service.updateById(openApi);
        return Result.ok("Chỉnh sửa thành công!");

    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @RequiresPermissions("openapi:open_api:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        service.removeById(id);
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @RequiresPermissions("openapi:open_api:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        OpenApi OpenApi = service.getById(id);
        return Result.ok(OpenApi);
    }

    /**
     * Gọi API
     * @param path
     * @return
     */
    //update-begin---author:scott ---date:20261024  for：【GHSA-m3rc-w9wv-5qmw】/call Không còn chấp nhận OPTIONS, TRACE; OPTIONS không thể dùng làm lối vào chuyển tiếp để vượt qua xác thực nữa-----------
    @RequestMapping(value = "/call/{path}", method = {RequestMethod.GET,RequestMethod.POST,RequestMethod.DELETE,RequestMethod.PUT,RequestMethod.PATCH,RequestMethod.HEAD})
    //update-end---author:scott ---date:20261024  for：【GHSA-m3rc-w9wv-5qmw】/call Không còn chấp nhận OPTIONS,TRACE-----------
    public Result<?> call(@PathVariable String path, @RequestBody(required = false) String json, HttpServletRequest request) {
        OpenApi openApi = service.findByPath(path);
        if (Objects.isNull(openApi)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("data", null);
            return Result.error("Thất bại", result);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        if (StrUtil.isNotEmpty(openApi.getHeadersJson())) {
            List<OpenApiHeader> headers = JSON.parseArray(openApi.getHeadersJson(),OpenApiHeader.class);
            if (headers.size()>0) {
                for (OpenApiHeader header : headers) {
                    httpHeaders.put(header.getHeaderKey(), Lists.newArrayList(request.getHeader(header.getHeaderKey())));
                }
            }
        }

        String url = openApi.getOriginUrl();
        // Kiểm tra đường dẫn giao diện gốc có hợp lệ hay không
        validOriginUrl(url);
        String method = openApi.getRequestMethod();
        String appkey = request.getHeader("appkey");
        OpenApiAuth openApiAuth = openApiAuthService.getByAppkey(appkey);
        SysUser systemUser = sysUserService.getUserByName(openApiAuth.getCreateBy());
        String token = this.getToken(systemUser.getUsername(), systemUser.getPassword());
        httpHeaders.put("X-Access-Token", Lists.newArrayList(token));
        httpHeaders.put("Content-Type",Lists.newArrayList("application/json"));
        HttpEntity<String> httpEntity = new HttpEntity<>(json, httpHeaders);
        //update-begin---author:scott ---date:20260430  for：【issues/9590] Khi triển khai microservice bằng nginx, không truy cập được interface openApi-----------
        // originUrl Hỗ trợ hai dạng:
        //  1) Đường dẫn tương đối (ví dụ như /house/houseTest/list): Mặc định nối baseUrl của yêu cầu hiện tại;
        //     Sử dụng CommonUtils.getBaseUrl(request)(chứ không phải RestUtil.getBaseUrl()），
        //     Có thể đọc X-Gateway-Base-Path Header yêu cầu, tương thích với base path thực tế dưới gateway microservice.
        //     Docker / K8s NodePort / Khi cổng vào như reverse proxy không nhất quán với cổng lắng nghe của container,
        //     Có thể thông qua cấu hình jeecg.domainUrl.back Chỉ định rõ ràng địa chỉ tự proxy của máy cục bộ.
        //  2) URL đầy đủ (http(s)://host:port/path): Dùng trực tiếp, phù hợp với kịch bản API được triển khai ở module microservice khác (ví dụ erp 7003) trong chế độ microservice
        String lowerUrl = url.toLowerCase();
        //update-begin---author:zhang ---date:20260806  for：【issues/9794] OpenAPI tự proxy tương đối so với originUrl, dùng inbound Host/port, khi ánh xạ cổng Docker Connection refused-----------
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            String baseUrl = null;
            if (jeecgBaseConfig.getDomainUrl() != null && oConvertUtils.isNotEmpty(jeecgBaseConfig.getDomainUrl().getBack())) {
                baseUrl = jeecgBaseConfig.getDomainUrl().getBack();
            } else {
                baseUrl = CommonUtils.getBaseUrl(request);
            }
            CommonUtils.checkInternalUrl(baseUrl);
            url = baseUrl + url;
        }
        //update-end---author:zhang ---date:20260806  for：【issues/9794] OpenAPI tự proxy tương đối so với originUrl, dùng inbound Host/port, khi ánh xạ cổng Docker Connection refused-----------
        //update-end---author:wangshuai ---date:20260616  for：【issues/9695] Sửa lỗ hổng SSRF, kiểm tra baseUrl phải là địa chỉ mạng nội bộ-----------
        //update-end---author:scott ---date:20260430  for：【issues/9590] Khi triển khai microservice bằng nginx, không truy cập được interface openApi-----------

        // Thay thế placeholder đường dẫn trong originUrl {key} Thay thế bằng giá trị tham số query do phía gọi truyền vào,
        // Các key đã thay thế được thu thập vào pathVarKeys, về sau không thêm trùng lặp vào query string
        Set<String> pathVarKeys = new HashSet<>();
        if (url.contains("{")) {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (url.contains(placeholder) && entry.getValue() != null && entry.getValue().length > 0) {
                    url = url.replace(placeholder, entry.getValue()[0]);
                    pathVarKeys.add(entry.getKey());
                }
            }
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        if (HttpMethod.GET.matches(method)
                || HttpMethod.DELETE.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method)
                || HttpMethod.TRACE.matches(method)) {
            //Ghép tham số
            if (!request.getParameterMap().isEmpty()) {
                if (StrUtil.isNotEmpty(openApi.getParamsJson())) {
                    List<OpenApiParam> params = JSON.parseArray(openApi.getParamsJson(),OpenApiParam.class);
                    if (params.size()>0) {
                        Map<String, OpenApiParam> openApiParamMap = params.stream().collect(Collectors.toMap(p -> p.getParamKey(), p -> p, (e, r) -> e));
                        request.getParameterMap().forEach((k, v) -> {
                            // Key đã được thay thế làm tham số đường dẫn sẽ không còn được thêm vào query string
                            if (pathVarKeys.contains(k)) {
                                return;
                            }
                            OpenApiParam openApiParam = openApiParamMap.get(k);
                            if (Objects.nonNull(openApiParam)) {
                                if(v==null&&StrUtil.isNotEmpty(openApiParam.getDefaultValue())){
                                    builder.queryParam(openApiParam.getParamKey(), openApiParam.getDefaultValue());
                                }
                                if (v!=null){
                                    builder.queryParam(openApiParam.getParamKey(), v);
                                }
                            }
                        });
                    }
                }

            }
        }
        URI targetUrl = builder.build().encode().toUri();
        return restTemplate.exchange(targetUrl.toString(), Objects.requireNonNull(HttpMethod.valueOf(method)), httpEntity, Result.class, request.getParameterMap()).getBody();
    }

    /**
     * Tạo token truy cập giao diện Token
     *
     * @param USERNAME
     * @param PASSWORD
     * @return
     */
    private String getToken(String USERNAME, String PASSWORD) {
        String token = JwtUtil.sign(USERNAME, PASSWORD, CommonConstant.CLIENT_TYPE_PC);
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 60);
        return token;
    }

    /**
     * Kiểm tra đường dẫn giao diện gốc có hợp lệ hay không:
     *  - Đường dẫn tương đối: phải bắt đầu bằng / ở đầu, không cho phép // và .. Ngăn chặn vượt qua đường dẫn
     *  - URL đầy đủ: chỉ cho phép http/https giao thức, cấm file/ftp/gopher/jar/netdoc và các giao thức khác (dùng để gọi liên mô-đun trong chế độ microservice)
     */
    private void validOriginUrl(String originUrl) {
        if (oConvertUtils.isEmpty(originUrl)) {
            throw new JeecgBootBizTipException("Đường dẫn API gốc không được để trống");
        }
        String decoded;
        try {
            decoded = java.net.URLDecoder.decode(originUrl, "UTF-8");
            // Giải mã lần hai, ngăn chặn %252f Việc bỏ qua bằng mã hóa kép dạng này
            decoded = java.net.URLDecoder.decode(decoded, "UTF-8");
        } catch (Exception e) {
            throw new JeecgBootBizTipException("Đường dẫn giao diện gốc chứa ký tự không hợp lệ");
        }
        //update-begin---author:scott ---date:20260430  for：【issues/9590] Khi triển khai microservice bằng nginx, không truy cập được interface openApi-----------
        // Khi triển khai microservice, API được cấu hình trong OpenAPI có thể nằm ở module microservice khác (ví dụ erp 7003), cho phép originUrl cấu hình trực tiếp đầy đủ http(s) URL
        String lower = decoded.toLowerCase();
        boolean isFullHttpUrl = lower.startsWith("http://") || lower.startsWith("https://");
        if (!isFullHttpUrl) {
            if (!decoded.startsWith("/")) {
                throw new JeecgBootBizTipException("Đường dẫn API gốc phải bắt đầu bằng / ở đầu, hoặc điền đầy đủ http(s) URL");
            }
            if (decoded.startsWith("//") || decoded.startsWith("/\\")) {
                throw new JeecgBootBizTipException("Đường dẫn giao diện gốc không được bắt đầu bằng // hoặc /\\ Đầu");
            }
            if (lower.contains("://") || lower.startsWith("file:") || lower.startsWith("ftp:") || lower.startsWith("gopher:")
                    || lower.startsWith("jar:") || lower.startsWith("netdoc:")) {
                throw new JeecgBootBizTipException("Đường dẫn giao diện gốc chỉ hỗ trợ đường dẫn tương đối hoặc http(s) đầy đủ URL");
            }
        } else {
            // Ngay cả khi là URL đầy đủ, cũng cấm các giao thức nguy hiểm khác (ngăn chặn http://x@file:/...  các tình huống vượt qua tương tự）
            String afterScheme = lower.substring(lower.indexOf("://") + 3);
            if (afterScheme.contains("file:") || afterScheme.contains("ftp:") || afterScheme.contains("gopher:")
                    || afterScheme.contains("jar:") || afterScheme.contains("netdoc:")) {
                throw new JeecgBootBizTipException("Đường dẫn giao diện gốc không được phép lồng nhau file/ftp/gopher/jar/netdoc các giao thức");
            }
            //update-begin---author:liusq ---date:2026-06-29  for：【issues/9726] Sửa SSRF lưu trữ: URL đầy đủ phải kiểm tra host, chỉ cho qua địa chỉ mạng nội bộ-----------
            // Chỉ kiểm tra giao thức không ngăn được SSRF: host của URL đầy đủ có thể trỏ tới loopback/Mạng nội bộ/Siêu dữ liệu đám mây. Ở đây tái sử dụng hai bộ kiểm tra bổ sung cho nhau,
            // Giao nhau chỉ cho phép mạng nội bộ RFC1918 (tương thích với gọi liên module của microservice, ví dụ erp:7003), chặn chết loopback, link-local, mạng công cộng:
            //  1) checkSsrfHttpUrl: Chặn loopback(127.x/::1)với liên kết cục bộ(169.254.x，chứa siêu dữ liệu đám mây 169.254.169.254)
            SsrfFileTypeFilter.checkSsrfHttpUrl(decoded);
            //  2) checkInternalUrl: Chặn địa chỉ mạng công cộng, host phải được phân giải thành mạng nội bộ (loopback/Mạng LAN/link-local)
            CommonUtils.checkInternalUrl(decoded);
            //update-end---author:liusq ---date:2026-06-29  for：【issues/9726] Sửa SSRF lưu trữ: URL đầy đủ phải kiểm tra host, chỉ cho qua địa chỉ mạng nội bộ-----------
        }
        if (decoded.contains("..")) {
            throw new JeecgBootBizTipException("Đường dẫn API gốc không được chứa ..");
        }
        //update-end---author:scott ---date:20260430  for：【issues/9590] Khi triển khai microservice bằng nginx, không truy cập được interface openApi-----------
    }

    @GetMapping("/json")
    public SwaggerModel swaggerModel(HttpServletRequest request) {
        // Phân tích động từ request hiện tại host/basePath/scheme, tương thích với kịch bản gateway và reverse proxy
        String baseUrl = CommonUtils.getBaseUrl(request);
        String host;
        // Ưu tiên lấy request.getContextPath()：
        //  - Chế độ đơn thể server.servlet.context-path=/jeecg-boot → basePath=/jeecg-boot
        //  - Chế độ gateway microservice: container system không có context-path → basePath=""(đường dẫn thực tế của request là "/openapi/json", sẽ bị uri.getPath() bị ghi đè thành rỗng)
        // Sửa【issues/URL tài liệu OpenAPI bị mang nhầm tiền tố trong môi trường vi dịch vụ /jeecg-boot】: Triển khai ban đầu hard-code mặc định "/jeecg-boot"，
        // Dẫn đến cổng microservice (gateway:9999) tài liệu bên dưới Request URL luôn mang theo /jeecg-boot trong khi gateway thực tế không có tiền tố này, curl báo lỗi 404.
        String basePath = request.getContextPath() == null ? "" : request.getContextPath();
        try {
            java.net.URI uri = new java.net.URI(baseUrl);
            host = uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost();
            if (uri.getPath() != null && !uri.getPath().isEmpty()) {
                basePath = uri.getPath();
            }
        } catch (Exception e) {
            host = request.getServerName() + ":" + request.getServerPort();
        }

        SwaggerModel swaggerModel = new SwaggerModel();
        swaggerModel.setSwagger("2.0");
        swaggerModel.setInfo(swaggerInfo());
        swaggerModel.setHost(host);
        swaggerModel.setBasePath(basePath);
        swaggerModel.setSchemes(Lists.newArrayList("http", "https"));

        SwaggerTag swaggerTag = new SwaggerTag();
        swaggerTag.setName("openapi");
        swaggerModel.setTags(Lists.newArrayList(swaggerTag));

        pathsAndDefinitions(swaggerModel);

        return swaggerModel;
    }

    private void pathsAndDefinitions(SwaggerModel swaggerModel) {
        Map<String, Map<String, SwaggerOperation>> paths = new HashMap<>();
        Map<String, SwaggerDefinition> definitions = new HashMap<>();
        List<OpenApi> openapis = service.list();
        for (OpenApi openApi : openapis) {
            Map<String, SwaggerOperation> operations = new HashMap<>();
            SwaggerOperation operation = new SwaggerOperation();
            operation.setTags(Lists.newArrayList("openapi"));
            operation.setSummary(openApi.getName());
            operation.setDescription(openApi.getName());
            operation.setOperationId(openApi.getRequestUrl()+"Using"+openApi.getRequestMethod());
            operation.setProduces(Lists.newArrayList("application/json"));
            parameters(operation, openApi);

            // body Tham số đầu vào
            if (StringUtils.hasText(openApi.getRequestBody())) {
                SwaggerDefinition definition = new SwaggerDefinition();
                definition.setType("object");
                Map<String, SwaggerDefinitionProperties> definitionProperties = new HashMap<>();
                definition.setProperties(definitionProperties);
                if (openApi.getRequestBody()!=null){
                    JSONObject jsonObject = JSONObject.parseObject(openApi.getRequestBody());
                    if (jsonObject.size()>0){
                        for (Map.Entry<String, Object> properties : jsonObject.entrySet()) {
                            SwaggerDefinitionProperties swaggerDefinitionProperties = new SwaggerDefinitionProperties();
                            swaggerDefinitionProperties.setType("string");
                            swaggerDefinitionProperties.setDescription(properties.getValue()+"");
                            definitionProperties.put(properties.getKey(), swaggerDefinitionProperties);
                        }
                    }
                }
                // body definition đã được xây dựng xong
                definitions.put(openApi.getRequestUrl()+"Using"+openApi.getRequestMethod()+"body", definition);

                SwaggerOperationParameter bodyParameter = new SwaggerOperationParameter();
                bodyParameter.setDescription(openApi.getName() + " body");
                bodyParameter.setIn("body");
                bodyParameter.setName(openApi.getName() + " body");
                bodyParameter.setRequired(true);

                Map<String, String> bodySchema = new HashMap<>();
                bodySchema.put("$ref", "#/definitions/" + openApi.getRequestUrl()+"Using"+openApi.getRequestMethod()+"body");
                bodyParameter.setSchema(bodySchema);

                // Xây dựng tham số hoàn tất
                operation.getParameters().add(bodyParameter);

            }

            // Phản hồi
            Map<String, SwaggerOperationResponse> responses = new HashMap<>();
            SwaggerOperationResponse resp200 = new SwaggerOperationResponse();
            resp200.setDescription("OK");
            Map<String, String> respSchema = new HashMap<>();
            respSchema.put("$ref", "#/definitions/OpenApiResult");
            resp200.setSchema(respSchema);

            responses.put("200", resp200);

            Map<String, String> emptySchema = new HashMap<>();
            SwaggerOperationResponse resp201 = new SwaggerOperationResponse();
            resp201.setDescription("Created");
            resp201.setSchema(emptySchema);
            responses.put("201", resp201);
            SwaggerOperationResponse resp401 = new SwaggerOperationResponse();
            resp401.setDescription("Unauthorized");
            resp401.setSchema(emptySchema);
            responses.put("401", resp401);
            SwaggerOperationResponse resp403 = new SwaggerOperationResponse();
            resp403.setDescription("Forbidden");
            resp403.setSchema(emptySchema);
            responses.put("403", resp403);
            SwaggerOperationResponse resp404 = new SwaggerOperationResponse();
            resp404.setDescription("Not Found");
            resp404.setSchema(emptySchema);
            responses.put("404", resp404);

            // Xây dựng phản hồi definition
            SwaggerDefinition respDefinition = new SwaggerDefinition();
            respDefinition.setType("object");

            Map<String, SwaggerDefinitionProperties> definitionProperties = new HashMap<>();
            respDefinition.setProperties(definitionProperties);

            SwaggerDefinitionProperties codeProperties = new SwaggerDefinitionProperties();
            codeProperties.setType("integer");
            codeProperties.setDescription("Mã trả về");
            definitionProperties.put("code", codeProperties);
            SwaggerDefinitionProperties messageProperties = new SwaggerDefinitionProperties();
            messageProperties.setType("string");
            messageProperties.setDescription("Trả về thông báo xử lý");
            definitionProperties.put("message", messageProperties);
            SwaggerDefinitionProperties resultProperties = new SwaggerDefinitionProperties();
            resultProperties.setType("object");
            resultProperties.setDescription("Trả về đối tượng dữ liệu");
            definitionProperties.put("result", resultProperties);
            SwaggerDefinitionProperties successProperties = new SwaggerDefinitionProperties();
            successProperties.setType("boolean");
            successProperties.setDescription("Cờ thành công");
            definitionProperties.put("success", successProperties);
            SwaggerDefinitionProperties timestampProperties = new SwaggerDefinitionProperties();
            timestampProperties.setType("integer");
            timestampProperties.setDescription("Dấu thời gian");
            definitionProperties.put("timestamp", timestampProperties);

            definitions.put("OpenApiResult", respDefinition);


            operation.setResponses(responses);
            operations.put(openApi.getRequestMethod().toLowerCase(), operation);
            paths.put("/openapi/call/"+openApi.getRequestUrl(), operations);
        }

        swaggerModel.setDefinitions(definitions);
        swaggerModel.setPaths(paths);

    }

    private void parameters(SwaggerOperation operation, OpenApi openApi) {
        List<SwaggerOperationParameter> parameters = new ArrayList<>();
        if (openApi.getParamsJson()!=null) {
            String originUrl = openApi.getOriginUrl() != null ? openApi.getOriginUrl() : "";
            List<OpenApiParam> openApiParams = JSON.parseArray(openApi.getParamsJson(), OpenApiParam.class);
            for (OpenApiParam openApiParam : openApiParams) {
                SwaggerOperationParameter parameter = new SwaggerOperationParameter();
                // Gọi từ bên ngoài thống nhất dùng query để truyền tham số; nếu tham số đó trong đường dẫn API gốc bắt đầu bằng {key} xuất hiện dưới dạng, thêm ghi chú giải thích
                boolean isPathVar = originUrl.contains("{" + openApiParam.getParamKey() + "}");
                parameter.setIn("query");
                parameter.setName(openApiParam.getParamKey());
                parameter.setRequired(openApiParam.getRequired() == 1);
                String desc = openApiParam.getNote() != null ? openApiParam.getNote() : "";
                parameter.setDescription(isPathVar ? desc + (desc.isEmpty() ? "" : " ") + "[Tham số đường dẫn]" : desc);
                parameters.add(parameter);
            }
        }
        if (openApi.getHeadersJson()!=null) {
            List<OpenApiHeader> openApiHeaders = JSON.parseArray(openApi.getHeadersJson(), OpenApiHeader.class);
            for (OpenApiHeader openApiHeader : openApiHeaders) {
                SwaggerOperationParameter parameter = new SwaggerOperationParameter();
                parameter.setIn("header");
                parameter.setName(openApiHeader.getHeaderKey());
                parameter.setRequired(openApiHeader.getRequired() == 1);
                parameter.setDescription(openApiHeader.getNote());
                parameters.add(parameter);
            }
        }
        operation.setParameters(parameters);
    }

    private SwaggerInfo swaggerInfo() {
        SwaggerInfo info = new SwaggerInfo();

        info.setDescription("OpenAPI Danh sách API");
        info.setVersion("3.9.6");
        info.setTitle("OpenAPI Danh sách API");
        //info.setTermsOfService("https://jeecg.com");

        SwaggerInfoContact contact = new SwaggerInfoContact();
        contact.setName("jeecg@qq.com");

        info.setContact(contact);

        SwaggerInfoLicense license = new SwaggerInfoLicense();
        license.setName("Apache 2.0");
        license.setUrl("http://www.apache.org/licenses/LICENSE-2.0.html");

        info.setLicense(license);

        return info;
    }

    /**
     * Tạo đường dẫn giao diện
     * @return
     */
    @GetMapping("genPath")
    public Result<String> genPath() {
        Result<String> r = new Result<String>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setResult(PathGenerator.genPath());
        return r;
    }
}
