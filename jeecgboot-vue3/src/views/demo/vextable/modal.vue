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
          <div>
            <a-row type="flex" style="margin-bottom: 10px" :gutter="16">
              <a-col :span="5">Tên khách hàng</a-col>
              <a-col :span="5">Giới tính</a-col>
              <a-col :span="6">Số chứng minh nhân dân</a-col>
              <a-col :span="6">Số điện thoại</a-col>
              <a-col :span="2">Thao tác</a-col>
            </a-row>
            <a-row type="flex" style="margin-bottom: 10px" :gutter="16" v-for="(item, index) in orderMainModel.jeecgOrderCustomerList" :key="index">
              <a-col :span="6" style="display: none">
                <a-form-item>
                  <a-input placeholder="id" v-model:value="item.id" />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item>
                  <a-input placeholder="Tên khách hàng" v-model:value="item.name" />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item>
                  <a-select placeholder="Giới tính" v-model:value="item.sex">
                    <a-select-option value="1">Nam</a-select-option>
                    <a-select-option value="2">Nữ</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item
                  :name="['jeecgOrderCustomerList', index, 'idcard']"
                  :rules="[{ required: true, message: 'Vui lòng nhập số CMND/CCCD', trigger: 'blur' }]"
                  :key="index"
                >
                  <a-input placeholder="Số chứng minh nhân dân" v-model:value="item.idcard" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :name="['jeecgOrderCustomerList', index, 'telphone']">
                  <a-input placeholder="Số điện thoại" v-model:value="item.telphone" />
                </a-form-item>
              </a-col>
              <a-col :span="2">
                <a-form-item>
                  <Icon icon="ant-design:minus-outlined" @click="delRowCustom(index)" style="fontsize: 20px" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-button type="dashed" style="width: 98%; margin-top: 10px" @click="addRowCustom">
              <Icon icon="ph:plus-bold" />
              Thêm thông tin khách hàng
            </a-button>
          </div>
        </a-tab-pane>

        <a-tab-pane tab="Thông tin vé máy bay" key="2" forceRender>
          <a-row type="flex" style="margin-bottom: 10px" :gutter="16">
            <a-col :span="6">Số hiệu chuyến bay</a-col>
            <a-col :span="6">Thời gian chuyến bay</a-col>
            <a-col :span="6">Thao tác</a-col>
          </a-row>
          <a-row type="flex" style="margin-bottom: 10px" :gutter="16" v-for="(item, index) in orderMainModel.jeecgOrderTicketList" :key="index">
            <a-col :span="6" style="display: none">
              <a-form-item>
                <a-input placeholder="id" v-model:value="item.id" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item
                :name="['jeecgOrderTicketList', index, 'ticketCode']"
                :rules="{ required: true, message: 'Vui lòng nhập số hiệu chuyến bay', trigger: 'blur' }"
              >
                <a-input placeholder="Số hiệu chuyến bay" v-model:value="item.ticketCode" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item>
                <a-date-picker placeholder="Thời gian chuyến bay" valueFormat="YYYY-MM-DD" v-model:value="item.tickectDate" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item>
                <Icon icon="ant-design:minus-outlined" @click="delRowTicket(index)" style="fontsize: 20px" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-button type="dashed" style="width: 98%; margin-top: 10px" @click="addRowTicket">
            <Icon icon="ph:plus-bold" />
            Thêm thông tin vé máy bay
          </a-button>
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
    name: 'tableModal',
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
        jeecgOrderCustomerList: [],
        jeecgOrderTicketList: [],
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        reset();
        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          Object.assign(orderMainModel, data.record);
          let params = { id: orderMainModel.id };
          const customerList = await orderCustomerList(params);
          orderMainModel.jeecgOrderCustomerList = customerList;
          const ticketList = await orderTicketList(params);
          orderMainModel.jeecgOrderTicketList = ticketList;
        }
      });
      const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

      //Thêm hàng động
      function addRowCustom() {
        orderMainModel.jeecgOrderCustomerList.push({});
      }

      //Xóa hàng
      function delRowCustom(index) {
        orderMainModel['jeecgOrderCustomerList'].splice(index, 1);
        orderMainModel.jeecgOrderCustomerList.splice(index, 1);
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
      }
      function addRowTicket() {
        orderMainModel.jeecgOrderTicketList.push({});
      }

      //Xóa vé máy bay
      function delRowTicket(index) {
        orderMainModel['jeecgOrderTicketList'].splice(index, 1);
        orderMainModel.jeecgOrderTicketList.splice(index, 1);
      }

      async function handleSubmit() {
        formRef.value
          .validate()
          .then(async () => {
            try {
              console.log('formData', JSON.stringify(orderMainModel));
              setModalProps({ confirmLoading: true });
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

      return {
        formRef,
        validatorRules,
        orderMainModel,
        registerModal,
        getTitle,
        labelCol,
        wrapperCol,
        addRowCustom,
        delRowCustom,
        addRowTicket,
        delRowTicket,
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
