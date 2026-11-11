<template>
  <div class="organ-box">
    <div class="organ-subject" :class="tenantType !== '' ? 'subject-margin' : ''">
      <AppLoginHeader :showLocalePicker="false" />
      <template v-if="organType === 'home'">
        <div class="flex-row align-items-center" style="margin-top: 20px">
          <div class="organ-title"> Tạo hoặc tham gia tổ chức </div>
        </div>
        <div class="organ-desc align-items-center">
          <div class="content-box pointer" @click="joinOrganClick">
            <div class="organ-title-desc"> Tham gia<span style="color: rgb(141, 198, 216)">Đã có</span>Tổ chức </div>
            <div> Nếu được thông báo cần sử dụng, hoặc có đồng nghiệp đã đang dùng, vui lòng chọn mục này. </div>
          </div>
          <div class="content-box pointer" style="margin-top: 35px" @click="createOrganClick">
            <div class="organ-title-desc"> Tạo<span style="color: rgb(74, 79, 175)">cái mới</span>Tổ chức </div>
            <div> Nếu muốn tạo tài khoản cho doanh nghiệp hoặc tổ chức, vui lòng chọn mục này. </div>
          </div>
        </div>
      </template>
      <template v-else-if="organType === 'join'">
        <div class="margin-top40 pointer" style="display: flex" @click="returnHome" v-if="tenantType === ''">
          <Icon icon="ant-design:arrow-left-outlined" style="color: #757575; margin-top: 2px" />
          <span style="margin-left: 10px">Quay lại</span>
        </div>
        <div class="align-items-center" style="margin-top: 20px">
          <div class="organ-title"> Vui lòng điền số nhà của tổ chức </div>
          <div class="organ-join-desc">
            <span style="color: #9e9e9e">Số nhà của tổ chức có thể lấy thông qua quản trị viên</span>
          </div>
          <a-form ref="joinRef" :model="formJoinState" :rules="getFormJoinRules">
            <div class="content-item" :class="getHouseNumberClass" @click="formStateInputClick('houseNumber')">
              <a-form-item name="houseNumber">
                <a-input ref="hoseNumberRef" v-model:value="formJoinState.houseNumber" style="height: 40px" @blur="formStateInputBlur" />
                <div class="form-title" :class="activekey === 'houseNumber' ? 'active-title' : ''"> Ví dụ mẫu: J9A2K8R </div>
              </a-form-item>
            </div>
            <div class="tenant-number">
              <a href="http://help.qiaoqiaoyun.com/org/new.html" target="_blank">Không có số nhà của tổ chức?</a>
            </div>
            <div class="create-btn pointer" @click="joinTenantClick"> Tham gia </div>
          </a-form>
        </div>
      </template>
      <template v-else-if="organType === 'create'">
        <div class="margin-top40 pointer" style="display: flex" @click="returnHome" v-if="tenantType === ''">
          <Icon icon="ant-design:arrow-left-outlined" style="color: #757575; margin-top: 2px" />
          <span style="margin-left: 10px">Quay lại</span>
        </div>
        <div class="align-items-center" style="margin-top: 20px">
          <div class="organ-title"> Tạo tổ chức </div>
          <div class="organ-join-desc">
            <span style="color: #9e9e9e">Tài khoản hiện tại của bạn mặc định trở thành quản trị viên của tổ chức</span>
          </div>

          <a-form ref="createRef" :model="formCreateState" :rules="getFormCreateRules">
            <div class="content-item" :class="getNameClass" @click="formStateInputClick('name')">
              <a-form-item name="name">
                <a-input
                  ref="nameRef"
                  v-model:value="formCreateState.name"
                  style="height: 40px; font-size: 13px; color: rgba(0, 0, 0, 0.65)"
                  @blur="formStateInputBlur"
                />
                <div class="form-title" :class="activekey === 'name' ? 'active-title' : ''"> Tên tổ chức </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="trade">
                <a-select placeholder="Vui lòng chọn" style="height: 40px" :options="tradeOption" v-model:value="formCreateState.trade" />
                <div class="active-form-title"> Ngành nghề </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="companySize">
                <a-select placeholder="Vui lòng chọn" style="height: 40px" :options="companySizeOption" v-model:value="formCreateState.companySize" />
                <div class="active-form-title"> Quy mô </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="position">
                <a-select placeholder="Vui lòng chọn" style="height: 40px" :options="positionOption" v-model:value="formCreateState.position" />
                <div class="active-form-title"> Cấp bậc chức vụ của bạn </div>
              </a-form-item>
            </div>
            <div class="content-item">
              <a-form-item name="department">
                <a-select placeholder="Vui lòng chọn" style="height: 40px" :options="departmentOption" v-model:value="formCreateState.department" />
                <div class="active-form-title"> Phòng ban của bạn </div>
              </a-form-item>
            </div>
            <div class="create-btn pointer" @click="addOrganClick"> Tạo </div>
          </a-form>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup name="app-organization">
  import { computed, onMounted, reactive, ref, unref, watch } from 'vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import AppLoginHeader from './AppLoginHeader.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { saveTenantJoinUser, joinTenantByHouseNumber } from '../AppLogin.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { router } from '/@/router';
  import { PageEnum } from '/@/enums/pageEnum';
  import { useUserStore } from '/@/store/modules/user';

  const props = defineProps({
    tenantType: { type: String, default: '' },
  });

  const { createMessage } = useMessage();
  //Biểu mẫu tạo đơn vị thuê
  const createRef = ref();
  //Biểu mẫu tham gia đơn vị thuê
  const joinRef = ref();
  //Loại tenant: trang chủ, tham gia, tạo
  const organType = ref<string>('home');
  //Tham gia tenant
  const formJoinState = reactive<any>({
    houseNumber: '',
  });
  //Tạo tenant
  const formCreateState = reactive<any>({
    name: '',
    trade: undefined,
    companySize: undefined,
    position: undefined,
    department: undefined,
  });
  //Giá trị đã chọn
  const activekey = ref<string>('');
  //Lấy kiểu dáng số nhà
  const getHouseNumberClass = computed(() => {
    return formJoinState.houseNumber != '' ? 'current-active' : '' || unref(activekey) === 'houseNumber' ? 'current-active' : '';
  });
  //Lấy kiểu dáng tên tenant
  const getNameClass = computed(() => {
    return formCreateState.name != '' ? 'current-active' : '' || unref(activekey) === 'name' ? 'current-active' : '';
  });
  const hoseNumberRef = ref();
  const nameRef = ref();
  //Kiểm chứng biểu mẫu tham gia đơn vị thuê
  const getFormJoinRules = {
    houseNumber: [{ required: true, message: 'Vui lòng nhập số nhà của tổ chức' }],
  };
  const { t } = useI18n();
  const getNameRule = computed(() => createRule('Vui lòng nhập tên tổ chức'));
  //Kiểm chứng biểu mẫu tạo đơn vị thuê
  const getFormCreateRules = computed(() => {
    return {
      name: unref(getNameRule),
    };
  });
  //Ngành trực thuộc
  const tradeOption = ref<any>([
    { label: 'Ngành truyền tải thông tin, phần mềm và dịch vụ công nghệ thông tin', value: '1' },
    { label: 'Ngành sản xuất chế tạo', value: '2' },
    { label: 'Ngành cho thuê và dịch vụ thương mại', value: '3' },
    { label: 'Giáo dục', value: '4' },
    { label: 'Ngành tài chính', value: '5' },
    { label: 'Ngành xây dựng', value: '6' },
    { label: 'Ngành nghiên cứu khoa học và dịch vụ kỹ thuật', value: '7' },
    { label: 'Ngành bán buôn và bán lẻ', value: '8' },
    { label: 'Ngành lưu trú và ăn uống', value: '9' },
    { label: 'Thương mại điện tử', value: '10' },
    { label: 'Ngành bán lẻ ngoại tuyến và dịch vụ', value: '11' },
    { label: 'Ngành văn hóa, thể thao và giải trí', value: '12' },
    { label: 'Ngành bất động sản', value: '13' },
    { label: 'Ngành vận tải, kho bãi và bưu chính', value: '14' },
    { label: 'Ngành y tế và công tác xã hội', value: '15' },
    { label: 'Ngành quản lý công, bảo đảm xã hội và tổ chức xã hội', value: '16' },
    { label: 'Ngành sản xuất và cung ứng điện, nhiệt, khí đốt và nước', value: '17' },
    { label: 'Ngành thủy lợi, môi trường và quản lý cơ sở công cộng', value: '18' },
    { label: 'Ngành dịch vụ dân cư, sửa chữa và các dịch vụ khác', value: '19' },
    { label: 'Cơ quan chính phủ', value: '20' },
    { label: 'Ngành nông, lâm, chăn nuôi, ngư nghiệp', value: '21' },
    { label: 'Ngành khai khoáng', value: '22' },
    { label: 'Tổ chức quốc tế', value: '23' },
    { label: 'Khác', value: '24' },
  ]);
  //Quy mô công ty
  const companySizeOption = ref<any>([
    { label: '20người trở xuống', value: '1' },
    { label: '21-99người', value: '2' },
    { label: '100-499người', value: '3' },
    { label: '500-999người', value: '4' },
    { label: '1000-9999người', value: '5' },
    { label: '10000người trở lên', value: '6' },
  ]);
  //Chức vụ
  const positionOption = ref<any>([
    { label: 'Chủ tịch (tổng tài)/Tổng giám đốc/CEO', value: '1' },
    { label: 'Phó chủ tịch/Phó tổng giám đốc/VP', value: '2' },
    { label: 'Giám đốc/Người quản lý/Quản lý (giám đốc)', value: '3' },
    { label: 'Nhân viên/chuyên viên/Thực thi', value: '4' },
    { label: 'Khác', value: '5' },
  ]);
  //Chức vụ
  const departmentOption = ref<any>([
    { label: 'Văn phòng tổng giám đốc', value: '1' },
    { label: 'Kỹ thuật/IT/Nghiên cứu & phát triển', value: '2' },
    { label: 'Sản phẩm/Thiết kế', value: '3' },
    { label: 'Bán hàng/Thị trường/Vận hành', value: '4' },
    { label: 'Nhân sự/Tài chính/Hành chính', value: '5' },
    { label: 'Tài nguyên/Kho bãi/Mua sắm (thu mua)', value: '6' },
    { label: 'Khác', value: '7' },
  ]);
  const emit = defineEmits(['success']);
  const userStore = useUserStore();

  /**
   * Tạo quy tắc
   * @param message
   */
  function createRule(message: string) {
    return [
      {
        required: true,
        message,
        trigger: 'change',
      },
    ];
  }

  /**
   * Tham gia đơn vị thuê
   */
  function joinOrganClick() {
    organType.value = 'join';
  }

  /**
   * Tạo đơn vị thuê
   */
  function createOrganClick() {
    organType.value = 'create';
  }

  /**
   * Sự kiện nhấp của component biểu mẫu
   */
  function formStateInputClick(val) {
    activekey.value = val;
    if (val === 'houseNumber') {
      hoseNumberRef.value.focus();
    } else {
      nameRef.value.focus();
    }
  }

  /**
   * Xóa giá trị đã chọn
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * Tạo đơn vị thuê
   */
  async function addOrganClick() {
    createRef.value.validateFields().then(async (values) => {
      values.status = '1';
      await saveTenantJoinUser(values).then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          userStore.setTenant(res.result);
          router.replace((userStore.getUserInfo && userStore.getUserInfo.homePath) || PageEnum.BASE_HOME);
          emit('success');
        } else {
          createMessage.warning(res.message);
        }
      });
    });
  }

  /**
   * Tham gia đơn vị thuê
   */
  function joinTenantClick() {
    joinRef.value.validateFields().then(async (values) => {
      await joinTenantByHouseNumber(values).then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          //userStore.setTenant(res.result);
          if (!props.tenantType) {
            router.replace((userStore.getUserInfo && userStore.getUserInfo.homePath) || PageEnum.BASE_HOME);
          }
          emit('success');
        } else {
          createMessage.warning(res.message);
        }
      }).catch((e)=>{
        createMessage.warning(e.message);
      });
    });
  }

  /**
   * Quay lại trang lựa chọn
   */
  function returnHome() {
    organType.value = 'home';
  }

  watch(
    () => props.tenantType,
    (val) => {
      if (val) {
        organType.value = val;
      }
    },
    { immediate: true }
  );
</script>

<style lang="less" scoped>
  .organ-box {
    display: block;
  }
  .organ-subject {
    max-width: 420px;
    padding: 48px 48px 23px;
    box-sizing: border-box;
    background: #ffffff;
    border-radius: 4px;
    margin: 80px auto 15px;
    min-height: 578px;
    position: relative;
  }
  .flex-row {
    display: flex;
    min-width: 0;
  }
  .align-items-center {
    align-items: center;
  }

  .margin-top40 {
    margin-top: 40px;
  }

  .organ-title {
    text-align: left;
    font-size: 20px;
    color: #333333;
  }
  .organ-desc {
    margin-top: 20px;
  }
  .content-box {
    width: 100%;
    height: 150px;
    background: #ffffff;
    box-shadow: rgba(0, 0, 0, 0.24) 0 0 3px;
    padding: 40px 20px;
    border-radius: 6px;
  }
  .organ-title-desc {
    font-size: 18px;
    font-weight: 500;
  }
  .pointer {
    cursor: pointer;
  }
  .organ-join-desc {
    margin-top: 10px;
    align-items: center;
    font-size: 15px;
  }
  .form-title {
    position: absolute;
    left: 12px;
    top: 13px;
    color: #9e9e9e;
    font-size: 14px;
    z-index: 0;
    line-height: 1;
    transition: all 0.3s;
  }
  .content-item {
    position: relative;
    margin-top: 30px;
    background: #ffffff;
    height: 40px;
    box-sizing: border-box;
  }
  .current-active {
    .form-title {
      font-size: 12px;
      color: #757575;
      top: -9px;
      z-index: 1;
      background: #fff;
      padding: 0 4px;
    }
    .active-title {
      color: #2196f3;
    }
  }
  .active-form-title {
    position: absolute;
    left: 12px;
    top: -9px;
    color: #757575;
    font-size: 12px;
    z-index: 0;
    line-height: 1;
    background: #fff;
  }
  .content-item :deep(.ant-select-selector) {
    height: 40px;
  }
  .content-item :deep(.ant-select-single.ant-select-show-arrow .ant-select-selection-placeholder) {
    line-height: 40px !important;
  }
  .content-item :deep(.ant-select-single.ant-select-show-arrow .ant-select-selection-item) {
    line-height: 40px !important;
  }
  .create-btn {
    font-weight: 600;
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: block;
    background: #2296f3;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    margin-top: 32px;
    text-align: center;
  }

  :deep(.ant-input-number-input) {
    font-size: 14px;
  }

  .subject-margin {
    margin: 15px auto 15px !important;
  }

  .tenant-number {
    align-items: center;
    color: #1e88e5;
    margin-top: 16px;
    vertical-align: middle;
  }

  @media screen and (max-width: 414px) {
    .organ-box {
      padding: 46px 16px;
    }
    .organ-subject {
      margin: 0;
      padding: 48px 24px 48px;
    }
  }
</style>
