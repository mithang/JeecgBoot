<template>
  <div :class="prefixCls">
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button @click="onShowCustomButton" type="primary" preIcon="ant-design:highlight">Nút tùy chỉnh</a-button>
        <a-button @click="onShowEnhanceJs" type="primary" preIcon="ant-design:strikethrough">JSTăng cường</a-button>
        <a-button @click="onShowEnhanceSql" type="primary" preIcon="ant-design:filter">SQLTăng cường</a-button>
        <a-button @click="onShowEnhanceJava" type="primary" preIcon="ant-design:tool">JavaTăng cường</a-button>
      </template>

      <template #dbSync="{ text }">
        <span v-if="text === 'Y'" style="color: limegreen">Đã đồng bộ</span>
        <span v-if="text === 'N'" style="color: red">Chưa đồng bộ</span>
      </template>

      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
  </div>
  <CgformModal @register="registerCgformModal" :actionButton="false" @success="reload" />
  <EnhanceJsModal @register="registerEnhanceJsModal" />
  <EnhanceJavaModal @register="registerEnhanceJavaModal" />
  <EnhanceSqlModal @register="registerEnhanceSqlModal" />
  <DbToOnlineModal @register="registerDbToOnlineModal" @success="reload" />
  <CustomButtonList @register="registerCustomButtonModal" />
  <AuthManagerDrawer @register="registerAuthManagerDrawer" />
  <AuthSetterModal @register="registerAuthSetterModal" />
  <CgformAddressModal @register="registerAddressModal" />
</template>

<script lang="ts">
  import { watch, provide, defineComponent } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import CgformModal from './components/CgformModal.vue';
  import DbToOnlineModal from './components/DbToOnlineModal.vue';
  import CustomButtonList from './components/button/CustomButtonList.vue';
  import EnhanceJsModal from './components/enhance/EnhanceJsModal.vue';
  import EnhanceJavaModal from './components/enhance/EnhanceJavaModal.vue';
  import EnhanceSqlModal from './components/enhance/EnhanceSqlModal.vue';
  import AuthManagerDrawer from './components/auth/AuthManagerDrawer.vue';
  import AuthSetterModal from './components/auth/AuthSetterModal.vue';
  import CgformAddressModal from "./components/CgformAddressModal.vue";
  import { useCgformList } from './hooks/useCgformList';
  import { CgformPageType } from './types';

  // noinspection JSUnusedGlobalSymbols
  export default defineComponent({
    name: 'CgformCopyList',
    components: {
      BasicTable,
      TableAction,
      CgformModal,
      DbToOnlineModal,
      CustomButtonList,
      EnhanceJsModal,
      EnhanceJavaModal,
      EnhanceSqlModal,
      AuthManagerDrawer,
      AuthSetterModal,
      CgformAddressModal,
    },
    setup() {
      const pageType = CgformPageType.copy;
      provide('cgformPageType', pageType);
      const {
        router,
        pageContext,
        getTableAction,
        getDropDownAction,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerCgformModal,
        registerDbToOnlineModal,
        registerAddressModal,
      } = useCgformList({
        pageType,
        designScope: 'online-cgform-list',
        columns: [
          { title: 'Tên bảng view', dataIndex: 'tableName' },
          { title: 'Mô tả bảng view', dataIndex: 'tableTxt' },
          { title: 'Phiên bản bảng gốc', dataIndex: 'copyVersion' },
          { title: 'Phiên bản view', dataIndex: 'tableVersion' },
        ],
        formSchemas: [{ label: 'Tên bảng', field: 'tableName', component: 'JInput' }],
      });
      const { prefixCls, tableContext } = pageContext;
      const [registerTable, { reload }, { rowSelection }] = tableContext;
      watch(router.currentRoute, () => reload());
      return {
        prefixCls,
        reload,
        rowSelection,
        getTableAction,
        getDropDownAction,
        onShowCustomButton,
        onShowEnhanceJs,
        onShowEnhanceSql,
        onShowEnhanceJava,
        registerCustomButtonModal,
        registerEnhanceJsModal,
        registerEnhanceSqlModal,
        registerEnhanceJavaModal,
        registerAuthManagerDrawer,
        registerAuthSetterModal,
        registerTable,
        registerCgformModal,
        registerDbToOnlineModal,
        registerAddressModal,
      };
    },
  });
</script>

<style scoped></style>
