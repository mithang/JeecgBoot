<template>
  <BasicModal v-bind="$attrs" @register="registerModal" @ok="handleSubmit" :title="title" :width="1200" destroyOnClose>
    <BasicForm @register="registerForm" />

    <a-tabs v-model:activeKey="activeKey" animated>
      <a-tab-pane tab="Quy tắc cục bộ" key="1" :forceRender="true">
        <JVxeTable ref="vTable1" toolbar rowNumber dragSort rowSelection :maxHeight="580" :dataSource="dataSource1" :columns="columns1">
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="Quy tắc cục bộ kiểm tra theo thứ tự dựa trên số chữ số bạn nhập" style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
      <a-tab-pane tab="Quy tắc toàn cục" key="2" :forceRender="true">
        <JVxeTable
          ref="vTable2"
          toolbar
          rowNumber
          dragSort
          rowSelection
          :maxHeight="580"
          :dataSource="dataSource2"
          :addSetActive="false"
          :columns="columns2"
        >
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="Quy tắc toàn cục có thể kiểm tra mọi ký tự người dùng nhập; mức ưu tiên của quy tắc toàn cục cao hơn quy tắc cục bộ." style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { computed, ref, unref } from 'vue';
  import { formSchema } from './check.rule.data';
  import { saveCheckRule, updateCheckRule } from './check.rule.api';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { pick } from 'lodash-es';

  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));
  // Khai báoEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const activeKey = ref('1');
  let arr1: any[] = [];
  let dataSource1 = ref(arr1);
  let arr2: any[] = [];
  let dataSource2 = ref(arr2);

  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    activeKey.value = '1';
    dataSource1.value = [];
    dataSource2.value = [];
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });

      let ruleJson = data.record.ruleJson;
      if (ruleJson) {
        let ruleList = JSON.parse(ruleJson);
        // Lọc ra quy tắc toàn cục và quy tắc cục bộ
        let global: any[] = [],
          design: any[] = [],
          priority = '1';
        ruleList.forEach((rule) => {
          if (rule.digits === '*') {
            global.push(Object.assign(rule, { priority }));
          } else {
            priority = '0';
            design.push(rule);
          }
        });
        dataSource1.value = design;
        dataSource2.value = global;
      }
    }
  });

  const vTable1 = ref<JVxeTableInstance>();
  const vTable2 = ref<JVxeTableInstance>();

  // Kiểm chứng bảng, trả về dữ liệu bảng
  function validateMyTable(tableRef, key) {
    return new Promise((resolve, reject) => {
      tableRef.value!.validateTable().then((errMap) => {
        if (errMap) {
          activeKey.value = key;
          reject();
        } else {
          const values = tableRef.value!.getTableData();
          resolve(values);
        }
      });
    });
  }

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    let mainData;
    let globalValues = [];
    let designValues = [];
    validate()
      .then((formValue) => {
        mainData = formValue;
        return validateMyTable(vTable1, '1');
      })
      .then((tableData1: []) => {
        if (tableData1 && tableData1.length > 0) {
          designValues = tableData1;
        }
        return validateMyTable(vTable2, '2');
      })
      .then((tableData2: []) => {
        if (tableData2 && tableData2.length > 0) {
          globalValues = tableData2;
        }
        // Tích hợp dữ liệu của hai bảng con
        let firstGlobal: any[] = [],
          afterGlobal: any[] = [];
        for (let i = 0; i < globalValues.length; i++) {
          let v: any = globalValues[i];
          v.digits = '*';
          if (v.priority === '1') {
            firstGlobal.push(v);
          } else {
            afterGlobal.push(v);
          }
        }
        let concatValues = firstGlobal.concat(designValues).concat(afterGlobal);
        let subValues = concatValues.map((i) => pick(i, 'digits', 'pattern', 'message'));
        // Tạo formData, dùng để truyền vào hậu trường
        let ruleJson = JSON.stringify(subValues);
        let formData = Object.assign({}, mainData, { ruleJson });
        saveOrUpdateFormData(formData);
      })
      .catch(() => {
        setModalProps({ confirmLoading: false });
        console.error('Xác thực không đạt!');
      });
  }

  // Yêu cầu gửi biểu mẫu
  async function saveOrUpdateFormData(formData) {
    try {
      console.log('Dữ liệu gửi biểu mẫu', formData);
      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        await updateCheckRule(formData);
      } else {
        await saveCheckRule(formData);
      }
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Kiểm tra
   * @param cellValue
   * @param callback
   */
  const validatePatternHandler = ({ cellValue }, callback) => {
    try {
      new RegExp(cellValue);
      callback(true);
    } catch (e) {
      callback(false, 'Vui lòng nhập biểu thức chính quy đúng');
    }
  };

  const columns1 = ref<JVxeColumn[]>([
    {
      title: 'Số chữ số',
      key: 'digits',
      type: JVxeTypes.inputNumber,
      minWidth: 180,
      validateRules: [
        { required: true, message: '${title}Không được để trống' },
        { pattern: /^[1-9]\d*$/, message: 'Vui lòng nhập số nguyên dương lớn hơn 0' },
      ],
    },
    {
      title: 'Quy tắc (biểu thức chính quy)',
      key: 'pattern',
      minWidth: 320,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: 'Quy tắc không được để trống' }, { handler: validatePatternHandler }],
    },
    {
      title: 'Văn bản nhắc nhở',
      key: 'message',
      minWidth: 180,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title}Không được để trống' }],
    },
  ]);

  const columns2 = ref<JVxeColumn[]>([
    {
      title: 'Mức ưu tiên',
      key: 'priority',
      type: JVxeTypes.select,
      defaultValue: '1',
      options: [
        { title: 'Ưu tiên chạy', value: '1' },
        { title: 'Chạy lần cuối', value: '0' },
      ],
      validateRules: [],
    },
    {
      title: 'Quy tắc (biểu thức chính quy)',
      key: 'pattern',
      width: '40%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: 'Quy tắc không được để trống' }, { handler: validatePatternHandler }],
    },
    {
      title: 'Văn bản nhắc nhở',
      key: 'message',
      width: '20%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title}Không được để trống' }],
    },
  ]);
</script>
