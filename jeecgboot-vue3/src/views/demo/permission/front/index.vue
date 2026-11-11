<template>
  <PageWrapper
    title="Ví dụ quyền frontend"
    contentBackground
    contentClass="p-4"
    content="Vì khi làm mới sẽ gọi API thông tin người dùng, sẽ đặt lại thông tin vai trò theo API, nên sau khi làm mới giao diện sẽ khôi phục như ban đầu, nếu không cần có thể ghi chú src/layout/default/indexAPI lấy thông tin người dùng bên trong"
  >
    <CurrentPermissionMode />

    <p>
      Vai trò hiện tại: <a> {{ userStore.getRoleList }} </a>
    </p>
    <Alert class="mt-4" type="info" message="Sau khi nhấn, vui lòng xem sự thay đổi của menu bên trái" show-icon />

    <div class="mt-4">
      Chuyển đổi quyền hạn(Vui lòng chuyển chế độ quyền sang chế độ quyền vai trò frontend trước):
      <a-button-group>
        <a-button @click="changeRole(RoleEnum.SUPER)" :type="isSuper ? 'primary' : 'default'">
          {{ RoleEnum.SUPER }}
        </a-button>
        <a-button @click="changeRole(RoleEnum.TEST)" :type="isTest ? 'primary' : 'default'">
          {{ RoleEnum.TEST }}
        </a-button>
      </a-button-group>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Alert } from 'ant-design-vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { useUserStore } from '/@/store/modules/user';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { Alert, CurrentPermissionMode, PageWrapper },
    setup() {
      const { changeRole } = usePermission();
      const userStore = useUserStore();

      return {
        userStore,
        RoleEnum,
        isSuper: computed(() => userStore.getRoleList.includes(RoleEnum.SUPER)),
        isTest: computed(() => userStore.getRoleList.includes(RoleEnum.TEST)),
        changeRole,
      };
    },
  });
</script>
<style lang="less" scoped>
  .demo {
    background-color: @component-background;
  }
</style>
