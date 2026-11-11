package org.jeecg.modules.system.service;

import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;

import java.util.List;

/**
 * Kết nối ứng dụng bên thứ ba
 * @author: jeecg-boot
 */
public interface IThirdAppService {

    /**
     * Lấy AccessToken
     * @return String
     */
    String getAccessToken();

    /**
     * Đồng bộ phòng ban cục bộ sang bên thứ ba App<br>
     * Hướng đồng bộ: Local --> Bên thứ ba APP
     * Logic đồng bộ:<br>
     * 1. Trước tiên phán đoán đã đồng bộ hay chưa, có thì sửa, không thì tạo mới;<br>
     * 2. Nếu local không có nhưng app bên thứ ba có thì xóa trong app bên thứ ba.
     * @param ids
     * @return thành công thì trả về true
     */
    SyncInfoVo syncLocalDepartmentToThirdApp(String ids);

//    /**
//     * Đồng bộ phòng ban App bên thứ ba về cục bộ<br>
//     * Hướng đồng bộ: bên thứ ba APP --> Cục bộ
//     * Logic đồng bộ:<br>
//     * 1. Trước tiên phán đoán đã đồng bộ hay chưa, có thì sửa, không thì tạo mới;<br>
//     * 2. Nếu local không có nhưng app bên thứ ba có thì xóa trong app bên thứ ba.
//     * @param ids
//     * @return thành công thì trả về true
//     */
//    SyncInfoVo syncThirdAppDepartmentToLocal(String ids);

    /**
     * Đồng bộ người dùng cục bộ sang bên thứ ba App<br>
     * Hướng đồng bộ: Local --> Bên thứ ba APP <br>
     * Logic đồng bộ: kiểm tra trước xem đã đồng bộ hay chưa, có rồi thì sửa, chưa có thì tạo mới<br>
     * Lưu ý: đồng bộ trạng thái của nhân sự, ví dụ như nghỉ việc, bị vô hiệu hóa, xóa logic, v.v.
     * (Điểm đặc biệt: 1. Logic hiện tại cố tình không xóa người dùng, để phòng trường hợp WeChat Doanh nghiệp triển khai sớm, người dùng đã tồn tại nhưng trên nền tảng không có người dùng này.
     *  WeChat Doanh nghiệp hỗ trợ vô hiệu hóa tài khoản; DingTalk không hỗ trợ
     *  2, trong WeChat Doanh nghiệp việc kích hoạt dùng số điện thoại, chỉ người dùng tự sửa được, không cho phép sửa qua API)
     * @param ids
     * @return Thành công trả về mảng rỗng, thất bại trả về thông tin lỗi
     */
    SyncInfoVo syncLocalUserToThirdApp(String ids);

//    /**
//     * Đồng bộ người dùng App bên thứ ba về cục bộ<br>
//     * Hướng đồng bộ: bên thứ ba APP --> Cục bộ <br>
//     * Logic đồng bộ: kiểm tra trước xem đã đồng bộ hay chưa, có rồi thì sửa, chưa có thì tạo mới<br>
//     * Lưu ý: đồng bộ trạng thái của nhân sự, ví dụ như nghỉ việc, bị vô hiệu hóa, xóa logic, v.v.
//     *
//     * @return Thành công trả về mảng rỗng, thất bại trả về thông tin lỗi
//     */
//    SyncInfoVo syncThirdAppUserToLocal();

    /**
     * Xóa người dùng của APP bên thứ ba theo ID người dùng local
     *
     * @param userIdList Danh sách ID người dùng cục bộ
     * @return 0 0 nghĩa là thành công, các giá trị khác nghĩa là thất bại
     */
    int removeThirdAppUser(List<String> userIdList);

    /**
     * Gửi tin nhắn
     *
     * @param message
     * @param verifyConfig Có kiểm tra cấu hình hay không (APP chưa bật sẽ từ chối gửi)
     * @return
     */
    boolean sendMessage(MessageDTO message, boolean verifyConfig);

    /**
     * Gửi tin nhắn
     * @param message
     * @return boolean
     */
    boolean sendMessage(MessageDTO message);

}
