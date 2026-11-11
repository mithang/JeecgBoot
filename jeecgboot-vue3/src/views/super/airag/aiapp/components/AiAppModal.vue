<template>
  <div>
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="800px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <template #title>
         <span style="display: flex">
          {{title}}
          <a-tooltip title="Tài liệu ứng dụng AI">
            <a style="color: unset" href="https://help.jeecg.com/aigc/guide/app" target="_blank">
              <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
            </a>
          </a-tooltip>
        </span>
      </template>
      <BasicForm @register="registerForm">
        <template #typeSlot="{ model, field }">
          <a-radio-group v-model:value="model[field]" style="display: flex">
            <a-card
              v-for="item in appTypeOption"
              style="margin-right: 10px; cursor: pointer; width: 100%"
              @click="model[field] = item.value"
              :style="model[field] === item.value ? { borderColor: '#3370ff' } : {}"
            >
              <a-radio :value="item.value">
                <div class="type-title">{{ item.title }}</div>
                <div class="type-desc">{{ item.desc }}</div>
              </a-radio>
            </a-card>
          </a-radio-group>
        </template>
      </BasicForm>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref, computed } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { formSchema } from '../AiApp.data';
  import { initDictOptions } from '@/utils/dict';
  import { saveApp } from '@/views/super/airag/aiapp/AiApp.api';

  export default {
    name: 'AiAppModal',
    components: {
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Lưu hoặc sửa đổi
      const isUpdate = ref<boolean>(false);

      const title = computed<string>(() => isUpdate.value ? 'Sửa đổi ứng dụng' : 'Tạo ứng dụng');

      //Loại app
      const appTypeOption = ref<any>([]);

      //Cấu hình biểu mẫu
      const [registerForm, { validate, resetFields, setFieldsValue }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
      });

      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        await resetFields();
        //update-begin---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Sửa phần head của popup---
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          //Gán giá trị biểu mẫu
          await setFieldsValue({
            ...data.record,
          });
        } else {
          await setFieldsValue({
            type: 'chatSimple',
          })
        }
        //update-end---author:wangshuai---date:2025-03-11---for:【QQYUN-11324】8.Sửa phần head của popup---
        setModalProps({ minHeight: 500, bodyStyle: { padding: '10px' } });
      });

      /**
       * Lưu
       */
      async function handleOk() {
        try {
          let values = await validate();
          setModalProps({ confirmLoading: true });
          let result = await saveApp(values);
          if (result) {
            //Đóng popup
            closeModal();
            //update-begin---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Sửa phần head của popup---
            if(isUpdate.value){
              //Làm mới danh sách
              emit('success', values);
            }else{
              //Làm mới danh sách
              emit('success', result);
            }
            //update-end---author:wangshuai---date:2025-03-11---for: 【QQYUN-11324】8.Sửa phần head của popup---
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      //Khởi tạo loại ứng dụng AI
      initAppTypeOption();

      function initAppTypeOption() {
        initDictOptions('ai_app_type').then((data) => {
          if (data && data.length > 0) {
            for (const datum of data) {
              if (datum.value === 'chatSimple') {
                datum['desc'] = 'Phù hợp cho người mới tạo trợ lý nhỏ';
              } else if (datum.value === 'chatFLow') {
                datum['desc'] = 'Quy trình làm việc phù hợp cho người dùng nâng cao tùy chỉnh trợ lý nhỏ';
              }
            }
          }
          appTypeOption.value = data;
        });
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        title,
        handleOk,
        handleCancel,
        appTypeOption,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }
  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }
</style>
