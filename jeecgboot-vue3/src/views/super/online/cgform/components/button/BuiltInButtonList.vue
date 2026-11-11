<template>
  <BasicModal @register="registerModal" title="Nút tích hợp sẵn" :width="1200" @cancel="onCancel">

    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)"/>
      </template>
    </BasicTable>

    <template #footer>
      <a-button @click="onCancel">Đóng</a-button>
    </template>

    <!-- Vùng biểu mẫu -->
    <BasicModal v-bind="formModalProps">
      <a-spin :spinning="formModalProps.confirmLoading">
        <div style="margin-top: 20px;">
          <BasicForm @register="registerForm"/>
        </div>
      </a-spin>
    </BasicModal>
  </BasicModal>
</template>

<script setup lang="ts">
import {computed, nextTick, reactive, ref} from 'vue';
import {useListPage} from '/@/hooks/system/useListPage';

import {BasicTable, TableAction} from '/@/components/Table';
import {BasicModal, useModal, useModalInner} from '/@/components/Modal';
import {BasicForm, useForm} from '/@/components/Form';
import {builtInList, saveOrUpdate} from './button.api';
import {columns, formSchemas} from './button.data';

const props = defineProps({
  record: {
    type: Object,
    required: false,
    default: () => ({}),
  },
})
const emit = defineEmits(['register']);

const code = computed(() => props.record?.id);
// 1bảng đơn,2bảng chính,3Bảng phụ
const tableType = computed(() => props.record?.tableType);

const isSingleTable = computed(() => tableType.value === 1);

// Tham số, phương thức dùng chung của trang danh sách
const {tableContext} = useListPage({
  tableProps: {
    api: async (params) => {
      const list = await builtInList(code.value, params)
      if (isSingleTable.value) {
        // Lọc bao gồm sub_ nút của, cho thấy là nút của bảng phụ, khi là bảng đơn thì không hiển thị
        return list.filter(item => !item.buttonCode.includes('sub_'));
      }
      return list;
    },
    columns: columns.filter(item => !['buttonStyle', 'optType', 'orderNum', 'exp'].includes(item.dataIndex as string)),
    canResize: false,
    useSearchForm: false,
    pagination: false,
  },
});
// Đăng kýtableDữ liệu
const [registerTable, {reload}, {rowSelection}] = tableContext;
// Popup đăng ký
const [registerModal, {closeModal}] = useModalInner(() => reload());

// Đăng ký form Popup
const [registerFormModal, formModal] = useModal();
const isUpdate = ref(false);
const formModalProps = reactive({
  onRegister: registerFormModal,
  title: computed(() => (isUpdate?.value ? 'Chỉnh sửa' : 'Thêm mới')),
  width: 600,
  centered: true,
  confirmLoading: false,
  onOk: onSubmit,
  onCancel: formModal.closeModal,
});
let formRecord = {};

const schemas = [
  ...formSchemas({redoModalHeight: formModal.redoModalHeight}),
].filter((schema) => {
  return ['buttonCode', 'buttonName', 'buttonIcon', 'buttonStatus'].includes(schema.field);
}).map(schema => {
  if ('buttonCode' === schema.field) {
    return {
      ...schema,
      dynamicRules: () => [],
      dynamicDisabled: () => true,
    }
  } else if ('buttonIcon' === schema.field) {
    return {
      ...schema,
      ifShow: () => true,
      componentProps: ({formModel}) => ({
        clearSelect: true,
        iconPrefixSave: false,
        iconPrefix: formModel.buttonCode === 'aigc_mock_data' ? 'mdi' : 'ant-design',
      }),
      // Các nút dưới đây không cho phép thiết lập biểu tượng
      dynamicDisabled: ({values}) => ['bpm', 'edit', 'detail', 'delete'].includes(values.buttonCode),
    }
  }
  return schema
});

// Đăng ký form
const [registerForm, {resetFields, setFieldsValue, validate}] = useForm({
  // update-begin--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
  schemas: schemas,
  // update-end--author:liaozhiyang---date:20240618---for：【TV360X-1306】Nút tùy chỉnh: khi chuyển đổi kiểu nút popup thì đặt lại chiều cao popup
  showActionButtonGroup: false,
});

async function openFormModal(data: Recordable) {
  isUpdate.value = data.isUpdate;
  formRecord = {...(data.record ?? {})};
  formModal.openModal();
  await nextTick();
  await resetFields();
  setFieldsValue(formRecord);
}

// Nút chỉnh sửa
function onEdit(record: Recordable) {
  openFormModal({isUpdate: true, record});
}

function onCancel() {
  closeModal();
}

async function onSubmit() {
  try {
    formModalProps.confirmLoading = true;
    let values = await validate();
    values = Object.assign({cgformHeadId: code.value}, formRecord, values);

    console.log('onSubmit - values :', values)
    const isUpdate = values.id != null

    await saveOrUpdate(values, isUpdate);
    reload();
    formModal.closeModal();
  } finally {
    formModalProps.confirmLoading = false;
  }
}

/**
 * Thanh thao tác
 */
function getTableAction(record: Recordable) {
  return [
    {
      label: 'Chỉnh sửa',
      onClick: () => onEdit(record),
    },
  ];
}


</script>
<style lang="less" scoped></style>
