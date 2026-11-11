<script lang="tsx">
  import { NamePath, ValidateOptions } from 'ant-design-vue/lib/form/interface';
  import type { PropType, Ref } from 'vue';
  import type { FormActionType, FormProps } from '../types/form';
  import type { FormSchema } from '../types/form';
  import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
  import type { TableActionType } from '/@/components/Table';
  import { defineComponent, computed, unref, toRefs } from 'vue';
  import { Form, Col, Divider } from 'ant-design-vue';
  import { componentMap } from '../componentMap';
  import { BasicHelp } from '/@/components/Basic';
  import { isBoolean, isFunction, isNull } from '/@/utils/is';
  import { getSlot } from '/@/utils/helper/tsxHelper';
  import { createPlaceholderMessage, setComponentRuleType } from '../helper';
  import { upperFirst, cloneDeep } from 'lodash-es';
  import { useItemLabelWidth } from '../hooks/useLabelWidth';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { usePermission } from '/@/hooks/web/usePermission';
  import Middleware from './Middleware.vue';
  import { useLocaleStoreWithOut } from '/@/store/modules/locale';

  export default defineComponent({
    name: 'BasicFormItem',
    inheritAttrs: false,
    props: {
      schema: {
        type: Object as PropType<FormSchema>,
        default: () => ({}),
      },
      formProps: {
        type: Object as PropType<FormProps>,
        default: () => ({}),
      },
      allDefaultValues: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      formModel: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      setFormModel: {
        type: Function as PropType<(key: string, value: any) => void>,
        default: null,
      },
      validateFields: {
        type: Function as PropType<(nameList?: NamePath[] | undefined, options?: ValidateOptions) => Promise<any>>,
        default: null,
      },
      tableAction: {
        type: Object as PropType<TableActionType>,
      },
      formActionType: {
        type: Object as PropType<FormActionType>,
      },
      // Giải thích logic mã: 【TV360X-857] Giải quyết việc kích hoạt kiểm chứng ở trạng thái vô hiệu hóa
      clearValidate: {
        type: Function,
        default: null,
      },
      // Giải thích logic mã: 【TV360X-1511] blur không có hiệu lực
      formName: {
        type: String,
        default: '',
      },
      source: {
        type: String,
        default: '',
      },
    },
    setup(props, { slots }) {
      const { t } = useI18n();
      const localeStore = useLocaleStoreWithOut();
      const { schema, formProps } = toRefs(props) as {
        schema: Ref<FormSchema>;
        formProps: Ref<FormProps>;
      };

      const itemLabelWidthProp = useItemLabelWidth(schema, formProps);

      const getValues = computed(() => {
        const { allDefaultValues, formModel, schema } = props;
        const { mergeDynamicData } = props.formProps;
        return {
          field: schema.field,
          model: formModel,
          values: {
            ...mergeDynamicData,
            ...allDefaultValues,
            ...formModel,
          } as Recordable,
          schema: schema,
        };
      });

      const getComponentsProps = computed(() => {
        const { schema, tableAction, formModel, formActionType } = props;
        let { componentProps = {} } = schema;
        if (isFunction(componentProps)) {
          componentProps = componentProps({ schema, tableAction, formModel, formActionType }) ?? {};
        }
        if (schema.component === 'Divider') {
          // Giải thích logic mã: 【QQYUN-6603] Vị trí tiêu đề đường phân cách hiển thị không đúng---
          componentProps = Object.assign({ type: 'horizontal',orientation:'left', plain: true, }, componentProps);
        }
        return componentProps as Recordable;
      });

      const getDisable = computed(() => {
        const { disabled: globDisabled } = props.formProps;
        // Giải thích logic mã: 【TV360X-594]Biểu mẫu vô hiệu hóa toàn cục thì dynamicDisabled không có hiệu lực
        if (!!globDisabled) {
          return globDisabled;
        }
        const { dynamicDisabled } = props.schema;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        let disabled = !!globDisabled || itemDisabled;
        if (isBoolean(dynamicDisabled)) {
          disabled = dynamicDisabled;
        }
        if (isFunction(dynamicDisabled)) {
          disabled = dynamicDisabled(unref(getValues));
        }
        return disabled;
      });

      // Giải thích logic mã: 【QQYUN-8377]formSchema props hỗ trợ sửa động
      const getDynamicPropsValue = computed(() => {
        const { dynamicPropsVal, dynamicPropskey } = props.schema;
        if (dynamicPropskey == null) {
          return null;
        } else {
          const { [dynamicPropskey]: itemValue } = unref(getComponentsProps);
          let value = itemValue;
          if (isFunction(dynamicPropsVal)) {
            value = dynamicPropsVal(unref(getValues));
            return value;
          }
        }
      });

      function getShow(): { isShow: boolean; isIfShow: boolean } {
        const { show, ifShow } = props.schema;
        const { showAdvancedButton } = props.formProps;
        const itemIsAdvanced = showAdvancedButton ? (isBoolean(props.schema.isAdvanced) ? props.schema.isAdvanced : true) : true;

        let isShow = true;
        let isIfShow = true;

        if (isBoolean(show)) {
          isShow = show;
        }
        if (isBoolean(ifShow)) {
          isIfShow = ifShow;
        }
        if (isFunction(show)) {
          isShow = show(unref(getValues));
        }
        if (isFunction(ifShow)) {
          isIfShow = ifShow(unref(getValues));
        }
        isShow = isShow && itemIsAdvanced;
        return { isShow, isIfShow };
      }
      // Giải thích logic mã: 【TV360X-434] validator kiểm chứng thực thi hai lần
      //update-begin---author:liaozhiyang ---date:20260518  for：[issues/9638]Thêm mới prevValueArr, khi giá trị thay đổi thì bắt buộc thực thi lại validator, tránh việc tải lên/Sau khi nhập, nhắc nhở đỏ bắt buộc vẫn không biến mất-----------
      let vSwitchArr: any = [],
        prevValidatorArr: any = [],
        prevValueArr: any = [];
      const hijackValidator = (rules) => {
        vSwitchArr = [];
        prevValidatorArr = [];
        prevValueArr = [];
        rules.forEach((item, index) => {
          const fn = item.validator;
          vSwitchArr.push(true);
          prevValidatorArr.push(null);
          prevValueArr.push(undefined);
          if (isFunction(fn)) {
            item.validator = (rule, value, callback) => {
              // 100msTrong 100ms không cho phép gọi nhiều lần, trừ khi giá trị thay đổi
              const valueChanged = !Object.is(prevValueArr[index], value);
              if (vSwitchArr[index] || valueChanged) {
                vSwitchArr[index] = false;
                setTimeout(() => {
                  vSwitchArr[index] = true;
                }, 100);
                const result = fn(rule, value, callback);
                prevValidatorArr[index] = result;
                prevValueArr[index] = value;
                return result;
              } else {
                return prevValidatorArr[index];
              }
            };
          }
        });
      };
      //update-end---author:liaozhiyang ---date:20260518  for：[issues/9638]Thêm mới prevValueArr, khi giá trị thay đổi thì bắt buộc thực thi lại validator, tránh việc tải lên/Sau khi nhập, nhắc nhở đỏ bắt buộc vẫn không biến mất-----------
      function handleRules(): ValidationRule[] {
        const { rules: defRules = [], component, rulesMessageJoinLabel, label, dynamicRules, required, auth, field } = props.schema;
        // Giải thích logic mã: 【TV360X-857] Giải quyết việc kích hoạt kiểm chứng ở trạng thái vô hiệu hóa
        const { disabled: globDisabled } = props.formProps;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        if (!!globDisabled || !!itemDisabled) {
          props.clearValidate(field);
          return [];
        }
        // Giải thích logic mã: 【TV360X-842] Trường bắt buộcv-auth, trong trường hợp show bị ẩn thì biểu mẫu không thể gửi
        const { hasPermission } = usePermission();
        const { isShow } = getShow();
        if ((auth && !hasPermission(auth)) || !isShow) {
          return [];
        }
        if (isFunction(dynamicRules)) {
          // Giải thích logic mã: 【issues/1244] Đã đánh dấu bắt buộc, nhưng dấu bắt buộc không hiển thị
          const ruleArr = dynamicRules(unref(getValues)) as ValidationRule[];
          if (required) {
            ruleArr.unshift({ required: true });
          }
          // Giải thích logic mã: 【TV360X-434] validator kiểm chứng thực thi hai lần
          hijackValidator(ruleArr);
          return ruleArr;
        }

        let rules: ValidationRule[] = cloneDeep(defRules) as ValidationRule[];
        const { rulesMessageJoinLabel: globalRulesMessageJoinLabel } = props.formProps;

        const joinLabel = Reflect.has(props.schema, 'rulesMessageJoinLabel') ? rulesMessageJoinLabel : globalRulesMessageJoinLabel;
        const defaultMsg = createPlaceholderMessage(component) + `${joinLabel ? label : ''}`;

        function validator(rule: any, value: any) {
          const msg = rule.message || defaultMsg;
          if (value === undefined || isNull(value)) {
            // Giá trị rỗng
            return Promise.reject(msg);
          } else if (Array.isArray(value) && value.length === 0) {
            // Loại mảng
            return Promise.reject(msg);
          } else if (typeof value === 'string' && value.trim() === '') {
            // Chuỗi rỗng
            return Promise.reject(msg);
          } else if (
            typeof value === 'object' &&
            Reflect.has(value, 'checked') &&
            Reflect.has(value, 'halfChecked') &&
            Array.isArray(value.checked) &&
            Array.isArray(value.halfChecked) &&
            value.checked.length === 0 &&
            value.halfChecked.length === 0
          ) {
            // Component tree chọn không liên kết
            return Promise.reject(msg);
          }
          return Promise.resolve();
        }

        const getRequired = isFunction(required) ? required(unref(getValues)) : required;

        if ((!rules || rules.length === 0) && getRequired) {
          rules = [{ required: getRequired, validator }];
        }

        const requiredRuleIndex: number = rules.findIndex((rule) => Reflect.has(rule, 'required') && !Reflect.has(rule, 'validator'));

        if (requiredRuleIndex !== -1) {
          const rule = rules[requiredRuleIndex];
          const { isShow } = getShow();
          if (!isShow) {
            rule.required = false;
          }
          if (component) {
            //https://github.com/vbenjs/vue-vben-admin/pull/3082 githubVăn bản gốc đã sửa
            /*if (!Reflect.has(rule, 'type')) {
              rule.type = component === 'InputNumber' ? 'number' : 'string';
            }*/

            rule.message = rule.message || defaultMsg;

            if (component.includes('Input') || component.includes('Textarea')) {
              rule.whitespace = true;
            }
            const valueFormat = unref(getComponentsProps)?.valueFormat;
            setComponentRuleType(rule, component, valueFormat);
          }
        }

        // Maximum input length rule check
        const characterInx = rules.findIndex((val) => val.max);
        if (characterInx !== -1 && !rules[characterInx].validator) {
          rules[characterInx].message = rules[characterInx].message || t('component.form.maxTip', [rules[characterInx].max] as Recordable);
        }
        // Giải thích logic mã: 【QQYUN-7495]pattern đổi từ chuỗi thành regex truyền cho antd (vì khi dùng InputNumber phát hiện regex không có hiệu lực)
        rules.forEach((item) => {
          if (typeof item.pattern === 'string') {
            try {
              const reg = new Function('item', `return ${item.pattern}`)(item);
              if (Object.prototype.toString.call(reg) === '[object RegExp]') {
                item.pattern = reg;
              } else {
                item.pattern = new RegExp(item.pattern);
              }
            } catch (error) {
              item.pattern = new RegExp(item.pattern);
            }
          }
        });
        // Giải thích logic mã: 【TV360X-434] validator kiểm chứng thực thi hai lần
        hijackValidator(rules);
        return rules;
      }

      function renderComponent() {
        const { renderComponentContent, component, field, changeEvent = 'change', valueField, componentProps, dynamicRules, rules:defRules = [] } = props.schema;

        const isCheck = component && ['Switch', 'Checkbox'].includes(component);
        // Giải thích logic mã: 【QQYUN-6679] input loại bỏ khoảng trắng
        let isTrim = false;
        if (component === 'Input' && componentProps && componentProps.trim) {
          isTrim = true;
        }
        const eventKey = `on${upperFirst(changeEvent)}`;
        const getRules = (): ValidationRule[] => {
          const dyRules = isFunction(dynamicRules) ? dynamicRules(unref(getValues)) : [];
          return [...dyRules, ...defRules];
        };
        // Giải thích logic mã: 【issues/752]Kiểm chứng biểu mẫu dynamicRules không thể dùng kiểm chứng sau khi mất focus trigger: 'blur'
        const on = {
          [eventKey]: (...args: Nullable<Recordable>[]) => {
            const [e] = args;
            if (propsData[eventKey]) {
              propsData[eventKey](...args);
              // Giải thích logic mã: 【issues/8791] popup của js tăng cườngonlChange()không có hiệu lực
              if (eventKey == 'onPopUpChange') {
                return;
              }
            }
            const target = e ? e.target : null;
            // Giải thích logic mã: 【QQYUN-6679] input loại bỏ khoảng trắng
            let value;
            if (target) {
              if (isCheck) {
                value = target.checked;
              } else {
                value = isTrim ? target.value.trim() : target.value;
              }
            } else {
              value = e;
            }
            props.setFormModel(field, value);
            // Giải thích logic mã: 【TV360X-1511] blur không có hiệu lực
            const findItem = getRules().find((item) => item?.trigger === 'blur');
            if (!findItem) {
              // Giải thích logic mã: 【TV360X-341] Sau khi có giá trị, kiểm chứng bắt buộc không biến mất
              props.validateFields([field]).catch((_) => {});
            }
          },
          // onBlur: () => {
          //   props.validateFields([field], { triggerName: 'blur' }).catch((_) => {});
          // },
        };
        const Comp = componentMap.get(component) as ReturnType<typeof defineComponent>;

        const { autoSetPlaceHolder, size } = props.formProps;
        const propsData: Recordable = {
          allowClear: true,
          getPopupContainer: (trigger: Element) => {

            return trigger?.parentNode;
          },
          size,
          ...unref(getComponentsProps),
          disabled: unref(getDisable),
        };
        // Giải thích logic mã: 【QQYUN-8377]formSchema props hỗ trợ sửa động
        const dynamicPropskey = props.schema.dynamicPropskey;
        if (dynamicPropskey) {
          propsData[dynamicPropskey] = unref(getDynamicPropsValue);
        }

        // const isCreatePlaceholder = !propsData.disabled && autoSetPlaceHolder;
        const isCreatePlaceholder = !!autoSetPlaceHolder;

        // RangePicker placelà một mảng
        if (isCreatePlaceholder && component !== 'RangePicker' && component) {
          //Tự động thiết lậpplaceholder
          // Giải thích logic mã: 【issues/6908]Khi chuyển đổi đa ngôn ngữ không cần làm mới, giá trị trong BasicColumn và FormSchema không thể chuyển đổi bình thường
          let label = isFunction(props.schema.label) ? props.schema.label() : props.schema.label;
          if (localeStore.getLocale === 'en' && !(/^\s/.test(label))) {
            label = ' ' + label;
          }
          propsData.placeholder = unref(getComponentsProps)?.placeholder || createPlaceholderMessage(component) + label;
        }
        propsData.codeField = field;
        propsData.formValues = unref(getValues);

        const bindValue: Recordable = {
          [valueField || (isCheck ? 'checked' : 'value')]: props.formModel[field],
        };

        const compAttr: Recordable = {
          ...propsData,
          ...on,
          ...bindValue,
        };

        if (!renderComponentContent) {
          return <Comp {...compAttr} />;
        }
        const compSlot = isFunction(renderComponentContent)
          ? { ...renderComponentContent(unref(getValues)) }
          : {
              default: () => renderComponentContent,
            };
        return <Comp {...compAttr}>{compSlot}</Comp>;
      }

      /**
       *RenderLabel
       * @updateBy:zyf
       */
      function renderLabelHelpMessage() {
        //labelĐộ rộng hỗ trợ tùy chỉnh
        const { label: itemLabel, helpMessage, helpComponentProps, subLabel, labelLength } = props.schema;
        // Giải thích logic mã: 【issues/6908]Khi chuyển đổi đa ngôn ngữ không cần làm mới, giá trị trong BasicColumn và FormSchema không thể chuyển đổi bình thường
        const label = isFunction(itemLabel) ? itemLabel() : itemLabel;
        let showLabel: string = label + '';
        // Giải thích logic mã: 【TV360X-98]Chữ hiển thị của label phải nhất quán với cấu hình labelLength
        if (labelLength) {
          showLabel = showLabel.substr(0, labelLength);
        }
        const titleObj = { title: label };
        const renderLabel = subLabel ? (
          <span>
            {label} <span class="text-secondary">{subLabel}</span>
          </span>
        ) : labelLength ? (
          <label {...titleObj}>{showLabel}</label>
        ) : (
          label
        );
        const getHelpMessage = isFunction(helpMessage) ? helpMessage(unref(getValues)) : helpMessage;
        if (!getHelpMessage || (Array.isArray(getHelpMessage) && getHelpMessage.length === 0)) {
          return renderLabel;
        }
        return (
          <span>
            {renderLabel}
            <BasicHelp placement="top" class="mx-1" text={getHelpMessage} {...helpComponentProps} />
          </span>
        );
      }

      function renderItem() {
        const { itemProps, slot, render, field, suffix, suffixCompact, component } = props.schema;
        const { labelCol, wrapperCol } = unref(itemLabelWidthProp);
        const { colon } = props.formProps;

        // Giải thích logic mã: itemProps Thuộc tính hỗ trợ dạng hàm
        let getItemProps = itemProps;
        if (typeof getItemProps === 'function') {
          getItemProps = getItemProps(unref(getValues));
        }

        if (component === 'Divider') {
          return (
            <Col span={24}>
              <Divider {...unref(getComponentsProps)}>{renderLabelHelpMessage()}</Divider>
            </Col>
          );
        } else {
          const getContent = () => {
            return slot ? getSlot(slots, slot, unref(getValues)) : render ? render(unref(getValues)) : renderComponent();
          };

          const showSuffix = !!suffix;
          const getSuffix = isFunction(suffix) ? suffix(unref(getValues)) : suffix;
          return (
            <Form.Item
              name={field}
              colon={colon}
              class={{ 'suffix-item': showSuffix, 'suffix-compact': showSuffix && suffixCompact }}
              {...(getItemProps as Recordable)}
              label={renderLabelHelpMessage()}
              rules={handleRules()}
              // Giải thích logic mã: 【issues/1244] Đã đánh dấu bắt buộc, nhưng dấu bắt buộc không hiển thị
              validateFirst = { true }
              labelCol={labelCol}
              wrapperCol={wrapperCol}
            >
              <div style="display:flex">
                {/* author: sunjianlei for: 【VUEN-744] thêm vào đây width: 100%; Vì cần ngăn chiều rộng component vượt quá FormItem */}
                {/* update-begin--author:liaozhiyang---date:20240510---for：【TV360X-719] Mục kiểm chứng biểu mẫu không đạt cuộn vào trong vùng hiển thị */}
                <Middleware formName={props.formName} fieldName={field} source={props.source}>{getContent()}</Middleware>
                {/* update-end--author:liaozhiyang---date:20240510---for：【TV360X-719] Mục kiểm chứng biểu mẫu không đạt cuộn vào trong vùng hiển thị */}
                {showSuffix && <span class="suffix">{getSuffix}</span>}
              </div>
            </Form.Item>
          );
        }
      }

      return () => {
        const { colProps = {}, colSlot, renderColContent, component } = props.schema;
        if (!componentMap.has(component)) {
          return null;
        }

        const { baseColProps = {} } = props.formProps;
        // Giải thích logic mã: 【issues-641] Điều chỉnh cấu hình span của biểu mẫu tìm kiếm bảng không có hiệu lực
        const { getIsMobile } = useAppInject();
        let realColProps;
        realColProps = { ...baseColProps, ...colProps };
        if (colProps['span'] && !unref(getIsMobile)) {
          ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].forEach((name) => delete realColProps[name]);
        }
        const { isIfShow, isShow } = getShow();
        const values = unref(getValues);

        const getContent = () => {
          return colSlot ? getSlot(slots, colSlot, values) : renderColContent ? renderColContent(values) : renderItem();
        };

        return (
          isIfShow && (
            <Col {...realColProps} v-show={isShow}>
              {getContent()}
            </Col>
          )
        );
      };
    },
  });
</script>
