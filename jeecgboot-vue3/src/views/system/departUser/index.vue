<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" :gutter="10">
    <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1">
      <a-card :bordered="false" style="height: 100%">
        <DepartTree @select="onTreeSelect" />
      </a-card>
    </a-col>
    <a-col :xl="18" :lg="16" :md="14" :sm="24" style="flex: 1">
      <a-card :bordered="false" style="height: 100%">
        <a-tabs defaultActiveKey="user-info">
          <a-tab-pane tab="Thông tin cơ bản" key="base-info" forceRender>
            <DepartBaseInfoTab :data="departData" />
          </a-tab-pane>
          <a-tab-pane tab="Thông tin người dùng" key="user-info">
            <DepartUserInfoTab :key="reRender" :data="departData" />
          </a-tab-pane>
          <a-tab-pane tab="Vai trò phòng ban" key="role-info">
            <DepartRoleInfoTab :key="reRender" :data="departData" />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup name="system-depart-user">
  import { provide, ref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  import DepartTree from './components/DepartTree.vue';
  import DepartBaseInfoTab from './components/DepartBaseInfoTab.vue';
  import DepartUserInfoTab from './components/DepartUserInfoTab.vue';
  import DepartRoleInfoTab from './components/DepartRoleInfoTab.vue';

  const { prefixCls } = useDesign('depart-user');
  provide('prefixCls', prefixCls);

  // Thông tin phòng ban đang chọn
  let departData = ref({});

  const reRender = ref(-1);

  // Kích hoạt sau khi chọn cây bên trái
  function onTreeSelect(data) {
    // Giải thích logic mã: 【issues/7658】Khi Phòng ban của tôi không có dữ liệu danh sách phòng ban, nhấn tìm kiếm hoặc đặt lại vẫn truy vấn ra dữ liệu
    if (reRender.value == -1) {
      // Kết xuất lại component
      reRender.value = Math.random();
    }
    departData.value = data;
  }
</script>

<style lang="less">
  @import './index.less';
</style>
