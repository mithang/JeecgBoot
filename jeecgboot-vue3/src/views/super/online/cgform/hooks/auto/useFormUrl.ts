import { ref, unref } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '/@/store/modules/user';
import { message } from 'ant-design-vue';

export function useFormUrl() {
  const route = useRoute();
  const token = ref(route.query.token);
  const createToken = ref('');
  const userStore = useUserStore();
  if (unref(token)) {
    // Kiểm chứngtokenCó hợp pháp hay không
  } else {
    if (userStore.getToken) {
      // đã đăng nhập hệ thống, cótoken
      token.value = userStore.getToken;
    } else {
      // vừa chưa đăng nhập,urlcũng khôngtoken
      getToken();
    }
  }
  // Không cótoken, thông qua interface để lấytoken, rồi lưu bộ nhớ đệm cục bộ và thiết lập vàourlrender lại ở phía trên
  function getToken() {
    const hide = message.loading('Lấytokentrong...', 0);
    setTimeout(() => {
      createToken.value =
        'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTAwMTc2NzUsInVzZXJuYW1lIjoiYWRtaW4ifQ.0q5ywkLF144SaqumsSgEXO5ERtqaYp8jqouIUxxhELc';

      setToken();
      setUrlToken();
      hide();
    }, 3e3);
  }
  function setToken() {
    userStore.setToken(createToken.value);
    // userStore.setTenant(1);
  }
  function setUrlToken() {
    window.location.replace(`${route.fullPath}?token=${createToken.value}`);
  }
  return { token };
}
