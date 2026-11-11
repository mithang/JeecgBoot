<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="70%">
    <a-form ref="formRef" :model="orderMainModel" :label-col="labelCol" :wrapper-col="wrapperCol" :rules="validatorRules">
      <a-row class="form-row" :gutter="16">
        <a-col :lg="8">
          <a-form-item label="Số đơn hàng" name="orderCode">
            <a-input v-model:value="orderMainModel.orderCode" placeholder="Vui lòng nhập mã đơn hàng" />
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="Loại đơn hàng">
            <a-select placeholder="Vui lòng chọn loại đơn hàng" v-model:value="orderMainModel.ctype">
              <a-select-option value="1">Đơn hàng nội địa</a-select-option>
              <a-select-option value="2">Đơn hàng quốc tế</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="Ngày đặt hàng">
            <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.orderDate" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row class="form-row" :gutter="16">
        <a-col :lg="8">
          <a-form-item label="Số tiền đơn hàng">
            <a-input v-model:value="orderMainModel.orderMoney" placeholder="Vui lòng nhập số tiền đơn hàng" />
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="Ghi chú đơn hàng">
            <a-input v-model:value="orderMainModel.content" placeholder="Vui lòng nhập ghi chú đơn hàng" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-tabs defaultActiveKey="1">
        <a-tab-pane tab="Thông tin khách hàng" key="1">
          <vxe-toolbar>
            <template #buttons>
              <vxe-button icon="fa fa-plus" @click="insertEvent()">Thêm mới</vxe-button>
              <vxe-button @click="$refs.xTable.removeCheckboxRow()">Xóa mục đã chọn</vxe-button>
            </template>
          </vxe-toolbar>

          <vxe-table
            border
            show-overflow
            keep-source
            ref="xTable"
            max-height="400"
            :data="orderMainModel.jeecgOrderCustomerList"
            :edit-config="{ trigger: 'click', mode: 'row', icon: 'fa fa-pencil', showStatus: true }"
          >
            <vxe-column type="checkbox" width="60" align="center"></vxe-column>
            <vxe-column type="seq" width="60" align="center"></vxe-column>
            <vxe-column field="name" title="Tên khách hàng" sortable :edit-render="{ name: 'input', defaultValue: '' }"></vxe-column>
            <vxe-column field="sex" title="Giới tính" :edit-render="{ name: '$select', options: sexList }"></vxe-column>
            <vxe-column field="idcard" title="Chứng minh nhân dân" sortable :edit-render="{ name: 'input', defaultValue: '' }"></vxe-column>
            <vxe-column field="telphone" title="Điện thoại" sortable :edit-render="{ name: 'input', defaultValue: '' }"></vxe-column>
          </vxe-table>
        </a-tab-pane>

        <a-tab-pane tab="Thông tin vé máy bay" key="2" forceRender> </a-tab-pane>
      </a-tabs>
    </a-form>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ValidateErrorEntity } from 'ant-design-vue/es/form/interface';
  import { VxeTableInstance } from 'vxe-table';
  export default defineComponent({
    name: 'VexTableModal',
    components: { BasicModal },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const isUpdate = ref(true);
      const xTable = ref({} as VxeTableInstance);
      const rowId = ref('');
      const formRef = ref();
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 5 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 16 },
      });
      const sexList = ref([
        { label: '', value: '' },
        { label: 'Nam', value: '1' },
        { label: 'Nữ', value: '2' },
      ]);
      const validatorRules = {
        orderCode: [{ required: true, message: 'Mã đơn hàng không được để trống', trigger: 'blur' }],
      };
      const orderMainModel = reactive({
        id: null,
        orderCode: '',
        orderMoney: '',
        ctype: '',
        content: '',
        jeecgOrderCustomerList: [],
        jeecgOrderTicketList: [],
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        orderMainModel.orderCode = '';
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          orderMainModel.orderCode = data.record.orderCode;
        }
      });
      const tableData = ref([]);
      const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

      //Thêm hàng động
      async function insertEvent(row: any) {
        const $table = xTable.value;
        const record = {
          name: '',
          sex: '1',
          idcard: '',
          telphone: '',
        };
        const { row: newRow } = await $table.insertAt(record, row);
        await $table.setActiveCell(newRow, 'sex');
      }

      async function handleSubmit() {
        formRef.value
          .validate()
          .then(() => {
            try {
              const $table = xTable.value;
              const { fullData } = $table.getTableData();
              orderMainModel.jeecgOrderCustomerList = fullData;
              console.log('formData', JSON.stringify(orderMainModel));
              setModalProps({ confirmLoading: true });
              closeModal();
              emit('success', { isUpdate: unref(isUpdate), values: { id: rowId.value } });
            } finally {
              setModalProps({ confirmLoading: false });
            }
          })
          .catch((error: ValidateErrorEntity<any>) => {
            console.log('error', error);
          });
      }

      return {
        xTable,
        tableData,
        sexList,
        formRef,
        validatorRules,
        orderMainModel,
        registerModal,
        getTitle,
        labelCol,
        wrapperCol,
        insertEvent,
        handleSubmit,
      };
    },
  });
</script>
<style scoped>
  .ant-btn {
    padding: 0 10px;
    margin-left: 3px;
  }

  .ant-form-item-control {
    line-height: 0px;
  }

  /** Khoảng cách hàng của biểu mẫu chính */
  .ant-form .ant-form-item {
    margin-bottom: 10px;
  }

  /** TabKhoảng cách dòng trang */
  .ant-tabs-content .ant-form-item {
    margin-bottom: 0px;
  }
</style>
