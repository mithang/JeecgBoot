<template>
  <BasicModal v-bind="config" @register="registerModal" :title="currTitle" wrapClassName="loginSelectModal" v-model:visible="visible" :maxHeight="500">
    <a-form ref="formRef" :model="formState" :rules="rules" v-bind="layout" :colon="false" class="loginSelectForm">
      <!--Chọn đa đơn vị thuê-->
      <a-form-item v-if="isMultiTenant" name="tenantId" :validate-status="validate_status">
        <!--labelNội dung-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>Bạn thuộc nhiều đơn vị thuê, vui lòng chọn đơn vị thuê để đăng nhập</span>
            </template>
            <a-avatar style="background-color: #87d068" :size="30"> Đơn vị thuê (tenant) </a-avatar>
          </a-tooltip>
        </template>
        <template #extra v-if="validate_status == 'error'">
          <span style="color: #ed6f6f">Vui lòng chọn đơn vị thuê để đăng nhập</span>
        </template>
        <!--Nội dung thả xuống của đơn vị thuê-->
        <a-select
          v-model:value="formState.tenantId"
          @change="handleTenantChange"
          placeholder="Vui lòng chọn đơn vị thuê để đăng nhập"
          :class="{ 'valid-error': validate_status == 'error' }"
        >
          <template v-for="tenant in tenantList" :key="tenant.id">
            <a-select-option :value="tenant.id">{{ tenant.name }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
      <!--Chọn đa phòng ban-->
      <a-form-item v-if="isMultiDepart" :validate-status="validate_status1" :colon="false">
        <!--labelNội dung-->
        <template #label>
          <a-tooltip placement="topLeft">
            <template #title>
              <span>Bạn thuộc nhiều phòng ban, vui lòng chọn phòng ban để đăng nhập</span>
            </template>
            <a-avatar style="background-color: rgb(104, 208, 203)" :size="30"> Phòng ban </a-avatar>
          </a-tooltip>
        </template>
        <template #extra v-if="validate_status1 == 'error'">
          <span style="color: #ed6f6f">Vui lòng chọn phòng ban để đăng nhập</span>
        </template>
        <!--Nội dung thả xuống của phòng ban-->
        <a-select
          v-model:value="formState.orgCode"
          @change="handleDepartChange"
          placeholder="Vui lòng chọn phòng ban để đăng nhập"
          :class="{ 'valid-error': validate_status1 == 'error' }"
        >
          <template v-for="depart in departList" :key="depart.orgCode">
            <a-select-option :value="depart.orgCode">{{ depart.departName }}</a-select-option>
          </template>
        </a-select>
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="handleSubmit" type="primary">Xác nhận</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, watch, unref, reactive, UnwrapRef } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { defHttp } from '/@/utils/http/axios';
  interface FormState {
    orgCode: string | undefined;
    tenantId: number;
  }
  export default defineComponent({
    name: 'loginSelect',
    components: {
      Avatar,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const userStore = useUserStore();
      const { notification } = useMessage();
      //Cấu hình đơn vị thuê
      const isMultiTenant = ref(false);
      const tenantList = ref([]);
      const validate_status = ref('');
      //Cấu hình phòng ban
      const isMultiDepart = ref(false);
      const departList = ref([]);
      const validate_status1 = ref('');
      //Hiện/ẩn hộp thoại
      const visible = ref(false);
      //Người dùng đăng nhập
      const username = ref('');
      //Biểu mẫu
      const formRef = ref();
      //Thông tin đơn vị thuê và phòng ban đã chọn
      const formState: UnwrapRef<FormState> = reactive({
        orgCode: undefined,
        tenantId: null,
      });

      const config = {
        maskClosable: false,
        closable: false,
        canFullscreen: false,
        width: '500px',
        minHeight: 20,
        maxHeight: 20,
      };
      //Thao tác hộp thoại
      const [registerModal, { closeModal }] = useModalInner();

      //Tiêu đề hiện tại
      const currTitle = computed(() => {
        if (unref(isMultiDepart) && unref(isMultiTenant)) {
          return 'Vui lòng chọn đơn vị thuê và phòng ban';
        } else if (unref(isMultiDepart) && !unref(isMultiTenant)) {
          return 'Vui lòng chọn phòng ban';
        } else if (!unref(isMultiDepart) && unref(isMultiTenant)) {
          return 'Vui lòng chọn đơn vị thuê';
        }
      });

      const rules = ref({
        tenantId: [{ required: unref(isMultiTenant), type: 'number', message: 'Vui lòng chọn đơn vị thuê', trigger: 'change' }],
        orgCode: [{ required: unref(isMultiDepart), message: 'Vui lòng chọn phòng ban', trigger: 'change' }],
      });

      const layout = {
        labelCol: { span: 4 },
        wrapperCol: { span: 18 },
      };
      /**
       * Xử lý tình huống phòng ban
       */
      function bizDepart(loginResult) {
        //Nếu API đăng nhập trả về ID đơn vị thuê đăng nhập lần trước của người dùng thì không cần chọn lại
        if(loginResult.userInfo?.orgCode && loginResult.userInfo?.orgCode!==''){
          isMultiDepart.value = false;
          return;
        }
        
        let multi_depart = loginResult.multi_depart;
        //0:Không có phòng ban 1: một phòng ban 2: nhiều phòng ban
        if (multi_depart == 0) {
          // notification.warn({
          //   message: 'Nhắc nhở',
          //   description: `Bạn chưa thuộc phòng ban nào,Vui lòng xác nhận thông tin tài khoản`,
          //   duration: 3,
          // });
          isMultiDepart.value = false;
        } else if (multi_depart == 2) {
          isMultiDepart.value = true;
          departList.value = loginResult.departs;
        } else {
          isMultiDepart.value = false;
        }
      }

      /**
       * Xử lý tình huống đơn vị thuê
       */
      function bizTenantList(loginResult) {
        //Nếu API đăng nhập trả về ID đơn vị thuê đăng nhập lần trước của người dùng thì không cần chọn lại
        if(loginResult.userInfo?.loginTenantId && loginResult.userInfo?.loginTenantId!==0){
          isMultiTenant.value = false;
          return;
        }
        
        let tenantArr = loginResult.tenantList;
        if (Array.isArray(tenantArr)) {
          if (tenantArr.length === 0) {
            isMultiTenant.value = false;
            userStore.setTenant(formState.tenantId);
          } else if (tenantArr.length === 1) {
            formState.tenantId = tenantArr[0].id;
            isMultiTenant.value = false;
            userStore.setTenant(formState.tenantId);
          } else {
            isMultiTenant.value = true;
            tenantList.value = tenantArr;
          }
        }
      }

      /**
       * Xác nhận thông tin đơn vị thuê và phòng ban đã chọn
       */
      function handleSubmit() {
        if (unref(isMultiTenant) && !formState.tenantId) {
          validate_status.value = 'error';
          return false;
        }
        if (unref(isMultiDepart) && !formState.orgCode) {
          validate_status1.value = 'error';
          return false;
        }
        formRef.value
          .validate()
          .then(() => {
            departResolve()
              .then(() => {
                userStore.setTenant(formState.tenantId);
                emit('success');
              })
              .catch((e) => {
                console.log('Lựa chọn đăng nhập xuất hiện vấn đề', e);
              })
              .finally(() => {
                close();
              });
          })
          .catch((err) => {
            console.log('Kiểm tra biểu mẫu không đạterror', err);
          });
      }
      /**
       *Chuyển đổi lựa chọn phòng ban
       */
      function departResolve() {
        return new Promise((resolve, reject) => {
          if (!unref(isMultiDepart) && !unref(isMultiTenant)) {
            resolve();
          } else {
            let params = { orgCode: formState.orgCode,loginTenantId: formState.tenantId, username: unref(username) };
            defHttp.put({ url: '/sys/selectDepart', params }).then((res) => {
              if (res.userInfo) {
                userStore.setUserInfo(res.userInfo);
                resolve();
              } else {
                requestFailed(res);
                userStore.logout();
                reject();
              }
            });
          }
        });
      }

      /**
       * Xử lý yêu cầu thất bại
       */
      function requestFailed(err) {
        notification.error({
          message: 'Đăng nhập thất bại',
          description: ((err.response || {}).data || {}).message || err.message || 'Yêu cầu xảy ra lỗi, vui lòng thử lại sau',
          duration: 4,
        });
      }

      /**
       * Đóngmodel
       */
      function close() {
        closeModal();
        reset();
      }
      /**
       * Xử lý trước khi hộp thoại mở
       */
      async function show(loginResult) {
        if (loginResult) {
          username.value = userStore.username;
          await reset();
          await bizDepart(loginResult);
          await bizTenantList(loginResult);
          if (!unref(isMultiDepart) && !unref(isMultiTenant)) {
            emit('success', userStore.getUserInfo);
          } else {
            visible.value = true;
          }
        }
        //Sau khi hoàn thành hộp thoại đăng nhập, đặt dấu hiệu đăng nhập thànhfalse
        loginResult.isLogin = false;
        userStore.setLoginInfo(loginResult);
      }

      /**
       *Đặt lại dữ liệu
       */
      function reset() {
        tenantList.value = [];
        validate_status.value = '';

        departList.value = [];
        validate_status1.value = '';
      }

      function handleTenantChange(e) {
        validate_status.value = '';
      }

      function handleDepartChange(e) {
        validate_status1.value = '';
      }

      return {
        registerModal,
        visible,
        tenantList,
        isMultiTenant,
        validate_status,
        isMultiDepart,
        departList,
        validate_status1,
        formState,
        rules,
        layout,
        formRef,
        currTitle,
        config,
        handleTenantChange,
        handleDepartChange,
        show,
        handleSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  .loginSelectForm {
    margin-bottom: -20px;
  }

  .loginSelectModal {
    top: 10px;
  }
</style>
