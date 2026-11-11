import type { FormProps, FormActionType, UseFormReturnType, FormSchema } from '../types/form';
import type { NamePath, ValidateOptions } from 'ant-design-vue/lib/form/interface';
import type { DynamicProps } from '/#/utils';
import { handleRangeValue } from '../utils/formUtils';
import { ref, onUnmounted, unref, nextTick, watch } from 'vue';
import { isProdMode } from '/@/utils/env';
import { error } from '/@/utils/log';
import { getDynamicProps, getValueType, getValueTypeBySchema } from '/@/utils';
export declare type ValidateFields = (nameList?: NamePath[], options?: ValidateOptions) => Promise<Recordable>;

type Props = Partial<DynamicProps<FormProps>>;

export function useForm(props?: Props): UseFormReturnType {
  const formRef = ref<Nullable<FormActionType>>(null);
  const loadedRef = ref<Nullable<boolean>>(false);

  async function getForm() {
    const form = unref(formRef);
    if (!form) {
      error('The form instance has not been obtained, please make sure that the form has been rendered when performing the form operation!');
    }
    await nextTick();
    return form as FormActionType;
  }

  function register(instance: FormActionType) {
    isProdMode() &&
      onUnmounted(() => {
        formRef.value = null;
        loadedRef.value = null;
      });
    if (unref(loadedRef) && isProdMode() && instance === unref(formRef)) return;

    formRef.value = instance;
    loadedRef.value = true;

    watch(
      () => props,
      () => {
        props && instance.setProps(getDynamicProps(props));
      },
      {
        immediate: true,
        deep: true,
      }
    );
  }

  const methods: FormActionType = {
    scrollToField: async (name: NamePath, options?: ScrollOptions | undefined) => {
      const form = await getForm();
      form.scrollToField(name, options);
    },
    setProps: async (formProps: Partial<FormProps>) => {
      const form = await getForm();
      form.setProps(formProps);
    },

    updateSchema: async (data: Partial<FormSchema> | Partial<FormSchema>[]) => {
      const form = await getForm();
      form.updateSchema(data);
    },

    resetSchema: async (data: Partial<FormSchema> | Partial<FormSchema>[]) => {
      const form = await getForm();
      form.resetSchema(data);
    },

    clearValidate: async (name?: string | string[]) => {
      const form = await getForm();
      form.clearValidate(name);
    },

    resetFields: async () => {
      getForm().then(async (form) => {
        await form.resetFields();
      });
    },

    removeSchemaByFiled: async (field: string | string[]) => {
      unref(formRef)?.removeSchemaByFiled(field);
    },

    // TODO promisify
    getFieldsValue: <T>() => {
      // Giải thích logic mã: VUEN-1341[Quy trình]Phương thức mã hóa: khi chỉnh sửa biểu mẫu tại nút quy trình, nhập dữ liệu bị lỗi, bao gồm component người dùng, component phòng ban, Tỉnh/Thành/Quận huyện
      let values = unref(formRef)?.getFieldsValue() as T;
      if(values){
        Object.keys(values).map(key=>{
          if (values[key] instanceof Array) {
            // Giải thích logic mã: 【issues/4330] Kiểm tra nếu là mảng đối tượng thì không nối
            let isObject = typeof (values[key][0] || '') === 'object';
            if (!isObject) {
              values[key] = values[key].join(',');
            }
          }
        });
      }
      return values;
    },

    setFieldsValue: async <T>(values: T) => {
      const form = await getForm();
      form.setFieldsValue<T>(values);
    },

    appendSchemaByField: async (schema: FormSchema, prefixField: string | undefined, first: boolean) => {
      const form = await getForm();
      form.appendSchemaByField(schema, prefixField, first);
    },

    submit: async (): Promise<any> => {
      const form = await getForm();
      return form.submit();
    },

    /**
     * Kiểm tra biểu mẫu và trả về giá trị biểu mẫu
     * @update:Thêm logic chuyển đổi giá trị biểu mẫu
     * @updateBy:zyf
     * @updateDate:2021-09-02
     */
    validate: async (nameList?: NamePath[]): Promise<Recordable> => {
      const form = await getForm();
      let getProps = props || form.getProps;
      let values = form.validate(nameList).then((values) => {
        for (let key in values) {
          if (values[key] instanceof Array) {
            let valueType = getValueTypeBySchema(form.getSchemaByField(key)!, form);
            if (valueType === 'string') {
              values[key] = values[key].join(',');
            }
          }
        }
        //--@updateBy-begin----author:liusq---date:20210916------for:Xử lý thông tin từ điển của sự kiện khu vực------
        return handleRangeValue(getProps, values);
        //--@updateBy-end----author:liusq---date:20210916------for:Xử lý thông tin từ điển của sự kiện khu vực------
      });
      return values;
    },
    validateFields: async (nameList?: NamePath[], options?: ValidateOptions): Promise<Recordable> => {
      const form = await getForm();
      return form.validateFields(nameList, options);
    },
  };

  return [register, methods];
}
