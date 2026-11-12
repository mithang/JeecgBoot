package org.jeecg.modules.airag.prompts.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.prompts.consts.AiPromptsConsts;
import org.jeecg.modules.airag.prompts.entity.AiragExtData;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import org.jeecg.modules.airag.prompts.mapper.AiragPromptsMapper;
import org.jeecg.modules.airag.prompts.service.IAiragExtDataService;
import org.jeecg.modules.airag.prompts.service.IAiragPromptsService;
import org.jeecg.modules.airag.prompts.vo.AiragExperimentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Slf4j
@Service("airagPromptsServiceImpl")
public class AiragPromptsServiceImpl extends ServiceImpl<AiragPromptsMapper, AiragPrompts> implements IAiragPromptsService {
    @Autowired
    IAIChatHandler aiChatHandler;

    @Autowired
    IAiragExtDataService airagExtDataService;

    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;
    // Tạo thread pool tĩnh, đảm bảo chỉ có một instance trong toàn bộ vòng đời ứng dụng
    private static final ExecutorService executor = new ThreadPoolExecutor(
            4, // Số luồng core
            8, // Số luồng tối đa
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), // Ngăn tràn bộ nhớ
            new ThreadPoolExecutor.CallerRunsPolicy() // Chính sách từ chối
    );

    //update-begin---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------
    @Override
    public IPage<AiragPrompts> recycleBinPage(Page<AiragPrompts> page) {
        return this.baseMapper.selectRecycleBinPage(page);
    }

    @Override
    public void revertRecycleBin(List<String> ids) {
        this.baseMapper.revertRecycleBin(ids);
    }

    @Override
    public void deleteRecycleBin(List<String> ids) {
        this.baseMapper.deleteRecycleBin(ids);
    }
    //update-end---author:chenrui ---date:2026-04-07  for：【QQYUN-14643] Triển khai thu hồi từ thùng rác và xóa hoàn toàn-----------

    /**
     * Thử nghiệm prompt
     * @param experimentVo
     * @return
     */
    @Override
    public Result<?> promptExperiment(AiragExperimentVo experimentVo, HttpServletRequest request) {
        log.info("Bắt đầu thực thi thử nghiệm prompt, tham số:{}", JSON.toJSONString(experimentVo));

        // Xác thực tham số
        String promptKey = experimentVo.getPromptKey();
        AssertUtils.assertNotEmpty("Vui lòng chọn prompt", promptKey);
        String dataId = experimentVo.getExtDataId();
        AssertUtils.assertNotEmpty("Vui lòng chọn tập dữ liệu", dataId);

        Map<String, String> fieldMappings = experimentVo.getMappings();
        AssertUtils.assertNotEmpty("Vui lòng cấu hình ánh xạ trường", fieldMappings);

        try {
            //1.Truy vấn prompt
            AiragPrompts airagPrompts = this.baseMapper.selectOne(new LambdaQueryWrapper<AiragPrompts>().eq(AiragPrompts::getId, promptKey));
            AssertUtils.assertNotEmpty("Không tìm thấy prompt đã chỉ định", airagPrompts);
            String modelParam = airagPrompts.getModelParam();
             // Lọc biến prompt
            JSONArray promptVariables;
            if(oConvertUtils.isNotEmpty(modelParam)){
                JSONObject airagPromptsParams = JSON.parseObject(modelParam);
                if(airagPromptsParams.containsKey("promptVariables")){
                    promptVariables = airagPromptsParams.getJSONArray("promptVariables");
                } else {
                    promptVariables = null;
                }
            } else {
                promptVariables = null;
            }
            //2.Truy vấn tập dữ liệu
            AiragExtData airagExtData = airagExtDataService.getById(dataId);
            AssertUtils.assertNotEmpty("Không tìm thấy tập dữ liệu đã chỉ định", airagExtData);
            String datasetValue = airagExtData.getDatasetValue();
            if(oConvertUtils.isEmpty(datasetValue)){
                return Result.error("Tập đánh giá không được trống!");
            }

            //3.Gọi bất đồng bộ: theo trường ánh xạ, gọi bộ đánh giá để đánh giá
            JSONObject datasetObj = JSONObject.parseObject(datasetValue);
            //Cấu hình cột đánh giá
            JSONArray columns = datasetObj.getJSONArray("columns");
            //Ngân hàng câu hỏi đánh giá
            JSONArray datasetArray = datasetObj.getJSONArray("dataSource");
            AssertUtils.assertNotEmpty("Không tìm thấy nguồn dữ liệu trong tập dữ liệu", datasetArray);
            AssertUtils.assertTrue("Nguồn dữ liệu trống", datasetArray.size() > 0);

            //Tập kết quả đánh giá - Sử dụng an toàn luồng CopyOnWriteArrayList
            List<JSONObject> scoreResult = new CopyOnWriteArrayList<>();

            // Gửi hàng loạt nhiệm vụ
            List<CompletableFuture<Void>> futures = IntStream.range(0, datasetArray.size())
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        log.info("Bắt đầu xử lý thứ{}bản ghi dữ liệu", i + 1);
                        //Định nghĩa kết quả trả về
                        JSONObject result = new JSONObject();
                        //Dữ liệu đánh giá
                        JSONObject dataset = datasetArray.getJSONObject(i);
                        result.putAll(dataset);
                        //Câu hỏi của người dùng
                        String userQuery = dataset.getString(fieldMappings.get("user_query"));
                        result.put("userQuery", userQuery);
                        //Xử lý biến
                       if(!CollectionUtils.isEmpty(promptVariables)){
                           String content = airagPrompts.getContent();
                           for (Object var : promptVariables){
                              JSONObject variable = JSONObject.parseObject(var.toString());
                              String name = dataset.getString(fieldMappings.get(variable.getString("name")));
                              //Giá trị biến mặc định của prompt
                              String defaultValue = variable.getString("value");
                              // Lấy loại đích
                              String dataType = findDataType(columns, variable);
                               if("FILE".equals(dataType)){
                                  defaultValue = getFileAccessHttpUrl(request, defaultValue);
                                  name = getFileAccessHttpUrl(request, name);
                              }
                              if(oConvertUtils.isNotEmpty(name)){
                                  //Thay thế giá trị biến của tập đánh giá trong prompt
                                  content = content.replaceAll(variable.getString("name"), name);
                              }else if(oConvertUtils.isNotEmpty(defaultValue)){
                                  content = content.replaceAll(variable.getString("name"), defaultValue);
                              }
                          }
                           airagPrompts.setContent(content);
                       }

                        //Câu trả lời của prompt
                        String promptAnswer = getPromptAnswer(airagPrompts, dataset, fieldMappings);
                        result.put("promptAnswer", promptAnswer);

                        //Câu trả lời của bộ đánh giá
                        String answerScore = getAnswerScore(promptAnswer, dataset, fieldMappings, airagExtData);
                        result.put("answerScore", answerScore);

                        scoreResult.add(result);
                        log.info("Thứ{}bản ghi dữ liệu đã xử lý xong", i + 1);
                    } catch (Exception e) {
                        log.error("Xử lý thứ{}bản ghi dữ liệu thì xảy ra ngoại lệ", i + 1, e);
                        // Ném lại ngoại lệ để CompletableFuture bắt được
                        throw new CompletionException(e);
                    }
                }, executor))
                .collect(Collectors.toList());

            // Xử lý xong theo cách không chặn
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Xử lý hàng loạt thất bại", ex);
                        // Cập nhật trạng thái thành thất bại
                        airagExtData.setStatus(AiPromptsConsts.STATUS_FAILED);
                    } else {
                        log.info("Tất cả dữ liệu đã xử lý xong, tổng cộng đã xử lý{}bản ghi dữ liệu", scoreResult.size());
                        // Truy vấn bản ghi đánh giá đã tồn tại
                        List<AiragExtData> existingTracks = airagExtDataService.queryTrackById(dataId);
                        Integer version = 1;
                        if(!CollectionUtils.isEmpty(existingTracks)) {
                            version = existingTracks.stream()
                                    .map(AiragExtData::getVersion)
                                    .max(Integer::compareTo)
                                    .orElse(0) + 1;
                        }
                        for (JSONObject item : scoreResult) {
                            // Lưu kết quả
                            AiragExtData track = new AiragExtData();
                            //Liên kết bộ đánh giá ID
                            track.setMetadata(dataId);
                            //Định nghĩa loại
                            track.setBizType(AiPromptsConsts.BIZ_TYPE_TRACK);
                            //Định nghĩa phiên bản
                            track.setVersion(version);
                            //Định nghĩa trạng thái
                            track.setStatus(AiPromptsConsts.STATUS_COMPLETED);
                            //Định nghĩa kết quả đánh giá
                            track.setDataValue(item.toJSONString());
                            airagExtDataService.save(track);
                        }
                        // Cập nhật trạng thái thành hoàn thành
                        airagExtData.setStatus(AiPromptsConsts.STATUS_COMPLETED);
                    }
                    airagExtDataService.updateById(airagExtData);
                });

            //4.Sửa trạng thái thành đang thực hiện
            airagExtData.setStatus(AiPromptsConsts.STATUS_RUNNING);
            airagExtDataService.updateById(airagExtData);

            log.info("Thử nghiệm prompt đã được gửi, tổng cộng{}bản ghi dữ liệu đang chờ xử lý", datasetArray.size());
            return Result.OK("Thử nghiệm đã bắt đầu, đang xử lý dữ liệu");
        } catch (Exception e) {
            log.error("Thực thi thử nghiệm prompt thất bại", e);
            return Result.error("Thực thi thử nghiệm thất bại:" + e.getMessage());
        }
    }


    /**
     * Kết quả trả lời của prompt
     * @param airagPrompts
     * @param questions
     * @param fieldMappings
     * @return
     */
    public String getPromptAnswer(AiragPrompts airagPrompts, JSONObject questions, Map<String, String> fieldMappings) {
        try {
            //0.Kiểm tra xem có cấu hình giá trị value của fieldMappings để phán đoán hay không, có chứa actual_output
            if (!fieldMappings.containsValue("actual_output")) {
                log.warn("Trong ánh xạ trường chưa cấu hình actual_output");
                return null;
            }

            //1.Prompt
            String prompt = airagPrompts.getContent();
            AssertUtils.assertNotEmpty("Vui lòng nhập prompt", prompt);

            String userQuery = questions.getString(fieldMappings.get("user_query"));
            AssertUtils.assertNotEmpty("Vui lòng nhập nội dung kiểm thử", userQuery);

            //2.ai Lắp ráp câu hỏi
            List<ChatMessage> messages = Arrays.asList(new SystemMessage(prompt), new UserMessage(userQuery));

            //3.Dữ liệu mô hình
            String modelId = airagPrompts.getModelId();
            AssertUtils.assertNotEmpty("Vui lòng chọn mô hình", modelId);

            //4.Tham số mô hình
            String modelParam = airagPrompts.getModelParam();
            // Tham số mô hình lớn mặc định
            AIChatParams params = new AIChatParams();
            params.setTemperature(0.8);
            params.setTopP(0.9);
            params.setPresencePenalty(0.1);
            params.setFrequencyPenalty(0.1);

            if(oConvertUtils.isNotEmpty(modelParam)){
                JSONObject param = JSON.parseObject(modelParam);
                if(param.containsKey("temperature")){
                    params.setTemperature(param.getDoubleValue("temperature"));
                }
                if(param.containsKey("topP")){
                    params.setTopP(param.getDoubleValue("topP")); // Sửa lỗi: đặt vào trường chính xác
                }
                if(param.containsKey("presencePenalty")){
                    params.setPresencePenalty(param.getDoubleValue("presencePenalty")); // Sửa lỗi: đặt vào trường chính xác
                }
                if(param.containsKey("frequencyPenalty")){
                    params.setFrequencyPenalty(param.getDoubleValue("frequencyPenalty")); // Sửa lỗi: đặt vào trường chính xác
                }
            }

            log.debug("Gọi mô hình AI, ID mô hình:{}，tham số:{}", modelId, JSON.toJSONString(params));
            //5.AI Hỏi đáp
            String promptAnswer = aiChatHandler.completions(modelId, messages, params);
            log.debug("AI Kết quả mô hình trả về:{}", promptAnswer);

            return promptAnswer;
        } catch (Exception e) {
            log.error("Lấy câu trả lời của prompt thất bại", e);
            return null;
        }
    }

    /**
     * Chấm điểm câu trả lời đánh giá
     * @return
     */
    public String getAnswerScore(String promptAnswer, JSONObject questions, Map<String, String> fieldMappings, AiragExtData airagExtData) {
        try {
            //1.Prompt
            String prompt = airagExtData.getDataValue();
            AssertUtils.assertNotEmpty("Vui lòng nhập prompt", prompt);
            prompt += "Định nghĩa định dạng trả về: Điểm: điểm cuối cùng, phải xuất ra một số thể hiện mức độ đáp ứng tiêu chuẩn chấm điểm trong Prompt. Phạm vi điểm từ 0.0 đến 1.0，1.0 thể hiện hoàn toàn đáp ứng tiêu chuẩn chấm điểm,0.0 thể hiện hoàn toàn không đáp ứng tiêu chuẩn chấm điểm.\n" +
                    "Lý do:{Giải thích về tính dễ đọc của điểm, nêu lý do chấm điểm}. Cuối cùng, phải kết thúc lý do bằng một câu, câu đó là: Vì vậy, điểm nên cho là<điểm bạn đã đánh giá>. Không được trả về câu hỏi đã hỏi, thêm quá trình phân tích, giải thích hay nội dung khác, chỉ trả về nội dung theo định dạng yêu cầu";

            String userQuery = "Nội dung nhập vào:";
            //2.Nối nội dung kiểm tra
            for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                // trong bộ đánh giá key
                String key = entry.getKey();
                // của ánh xạ trong bộ đánh giá key
                String value = entry.getValue();
                String valueData;
                if("actual_output".equalsIgnoreCase(value)){
                    valueData = promptAnswer;
                }else{
                    valueData = questions.getString(value);
                }
                userQuery += (key + ":" + valueData + " ");
            }
            List<ChatMessage> messages = Arrays.asList(new SystemMessage(prompt), new UserMessage(userQuery));

            //3.Dữ liệu mô hình
            String metadata = airagExtData.getMetadata();
            if(oConvertUtils.isNotEmpty(metadata)){
                JSONObject modelParam = JSONObject.parseObject(metadata);
                String modelId = modelParam.getString("modelId");
                AssertUtils.assertNotEmpty("ID mô hình bộ đánh giá không được trống", modelId);

                // Tham số mô hình lớn mặc định
                AIChatParams params = new AIChatParams();
                params.setTemperature(0.8);
                params.setTopP(0.9);
                params.setPresencePenalty(0.1);
                params.setFrequencyPenalty(0.1);

                if(oConvertUtils.isNotEmpty(modelParam)){
                    if(modelParam.containsKey("temperature")){
                        params.setTemperature(modelParam.getDoubleValue("temperature"));
                    }
                    if(modelParam.containsKey("topP")){
                        params.setTopP(modelParam.getDoubleValue("topP")); // Sửa lỗi: đặt vào trường chính xác
                    }
                    if(modelParam.containsKey("presencePenalty")){
                        params.setPresencePenalty(modelParam.getDoubleValue("presencePenalty")); // Sửa lỗi: đặt vào trường chính xác
                    }
                    if(modelParam.containsKey("frequencyPenalty")){
                        params.setFrequencyPenalty(modelParam.getDoubleValue("frequencyPenalty")); // Sửa lỗi: đặt vào trường chính xác
                    }
                }

                log.debug("Gọi mô hình bộ đánh giá, ID mô hình:{}，tham số:{}", modelId, JSON.toJSONString(params));
                //5.AI Hỏi đáp
                String answerScore = aiChatHandler.completions(modelId, messages, params);
                log.debug("Kết quả mô hình bộ đánh giá trả về:{}", answerScore);

                return answerScore;
            }
            return null;
        } catch (Exception e) {
            log.error("Lấy điểm chấm câu trả lời thất bại", e);
            return null;
        }
    }

    /**
     *
     * @param columns
     * @param variable
     * @return
     */
    public static String findDataType(JSONArray columns, JSONObject variable) {
        // Lấy giá trị trường đích
        String targetName = variable.getString("name");

        // Sử dụng Stream API để tìm và lấy dataType
        return columns.stream()
                .map(obj -> JSONObject.parseObject(obj.toString()))
                .filter(column -> targetName.equals(column.getString("name")))
                .findFirst()
                .map(column -> column.getString("dataType"))
                .orElse(null); // Nếu không tìm thấy, trả về null
    }
    /**
     * Lấy địa chỉ hình ảnh
     * @param request
     * @param url
     * @return
     */
    private String getFileAccessHttpUrl(HttpServletRequest request,String url){
        if(oConvertUtils.isNotEmpty(url) && url.startsWith("http")){
            return url;
        }else{
            return CommonUtils.getBaseUrl(request) + "/sys/common/static/" + url;
        }
    }
}
