<template>
  <a-card title="Issue #9840: Kiểm thử truy cập trường mật khẩu người dùng" size="small" style="margin-bottom: 16px">
    <a-alert
      message="Bài kiểm tra này sẽ thử truy vấn qua API từ điển bảng sys_user.password, khi bản sửa có hiệu lực, yêu cầu sẽ bị từ chối."
      type="warning"
      show-icon
    />
    <a-space style="margin-top: 12px">
      <a-button danger :loading="passwordTestLoading" @click="handlePasswordQueryTest">Tìm kiếm trường mật khẩu người dùng</a-button>
      <a-typography-text code>GET /sys/dict/loadDict/sys_user,password,id</a-typography-text>
    </a-space>
    <a-alert
      v-if="passwordTestStatus"
      :message="passwordTestStatus"
      :type="passwordTestAlertType"
      show-icon
      style="margin-top: 12px"
    />
    <a-textarea
      v-if="passwordTestResult"
      :value="passwordTestResult"
      :rows="6"
      readonly
      style="margin-top: 12px; font-family: monospace"
    />
  </a-card>
  <BasicForm
    ref="formElRef"
    :class="'jee-select-demo-form'"
    :labelCol="{ span: 6 }"
    :wrapperCol="{ span: 14 }"
    :showResetButton="false"
    :showSubmitButton="false"
    :schemas="schemas"
    :actionColOptions="{ span: 24 }"
    @submit="handleSubmit"
    @reset="handleReset"
    style="height: 100%"
  >
    <template #jAreaLinkage="{ model, field }">
      <JAreaLinkage v-model:value="model[field]" :showArea="true" :showAll="false" />
    </template>
    <template #jAreaLinkage1="{ model, field }">
      <JAreaLinkage :disabled="isDisabledAuth(['demo.dbarray'])" v-model:value="model[field]" :showArea="true" :showAll="false" />
    </template>
    <template #JPopup="{ model, field }">
      <JPopup v-model:value="model[field]" :formElRef="formElRef" code="report_user" :fieldConfig="[{ source: 'username', target: 'pop1' }]" />
    </template>
    <template #JPopup2="{ model, field }">
      <JPopup
        v-model:value="model[field]"
        :formElRef="formElRef"
        code="withparamreport"
        :param="{ sex: '1' }"
        :fieldConfig="[{ source: 'name', target: 'pop2' }]"
      />
    </template>
    <template #JPopup3="{ model, field }">
      <JPopup
        v-model:value="model[field]"
        :formElRef="formElRef"
        code="report_user"
        :param="{ sex: '1' }"
        :fieldConfig="[{ source: 'realname', target: 'pop3' }]"
      />
    </template>
    <template #JAreaSelect="{ model, field }">
      <JAreaSelect v-model:value="model[field]" />
    </template>
    <template #JCheckbox="{ model, field }">
      <JCheckbox v-model:value="model[field]" dictCode="remindMode" />
    </template>
    <template #JInput="{ model, field }">
      <JInput v-model:value="model[field]" :type="model['jinputtype']" />
    </template>
    <template #dargVerify="{ model, field }">
      <BasicDragVerify v-model:value="model[field]" />
    </template>
    <template #superQuery="{ model, field }">
      <super-query :config="superQueryConfig" @search="(value)=>handleSuperQuery(value, model, field)"/>
    </template>
    <template #superQuery1="{ model, field }">
      <super-query :config="superQueryConfig" @search="(value)=>handleSuperQuery(value, model, field)" :isCustomSave="true" :saveSearchData="saveSearchData" :save="handleSuperQuerySave"/>
    </template>
    <template #tabsSelectUser="{ model, field }">
      <JTabsSelectUser v-model:value="model[field]" ></JTabsSelectUser>
    </template>
  </BasicForm>
</template>
<script lang="ts">
  import { computed, defineComponent, unref, ref } from 'vue';
  import { BasicForm, ApiSelect, JAreaLinkage, JPopup, JAreaSelect, FormActionType, JCheckbox, JInput, JEllipsis } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { optionsListApi } from '/@/api/demo/select';
  import { useDebounceFn } from '@vueuse/core';
  import { schemas } from './jeecgComponents.data';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { BasicDragVerify } from '/@/components/Verify';
  import JTabsSelectUser from '/@/components/jeecg/JTabsSelectUser/index.vue';
  import { defHttp } from '/@/utils/http/axios';
  export default defineComponent({
    components: {
      BasicForm,
      ApiSelect,
      JAreaLinkage,
      JPopup,
      JAreaSelect,
      JCheckbox,
      JInput,
      JEllipsis,
      JTabsSelectUser,
      BasicDragVerify,
    },
    name: 'JeecgComponents',
    setup() {
      const { isDisabledAuth } = usePermission();
      const check = ref(null);
      const formElRef = ref<Nullable<FormActionType>>(null);
      const { createMessage } = useMessage();
      const keyword = ref<string>('');
      const passwordTestLoading = ref(false);
      const passwordTestStatus = ref('');
      const passwordTestAlertType = ref<'success' | 'error' | 'warning'>('warning');
      const passwordTestResult = ref('');
      const submitButtonOptions = ref({
        text: 'Xác nhận',
      });
      const searchParams = computed<Recordable>(() => {
        return { keyword: unref(keyword) };
      });

      function onSearch(value: string) {
        keyword.value = value;
      }

      async function handlePasswordQueryTest() {
        passwordTestLoading.value = true;
        passwordTestStatus.value = '';
        passwordTestResult.value = '';
        try {
          const response = await defHttp.get(
            {
              url: '/sys/dict/loadDict/sys_user,password,id',
              params: { pageNo: 1, pageSize: 10 },
            },
            { isTransformResponse: false },
          );
          passwordTestResult.value = JSON.stringify(response, null, 2);
          if (response?.success) {
            passwordTestStatus.value = 'Chưa chặn: API vẫn có thể trả về trường mật khẩu, tồn tại rủi ro bảo mật';
            passwordTestAlertType.value = 'error';
            createMessage.error('Trường mật khẩu người dùng chưa bị chặn');
          } else {
            passwordTestStatus.value = 'Đã chặn: Máy chủ từ chối truy cập sys_user.password';
            passwordTestAlertType.value = 'success';
            createMessage.success('Chặn trường nhạy cảm đã có hiệu lực');
          }
        } catch (error: any) {
          passwordTestStatus.value = 'Yêu cầu bất thường: Vui lòng căn cứ vào thông tin trả về để xác nhận có phải do chặn trường nhạy cảm hay không';
          passwordTestAlertType.value = 'warning';
          passwordTestResult.value = JSON.stringify(error?.response?.data ?? { message: error?.message ?? String(error) }, null, 2);
        } finally {
          passwordTestLoading.value = false;
        }
      }

      const superQueryConfig = {
        name:{ title: "Tên", view: "text", type: "string", order: 1 },
        birthday:{ title: "Ngày sinh", view: "date", type: "string", order: 2 },
        age:{ title: "Tuổi", view: "number", type: "number", order: 4 },
        sex:{ title: "Giới tính", view: "list", type: "string", dictCode: "sex", order: 5 },
        bpmStatus:{ title: "Trạng thái quy trình", view: "list_multi", type: "string",  dictCode: "bpm_status", order: 6 },
      }
      function handleSuperQuery(value, model, field){
        if(value){
          let str = decodeURI(value.superQueryParams)
          console.log(str)
          model[field] = str
        }
      }
      const saveSearchData = ref([
        {
          content: '[{"field":"age","rule":"eq","val":14}]',
          title: 'Tuổi đậu khấu',
          type: 'and',
        },
        {
          content: '[{"field":"name","rule":"eq","val":"Trương Tam"}]',
          title: 'Quản lý dự án',
          type: 'and',
        },
      ]);
      const handleSuperQuerySave = (data) => {
        // Thông tin sau khi lưu truy vấn nâng cao
        return new Promise<void>((resolve, reject) => {
          // API mô phỏng
          setTimeout(() => {
            if (Math.random() > 0.5) {
              console.log('API thành công~');
              saveSearchData.value = data;
              resolve();
            } else {
              console.log('API thất bại~');
              reject();
            }
          }, 1e3);
        });
      }
      return {
        schemas,
        formElRef,
        isDisabledAuth,
        optionsListApi,
        submitButtonOptions,
        passwordTestLoading,
        passwordTestStatus,
        passwordTestAlertType,
        passwordTestResult,
        handlePasswordQueryTest,
        onSearch: useDebounceFn(onSearch, 300),
        searchParams,
        superQueryConfig,
        handleSuperQuery,
        handleReset: () => {
          keyword.value = '';
        },
        handleSubmit: (values: any) => {
          console.log('values:', values);
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        check,
        handleSuperQuerySave,
        saveSearchData,
      };
    },
  });
</script>
<style lang="less" scoped>
  /**update-begin-author:taoyan date:20220324 for: VUEN-351[vue3] Hiển thị không đầy đủ*/
  .jee-select-demo-form .ant-col-5 {
    flex: 0 0 159px;
    max-width: 159px;
  }
  /**update-end-author:taoyan date:20220324 for: VUEN-351[vue3] Hiển thị không đầy đủ*/
</style>
