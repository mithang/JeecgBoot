// Lớp lõi đăng nhập một lần
import { getToken } from '/@/utils/auth';
import { getUrlParam } from '/@/utils';
import { useGlobSetting } from '/@/hooks/setting';
import { validateCasLogin } from '/@/api/sys/user';
import { useUserStore } from '/@/store/modules/user';
const globSetting = useGlobSetting();
const openSso = globSetting.openSso;

/**
 * Lấy CAS Địa chỉ callback đăng nhập, giữ đường dẫn trang hiện tại và xóa CAS được thêm vào ticket Tham số
 * for: 【issues/9797]BậtCASKiểm tra, địa chỉ quay lại sai
 */
export function getCasServiceUrl() {
  const serviceUrl = new URL(window.location.href);
  serviceUrl.searchParams.delete('ticket');
  return serviceUrl.href;
}

export function useSso() {
  // Giải thích logic mã: 【QQYUN-7805】SSOĐăng nhập bắt buộc dùnghttp #957---
  let locationUrl = document.location.protocol +"//" + window.location.host + '/';

  /**
   * Đăng nhập một lần
   */
  async function ssoLogin() {
    if (openSso == 'true') {
      let token = getToken();
      let ticket = getUrlParam('ticket');
      if (!token) {
        const loginServiceUrl = getCasServiceUrl();
        if (ticket) {
          await validateCasLogin({
            ticket: ticket,
            service: loginServiceUrl,
          }).then((res) => {
            const userStore = useUserStore();
            userStore.setToken(res.token);
            return userStore.afterLoginAction(true, {});
          });
        } else {
          window.location.href = globSetting.casBaseUrl + '/login?service=' + encodeURIComponent(loginServiceUrl);
        }
      }
    }
  }

  /**
   * Đăng xuất
   */
  async function ssoLoginOut() {
    window.location.href = globSetting.casBaseUrl + '/logout?service=' + encodeURIComponent(locationUrl);
  }
  return { ssoLogin, ssoLoginOut };
}
