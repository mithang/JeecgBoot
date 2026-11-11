<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="70%" @fullScreen="handleFullScreen">
    <a-form ref="formRef" :model="orderMainModel" :label-col="labelCol" :wrapper-col="wrapperCol" :rules="validatorRules">
      <div style="overflow-x: hidden">
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8">
            <a-form-item label="Số đơn hàng" name="orderCode">
              <a-input v-model:value="orderMainModel.orderCode" placeholder="Vui lòng nhập mã đơn hàng" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Loại đơn hàng" name="ctype">
              <a-select placeholder="Vui lòng chọn loại đơn hàng" v-model:value="orderMainModel.ctype">
                <a-select-option value="1">Đơn hàng nội địa</a-select-option>
                <a-select-option value="2">Đơn hàng quốc tế</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Ngày đặt hàng" name="orderDate">
              <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.orderDate" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8">
            <a-form-item label="Số tiền đơn hàng" name="orderMoney">
              <a-input v-model:value="orderMainModel.orderMoney" placeholder="Vui lòng nhập số tiền đơn hàng" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Ghi chú đơn hàng" name="content">
              <a-input v-model:value="orderMainModel.content" placeholder="Vui lòng nhập ghi chú đơn hàng" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>
      <!-- Khu vực biểu mẫu con -->
      <a-tabs v-model:activeKey="activeKey" @change="handleChangeTabs">
        <a-tab-pane tab="Thông tin khách hàng" key="tableRef1">
          <JVxeTable
            ref="tableRef1"
            stripe
            toolbar
            rowNumber
            rowSelection
            resizable
            keepSource
            :height="tableH"
            :checkbox-config="{ range: true }"
            :loading="table1.loading"
            :columns="table1.columns"
            :dataSource="table1.dataSource"
          ></JVxeTable>
        </a-tab-pane>

        <a-tab-pane tab="Thông tin vé máy bay" key="tableRef2" forceRender>
          <JVxeTable
            ref="tableRef2"
            stripe
            toolbar
            rowNumber
            rowSelection
            resizable
            keepSource
            :height="tableH"
            :checkbox-config="{ range: true }"
            :loading="table2.loading"
            :columns="table2.columns"
            :dataSource="table2.dataSource"
          ></JVxeTable>
        </a-tab-pane>
      </a-tabs>
    </a-form>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { columns, columns1 } from './jvxetable.data';
  import { orderCustomerList, orderTicketList, saveOrUpdate } from './jvxetable.api';
  import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts';
  export default defineComponent({
    name: 'JVexTableModal',
    components: { BasicModal },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const tableH = ref(300);
      const isUpdate = ref(true);
      const tableRef1 = ref();
      const tableRef2 = ref();
      const refKeys = ref(['tableRef1', 'tableRef2']);
      const activeKey = ref('tableRef1');
      const tableRefs = { tableRef1, tableRef2 };
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 5 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 16 },
      });
      // Thông tin khách hàng
      const table1 = reactive({
        loading: false,
        dataSource: [],
        columns,
      });
      // Thông tin vé máy bay
      const table2 = reactive({
        loading: false,
        dataSource: [],
        columns: columns1,
      });
      const orderMainModel = reactive({
        id: null,
        orderCode: '',
        orderMoney: '',
        ctype: '',
        content: '',
        jeecgOrderCustomerList: [],
        jeecgOrderTicketList: [],
      });
      const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(
        requestAddOrEdit,
        classifyIntoFormData,
        tableRefs,
        activeKey,
        refKeys
      );
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        reset();
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          Object.assign(orderMainModel, data.record);
          //Tải dữ liệu bảng con
          let params = { id: orderMainModel.id };
          requestSubTableData(orderCustomerList, params, table1);
          requestSubTableData(orderTicketList, params, table2);
        }
      });

      const validatorRules = { orderCode: [{ required: true, message: 'Mã đơn hàng không được để trống', trigger: 'blur' }] };
      const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

      function classifyIntoFormData(allValues) {
        let orderMain = Object.assign(orderMainModel, allValues.formValue);
        return {
          ...orderMain, // Mở rộng
          jeecgOrderCustomerList: allValues.tablesValue[0].tableData,
          jeecgOrderTicketList: allValues.tablesValue[1].tableData,
        };
      }
      function reset() {
        orderMainModel.id = null;
        orderMainModel.orderCode = '';
        orderMainModel.orderMoney = '';
        orderMainModel.orderDate = null;
        orderMainModel.ctype = '';
        orderMainModel.content = '';
        orderMainModel.jeecgOrderCustomerList = [];
        orderMainModel.jeecgOrderTicketList = [];
        table1.dataSource = [];
        table2.dataSource = [];
      }
      async function requestAddOrEdit(values) {
        setModalProps({ confirmLoading: true });
        //Gửi biểu mẫu
        await saveOrUpdate(values, unref(isUpdate));
        //Đóng popup
        closeModal();
        //Làm mới danh sách
        emit('success');
      }
      // Giải thích logic mã: 【QQYUN-5866] phóng to số dòng tự thích ứng
      const handleFullScreen = (val) => {
        tableH.value=val ? document.documentElement.clientHeight - 387 :  300;
      };
      return {
        formRef,
        activeKey,
        table1,
        table2,
        tableRef1,
        tableRef2,
        getTitle,
        labelCol,
        wrapperCol,
        validatorRules,
        orderMainModel,
        registerModal,
        handleChangeTabs,
        handleSubmit,
        handleFullScreen,
        tableH,
      };
    },
  });
</script>
<style scoped></style>
