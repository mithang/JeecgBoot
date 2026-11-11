<template>
  <PageWrapper
    title="Ví dụ nút quyền frontend"
    contentBackground
    contentClass="p-4"
    content="Vì khi làm mới sẽ gọi API thông tin người dùng, sẽ đặt lại thông tin vai trò theo API, nên sau khi làm mới giao diện sẽ khôi phục như ban đầu, nếu không cần có thể ghi chú src/layout/default/indexAPI lấy thông tin người dùng bên trong"
  >
    <CurrentPermissionMode />

    <p>
      Vai trò hiện tại: <a> {{ userStore.getRoleList }} </a>
    </p>
    <Alert class="mt-4" type="info" message="Sau khi nhấn, vui lòng xem sự thay đổi của nút" show-icon />

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
    <Divider>Xác định quyền hạn bằng component(Nếu cần, có thể tự đăng ký toàn cục)</Divider>
    <Authority :value="RoleEnum.SUPER">
      <a-button type="primary" class="mx-4"> Hiển thị khi có quyền vai trò super </a-button>
    </Authority>

    <Authority :value="RoleEnum.TEST">
      <a-button color="success" class="mx-4"> Hiển thị khi có quyền vai trò test </a-button>
    </Authority>

    <Authority :value="[RoleEnum.TEST, RoleEnum.SUPER]">
      <a-button color="error" class="mx-4"> Có[test,super]Hiển thị khi có quyền vai trò </a-button>
    </Authority>

    <Divider>Kiểm tra quyền hạn bằng phương thức hàm(Áp dụng cho việc lọc bên trong hàm)</Divider>
    <a-button v-if="hasPermission(RoleEnum.SUPER)" type="primary" class="mx-4"> Hiển thị khi có quyền vai trò super </a-button>

    <a-button v-if="hasPermission(RoleEnum.TEST)" color="success" class="mx-4"> Hiển thị khi có quyền vai trò test </a-button>

    <a-button v-if="hasPermission([RoleEnum.TEST, RoleEnum.SUPER])" color="error" class="mx-4"> Có[test,super]Hiển thị khi có quyền vai trò </a-button>

    <Divider>Kiểm tra quyền hạn bằng phương thức chỉ thị(Cách này không thể sửa quyền hạn một cách động.)</Divider>
    <a-button v-auth="RoleEnum.SUPER" type="primary" class="mx-4"> Hiển thị khi có quyền vai trò super </a-button>

    <a-button v-auth="RoleEnum.TEST" color="success" class="mx-4"> Hiển thị khi có quyền vai trò test </a-button>

    <a-button v-auth="[RoleEnum.TEST, RoleEnum.SUPER]" color="error" class="mx-4"> Có[test,super]Hiển thị khi có quyền vai trò </a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Alert, Divider } from 'ant-design-vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { useUserStore } from '/@/store/modules/user';
  import { RoleEnum } from '/@/enums/roleEnum';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { Authority } from '/@/components/Authority';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { Alert, PageWrapper, CurrentPermissionMode, Divider, Authority },
    setup() {
      const { changeRole, hasPermission } = usePermission();
      const userStore = useUserStore();

      return {
        userStore,
        RoleEnum,
        isSuper: computed(() => userStore.getRoleList.includes(RoleEnum.SUPER)),
        isTest: computed(() => userStore.getRoleList.includes(RoleEnum.TEST)),
        changeRole,
        hasPermission,
      };
    },
  });
</script>
<style lang="less" scoped>
  .demo {
    background-color: @component-background;
  }
</style>
