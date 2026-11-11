package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.vo.SysPositionVO;

import java.util.List;

/**
 * @Description: Bảng chức vụ
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
public interface ISysPositionService extends IService<SysPosition> {

    /**
     * Truy vấn theo code
     * @param code Mã chức vụ
     * @return SysPosition
     */
    SysPosition getByCode(String code);

    /**
     * Lấy danh sách tên chức vụ qua id người dùng
     * @param userId
     * @return
     */
    List<SysPosition> getPositionList(String userId);

    /**
     * Lấy tên vị trí
     * @param postList
     * @return
     */
    String getPositionName(List<String> postList);

    /**
     * Truy vấn hàng loạt VO chức vụ theo danh sách id người dùng (chứa trường userId, dùng trong kịch bản đồng bộ hàng loạt để loại bỏ N+1 truy vấn)
     *
     * @param userIds Danh sách ID người dùng
     * @return Danh sách VO vị trí (mỗi bản ghi chứa trường userId để phía gọi nhóm lại)
     */
    List<SysPositionVO> getPositionListByUserIds(List<String> userIds);

    //update-begin---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------
    /**
     * Xác thực quyền quản lý chức vụ: admin, quản trị viên tenant(zuhuadmin)Vai trò hoặc khi có quyền nút tương ứng thì cho phép (quản lý thành viên vẫn dùng system:position:edit）；
     * Nếu không, chỉ có trang «Quản lý tổ chức» của QiaoQiaoYun-Các API mà trang [Quản lý tổ chức - Chức vụ] dùng đến có thể tiếp tục: phải là quản trị viên tài khoản tổ chức của tenant hiện tại hoặc siêu quản trị viên, và chỉ được thao tác chức vụ và người dùng của tenant này.
     * Người dùng đăng ký có thể tự tạo tổ chức và trở thành siêu quản trị viên, nên đường dẫn này phải giới hạn trong tenant hiện tại
     *
     * @param permission       Mã quyền nút bấm
     * @param allowTenantAdmin Có cho phép quản trị viên Tenant của QiaoQiaoYun hay không
     * @return Khi cần giới hạn thao tác trong tenant hiện tại thì trả về ID tenant hiện tại; khi là admin, zuhuadmin hoặc có quyền nút thì trả về null
     */
    Integer checkPositionAuth(String permission, boolean allowTenantAdmin);

    /**
     * Chức vụ có thuộc Tenant chỉ định hay không
     *
     * @param positionId Chức danh ID
     * @param tenantId   Tenant ID
     * @return thuộc về thì trả về true
     */
    boolean isPositionInTenant(String positionId, Integer tenantId);
    //update-end---author:wangshuai ---date:2026-10-26  for：【GHSA-p4f8-h832-99pw】Bổ sung kiểm tra quyền hạn khi thêm, xóa, sửa chức vụ và quản lý thành viên-----------

}
