<template>
  <BasicModal v-bind="config" :maxHeight="500" :title="currTitle" v-model:visible="visible" wrapClassName="loginSelectModal">
    <a-form ref="formRef" v-bind="layout" :colon="false" class="loginSelectForm">
      <a-form-item v-if="isMultiTenant" :validate-status="validate_status">
        <!-- Nội dung label -->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>Bạn thuộc nhiều tenant, vui lòng chọn tenant hiện tại</span>
            </template>
            <a-avatar style="background-color: #87d068" :size="30"> Tenant </a-avatar>
          </a-tooltip>
        </template>
        <!-- Nội dung dropdown phòng ban -->
        <a-select v-model:value="tenantSelected" placeholder="Vui lòng chọn phòng ban đăng nhập" :class="{ 'valid-error': validate_status == 'error' }">
          <template #suffixIcon>
            <Icon icon="ant-design:gold-outline" />
          </template>
          <template v-for="tenant in tenantList" :key="tenant.id">
            <a-select-option :value="tenant.id">{{ tenant.name }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
      <a-form-item v-if="isMultiDepart && departList.length > 0" :validate-status="validate_status1">
        <!-- Nội dung label -->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>Bạn thuộc nhiều phòng ban, vui lòng chọn phòng ban hiện tại</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> Phòng ban </a-avatar>
          </a-tooltip>
        </template>
        <!-- Nội dung dropdown phòng ban -->
        <a-select v-model:value="departSelected" placeholder="Vui lòng chọn phòng ban đăng nhập" :class="{ 'valid-error': validate_status1 == 'error' }">
          <template #suffixIcon>
            <Icon icon="ant-design:gold-outline" />
          </template>
          <template v-for="depart in departList" :key="depart.orgCode">
            <a-select-option :value="depart.orgCode">{{ getShortDeptName(depart) }} </a-select-option>
          </template>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="close">Đóng</a-button>
      <a-button @click="handleSubmit" type="primary">Xác nhận</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, watch, unref } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal } from '/@/components/Modal';
  import { getUserDeparts, selectDepart } from '/@/views/system/depart/depart.api';
  import { getUserTenants } from '/@/views/system/tenant/tenant.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  const userStore = useUserStore();
  const { createMessage, notification } = useMessage();
  const props = defineProps({
    title: { type: String, default: 'Chọn phòng ban' },
    closable: { type: Boolean, default: false },
    username: { type: String, default: '' },
  });

  const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 18 },
  };

  const config = {
    maskClosable: false,
    closable: false,
    canFullscreen: false,
    width: '500px',
    minHeight: 20,
    maxHeight: 20,
  };
  const currTitle = ref('');

  const isMultiTenant = ref(false);
  const currentTenantName = ref('');
  const tenantSelected = ref();
  const tenantList = ref([]);
  const validate_status = ref('');

  const isMultiDepart = ref(false);
  const currentDepartName = ref('');
  const departSelected = ref('');
  const departList = ref([]);
  const validate_status1 = ref('');
  // Hiện/ẩn popup
  const visible = ref(false);
  // Lấy viết tắt phòng ban
  const getShortDeptName = computed(()=>{
    return (depart) => {
      let deptName = depart.departNameAbbr || depart.departPathName || depart.departName;
      if (deptName.length > 18) {
        return '...' + deptName.substring(deptName.length-18, deptName.length) ;
      }
      return deptName;
    };
  })
  /**
   * Xử lý trước khi mở popup
   */
  async function show() {
    // update-begin--author:liaozhiyang---date:20260812---for:【LHZP-1359】 Phòng ban và tenant, bên nào có dữ liệu cũng cần hiển thị, trừ khi cả hai đều không có dữ liệu thì mới nhắc
    const [canSwitchDepart, canSwitchTenant] = await Promise.all([loadDepartList(), loadTenantList()]);
    if (!canSwitchDepart && !canSwitchTenant) {
      createMessage.info('Hiện tại không có tenant hoặc phòng ban nào để chuyển');
      return;
    }
    // update-end--author:liaozhiyang---date:20260812---for:【LHZP-1359】 Phòng ban và tenant, bên nào có dữ liệu cũng cần hiển thị, trừ khi cả hai đều không có dữ liệu thì mới nhắc
    // Cấu hình tiêu đề
    if (unref(isMultiTenant) && unref(isMultiDepart)) {
      currTitle.value = 'Chuyển tenant và phòng ban';
    } else if (unref(isMultiTenant)) {
      currTitle.value =
        unref(currentTenantName) && unref(currentTenantName).length > 0 ? `Chuyển tenant (tenant hiện tại: ${unref(currentTenantName)})` : props.title;
    } else if (unref(isMultiDepart)) {
      currTitle.value =
        unref(currentDepartName) && unref(currentDepartName).length > 0 ? `Chuyển phòng ban (phòng ban hiện tại: ${unref(currentDepartName)})` : props.title;
    }
    // Hiện/ẩn model
    if (unref(isMultiTenant) || unref(isMultiDepart)) {
      visible.value = true;
    }
  }
  /**
   * Tải thông tin phòng ban
   */
  async function loadDepartList() {
    const result = await getUserDeparts();
    // update-begin--author:liaozhiyang---date:20260812---for:【LHZP-1359】 Phòng ban và tenant, bên nào có dữ liệu cũng cần hiển thị, trừ khi cả hai đều không có dữ liệu thì mới nhắc
    const userDeparts = Array.isArray(result.list) ? result.list.filter((item) => item.orgCategory == '2') : [];
    if (userDeparts.length == 0) {
      return false;
    }
    // update-end--author:liaozhiyang---date:20260812---for:【LHZP-1359】 Phòng ban và tenant, bên nào có dữ liệu cũng cần hiển thị, trừ khi cả hai đều không có dữ liệu thì mới nhắc
    let currentDepart = userDeparts.filter((item) => item.orgCode == result.orgCode);
    departList.value = userDeparts;
    // Giải thích logic mã: JHHB-790 Phòng ban người dùng thay đổi, sẽ xuất hiện trường hợp này (vì trước đây cài đặt ở đây chỉ chuyển phòng ban, đã lọc thông tin công ty và vị trí)
    const hasCurrentDepart = userDeparts.some(item => item.orgCode == result.orgCode);
    departSelected.value = hasCurrentDepart && currentDepart && currentDepart.length > 0 ? result.orgCode : '';
    currentDepartName.value = currentDepart && currentDepart.length > 0 ? currentDepart[0].departName : '';
    isMultiDepart.value = true;
    return true;
  }
  /**
   * Tải thông tin tenant
   */
  async function loadTenantList() {
    const result = await getUserTenants();
    if (!result.list || result.list.length == 0) {
      return false;
    }
    let tenantId = userStore.getTenant;
    let currentTenant = result.list.filter((item) => item.id == tenantId);
    currentTenantName.value = currentTenant && currentTenant.length > 0 ? currentTenant[0].name : '';
    tenantList.value = result.list;
    tenantSelected.value = tenantId;
    isMultiTenant.value = true;
    return true;
  }

  /**
   * Dữ liệu gửi
   */
  async function handleSubmit() {
    if (unref(isMultiTenant) && unref(tenantSelected)==null) {
      validate_status.value = 'error';
      return false;
    }
    if (unref(isMultiDepart) && unref(departList).length > 0 && !unref(departSelected)) {
      validate_status1.value = 'error';
      return false;
    }
    departResolve()
      .then(() => {
        if (unref(isMultiTenant)) {
          userStore.setTenant(unref(tenantSelected));
        }
        createMessage.success('Chuyển đổi thành công');
        
        // Sau khi chuyển tenant cần làm mới trang chủ
        window.location.reload();
      })
      .catch((e) => {
        console.log('Lựa chọn đăng nhập gặp vấn đề', e);
      })
      .finally(() => {
        if (unref(isMultiTenant)) {
          userStore.setTenant(unref(tenantSelected));
        }
        close();
      });
  }
  /**
   * Chuyển lựa chọn phòng ban
   */
  function departResolve() {
    return new Promise(async (resolve, reject) => {
      const hasDepart = unref(isMultiDepart) && unref(departList).length > 0;
      const hasTenant = unref(isMultiTenant);
      if (!hasDepart && !hasTenant) {
        resolve();
        return;
      }
      const result = await selectDepart({
        username: userStore.getUserInfo.username,
        orgCode: hasDepart ? unref(departSelected) : undefined,
        loginTenantId: hasTenant ? unref(tenantSelected) : undefined,
      });
      if (result.userInfo) {
        userStore.setUserInfo(result.userInfo);
        resolve();
      } else {
        requestFailed(result);
        userStore.logout();
        reject();
      }
    });
  }
  /**
   * Xử lý yêu cầu thất bại
   */
  function requestFailed(err) {
    notification.error({
      message: 'Đăng nhập thất bại',
      description: ((err.response || {}).data || {}).message || err.message || 'Yêu cầu gặp lỗi, vui lòng thử lại sau',
      duration: 4,
    });
  }
  /**
   * Đóng model
   */
  function close() {
    departClear();
  }

  /**
   * Dữ liệu khởi tạo
   */
  function departClear() {
    currTitle.value = '';

    isMultiTenant.value = false;
    currentTenantName.value = '';
    tenantSelected.value = '';
    tenantList.value = [];
    validate_status.value = '';

    isMultiDepart.value = false;
    currentDepartName.value = '';
    departSelected.value = '';
    departList.value = [];
    validate_status1.value = '';

    visible.value = false;
  }

  /**
   * Lắng nghe username
   */
  watch(
    () => props.username,
    (value) => {
      value && loadDepartList();
    }
  );

  defineExpose({
    show,
  });
</script>
<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 20px;
  }

  .valid-error .ant-select-selection__placeholder {
    color: #f5222d;
  }
</style>
