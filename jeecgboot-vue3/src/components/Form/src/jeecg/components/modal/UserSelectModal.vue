<!--Hộp chọn người dùng-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      :width="showSelected ? '1200px' : '900px'"
      :wrapClassName="modalWrapClassName"
      @ok="handleOk"
      @cancel="handleCancel"
      :maxHeight="maxHeight"
      :centered="true"
      destroyOnClose
      @visible-change="visibleChange"
      
    >
      <a-row>
        <a-col :span="showSelected ? 18 : 24">
          <BasicTable
            ref="tableRef"
            :columns="columns"
            :scroll="tableScroll"
            v-bind="getBindValue"
            :useSearchForm="true"
            :formConfig="formConfig"
            :api="hasCustomApi ? customListApi : getUserList"
            :searchInfo="searchInfo"
            :rowSelection="rowSelection"
            :indexColumnProps="indexColumnProps"
            :afterFetch="afterFetch"
            :beforeFetch="beforeFetch"
            :defSort="{ column: '', order: '' }"
          >
            <template #tableTitle></template>
          </BasicTable>
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
  import { defineComponent, unref, ref, watch, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getUserList } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import { isArray, cloneDeep } from 'lodash-es';
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
        default: 'Chọn người dùng',
      },
      //Tập hợp id người dùng bị loại trừ
      excludeUserIdList: {
        type: Array,
        default: [],
      },

      // wrapTên class
      modalWrapClassName: {
        type: String,
        default: 'j-user-select-modal',
      },
      // API tùy chỉnh dùng để truy vấn dữ liệu table
      customListApi: {type: Function},
      // Điều kiện truy vấn của API tùy chỉnh có sử dụng hay không JInput
      customApiJInput: {type: Boolean, default: true},
      // Điều kiện cấu hình biểu mẫu tùy chỉnh
      customFormConfig: {type: Object},
      // Cột bảng tùy chỉnh
      customTableColumns: {type: Array},
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit, refs }) {
      // Giải thích logic mã: VUEN-1086 [Mobile] Chọn người dùng, nút tìm kiếm hiệu quả không tốt, danh sách hiển thị không có thanh cuộn
      const tableScroll = ref<any>({ x: false });
      const tableRef = ref();
      const maxHeight = ref(600);

      const hasCustomApi = computed(() => typeof props.customListApi === 'function');

      //Đăng ký popup
      const [register, { closeModal }] = useModalInner(() => {
        if (window.innerWidth < 900) {
          tableScroll.value = { x: 900 };
        } else {
          tableScroll.value = { x: false };
        }
        // Giải thích logic mã: VUEN-1112 Một-nhiều: chọn người dùng chưa hiển thị số lượng đã chọn, và xóa
        setTimeout(() => {
          // update-begin--author:liaozhiyang---date:20260120--for：【issues/9275] Component người dùng khi nhấn hủy lần thứ hai thì giá trị đã tích chọn vẫn hiển thị lại
          let selectedData = selectValues['value'];
          if (isArray(selectedData)) {
            selectedData = cloneDeep(selectedData);
          }
          // update-end--author:liaozhiyang---date:20260120--for：【issues/9275] Component người dùng khi nhấn hủy lần thứ hai thì giá trị đã tích chọn vẫn hiển thị lại
          if (tableRef.value) {
            tableRef.value.setSelectedRowKeys(selectedData || []);
          }
        }, 800);
      });
      const attrs = useAttrs();
      //Cấu hình bảng
      const config = {
        canResize: false,
        bordered: true,
        size: 'small',
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), config);
      const [{ rowSelection, visibleChange, selectValues, indexColumnProps, getSelectResult, handleDeleteSelected, selectRows, checkedKeys }] = useSelectBiz(
        getUserList,
        getBindValue,
        emit
      );
      // [Sửa lỗi]params là cảnh snapshot một lần của props được đổi sang watch để theo dõi props.params đồng bộ,
      // Tránh việc sau khi component cha chuyển phòng ban, searchInfo vẫn trỏ tới tham chiếu cũ được bắt lần đầu
      const searchInfo = ref<Recordable>(props.params ?? {});
      watch(
        () => props.params,
        (newParams) => {
          searchInfo.value = newParams ?? {};
        },
        { deep: true, immediate: true }
      );
      // Giải thích logic mã: 【issues/657] xóa trong danh sách đã chọn bên phải không có hiệu lực
      watch(checkedKeys, (newVal) => {
        // Giải thích logic mã: nullLỗi con trỏ null khiến console báo lỗi, trang không hiển thị------------
        if(tableRef.value){
          tableRef.value.setSelectedRowKeys(newVal);
        }
      });
      //Truy vấnform
      const formConfig = {
        baseColProps: {
          xs: 24,
          sm: 8,
          md: 6,
          lg: 8,
          xl: 6,
          xxl: 6,
        },
        // Giải thích logic mã: VUEN-1086 [Mobile] Chọn người dùng, nút tìm kiếm hiệu quả không tốt, danh sách hiển thị không có thanh cuộn---Bố cục lưới của nút biểu mẫu truy vấn giữ nhất quán với biểu mẫu
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
            label: 'Tài khoản',
            field: 'username',
            component: (hasCustomApi.value && !props.customApiJInput) ? 'Input' : 'JInput',
          },
          {
            label: 'Họ tên',
            field: 'realname',
            component: (hasCustomApi.value && !props.customApiJInput) ? 'Input' : 'JInput',
          },
        ],
        autoSubmitOnEnter: true,

        ...props.customFormConfig,
      };
      //Định nghĩa cột bảng
      const columns = props.customTableColumns?.length ? props.customTableColumns : [
        {
          title: 'Tài khoản người dùng',
          dataIndex: 'username',
          width: 120,
          align: 'left',
        },
        {
          title: 'Họ tên người dùng',
          dataIndex: 'realname',
          width: 120,
        },
        {
          title: 'Giới tính',
          dataIndex: 'sex_dictText',
          width: 50,
        },
        {
          title: 'Số điện thoại',
          dataIndex: 'phone',
          width: 120,
          customRender:( { record, text })=>{
            if(record.izHideContact && record.izHideContact === '1'){
              return '/';
            }
            return text;
          }
        },
        {
          title: 'Email',
          dataIndex: 'email',
          customRender:( { record, text })=>{
            if(record.izHideContact && record.izHideContact === '1'){
              return text?'/':'';
            }
            return text;
          }
          // width: 40,
        },
        {
          title: 'Trạng thái',
          dataIndex: 'status_dictText',
          width: 80,
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
            title: 'Họ tên người dùng',
            dataIndex: 'realname',
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
      
      /**
       * Truy vấn logic kết quả trả về của người dùng
       */
      function afterFetch(record) {
        let excludeList = props.excludeUserIdList;
        if(!excludeList){
          return record;
        }
        let arr:any[] = [];
        //Nếu tồn tại tập hợp id người dùng cần lọc, và dữ liệu backend trả về không rỗng
        if(excludeList.length>0 && record && record.length>0){
          for(let item of record){
            if(excludeList.indexOf(item.id)<0){
              arr.push({...item})
            }
          }
          return arr;
        }
        return record;
      }
      // Giải thích logic mã: 【QQYUN-9366]Component chọn người dùng khi hủy và đóng sẽ mang dữ liệu đã chọn vào
      const handleCancel = () => {
        emit('close');
      };

      // Giải thích logic mã: 【TV360X-305] màn hình nhỏ hiển thị 10 dòng
      const clientHeight = document.documentElement.clientHeight * 200;
      maxHeight.value = clientHeight > 600 ? 600 : clientHeight;

      /**
       * Sắp xếp theo thời gian tạo trước khi gửi yêu cầu
       *
       * @param params
       */
      function beforeFetch(params) {
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      }

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        getUserList,
        formConfig,
        columns,
        rowSelection,
        selectRows,
        selectedTable,
        handleDeleteSelected,
        tableScroll,
        tableRef,
        afterFetch,
        handleCancel,
        maxHeight,
        beforeFetch,
        hasCustomApi,
      };
    },
  });
</script>
