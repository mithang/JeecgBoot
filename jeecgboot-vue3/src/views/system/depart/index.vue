<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" type="flex" :gutter="10">
    <a-col :xl="10" :lg="24" :md="24" style="margin-bottom: 10px">
      <DepartLeftTree ref="leftTree" @select="onTreeSelect" @rootTreeData="onRootTreeData" @dragSuccess="onDepartDragSuccess" />
    </a-col>
    <a-col :xl="14" :lg="24" :md="24" style="margin-bottom: 10px">
      <div style="height: 100%;" :class="[`${prefixCls}`]">
        <a-tabs v-show="departData != null" defaultActiveKey="base-info">
          <a-tab-pane tab="Thông tin cơ bản" key="base-info" forceRender style="position: relative">
            <div style="padding: 20px">
              <DepartFormTab :data="departData" :rootTreeData="rootTreeData" @success="onSuccess" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Quyền phòng ban" key="role-info">
            <div style="padding: 0 20px 20px">
              <DepartRuleTab :data="departData" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Quan hệ báo cáo cấp bậc" key="rank">
            <div style="padding: 0 20px 20px">
              <DepartRankRelation :data="departData" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Danh sách người dùng" key="user">
            <div style="padding: 0 20px 20px">
              <DepartUserList :data="departData" :key="reRender"></DepartUserList>
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Người đứng đầu phòng ban" key="departmentHead">
            <DepartmentHeadList :data="departData"></DepartmentHeadList>
          </a-tab-pane>
        </a-tabs>
        <div v-show="departData == null" style="padding-top: 40px">
          <a-empty description="Chưa chọn phòng ban" />
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup name="system-depart">
  import { provide, ref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DepartLeftTree from './components/DepartLeftTree.vue';
  import DepartFormTab from './components/DepartFormTab.vue';
  import DepartRuleTab from './components/DepartRuleTab.vue';
  import DepartRankRelation from './components/DepartRankRelation.vue';
  import DepartUserList from './components/DepartUserList.vue';
  import DepartmentHeadList from './components/DepartmentHeadList.vue';
  import { clearDepartPathNameCache } from '@/utils/common/compUtils';

  const { prefixCls } = useDesign('depart-manage');
  provide('prefixCls', prefixCls);

  // Định nghĩa một biến ref cho component con
  const leftTree = ref();

  // Thông tin phòng ban đang chọn
  const departData = ref({});
  const rootTreeData = ref<any[]>([]);
  const reRender = ref(-1);

  // Kích hoạt sau khi chọn cây bên trái
  function onTreeSelect(data) {
    console.log('onTreeSelect: ', data);
    if (reRender.value == -1) {
      // Kết xuất lại component
      reRender.value = Math.random();
    }
    departData.value = data;
  }

  // Kích hoạt rootTreeData của cây bên trái
  function onRootTreeData(data) {
    rootTreeData.value = data;
  }

  function onSuccess() {
    clearDepartPathNameCache();
    reRender.value = Math.random();
    leftTree.value.loadRootTreeData();
  }

  /**
   * Sau khi điều chỉnh cấp bậc phòng ban, làm mới thông tin vị trí trong danh sách người dùng
   */
  function onDepartDragSuccess() {
    clearDepartPathNameCache();
    reRender.value = Math.random();
  }
</script>

<style lang="less">
  @import './index.less';
</style>
