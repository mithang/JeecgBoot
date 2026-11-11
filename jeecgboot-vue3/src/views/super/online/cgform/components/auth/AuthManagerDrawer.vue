<template>
  <BasicDrawer class="online-auth-manager-drawer" @register="registerDrawer" title="Quản lý quyền" :width="800" @close="onClose">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane tab="Quyền trường" key="field" forceRender>
        <AuthFieldConfig :headId="headId" v-model:authFields="authFields" />
      </a-tab-pane>
      <template v-if="hasDataAuth">
        <a-tab-pane tab="Quyền nút" key="button" forceRender>
          <AuthButtonConfig :headId="headId" :tableType="curTableType" :isTree="curIsTree" />
        </a-tab-pane>
        <a-tab-pane tab="Quyền dữ liệu" key="data" forceRender>
          <!-- Quyền dữ liệu không cần làm mới theo thời gian thực Vì vậy tại đây truyền biểu mẫu gốcID -->
          <AuthDataConfig :cgformId="cgformId" :authFields="authFields" :tableType="curTableType" />
        </a-tab-pane>
      </template>
    </a-tabs>
  </BasicDrawer>
</template>

<script lang="ts">
  import { computed, defineComponent, ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import AuthFieldConfig from './manager/AuthFieldConfig.vue';
  import AuthButtonConfig from './manager/AuthButtonConfig.vue';
  import AuthDataConfig from './manager/AuthDataConfig.vue';

  export default defineComponent({
    name: 'AuthManagerDrawer',
    components: {
      BasicDrawer,
      AuthFieldConfig,
      AuthButtonConfig,
      AuthDataConfig,
    },
    props: {
      // 1bảng đơn 2bảng chính 3Bảng phụ
      tableType: {
        type: Number,
        default: 1,
      },
    },
    emits: ['register'],
    setup(props) {
      const cgformId = ref('');
      const headId = ref('');
      const authFields = ref([]);
      const activeKey = ref('field');
      const curTableType = ref(1);
      const curIsTree = ref('N');
      const hasDataAuth = computed(() => props.tableType == 1 || props.tableType == 2);

      const [registerDrawer, { closeDrawer }] = useDrawerInner((data) => {
        cgformId.value = data.cgformId;
        headId.value = cgformId.value + '?' + new Date().getTime();
        activeKey.value = 'field';
        // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-187】Bỏ truy vấn nâng cao của quyền nút trong quản lý quyền bảng con
        curTableType.value = data.tableType;
        curIsTree.value = data.isTree || 'N';
        // update-end--author:liaozhiyang---date:20240520---for：【TV360X-187】Bỏ truy vấn nâng cao của quyền nút trong quản lý quyền bảng con
      });

      function onClose() {
        closeDrawer();
      }

      return {
        activeKey,
        cgformId,
        headId,
        authFields,
        hasDataAuth,
        onClose,
        registerDrawer,
        curTableType,
        curIsTree,
      };
    },
  });
</script>

<style lang="less">
// update-begin--author:liaozhiyang---date:20260811---for：【LHZP-311】Điều chỉnh quản lý quyền tới50mục (điều)/Khi ở trang sẽ xuất hiện hai thanh cuộn
.online-auth-manager-drawer {
  .is-vertical {
    display: none;
  }
}
// update-end--author:liaozhiyang---date:20260811---for：【LHZP-311】Điều chỉnh quản lý quyền tới50mục (điều)/Khi ở trang sẽ xuất hiện hai thanh cuộn
</style>
