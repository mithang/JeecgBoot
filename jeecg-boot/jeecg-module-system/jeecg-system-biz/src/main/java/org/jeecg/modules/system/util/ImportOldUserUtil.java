package org.jeecg.modules.system.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysUserDepartServiceImpl;
import org.jeecg.modules.system.service.impl.SysUserServiceImpl;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: Nhập phiên bản cũ
 * @author: wangshuai
 * @date: 2025/4/2 10:19
 */
@Slf4j
public class ImportOldUserUtil {

    public static Result<?> importOldSysUser(HttpServletRequest request) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        // Thông tin lỗi
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// Lấy đối tượng tệp tải lên
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysUser> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), SysUser.class, params);
                for (int i = 0; i < listSysUsers.size(); i++) {
                    SysUser sysUserExcel = listSysUsers.get(i);
                    if (StringUtils.isBlank(sysUserExcel.getPassword())) {
                        // Mật khẩu mặc định là “123456”
                        sysUserExcel.setPassword("123456");
                    }
                    // Mã hóa mật khẩu kèm salt
                    String salt = oConvertUtils.randomGen(8);
                    sysUserExcel.setSalt(salt);
                    String passwordEncode = PasswordUtil.encrypt(sysUserExcel.getUsername(), sysUserExcel.getPassword(), salt);
                    sysUserExcel.setPassword(passwordEncode);
                    try {
                        ISysUserService service = SpringContextUtils.getBean(SysUserServiceImpl.class);
                        service.save(sysUserExcel);
                        successLines++;
                    } catch (Exception e) {
                        errorLines++;
                        String message = e.getMessage().toLowerCase();
                        int lineNumber = i + 1;
                        // Xác định thông tin lỗi qua tên chỉ mục
                        if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Tên người dùng đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
                            errorMessage.add(" " + lineNumber + " Dòng: mã nhân viên đã tồn tại, bỏ qua khi nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Số điện thoại đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Email đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Vi phạm ràng buộc duy nhất của bảng.");
                        } else {
                            errorMessage.add(" " + lineNumber + " Dòng: Lỗi không xác định, bỏ qua việc nhập");
                            log.error(e.getMessage(), e);
                        }
                    }
                    // Thiết lập quan hệ liên kết giữa phòng ban và thông tin người dùng hàng loạt
                    String departIds = sysUserExcel.getDepartIds();
                    if (StringUtils.isNotBlank(departIds)) {
                        String userId = sysUserExcel.getId();
                        String[] departIdArray = departIds.split(",");
                        List<SysUserDepart> userDepartList = new ArrayList<>(departIdArray.length);
                        for (String departId : departIdArray) {
                            userDepartList.add(new SysUserDepart(userId, departId));
                        }
                        ISysUserDepartService service = SpringContextUtils.getBean(SysUserDepartServiceImpl.class);
                        service.saveBatch(userDepartList);
                    }

                }
            } catch (Exception e) {
                errorMessage.add("Đã xảy ra ngoại lệ:" + e.getMessage());
                log.error(e.getMessage(), e);
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }
}
