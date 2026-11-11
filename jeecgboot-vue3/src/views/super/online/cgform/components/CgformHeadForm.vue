<template>
  <div class="cgform-fields" :class="{ 'cgform-fields--inline': isInlineLayout }">
    <CgformFieldItem
      v-for="item in baseFields"
      :key="item.field"
      :label="item.label"
      :type="item.type"
      :model-value="(formModel as Recordable)[item.field]"
      :required="item.required"
      :disabled="item.disabled"
      :allow-clear="item.allowClear"
      :options="item.options"
      :status="item.status"
      @update:model-value="(formModel as Recordable)[item.field] = $event"
      @change="item.onChange?.($event)"
    />
    <template v-if="[1, 2].includes(formModel.tableType)">
      <!-- Trường điều kiện không phải bảng phụ (bảng đơn/bảng chính) -->
      <CgformFieldItem
        v-for="item in conditionalFields"
        v-show="item.show !== false"
        :key="item.field"
        :label="item.label"
        :type="item.type"
        :model-value="(formModel as Recordable)[item.field]"
        :required="item.required"
        :disabled="item.disabled"
        :allow-clear="item.allowClear"
        :options="item.options"
        :status="item.status"
        @update:model-value="(formModel as Recordable)[item.field] = $event"
        @change="item.onChange?.($event)"
      />
      <!-- Nút loại không phải bảng phụ -->
      <div v-if="formModel.tableType !== 3" class="cgf-item cgf-item--buttons">
        <a-button preIcon="ant-design:setting" @click="onOpenExtConfig">Cấu hình mở rộng</a-button>
        <a-button type="link" :preIcon="expandStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" @click="expandStatus = !expandStatus">{{
          expandStatus ? 'Thu gọn' : 'Mở rộng'
        }}</a-button>
      </div>
    </template>
    <template v-if="formModel.tableType === 3">
      <!-- Loại bảng phụ mở rộng:row2=Hiển thị ô kiểm+Phong cách biểu mẫu+Loại liên kết+Số thứ tự -->
      <template v-if="expandStatus">
        <CgformFieldItem
          v-for="item in preGroupFields"
          :key="item.field"
          :label="item.label"
          :type="item.type"
          :model-value="(formModel as Recordable)[item.field]"
          :options="item.options"
          @update:model-value="(formModel as Recordable)[item.field] = $event"
        />
        <div class="cgf-item cgf-item--group">
          <div class="cgf-item--group-left">
            <a-radio-group v-model:value="formModel.relationType" :options="relationTypeOptions" @change="onRelationTypeChange" />
            <label class="cgf-label cgf-label--inline">Số thứ tự</label>
            <a-input-number v-model:value="formModel.tabOrderNum" />
          </div>
          <div class="cgf-item--group-right">
            <a-button preIcon="ant-design:setting" :disabled="formModel.relationType === 1" @click="onOpenExtConfig">Cấu hình mở rộng</a-button>
            <a-button type="link" preIcon="ant-design:up-outlined" @click="expandStatus = !expandStatus">Thu gọn</a-button>
          </div>
        </div>
        <CgformFieldItem
          v-for="item in postGroupFields"
          :key="item.field"
          :label="item.label"
          :type="item.type"
          :model-value="(formModel as Recordable)[item.field]"
          :options="item.options"
          @update:model-value="(formModel as Recordable)[item.field] = $event"
        />
      </template>
      <!-- Loại bảng phụ thu gọn:col1+col2Khoảng trắng + col3Toàn bộ nội dung-->
      <template v-else>
        <div class="cgf-spacer"></div>
        <div class="cgf-spacer"></div>
        <div class="cgf-item cgf-item--group">
          <div class="cgf-item--group-left">
            <a-radio-group v-model:value="formModel.relationType" :options="relationTypeOptions" @change="onRelationTypeChange" />
            <label class="cgf-label cgf-label--inline">Số thứ tự</label>
            <a-input-number v-model:value="formModel.tabOrderNum" style="width: 50px" />
          </div>
          <div class="cgf-item--group-right">
            <a-button preIcon="ant-design:setting" :disabled="formModel.relationType === 1" @click="onOpenExtConfig">Cấu hình mở rộng</a-button>
            <a-button type="link" preIcon="ant-design:down-outlined" @click="expandStatus = !expandStatus">Mở rộng</a-button>
          </div>
        </div>
      </template>
    </template>
  </div>
  <ExtendConfigModal @register="registerExtendConfigModal" :parentForm="formActionForModal" @ok="onExtConfigOk" />
</template>

<script setup lang="ts">
  import { computed, reactive, ref, toRaw, inject } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { duplicateValidate } from '/@/utils/helper/validator';
  import ExtendConfigModal from './ExtendConfigModal.vue';
  import CgformFieldItem from './CgformFieldItem.vue';
  import { ExtConfigDefaultJson } from '../cgform.data';
  import { parseExtConfigJson } from '../util/utils';
  import { CgformPageType } from '../types';

  interface FieldItem {
    label: string;
    field: string;
    type: 'input' | 'select' | 'input-number' | 'radio-group';
    required?: boolean;
    disabled?: boolean;
    allowClear?: boolean;
    options?: { label: string; value: any }[];
    onChange?: (value: any) => void;
    show?: boolean;
    status?: '' | 'error';
  }

  const fieldErrors = reactive<Record<string, '' | 'error'>>({
    tableName: '',
    tableTxt: '',
    treeFieldname: '',
  });

  const emit = defineEmits<{
    (e: 'tableTypeChange', value: number): void;
    (e: 'relationTypeChange', value: number): void;
    (e: 'isTreeChange', value: string): void;
    (e: 'extConfigSaved', values: Recordable): void;
  }>();

  const { createMessage: $message } = useMessage();

  const pageType = inject<CgformPageType>('cgformPageType', CgformPageType.normal);

  const expandStatus = ref(false);

  // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-328】Trong popup chỉnh sửa mở từ view, loại bảng và có phải dạng cây hay không đều phải vô hiệu hóa
  const cgformPageType = inject('cgformPageType', CgformPageType.normal);
  const isCopyMode = computed(() => cgformPageType === CgformPageType.copy);
  // update-end--author:liaozhiyang---date:20260713---for：【LHZP-328】Trong popup chỉnh sửa mở từ view, loại bảng và có phải dạng cây hay không đều phải vô hiệu hóa

  const tableTypeOptions = [
    { label: 'bảng đơn', value: 1 },
    { label: 'bảng chính', value: 2 },
    { label: 'Bảng phụ', value: 3 },
  ];
  const relationTypeOptions = [
    { label: 'một-nhiều', value: 0 },
    { label: 'một-một', value: 1 },
  ];
  const isCheckboxOptions = [
    { label: 'Hiển thị', value: 'Y' },
    { label: 'không hiển thị', value: 'N' },
  ];
  const themeTemplateOptions = [
    { label: 'Chủ đề mặc định', value: 'normal' },
    { label: 'ERPChủ đề (theme)(một-nhiều)', value: 'erp' },
    { label: 'Chủ đề bảng con nhúng(một-nhiều)', value: 'innerTable' },
    { label: 'TABChủ đề (theme)(một-nhiều)', value: 'tab' },
  ];
  const formTemplateOptions = [
    { label: 'một cột', value: '1' },
    { label: 'hai cột', value: '2' },
    { label: 'ba cột', value: '3' },
    { label: 'bốn cột', value: '4' },
  ];
  const scrollOptions = [
    { label: 'Có', value: 1 },
    { label: 'Không có', value: 0 },
  ];
  const isPageOptions = [
    { label: 'Hiển thị', value: 'Y' },
    { label: 'không hiển thị', value: 'N' },
  ];
  const isTreeOptions = [
    { label: 'là', value: 'Y' },
    { label: 'Không', value: 'N' },
  ];

  const DEFAULT_FORM_MODEL = {
    id: '',
    tableVersion: null as any,
    tableName: '',
    tableTxt: '',
    tableType: 1,
    relationType: 0,
    tabOrderNum: null as any,
    idSequence: '',
    isCheckbox: 'Y',
    themeTemplate: 'normal',
    formTemplate: '1',
    scroll: 1,
    isPage: 'Y',
    isTree: 'N',
    treeParentIdField: '',
    treeIdField: '',
    treeFieldname: '',
    subTableStr: '',
    extConfigJson: '',
  };
  const formModel = reactive({ ...DEFAULT_FORM_MODEL });

  function onTableTypeChange(value: number) {
    if (value === 1) {
      formModel.themeTemplate = 'normal';
    } else if (value === 3) {
      formModel.isTree = 'N';
      emit('isTreeChange', 'N');
    }
    emit('tableTypeChange', value);
  }

  function onRelationTypeChange(e: any) {
    const value = e?.target?.value ?? e;
    emit('relationTypeChange', value);
  }

  function onIsTreeChange(value: string) {
    emit('isTreeChange', value);
  }

  let _tableNameLastMsg = '';
  function validateTableNameSync(val: string) {
    let msg = '';
    if (!val) {
      fieldErrors.tableName = 'error';
    } else if (/[\u4E00-\u9FA5]/g.test(val)) {
      fieldErrors.tableName = 'error';
      msg = 'Tên bảng không cho phép nhập tiếng Trung';
    } else if (val.length > 50) {
      fieldErrors.tableName = 'error';
      msg = 'Tên bảng dài nhất50ký tự';
    } else {
      fieldErrors.tableName = '';
    }
    if (msg && msg !== _tableNameLastMsg) {
      $message.warning(msg);
    }
    _tableNameLastMsg = msg;
  }

  function validateTableTxtSync(val: string) {
    fieldErrors.tableTxt = !val || val.length > 200 ? 'error' : '';
  }

  // Trường cơ bản (luôn hiển thị)
  const baseFields = computed<FieldItem[]>(() => [
    {
      label: 'Tên bảng',
      field: 'tableName',
      type: 'input',
      required: true,
      disabled: pageType === CgformPageType.copy || !!(formModel.tableVersion && formModel.tableVersion != 1),
      allowClear: true,
      status: fieldErrors.tableName,
      onChange: (e: any) => validateTableNameSync(e?.target?.value ?? e ?? ''),
    },
    {
      label: 'Mô tả bảng',
      field: 'tableTxt',
      type: 'input',
      required: true,
      allowClear: true,
      status: fieldErrors.tableTxt,
      onChange: (e: any) => validateTableTxtSync(e?.target?.value ?? e ?? ''),
    },
    {
      label: 'Loại bảng',
      field: 'tableType',
      type: 'select',
      options: tableTypeOptions,
      onChange: onTableTypeChange,
      disabled: isCopyMode.value,
    },
  ]);

  // Trường bảng phụ
  const subTableField: FieldItem = {
    label: 'Bảng phụ',
    field: 'subTableStr',
    type: 'input',
    disabled: true,
  };

  // Trường loại liên kết
  // const relationTypeField: FieldItem = {
  //   label: 'Loại liên kết',
  //   field: 'relationType',
  //   type: 'radio-group',
  //   options: relationTypeOptions,
  //   onChange: onRelationTypeChange,
  // };

  // Trường số thứ tự
  // const tabOrderNumField: FieldItem = {
  //   label: 'Số thứ tự',
  //   field: 'tabOrderNum',
  //   type: 'input-number',
  // };

  // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm
  function onThemeTemplateChange(value: string) {
    // ERP Danh sách phong cách cố định cần chọn hàng, hộp kiểm ép hiển thị và không thể sửa
    if (value === 'erp') {
      formModel.isCheckbox = 'Y';
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm

  // Trường dùng chung sau khi mở rộng
  const expandCommonFields = computed<FieldItem[]>(() => [
    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm
    { label: 'Ô kiểm (checkbox)', field: 'isCheckbox', type: 'select', options: isCheckboxOptions, disabled: formModel.themeTemplate === 'erp' },
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm
    { label: 'Phong cách biểu mẫu', field: 'formTemplate', type: 'select', options: formTemplateOptions },
    { label: 'Thanh cuộn', field: 'scroll', type: 'select', options: scrollOptions },
    { label: 'Phân trang', field: 'isPage', type: 'select', options: isPageOptions },
  ]);

  // Trường điều kiện, căn cứ theo tableType và expandStatus Thay đổi động
  const conditionalFields = computed<FieldItem[]>(() => {
    const { tableType, isTree } = formModel;
    const expanded = expandStatus.value;

    // Bảng phụ (tableType === 3): hoàn toàn do trong mẫu preGroupFields/postGroupFields Xử lý
    if (tableType === 3) return [];

    // bảng chính (tableType === 2)
    if (tableType === 2) {
      const fields: FieldItem[] = [{ ...subTableField, show: !!formModel.subTableStr }];
      if (expanded) {
        // update-begin--author:liaozhiyang---date:20260804---for【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm
        fields.push(
          ...expandCommonFields.value,
          { label: 'Mẫu giao diện', field: 'themeTemplate', type: 'select', options: themeTemplateOptions, onChange: onThemeTemplateChange }
        );
        // update-end--author:liaozhiyang---date:20260804---for：【LHZP-183】Mẫu giao diện làERPkhi vô hiệu hóa thì cấu hình ô kiểm
      }
      return fields;
    }

    // bảng đơn (tableType === 1)
    if (expanded) {
      const fields: FieldItem[] = [
        ...expandCommonFields.value,
        { label: 'Có phải cây hay không', field: 'isTree', type: 'select', options: isTreeOptions, onChange: onIsTreeChange, disabled: isCopyMode.value },
      ];
      if (isTree === 'Y') {
        fields.push(
          { label: 'Cha của câyID', field: 'treeParentIdField', type: 'input' },
          { label: 'Cột biểu mẫu cây', field: 'treeFieldname', type: 'input', required: true, status: fieldErrors.treeFieldname }
        );
      }
      return fields;
    }

    return [];
  });

  // Bảng phụ mở rộng: loại liên kết+Số thứ tự group trường phía trước (isCheckbox, formTemplate → row2 col1-2）
  const preGroupFields = computed<FieldItem[]>(() => {
    if (formModel.tableType !== 3 || !expandStatus.value) return [];
    return expandCommonFields.value.slice(0, 2);
  });

  // Bảng phụ mở rộng: loại liên kết+Số thứ tự group trường phía sau (scroll, isPage → row3 col1-2）
  const postGroupFields = computed<FieldItem[]>(() => {
    if (formModel.tableType !== 3 || !expandStatus.value) return [];
    return expandCommonFields.value.slice(2);
  });

  // Nút có nằm cùng hàng với trường cơ bản hay không (khi không có trường điều kiện)
  const isInlineLayout = computed(() => {
    if (expandStatus.value) return false;
    if (formModel.tableType === 1) return true;
    if (formModel.tableType === 2 && !formModel.subTableStr) return true;
    return false;
  });

  const FORM_FIELD_KEYS = Object.keys(DEFAULT_FORM_MODEL);

  function resetFields() {
    Object.assign(formModel, DEFAULT_FORM_MODEL);
    fieldErrors.tableName = '';
    fieldErrors.tableTxt = '';
  }

  function setFieldsValue(values: Recordable) {
    // update-begin--author:liaozhiyang---date:20260520---for:【QQYUN-15469】Bảng mới thêm, thời gian tạo là thời gian của biểu mẫu đã mở trước đó
    // Chỉ tiếp nhận các trường trong danh sách trắng, tránh các trường bị chỉnh sửa record ô nhiễm bởi trường dư thừa của formModel
    if (!values) return;
    for (const key of FORM_FIELD_KEYS) {
      if (key in values) {
        (formModel as Recordable)[key] = values[key];
      }
    }
    // update-end--author:liaozhiyang---date:20260520---for:【QQYUN-15469】Bảng mới thêm, thời gian tạo là thời gian của biểu mẫu đã mở trước đó
    // update-begin--author:liaozhiyang---date:20260804---for: mẫu chủ đề làERPkhi vô hiệu hóa thì cấu hình ô kiểm
    if (formModel.themeTemplate === 'erp') {
      formModel.isCheckbox = 'Y';
    }
    // update-end--author:liaozhiyang---date:20260804---for: mẫu chủ đề làERPkhi vô hiệu hóa thì cấu hình ô kiểm
  }

  function getFieldsValue(fields?: string[]) {
    if (fields) {
      return Object.fromEntries(fields.map((f) => [f, (formModel as Recordable)[f]]));
    }
    return { ...toRaw(formModel) };
  }

  async function validate() {
    fieldErrors.tableName = '';
    fieldErrors.tableTxt = '';
    fieldErrors.treeFieldname = '';
    if (!formModel.tableName) {
      fieldErrors.tableName = 'error';
      return Promise.reject('Vui lòng nhập tên bảng');
    }
    if (/[\u4E00-\u9FA5]/g.test(formModel.tableName)) {
      fieldErrors.tableName = 'error';
      return Promise.reject('Tên bảng không cho phép nhập tiếng Trung');
    }
    if (formModel.tableName.length > 50) {
      fieldErrors.tableName = 'error';
      return Promise.reject('Tên bảng dài nhất50ký tự');
    }
    try {
      await duplicateValidate('onl_cgform_head', 'table_name', formModel.tableName, formModel.id);
    } catch (e: any) {
      fieldErrors.tableName = 'error';
      return Promise.reject('Tên bảng đã tồn tại');
    }
    if (!formModel.tableTxt) {
      fieldErrors.tableTxt = 'error';
      return Promise.reject('Vui lòng nhập mô tả bảng');
    }
    if (formModel.tableTxt.length > 200) {
      fieldErrors.tableTxt = 'error';
      return Promise.reject('Mô tả bảng dài nhất200ký tự');
    }
    if (formModel.isTree === 'Y' && formModel.tableType === 1 && !formModel.treeFieldname) {
      fieldErrors.treeFieldname = 'error';
      expandStatus.value = true;
      return Promise.reject('Vui lòng điền cột biểu mẫu cây');
    }
    if (extConfigJson.joinQuery && ['erp'].includes(formModel.themeTemplate)) {
      return Promise.reject('ERPkhông hỗ trợ chức năng truy vấn liên hợp');
    }
    if (extConfigJson.joinQuery && ['innerTable'].includes(formModel.themeTemplate)) {
      return Promise.reject('Bảng con nhúng không hỗ trợ chức năng truy vấn liên hợp');
    }
    return getFieldsValue();
  }

  // Cấu hình mở rộng JSON
  let extConfigJson: Recordable = {};
  const [registerExtendConfigModal, extendConfigModal] = useModal();

  // ExtendConfigModal cái cần parentForm API (giao diện)
  const formActionForModal = {
    getFieldsValue,
    setFieldsValue,
    validateFields: (_fields?: string[]) => Promise.resolve(),
  };

  function initialExtConfigJson(record: Recordable) {
    const parseJSON = parseExtConfigJson(record);
    extConfigJson = Object.assign({}, ExtConfigDefaultJson, parseJSON, {
      isDesForm: record.isDesForm || 'N',
      desFormCode: record.desFormCode || '',
      // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu (dbSource là onl_cgform_head cột độc lập)
      dbSource: record.dbSource || '',
      enableMultiDataSource: record.dbSource ? 1 : 0,
      // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
    });
  }

  function getExtConfigJson() {
    return extConfigJson;
  }

  function onOpenExtConfig() {
    extendConfigModal.openModal(true, { extConfigJson });
  }

  async function onExtConfigOk(values: Recordable) {
    extConfigJson = values;
    emit('extConfigSaved', values);
  }

  defineExpose({ formModel, resetFields, setFieldsValue, getFieldsValue, validate, initialExtConfigJson, getExtConfigJson });
</script>

<style lang="less" scoped>
  .cgform-fields {
    display: flex;
    flex-wrap: wrap;
    padding-top: 5px;
    :deep(.cgf-item) {
      width: 33.333%;
      display: flex;
      align-items: center;
      padding: 4px 8px;
      .cgf-label {
        flex-shrink: 0;
        width: 75px;
        text-align: right;
        padding-right: 8px;
        font-size: 14px;
        .cgf-required {
          color: #ff4d4f;
          margin-right: 2px;
        }
      }
    }
    .cgf-item--buttons {
      flex: 1;
      min-width: 33.333%;
      justify-content: flex-end;
      display: flex;
      align-items: center;
      padding: 4px 8px;
      // Khi bảng phụ thu gọn, cố định độ rộng (không flex-grow）
      &-fixed {
        flex: 0 0 33.333%;
        min-width: unset;
      }
    }
    .cgf-spacer {
      width: 33.333%;
    }
    .cgf-item--group {
      width: 33.333%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 4px 8px;
      padding-left: 34px;
      .cgf-label {
        width: 50px;
      }
      .cgf-item--group-left {
        display: flex;
        align-items: center;
      }
      .cgf-item--group-right {
        display: flex;
        align-items: center;
      }
    }
    // Khi không có trường điều kiện, nút nằm cùng hàng với trường cơ bản (bảng đơn thu gọn / bảng chính không có bảng phụ thì thu gọn)
    &--inline {
      flex-wrap: nowrap;
      :deep(.cgf-item) {
        flex: 1 0 0;
        width: auto;
        min-width: 0;
      }
      .cgf-item--buttons {
        flex: 0 0 auto;
        min-width: unset;
      }
    }
  }
  :deep(.ant-btn) {
    &.ant-btn-link {
      margin-right: -5px;
      padding-right: 5px;
    }
    & > .anticon + span {
      margin-left: 4px;
    }
  }
</style>
