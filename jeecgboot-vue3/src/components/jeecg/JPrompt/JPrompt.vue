<template>
  <ConfigProvider :locale="getAntdLocale">
    <Modal v-bind="getProps">
      <Spin :spinning="loading">
        <div class="j-prompt-content-body">
          <div v-html="options.content" style="margin-bottom: 8px"></div>
          <BasicForm @register="registerForm">
            <template #customInput="{ model, field }">
              <Input ref="inputRef" v-model:value="model[field]" :placeholder="placeholder" @pressEnter="onSubmit" @input="onChange" />
            </template>
          </BasicForm>
        </div>
      </Spin>
    </Modal>
  </ConfigProvider>
</template>

<script lang="ts">
  import type { JPromptProps } from './typing';
  import type { ModalProps } from '/@/components/Modal';
  import { ref, defineComponent, computed, unref, onMounted, nextTick } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { Modal, Spin, Input, ConfigProvider } from 'ant-design-vue';
  import { useLocale } from '/@/locales/useLocale';

  export default defineComponent({
    name: 'JPrompt',
    components: {
      Modal,
      Spin,
      Input,
      BasicForm,
      ConfigProvider,
    },
    emits: ['register'],
    setup(props, { emit }) {
      const inputRef = ref();
      const { getAntdLocale } = useLocale();
      const visible = ref(false);
      // Hiện có đang tải không
      const loading = ref(false);
      const options = ref<JPromptProps>({});
      const placeholder = computed(() => options.value.placeholder ?? 'Vui lòng nhập nội dung');
      // Đăng ký biểu mẫu
      const [registerForm, { clearValidate, setFieldsValue, validate, updateSchema }] = useForm({
        compact: true,
        wrapperCol: { span: 24 },
        schemas: [
          {
            label: '',
            field: 'input',
            component: 'Input',
            slot: 'customInput',
          },
        ],
        showActionButtonGroup: false,
      });

      // Popup cuối cùngprops
      const getProps = computed(() => {
        let opt = options.value;
        let modalProps: Partial<ModalProps> = {
          width: (opt.width ?? 500) as number,
          title: (opt.title ?? 'prompt') as string,
          open: unref(visible),
          confirmLoading: unref(loading),
        };
        let finalProps: Recordable = {
          ...modalProps,
          ...props,
          ...opt,
          onOk: onSubmit,
          onCancel() {
            if (typeof options.value.onCancel === 'function') {
              options.value.onCancel();
            }
            close();
          },
        };
        return finalProps;
      });

      onMounted(() => {
        emit('register', {
          openModal,
          setLoading,
          getVisible: visible,
        });
      });

      /** Mở popup */
      async function openModal(opt: any) {
        document.body.focus();

        options.value = opt;
        visible.value = true;
        await nextTick();
        await updateSchema({
          field: 'input',
          required: options.value.required,
          rules: options.value.rules,
          dynamicRules: options.value.dynamicRules,
        } as any);
        await setFieldsValue({
          input: options.value.defaultValue ?? '',
        });
        await clearValidate();
        inputRef.value?.focus();
      }

      /** Đóng popup */
      function close() {
        visible.value = false;
      }

      function onChange() {
        validate()
      }

      /** Gửi biểu mẫu */
      async function onSubmit() {
        try {
          const { onOk } = options.value;
          // Kiểm tra biểu mẫu
          let values = await validate();
          setLoading(true);
          if (typeof onOk === 'function') {
            let flag = await onOk(values.input);
            // Chỉ khi trả về false mới ngăn đóng popup
            if (!(flag === false)) {
              close();
            }
          } else {
            close();
          }
        } finally {
          setLoading(false);
        }
      }

      /** Đặt trạng thái tải*/
      function setLoading(flag) {
        loading.value = flag;
      }

      return {
        inputRef,
        getProps,
        loading,
        options,
        placeholder,
        getAntdLocale,
        onChange,
        onSubmit,

        registerForm,
      };
    },
  });
</script>

<style lang="less">
.j-prompt-content-body {
  padding: 20px;

  .ant-form-item-control-input-content {
    & > div {
      width: 100%;

      & > .j-form-item-middleware {
        width: 100%;
      }
    }
  }
}
</style>
