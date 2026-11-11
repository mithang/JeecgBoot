<!--Hộp chọn vai trò-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="800px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <BasicTable
        :columns="columns"
        v-bind="config"
        :useSearchForm="true"
        :formConfig="formConfig"
        :api="getRoleList"
        :searchInfo="searchInfo"
        :rowSelection="rowSelection"
        :indexColumnProps="indexColumnProps"
      ></BasicTable>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getRoleList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import { useAttrs } from '/@/hooks/core/useAttrs';

  export default defineComponent({
    name: 'UserSelectModal',
    components: {
      //Tại đây cần tải bất đồng bộBasicTable
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      ...selectProps,
      //Tiêu đề hộp chọn
      modalTitle: {
        type: String,
        default: 'Chọn vai trò',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      //Đăng ký popup
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      //Cấu hình bảng
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
        rowKey: unref(props).rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, indexColumnProps, visibleChange, getSelectResult }] = useSelectBiz(getRoleList, getBindValue);
      const searchInfo = ref(props.params);
      //Truy vấnform
      const formConfig = {
        //labelWidth: 220,
        baseColProps: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 14,
          xl: 14,
          xxl: 14,
        },
        // Giải thích logic mã: [issues/5514]Trang component hiển thị lệch vị trí
        actionColOptions: {
            xs: 24,
            sm: 8,
            md: 8,
            lg: 8,
            xl: 8,
            xxl: 8,
        },
        schemas: [
          {
            label: 'Tên vai trò',
            field: 'roleName',
            component: 'Input',
          },
        ],
        autoSubmitOnEnter: true,
      };
      //Định nghĩa cột bảng
      const columns = [
        {
          title: 'Tên vai trò',
          dataIndex: 'roleName',
          width: 240,
          align: 'left',
        },
        {
          title: 'Mã vai trò',
          dataIndex: 'roleCode',
          // width: 40,
        },
      ];

      /**
       * Xác nhận lựa chọn
       */
      function handleOk() {
        getSelectResult((options, values) => {
          //Trả về tùy chọn và giá trị đã chọn
          emit('getSelectResult', options, values);
          //Đóng popup
          closeModal();
        });
      }

      return {
        config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getRoleList,
        formConfig,
        getBindValue,
        columns,
        rowSelection,
      };
    },
  });
</script>
