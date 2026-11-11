<template>
  <PageWrapper contentBackground title="Kiểm soát quyền nút" contentClass="p-4">
    <CurrentPermissionMode />
    <p>
      Danh sách code hiện đang có: <a> {{ permissionStore.getPermCodeList }} </a>
    </p>
    <Divider />
    <Alert class="mt-4" type="info" message="Sau khi nhấn, vui lòng xem sự thay đổi của nút(Phải ở chế độ quyền hậu trường mới có thể kiểm tra chức năng mà trang này hiển thị)" show-icon />
    <Divider />
    <a-button type="primary" class="mr-2" @click="switchToken(2)" :disabled="!isBackPremissionMode"> Nhấn để chuyển quyền hạn nút(id người dùng là2) </a-button>
    <a-button type="primary" @click="switchToken(1)" :disabled="!isBackPremissionMode"> Nhấn để chuyển quyền hạn nút(id người dùng là1,Mặc định) </a-button>

    <template v-if="isBackPremissionMode">
      <Divider>Xác định quyền hạn bằng component</Divider>
      <Authority :value="'1000'">
        <a-button type="primary" class="mx-4"> Cócode ['1000']Hiển thị theo quyền hạn </a-button>
      </Authority>

      <Authority :value="'2000'">
        <a-button color="success" class="mx-4"> Cócode ['2000']Hiển thị theo quyền hạn </a-button>
      </Authority>

      <Authority :value="['1000', '2000']">
        <a-button color="error" class="mx-4"> Cócode ['1000','2000']Hiển thị khi có quyền vai trò </a-button>
      </Authority>

      <Divider>Kiểm tra quyền hạn bằng phương thức hàm</Divider>
      <a-button v-if="hasPermission('1000')" type="primary" class="mx-4"> Cócode ['1000']Hiển thị theo quyền hạn </a-button>

      <a-button v-if="hasPermission('2000')" color="success" class="mx-4"> Cócode ['2000']Hiển thị theo quyền hạn </a-button>

      <a-button v-if="hasPermission(['1000', '2000'])" color="error" class="mx-4"> Cócode ['1000','2000']Hiển thị khi có quyền vai trò </a-button>

      <Divider>Kiểm tra quyền hạn bằng phương thức chỉ thị(Cách này không thể sửa quyền hạn một cách động.)</Divider>
      <a-button v-auth="'1000'" type="primary" class="mx-4"> Cócode ['1000']Hiển thị theo quyền hạn </a-button>

      <a-button v-auth="'2000'" color="success" class="mx-4"> Cócode ['2000']Hiển thị theo quyền hạn </a-button>

      <a-button v-auth="['1000', '2000']" color="error" class="mx-4"> Cócode ['1000','2000']Hiển thị khi có quyền vai trò </a-button>
    </template>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { Alert, Divider } from 'ant-design-vue';
  import CurrentPermissionMode from '../CurrentPermissionMode.vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { Authority } from '/@/components/Authority';
  import { usePermissionStore } from '/@/store/modules/permission';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { PageWrapper } from '/@/components/Page';
  import { useAppStore } from '/@/store/modules/app';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    components: { Alert, PageWrapper, CurrentPermissionMode, Divider, Authority },
    setup() {
      const { hasPermission } = usePermission();
      const permissionStore = usePermissionStore();
      const appStore = useAppStore();
      const userStore = useUserStore();

      const isBackPremissionMode = computed(() => appStore.getProjectConfig.permissionMode === PermissionModeEnum.BACK);

      async function switchToken(userId: number) {
        // Phần chuyển Token đăng nhập người dùng trong hàm này chỉ dùng để demo, khi vận hành thực tế, chuyển đổi danh tính phải đăng nhập lại
        const token = 'fakeToken' + userId;
        userStore.setToken(token);

        // Lấy lại thông tin người dùng và menu
        userStore.getUserInfoAction();
        permissionStore.changePermissionCode();
      }

      return {
        hasPermission,
        permissionStore,
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
