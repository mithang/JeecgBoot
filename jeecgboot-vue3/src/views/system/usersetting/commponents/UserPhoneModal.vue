<template>
<BasicModal v-bind="$attrs" @register="registerModal" width="500px"  :title="title" :showCancelBtn="false" :showOkBtn="false">
  <a-form v-if="type==='updatePhone'" class="antd-modal-form" ref="updateFormRef" :model="updateFormState"
          :rules="updateValidatorRules">
    <div v-if="current === 0">
      <a-form-item name="phoneText">
        <span class="black font-size-13">Số điện thoại gốc</span>
        <div class="phone-padding">
          <span>{{ updateFormState.phoneText }}</span>
        </div>
      </a-form-item>
      <a-form-item name="smscode">
        <span class="black font-size-13">Mã xác thực</span>
        <CountdownInput class="phone-padding" size="large" v-model:value="updateFormState.smscode" placeholder="Nhập mã xác thực 6 số"
                        :sendCodeApi="()=>updateSendCodeApi('verifyOriginalPhone')"/>
      </a-form-item>
      <a-form-item>
        <a-button size="large" type="primary" block @click="nextStepClick">
          Bước tiếp theo
        </a-button>
      </a-form-item>
    </div>
    <div v-else-if="current === 1">
      <a-form-item name="newPhone">
        <span class="black font-size-13">Số điện thoại mới</span>
        <div class="phone-padding">
          <a-input v-model:value="updateFormState.newPhone" placeholder="Vui lòng nhập số điện thoại mới"/>
        </div>
      </a-form-item>
      <a-form-item name="smscode">
        <span class="black font-size-13">Mã xác thực</span>
        <CountdownInput class="phone-padding" size="large" v-model:value="updateFormState.smscode" placeholder="Nhập mã xác thực 6 số"
                        :sendCodeApi="()=>updateSendCodeApi('updatePhone')"/>
      </a-form-item>
      <a-form-item>
        <a-button size="large" type="primary" block @click="finishedClick">
          Hoàn thành
        </a-button>
      </a-form-item>
    </div>
  </a-form>

  <a-form v-else-if="type==='bindPhone'" class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
    <a-form-item  name="phone">
      <a-input size="large" v-model:value="formState.phone" placeholder="Vui lòng nhập số điện thoại" />
    </a-form-item>
    <a-form-item name="smscode">
      <CountdownInput size="large" v-model:value="formState.smscode" placeholder="Nhập mã xác thực 6 số" :sendCodeApi="sendCodeApi" />
    </a-form-item>
    <a-form-item>
      <a-button size="large" type="primary" block @click="updatePhone">
        Xác nhận
      </a-button>
    </a-form-item>
  </a-form>
</BasicModal>
</template>

<script lang="ts" setup name="user-replace-phone-modal">
import BasicModal from "/@/components/Modal/src/BasicModal.vue";
import { CountdownInput } from '/@/components/CountDown';
import { useUserStore } from "/@/store/modules/user";
import { useMessage } from "/@/hooks/web/useMessage";
import { defineEmits, ref, reactive, toRaw } from "vue";
import { useModalInner } from "/@/components/Modal";
import { getCaptcha } from "/@/api/sys/user";
import { SmsEnum } from "/@/views/sys/login/useLogin";
import { Rule } from "/@/components/Form";
import { rules } from "/@/utils/helper/validator";
import { Form } from "ant-design-vue";
import { updateMobile, changePhone } from "../UserSetting.api";
import { duplicateCheck } from "/@/views/system/user/user.api";
import {defHttp} from "@/utils/http/axios";
import { ExceptionEnum } from "@/enums/exceptionEnum";

const userStore = useUserStore();
const { createMessage } = useMessage();
const formState = reactive<Record<string, any>>({
  phone:'',
  smscode:''
});

//Đổi số điện thoại
const updateFormState = reactive<Record<string, any>>({
  phone:'',
  smscode:'',
  newPhone:'',
  phoneText:'',
  newSmsCode:''
});

const formRef = ref();
const userData = ref<any>({})

const validatorRules: Record<string, Rule[]> = {
  phone: [{...rules.duplicateCheckRule("sys_user",'phone',formState,{ label:'Số điện thoại' })[0]},{ pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại không đúng' }],
  smscode: [{ required: true,message:'Vui lòng nhập mã xác thực' }],
};

//Sửa quy tắc xác thực số điện thoại
const updateValidatorRules: Record<string, Rule[]> = {
  newPhone: [{...rules.duplicateCheckRule("sys_user",'phone',formState,{ label:'Số điện thoại' })[0]},{ pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại không đúng' }],
  smscode: [{ required: true,message:'Vui lòng nhập mã xác thực' }],
  newSmsCode: [{ required: true,message:'Vui lòng nhập mã xác thực' }],
};
const useForm = Form.useForm;
const title = ref<string>('');
const emit = defineEmits(['register','success']);
//Đổi số điện thoại hay liên kết số điện thoại
const type = ref<string>('updatePhone');
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: false });
  if(data.record.phone){
    updateFormState.phone = "";
    updateFormState.smscode = "";
    current.value = 0;
    title.value = 'Đổi số điện thoại';
    type.value = "updatePhone";
    Object.assign(updateFormState, data.record);
  }else{
    title.value = 'Liên kết số điện thoại';
    type.value = "bindPhone"
    //Gán giá trị
    data.record.smscode = '';
    Object.assign(formState, data.record);
    setTimeout(()=>{
      formRef.value.resetFields();
      formRef.value.clearValidate();
    },300)
  }
  userData.value = data.record;
});

/**
 * Hàm chạy trước khi đếm ngược
 */
function sendCodeApi() {
  return getCaptcha({ mobile: formState.phone, smsmode: SmsEnum.REGISTER });
}

/**
 * Hàm chạy trước khi đếm ngược
 * 
 * @param type Loại verifyOriginalPhone: số điện thoại của người xác thực, updatePhone: đổi số điện thoại
 */
function updateSendCodeApi(type) {
  let phone = ""
  if(current.value === 0){
    phone = updateFormState.phone;
  }else{
    phone = updateFormState.newPhone;
  }
  let params = { phone: phone,  type: type };
  return new Promise((resolve, reject) => {
    defHttp.post({ url: "/sys/user/sendChangePhoneSms", params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        // Giải thích logic mã: 【QQYUN-9005】Cùng một IP, nếu gửi quá 5 tin nhắn SMS trong 1 phút thì nhắc cần mã xác thực---
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createMessage.error(res.message || 'Sự cố không xác định');
          reject();
        }
        reject(res);
      }
    }).catch((res)=>{
      createMessage.error(res.message || 'Sự cố không xác định');
      reject();
    });
  });
}

/**
 * Cập nhật số điện thoại
 */
async function updatePhone() {
  await formRef.value.validateFields();
  updateMobile(formState).then((res) =>{
    if(res.success){
      createMessage.success(type.value === "updatePhone"?"Đổi số điện thoại thành công":"Liên kết số điện thoại thành công")
      emit("success")
      closeModal();
    }else{
      createMessage.warning(res.message)
    }
  })
}
  //Đi đến bước thứ mấy
  const current = ref<number>(0);
  const updateFormRef = ref();

  /**
   * Sự kiện nhấp vào bước tiếp theo
   */
  async function nextStepClick() {
    let params = { phone: updateFormState.phone, smscode: updateFormState.smscode, type: 'verifyOriginalPhone' };
    changeAndVerifyPhone(params,1)
  }

  /**
   * Hoàn thành
   */
  function finishedClick() {
    changeAndVerifyPhone({ phone: updateFormState.phone, newPhone: updateFormState.newPhone, smscode: updateFormState.smscode, type: 'updatePhone' },0);
  }

  /**
   * Sửa và xác thực số điện thoại
   * @param params
   * @param index
   */
  async function changeAndVerifyPhone(params, index) {
    await updateFormRef.value.validateFields();
    changePhone(params).then((res)=>{
      if(res.success){
        current.value = index;
        if(index == 0){
          createMessage.success(res.message);
          emit("success");
          closeModal();
        }
        updateFormState.smscode = "";
      }else{
        createMessage.warn(res.message);
      }
    }).catch((res) =>{
      createMessage.warn(res.message);
    })
  }
</script>
<style lang="less" scoped>
  .antd-modal-form {
    padding: 10px 24px 10px 24px;
  }
 .black {
   color: #000000;
 }
 .font-size-13 {
   font-size: 13px;
   line-height: 15px;
 }
  .phone-padding {
    padding-top: 10px;
    padding-bottom: 10px;
  }
  :deep(.ant-form-item){
    margin-bottom: 10px;
  }
</style>