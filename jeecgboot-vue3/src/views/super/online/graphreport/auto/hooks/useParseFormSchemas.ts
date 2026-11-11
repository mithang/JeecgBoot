import { nextTick, Ref, h } from 'vue';
import { FormSchema } from '/@/components/Form';
import { InputNumber, Input, DatePicker } from 'ant-design-vue';

type PSchema = Partial<FormSchema>;

export function useParseFormSchemas(chartsData: Ref<any>, showSearchField: Ref<boolean>) {
  // Phân tích điều kiện truy vấn FormSchemas
  async function parseFormSchemas() {
    let { head, items, dictOptions } = chartsData.value;
    if (head.dataType === 'sql') {
      let formSchemas: FormSchema[] = [];
      items.forEach((field) => {
        // Phán đoán có truy vấn hay không
        if (field.searchFlag !== 'Y') return;
        let isRange = field.searchMode === 'group';
        const fieldDictOptions = dictOptions?.[field.fieldName] ?? dictOptions?.[field.dictCode];
        const selectOptions = Array.isArray(fieldDictOptions)
          ? fieldDictOptions.filter(Boolean).map((option) => ({ ...option, label: option.label ?? option.text ?? option.title }))
          : null;
        let schema: PSchema = {};
        let schemas: FormSchema[] = [];
        // Phán đoán có từ điển hay không, component từ điển không thể truy vấn theo phạm vi
        if (field.dictCode && selectOptions) {
          schema.component = 'JDictSelectTag';
          schema.componentProps = {
            options: selectOptions,
          };
        } else if (['Integer', 'Long'].includes(field.fieldType)) {
          // Ô nhập số
          schema.component = 'InputNumber';
          // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
          schema.componentProps = {
            style: { width: '100%' },
          };
          // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
          if (isRange) {
            schema.render = getRangeRender(schemas, field, InputNumber);
          }
        } else if (field.fieldType === 'Date') {
          // Component chọn ngày
          schema.component = 'DatePicker';
          schema.componentProps = {
            format: 'YYYY-MM-DD',
            valueFormat: 'YYYY-MM-DD',
            style: { width: '100%' },
          };
          if (isRange) {
            schema.render = getRangeRender(schemas, field, DatePicker);
          }
        } else {
          // Ô văn bản thông thường
          schema.component = 'Input';
          if (isRange) {
            schema.render = getRangeRender(schemas, field, Input);
          }
        }
        formSchemas = formSchemas
          .concat({
            label: field.fieldTxt,
            field: field.fieldName,
            component: 'Input',
            itemProps: {
              class: { 'range-query': isRange },
            } as any,
            ...schema,
          })
          .concat(schemas);
      });
      showSearchField.value = formSchemas.length > 0;
      await nextTick();
      return formSchemas;
    } else {
      showSearchField.value = false;
      return null;
    }
  }

  return { parseFormSchemas };
}

/**
 * Lấy phương thức render phạm vi
 *
 * @param schemas Tên hiển thị
 * @param fieldItem trường
 * @param component vue Component
 */
function getRangeRender(schemas: FormSchema[], fieldItem, component: any) {
  let { fieldTxt: label, fieldName: beginField } = fieldItem;
  let endField = beginField + '_end';
  // Thêm phần giữ chỗ
  schemas.push({ label: '', field: endField, component: 'Input', show: false });
  return function ({ model }) {
    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
    const commonProps = {
      style: { flex: 1, width: '100%', minWidth: 0 },
      allowClear: true,
    };
    return h('div', { class: 'range-query-inner' }, [
      h(component, {
        ...commonProps,
        value: model[beginField],
        'onUpdate:value': (v) => (model[beginField] = v),
        placeholder: 'Vui lòng nhập bắt đầu' + label,
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
      }),
      h('span', { class: 'range-span' }, '~'),
      h(component, {
        ...commonProps,
        value: model[endField],
        'onUpdate:value': (v) => (model[endField] = v),
        placeholder: 'Vui lòng nhập kết thúc' + label,
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
      }),
    ]);
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
  };
}
