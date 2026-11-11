<template>
  <PageWrapper
    title="Ví dụ quyền backend"
    contentBackground
    contentClass="p-4"
    content="Hiện tại đã mock hai bộ dữ liệu, id là 1 và 2, menu cụ thể trả về có thể xem tạimock/sys/menu.tsxem trong"
  >
    <CurrentPermissionMode />

    <Alert class="mt-4" type="info" message="Sau khi nhấn, vui lòng xem sự thay đổi của menu bên trái" show-icon />

    <div class="mt-4">
      Chuyển đổi quyền hạn(Vui lòng chuyển chế độ quyền sang chế độ quyền backend trước):
      <a-button-group>
        <a-button @click="switchToken(1)" :disabled="!isBackPremissionMode"> Lấy menu của người dùng có id là 1 </a-button>
        <a-button @click="switchToken(2)" :disabled="!isBackPremissionMode"> Lấy menu của người dùng có id là 2 </a-button>
      </a-button-group>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useUserStore } from '/@/store/modules/user';
  import { PageWrapper } from '/@/components/Page';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { useAppStore } from '/@/store/modules/app';
  import { Alert } from 'ant-design-vue';
  export default defineComponent({
    components: { Alert, CurrentPermissionMode, PageWrapper },
    setup() {
      const { refreshMenu } = usePermission();
      const userStore = useUserStore();
      const appStore = useAppStore();

      const isBackPremissionMode = computed(() => appStore.getProjectConfig.permissionMode === PermissionModeEnum.BACK);

      async function switchToken(userId: number) {
        // Phần chuyển Token đăng nhập người dùng trong hàm này chỉ dùng để demo, khi vận hành thực tế, chuyển đổi danh tính phải đăng nhập lại
        const token = 'fakeToken' + userId;
        userStore.setToken(token);

        // Lấy lại thông tin người dùng và menu
        userStore.getUserInfoAction();
        refreshMenu();
      }

      return {
        RoleEnum,
        refreshMenu,
        switchToken,
        isBackPremissionMode,
      };
    },
  });
</script>
<style lang="less" scoped>
  .demo {
    background-color: @component-background;
  }
</style>
