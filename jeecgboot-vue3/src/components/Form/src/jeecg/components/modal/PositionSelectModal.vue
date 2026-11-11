<!--Hộp chọn chức danh-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      width="1100px"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      destroyOnClose
      @visible-change="visibleChange"
    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            :columns="columns"
            :bordered="true"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="getPositionList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            v-bind="getBindValue"
          ></BasicTable>
        </a-col>
        <a-col :span="showSelected ? 6 : 0">
          <BasicTable
            v-bind="selectedTable"
            :dataSource="selectRows"
            :useSearchForm="true"
            :formConfig="{ showActionButtonGroup: false, baseRowStyle: { minHeight: '40px' } }"
          >
            <!--Thanh thao tác-->
            <template #action="{ record }">
              <a href="javascript:void(0)" @click="handleDeleteSelected(record)"><Icon icon="ant-design:delete-outlined"></Icon></a>
            </template>
          </BasicTable>
        </a-col>
      </a-row>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getPositionList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';

  export default defineComponent({
    name: 'PositionSelectModal',
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
        default: 'Chọn chức danh',
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
        //Đổi thành đọcrowKey,Tùy chỉnh tham số truyền vào
        rowKey: props.rowKey,
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows }] = useSelectBiz(
        getPositionList,
        getBindValue
      );
      const searchInfo = ref(props.params);
      //Truy vấnform
      const formConfig = {
        labelCol: {
          span: 4,
        },
        baseColProps: {
          xs: 24,
          sm: 10,
          md: 10,
          lg: 10,
          xl: 10,
          xxl: 10,
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
            label: 'Tên chức danh',
            field: 'name',
            component: 'JInput',
            colProps: { span: 10 },
          },
        ],
      };
      //Định nghĩa cột bảng
      const columns = [
        {
          title: 'Tên chức danh',
          dataIndex: 'name',
          // width: 180,
        },
        {
          title: 'Cấp bậc chức danh',
          dataIndex: 'postLevel',
        },
      ];
      //Thông tin table đã chọn
      const selectedTable = {
        pagination: false,
        showIndexColumn: false,
        scroll: { y: 390 },
        size: 'small',
        canResize: false,
        bordered: true,
        rowKey: 'id',
        columns: [
          {
            title: 'Tên chức danh',
            dataIndex: 'name',
            width: 40,
          },
          {
            title: 'Thao tác',
            dataIndex: 'action',
            align: 'center',
            width: 40,
            slots: { customRender: 'action' },
          },
        ],
      };
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
        handleOk,
        getPositionList,
        register,
        visibleChange,
        getBindValue,
        formConfig,
        indexColumnProps,
        columns,
        rowSelection,

        selectedTable,
        selectRows,
        handleDeleteSelected,
        searchInfo,
      };
    },
  });
</script>
