package org.jeecg.common.constant;

/**
 * @Description: TenantConstant
 * @author: scott
 * @date: 2022 năm 08 tháng 29 ngày 15:29
 */
public interface TenantConstant {
    /*------【Tham số ứng dụng low-code】----------------------------------------------*/
    /**
     * header Định danh lowAppId của
     */
    String X_LOW_APP_ID = "X-Low-App-ID";
    /**
     * ID ứng dụng — Trường thực thể
     */
    String FIELD_LOW_APP_ID = "lowAppId";
    /**
     * ID ứng dụng — Trường bảng
     */
    String DB_FIELD_LOW_APP_ID = "low_app_id";
    /*------【Tham số ứng dụng low-code】---------------------------------------------*/

    /*--------【Tham số tenant】-----------------------------------------------*/
    /**
     * ID tenant (tên trường thực thể và tên tham số URL)
     */
    String TENANT_ID = "tenantId";
    /**
     * Tên trường cơ sở dữ liệu của ID tenant
     */
    String TENANT_ID_TABLE = "tenant_id";
    /*-------【Tham số tenant】-----------------------------------------------*/

    /**
     * Siêu quản trị viên
     */
    String SUPER_ADMIN = "superAdmin";

    /**
     * Quản trị viên tài khoản của tổ chức
     */
    String ACCOUNT_ADMIN = "accountAdmin";

    /**
     * Quản trị viên ứng dụng của tổ chức
     */
    String APP_ADMIN = "appAdmin";

    /**
     * Của yêu cầu POST có thêm annotation SignatureCheck URL
     */
    String[] SIGNATURE_CHECK_POST_URL = { "/sys/tenant/joinTenantByHouseNumber", "/sys/tenant/invitationUser" };
}
