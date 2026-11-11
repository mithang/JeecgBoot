<!--Chi tiết đơn vị thuê của tôi-->
<template>
  <div class="message-set-container">
    <div class="message-set-box">
      <div class="message-set-header">
        <span class="font17">Thông tin tổ chức</span>
      </div>
      <a-form :model="formState">
        <div class="message-set-content">
          <div class="common-info">
            <div class="common-info-row">
              <div class="common-info-row-label">Tổ chứcLOGO</div>
              <div class="common-info-row-content">
                <JImageUpload v-model:value="formState.companyLogo" @change="handleCompanyLogoChange"></JImageUpload>
              </div>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Tên tổ chức</div>
              <span class="m-right16">{{ formState.name }}</span>
              <span class="edit-name" @click="goUpdate('name')">Sửa</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Số nhà của tổ chức</div>
              <div class="common-info-row-content">
                <span class="pointer">
                  <span>{{ formState.houseNumber }}</span>
                </span>
              </div>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Mã tổ chức(ID)</div>
              <div class="common-info-row-content">
                <span class="pointer">
                  <span>{{ formState.id }}</span>
                </span>
              </div>
            </div>
            <div class="split-line"></div>
            <div class="common-info-row">
              <div class="common-info-row-label">Địa điểm</div>
              <span class="m-right16">{{ formState.companyAddress_dictText }}</span>
              <span class="edit-name" @click="goUpdate('companyAddress')">Sửa</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Ngành nghề</div>
              <span class="m-right16">{{ formState.trade_dictText }}</span>
              <span class="edit-name" @click="goUpdate('trade')">Sửa</span>
            </div>
            <div class="common-info-row m-top24">
              <div class="common-info-row-label">Địa điểm làm việc</div>
              <span class="m-right16">{{ formState.workPlace }}</span>
              <span class="edit-name" @click="goUpdate('workPlace')">Sửa</span>
            </div>
            <div class="cancel-split-line"></div>
          </div>
        </div>
      </a-form>
    </div>
  </div>
  <!-- Hộp thoại sửa tên tổ chức -->
  <a-modal v-model:open="modalVisible.name" title="Sửa tên tổ chức" width="500" destroy-on-close @ok="doUpdate('name')">
    <a-form ref="manageNameRef" :model="updateInfo" :rules="getManageNameRules">
      <a-form-item name="name" class="form-item-padding">
        <div class="form-group">
              <span class="form-label">
                Tên tổ chức
                <span class="txt-middle red">*</span>
              </span>
          <a-input v-model:value="updateInfo.name" />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
  
  <!-- Hộp thoại địa điểm của tổ chức -->
  <a-modal v-model:open="modalVisible.companyAddress" title="Địa điểm" width="500" destroy-on-close @ok="doUpdate('companyAddress')">
    <a-form :model="updateInfo">
      <a-form-item name="companyAddress" class="form-item-padding">
        <div style="margin-top: 20px">
          <j-area-select v-model:value="updateInfo.companyAddress" />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Hộp thoại ngành nghề của tổ chức -->
  <a-modal v-model:open="modalVisible.trade" title="Đặt ngành nghề" width="500" destroy-on-close @ok="doUpdate('trade')">
    <a-form :model="updateInfo">
      <a-form-item name="trade" class="form-item-padding">
        <div style="margin-top: 20px">
          <j-dict-select-tag v-model:value="updateInfo.trade" dictCode="trade" />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- Hộp thoại địa điểm làm việc -->
  <a-modal v-model:open="modalVisible.workPlace" title="Đặt địa điểm làm việc" width="500" destroy-on-close @ok="doUpdate('workPlace')">
    <a-form ref="workPlaceRef" :model="updateInfo">
      <a-form-item name="name" class="form-item-padding">
        <div style="margin-top: 20px">
          <a-textarea placeholder="Vui lòng điền địa điểm làm việc" v-model:value="updateInfo.workPlace" />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" name="tenant-my-tenant-list" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {getFileAccessHttpUrl, tenantSaasMessage} from '@/utils/common/compUtils';
  import { getTenantById, saveOrUpdateTenant } from '@/views/system/tenant/tenant.api';
  import { getTenantId } from '@/utils/auth';
  import { getDataByCode, getRealCode, provinceOptions } from '@/components/Form/src/utils/areaDataUtil';
  import { initDictOptions } from '@/utils/dict';
  import {createImgPreview} from "@/components/Preview";
  import { JImageUpload } from "@/components/Form";
  // import {updateTenantInfo} from "@/views/super/myapps/organization/organization.api";
  import { defHttp } from "/@/utils/http/axios";
  import JAreaSelect from "/@/components/Form/src/jeecg/components/JAreaSelect.vue";
  import JDictSelectTag from "/@/components/Form/src/jeecg/components/JDictSelectTag.vue";

  const { createMessage } = useMessage();
  const formState = reactive({
    id: '',
    name: '',
    houseNumber: '',
    companyAddress_dictText: '',
    trade_dictText: '',
    workPlace: '',
    createBy: '',
    companyLogo: '',
  });
  let tradeOptions: any[] = [];
  //Tên tổ chứcref
  const manageNameRef= ref();
  // modalHiển thị
  const modalVisible = reactive<any>({
    name: false,
    trade: false,
    companyAddress: false
  });

  // Quy tắc kiểm tra tên tổ chức
  const getManageNameRules =  {
    name: [{ required: true, message: 'Tên tổ chức không được để trống', trigger: 'blur' }],
  };

  //Đối tượng sửa đổi
  const updateInfo = reactive<any>({
    name: '',
    trade:'',
    companyAddress: '',
    workPlace: '',
  });
  
  /**
   * Khởi tạo thông tin đơn vị thuê
   */
  async function initTenant() {
    let result = await getTenantById({ id: getTenantId() });
    if (result) {
      if (result.companyAddress) {
        formState.companyAddress_dictText = getPcaText(result.companyAddress);
      } else {
        formState.companyAddress_dictText = '';
      }
      if (result.trade) {
        formState.trade_dictText = await getTradeText(result.trade);
      } else {
        formState.trade_dictText = '';
      }
      Object.assign(formState, result);
    }
  }

  /**
   * Lấy văn bản tỉnh/thành/quận
   * @param code
   */
  function getPcaText(code) {
    let arr = getRealCode(code, 3);
    console.log("arr:::",arr)
    let provinces: any = provinceOptions.filter((item) => item.value == arr[0]);
    let cities: any[] = getDataByCode(arr[0]);
    let areas: any[] = getDataByCode(arr[1]);
    let str = '';
    if (provinces && provinces.length > 0) {
      str = provinces[0].label;
      if (cities && cities.length > 0) {
        let temp1 = cities.filter((item) => item.value == arr[1]);
        str = str + '/' + temp1[0].label;
        if (areas && areas.length > 0) {
          let temp2 = areas.filter((item) => item.value == arr[2]);
          str = str + '/' + temp2[0].label;
        }
      }
    }
    return str;
  }

  /**
   * Lấy văn bản ngành nghề
   *
   * @param trade
   */
  async function getTradeText(trade) {
    if (tradeOptions.length == 0) {
      let options: any = await initDictOptions('trade');
      tradeOptions = options;
    }
    let arr = tradeOptions.filter((item) => item.value == trade);
    if (arr.length > 0) {
      return arr[0].label;
    }
    return '';
  }

  /**
   * Sự kiện tải lên logo công ty thành công
   * 
   * @param val
   */
  function handleCompanyLogoChange(val) {
    if(val){
      saveOrUpdateTenant({ id: formState.id, companyLogo: val }, true)
    }
  }

  /**
   * Cập nhật mở hộp thoại
   * 
   * @param key
   */
  function goUpdate(key){
    modalVisible[key] = true;
    updateInfo[key] = formState[key];
  }

  /**
   * Chỉnh sửa thông tin đơn vị thuê
   * @param params
   */
  async function updateTenantInfo(params){
    return defHttp.put({ url: '/sys/tenant/editOwnTenant', params });
  }

  /**
   * Cập nhật dữ liệu
   * @param key
   */
  async function doUpdate(key) {
    if(key=='name'){
      await manageNameRef.value.validateFields();
    }
    //Báo lỗi khi địa điểm trống
    if(key == 'companyAddress'){
      if(updateInfo[key] instanceof Array){
        updateInfo[key] = '';
      }
    }
    let params = {
      id: formState.id,
      [key]: updateInfo[key]
    };
    await updateTenantInfo(params);
    initTenant();
    modalVisible[key] = false
  }

  onMounted(() => {
    //Thông tin thông báo
    tenantSaasMessage('Đơn vị thuê của tôi');
    initTenant();
  });
</script>
<style lang="less" scoped>
  .message-set-container {
    box-sizing: border-box;
    flex: 1;
    margin: 16px;
    min-height: 0;
  }
  .message-set-box {
    background: #fff;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    min-width: 750px;
    position: relative;
    height: 100%;
  }
  .message-set-header {
    align-items: center;
    background: #fff;
    border-bottom: 1px solid #eaeaea;
    box-sizing: border-box;
    color: #333;
    display: flex;
    font-weight: 600;
    height: 57px;
    line-height: 57px;
    padding: 0 18px 0 24px;
  }
  .message-set-content {
    box-sizing: border-box;
    flex: 1;
    min-height: 0;
    overflow-x: hidden;
  }
  .font17 {
    font-size: 17px;
  }
  .common-info {
    padding: 20px 24px;
    background: #ffffff;
    margin-bottom: 20px;
  }
  .common-info-row {
    color: #333;
    display: flex;
    font-size: 13px;
  }
  .common-info-row-label {
    color: #757575;
    display: flex;
    justify-content: flex-start;
    width: 140px;
  }
  .common-info-row-content {
    display: flex;
    flex: 1;
    flex-direction: column;
  }
  .pointer {
    cursor: pointer;
  }
  .delete-color {
    color: #f51744;
    cursor: pointer;
  }
  .set-describe {
    color: #757575;
    margin-top: 10px !important;
  }
  .m-top24 {
    margin-top: 24px;
  }
  .edit-name {
    border: none;
    border-radius: 3px;
    box-sizing: border-box;
    color: #1e88e5;
    cursor: pointer;
    display: inline-block;
    outline: none;
    text-shadow: none;
    user-select: none;
    vertical-align: middle;
  }
  .m-right16 {
    margin-right: 16px;
  }
  .split-line {
    background: #eaeaea;
    height: 1px;
    margin: 40px 0;
    width: 100%;
  }
  .cancel-split-line {
    background: #eaeaea;
    height: 1px;
    margin: 40px 0 20px;
    width: 100%;
  }
  .form-item-padding {
    padding: 0 24px 22px;
  }
  .form-group {
    display: table;
    font-size: 13px;
    position: relative;
    width: 100%;
    .form-label {
      color: #333;
      font-weight: 600;
      line-height: 29px;
    }
    .txt-middle {
      vertical-align: middle !important;
    }
  }
  .red {
    color: red;
  }
  .domain-background {
    height: 56px;
    margin-top: 6px;
    width: 100px;
    margin-left: 142px;
  }
  .cancellation {
    color: #333333;
    font-size: 20px;
    font-weight: 700;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  :deep(.ant-upload.ant-upload-select){
    width: 80px !important;
    height: 80px !important;
    border: unset !important;
  }
  :deep(.ant-upload-list-item-container){
    width: 80px !important;
    height: 80px !important;
    border: unset !important;
  }
  .edit-name {
    border: none;
    border-radius: 3px;
    box-sizing: border-box;
    color: #1e88e5;
    cursor: pointer;
    display: inline-block;
    outline: none;
    text-shadow: none;
    user-select: none;
    vertical-align: middle;
  }
</style>
