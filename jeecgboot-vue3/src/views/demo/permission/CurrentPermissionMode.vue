<template>
  <div class="mt-2">
    Chế độ quyền hạn hiện tại:
    <a-button type="link">
      {{ permissionMode === PermissionModeEnum.BACK ? 'Chế độ quyền backend' : 'Chế độ quyền vai trò frontend' }}
    </a-button>
    <a-button class="ml-4" @click="togglePermissionMode" type="primary"> Chuyển đổi chế độ quyền hạn </a-button>
    <Divider />
  </div>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { useAppStore } from '/@/store/modules/app';
  import { PermissionModeEnum } from '/@/enums/appEnum';
  import { Divider } from 'ant-design-vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  export default defineComponent({
    name: 'CurrentPermissionMode',
    components: { Divider },
    setup() {
      const appStore = useAppStore();
      const permissionMode = computed(() => appStore.getProjectConfig.permissionMode);
      const { togglePermissionMode } = usePermission();

      return {
        permissionMode,
        PermissionModeEnum,
        togglePermissionMode,
      };
    },
  });
</script>
