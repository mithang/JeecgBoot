package org.jeecg.modules.airag.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.vo.AiArticleWriteVersionVo;
import java.util.List;

/**
 * @Description: AI Ứng dụng
 * @Author: jeecg-boot
 * @Date:   2025-02-26
 * @Version: V1.0
 */
public interface IAiragAppService extends IService<AiragApp> {

    /**
     * Phát hành/Hủy phát hành ứng dụng và duy trì token chia sẻ
     *
     * @param id Ứng dụng ID
     * @param release true=Phát hành,false=Hủy phát hành
     * @return Khi phát hành thành công trả về shareToken; khi hủy phát hành trả về null
     * @author scott
     * @since 2026-07-21 【issues/9787] Token chia sẻ cấp ứng dụng
     */
    String releaseApp(String id, boolean release);

	/**
	 * Sao chép ứng dụng
	 *
	 * @param id Ứng dụng gốc ID
	 * @param currentTenantId Tenant hiện tại ID
	 * @return Ứng dụng mới ID
	 * @author scott
	 * @since 2026-08-06 【LHZP-1512] Ứng dụng AI bổ sung chức năng sao chép
	 */
	String copyApp(String id, String currentTenantId);

    /**
     * Tạo prompt
     * @param prompt
     * @return blocking Có chặn hay không
     * @return
     * @author chenrui
     * @date 2025/3/12 14:45
     */
    Object generatePrompt(String prompt,boolean blocking);

    /**
     * Tạo prompt theo id ứng dụng
     *
     * @param variables
     * @param memoryId
     * @param blocking
     * @return
     */
    Object generateMemoryByAppId(String variables, String memoryId, boolean blocking);

    /**
     * Lưu bài viết
     * 
     * @param aiWriteVersionVo
     */
    void saveArticleWrite(AiArticleWriteVersionVo aiWriteVersionVo);

    /**
     * Danh sách bài viết
     * 
     * @return
     */
    List<AiArticleWriteVersionVo> listArticleWrite();

    /**
     * Xóa bài viết
     * 
     * @param version
     */
    void deleteArticleWrite(String version);

}
