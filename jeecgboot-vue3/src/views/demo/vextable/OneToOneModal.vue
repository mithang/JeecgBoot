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
          <a-row class="form-row" :gutter="16">
            <a-col :lg="8">
              <a-form-item label="Tên khách hàng">
                <a-input v-model:value="orderMainModel.jeecgOrderCustomerList.name" placeholder="Vui lòng nhập tên khách hàng" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="Số điện thoại">
                <a-input v-model:value="orderMainModel.jeecgOrderCustomerList.telphone" placeholder="Vui lòng nhập số điện thoại" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>

        <a-tab-pane tab="Thông tin vé máy bay" key="2" forceRender>
          <a-row class="form-row" :gutter="16">
            <a-col :lg="8">
              <a-form-item label="Số hiệu chuyến bay">
                <a-input v-model:value="orderMainModel.jeecgOrderTicketList.ticketCode" placeholder="Vui lòng nhập số hiệu chuyến bay" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="Thời gian khởi hành">
                <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.jeecgOrderTicketList.tickectDate" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </a-form>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ValidateErrorEntity } from 'ant-design-vue/es/form/interface';
  import { saveOrUpdate } from './jvxetable/jvxetable.api';
  import { orderCustomerList, orderTicketList } from './api';

  export default defineComponent({
    name: 'OneToOneModal',
    components: { BasicModal },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const isUpdate = ref(true);
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
      const validatorRules = {
        orderCode: [{ required: true, message: 'Mã đơn hàng không được để trống', trigger: 'blur' }],
      };
      const orderMainModel = reactive({
        id: null,
        orderCode: '',
        orderMoney: '',
        ctype: '',
        content: '',
        jeecgOrderCustomerList: {
          name: '',
          telphone: '',
        },
        jeecgOrderTicketList: {
          ticketCode: '',
          tickectDate: '',
        },
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        reset();
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          Object.assign(orderMainModel, data.record);
          let params = { id: orderMainModel.id };
          const customerList = await orderCustomerList(params);
          // Giải thích logic mã: [VUEN-1484]Trên trang ví dụ một-nhiều, chỉnh sửa một dòng (đơn hàng Thanh Đảo A0001), thông tin khách hàng không thể điền vào------------
          orderMainModel.jeecgOrderCustomerList = customerList[0]?customerList[0]:{};
          const ticketList = await orderTicketList(params);
          orderMainModel.jeecgOrderTicketList = ticketList[0]?ticketList[0]:{};
        }
      });
      const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

      function reset() {
        orderMainModel.id = null;
        orderMainModel.orderCode = '';
        orderMainModel.orderMoney = '';
        orderMainModel.orderDate = null;
        orderMainModel.ctype = '';
        orderMainModel.content = '';
        orderMainModel.jeecgOrderCustomerList = {};
        orderMainModel.jeecgOrderTicketList = {};
      }
      async function handleSubmit() {
        formRef.value
          .validate()
          .then(async () => {
            try {
              console.log('formData', JSON.stringify(orderMainModel));
              setModalProps({ confirmLoading: true });
              orderMainModel.jeecgOrderCustomerList =
                Object.keys(orderMainModel.jeecgOrderCustomerList).length > 0 ? [orderMainModel.jeecgOrderCustomerList] : [];
              orderMainModel.jeecgOrderTicketList =
                Object.keys(orderMainModel.jeecgOrderTicketList).length > 0 ? [orderMainModel.jeecgOrderTicketList] : [];
              await saveOrUpdate(orderMainModel, unref(isUpdate));
              closeModal();
              emit('success');
            } finally {
              setModalProps({ confirmLoading: false });
            }
          })
          .catch((error: ValidateErrorEntity<any>) => {
            console.log('error', error);
          });
      }

      return { formRef, validatorRules, orderMainModel, registerModal, getTitle, labelCol, wrapperCol, handleSubmit };
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
