<template>
  <div>
    <!--Khu vực truy vấn tùy chỉnh-->
    <div class="jeecg-basic-table-form-container" @keyup.enter="searchQuery" v-if="customSearch">
      <a-form ref="formRef" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :lg="8">
            <a-form-item label="Tên người dùng">
              <a-input placeholder="Vui lòng nhập tên để tìm kiếm mờ" v-model:value="queryParam.name"></a-input>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Tuổi">
              <a-input placeholder="Tuổi tối thiểu" type="ge" v-model:value="queryParam.age_begin" style="width: calc(50% - 15px)"></a-input>
              <span>~</span>
              <a-input placeholder="Tuổi tối đa" type="le" v-model:value="queryParam.age_end" style="width: calc(50% - 15px)"></a-input>
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :lg="8">
              <a-form-item label="Giới tính">
                <JDictSelectTag v-model:value="queryParam.sex" placeholder="Vui lòng chọn giới tính" dictCode="sex" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="Chọn người dùng">
                <JDictSelectTag v-model:value="queryParam.id" placeholder="Vui lòng chọn người dùng" dictCode="demo,name,id" />
              </a-form-item>
            </a-col>
          </template>
          <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
            <a-col :lg="6">
              <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Tìm kiếm</a-button>
              <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">Đặt lại</a-button>
              <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                {{ toggleSearchStatus ? 'Thu gọn' : 'Mở rộng' }}
                <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
              </a>
            </a-col>
          </span>
        </a-row>
      </a-form>
    </div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :class="{ 'p-4': customSearch }">
      <template #form-age="{ model, field }">
        <a-input placeholder="Tuổi tối thiểu" type="ge" v-model:value="min" style="width: calc(50% - 15px)" @change="ageChange(model, field)"></a-input>
        <span>~</span>
        <a-input placeholder="Tuổi tối đa" type="le" v-model:value="max" style="width: calc(50% - 15px)" @change="ageChange(model, field)"></a-input>
      </template>
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">Thêm mới</a-button>
        <a-upload name="file" :showUploadList="false" :customRequest="(file) => handleImportXls(file, getImportUrl, reload)">
          <a-button preIcon="ant-design:import-outlined" type="primary">Nhập (import)</a-button>
        </a-upload>
        <a-button preIcon="ant-design:export-outlined" type="primary" @click="handleExportXls('Ví dụ bảng đơn', getExportUrl,exportParams)">Xuất (export)</a-button>
        <a-button preIcon="ant-design:filter" type="primary" @click="">Truy vấn nâng cao</a-button>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="openTab">Mở trang Tab</a-button>
        <a-button preIcon="ant-design:retweet-outlined" type="primary" @click="customSearch = !customSearch">{{
          customSearch ? 'Truy vấn cấu hình biểu mẫu' : 'Truy vấn tùy chỉnh'
        }}</a-button>
        <a-button preIcon="ant-design:import-outlined" type="primary" @click="handleImport">Nhập trong hộp thoại</a-button>

        <super-query :config="superQueryConfig" @search="handleSuperQuery"/>

        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon style="fontsize: 12px" icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <DemoModal @register="registerModal" @success="reload" :isDisabled="isDisabled"/>
    <JImportModal @register="registerModalJimport" :url="getImportUrl" online />
  </div>
</template>
<script lang="ts" setup>
  import { ref, unref, reactive, toRaw, watch,computed } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import DemoModal from './DemoModal.vue';
  import JImportModal from '/@/components/Form/src/jeecg/components/JImportModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { getDemoList, deleteDemo, batchDeleteDemo, getExportUrl, getImportUrl } from './demo.api';
  import { columns, searchFormSchema } from './demo.data';
  import { useGo } from '/@/hooks/web/usePage';
  import { router } from '/@/router';
  import { filterObj } from '/@/utils/common/compUtils';
  
  const go = useGo();
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { openModal }] = useModal();
  const [registerModalJimport, { openModal: openModalJimport }] = useModal();
  const { handleExportXls, handleImportXls } = useMethods();
  const min = ref();
  const max = ref();
  const isDisabled = ref(false);
  
  const [registerTable, { reload, setProps }] = useTable({
    title: 'Ví dụ bảng đơn',
    api: getDemoList,
    columns,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      fieldMapToTime: [['birthday', ['birthday_begin', 'birthday_end'], 'YYYY-MM-DD']],
      fieldMapToNumber: [['age', ['age_begin', 'age_end']]],
      autoAdvancedCol: 2,
      actionColOptions: {
        style: { textAlign: 'left' },
      },
    },
    //Sắp xếp mặc định theo nhiều trường tùy chỉnh
    defSort: [
      {
        column: 'name',
        order: 'desc',
      },
      {
        column: 'sex',
        order: 'asc',
      },
    ],
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    rowKey: 'id',
    actionColumn: {
      width: 180,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  /**
   * Cấu hình cột chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 40,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  function handleImport() {
    openModalJimport(true);
  }

  const exportParams = computed(()=>{
    let paramsForm = {};
    if (checkedKeys.value && checkedKeys.value.length > 0) {
      paramsForm['selections'] = checkedKeys.value.join(',');
    }
    return filterObj(paramsForm)
  })
  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    console.log("checkedKeys------>",checkedKeys)
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    isDisabled.value = false;
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    isDisabled.value = false;
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Trang chi tiết
   */
  function handleDetail(record) {
    isDisabled.value = true;
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteDemo({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteDemo({ ids: checkedKeys.value }, reload);
  }
  /**
   * Sự kiện sửa tuổi
   */
  function ageChange(model, field) {
    model[field] = [unref(min), unref(max)];
  }

  /**
   * Mở trang tab
   */
  function openTab() {
    go(`/comp/jeecg/basic`);
  }
  //-----Truy vấn tùy chỉnh----begin--------
  const formElRef = ref();
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 7 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });
  const toggleSearchStatus = ref(false);
  const customSearch = ref(false);
  const queryParam = reactive({
    name: '',
    age_begin: '',
    age_end: '',
    sex: '',
    id: '',
  });
  watch(customSearch, () => {
    setProps({ useSearchForm: !unref(customSearch) });
  });
  function searchQuery() {
    setProps({ searchInfo: toRaw(queryParam) });
    reload();
  }
  function searchReset() {
    Object.assign(queryParam, { name: '', age_begin: '', age_end: '', sex: '', id: '' });
    reload();
  }
  //Truy vấn tùy chỉnh----end---------

  const superQueryConfig = reactive({
    name:{ title: "Tên", view: "text", type: "string", order: 1 },
    sex:{ title: "Giới tính", view: "list", type: "string", dictCode:'sex', order: 2 },
  });
  
  function handleSuperQuery(params) {
    Object.keys(params).map(k=>{
      queryParam[k] = params[k]
    });
    searchQuery();
  }
</script>
<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
  }
</style>
