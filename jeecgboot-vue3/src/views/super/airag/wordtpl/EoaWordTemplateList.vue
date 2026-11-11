<template>
  <div>
    <!--Bảng trích dẫn-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tiêu đề table-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'wordtpl:template:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới </a-button>

        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'wordtpl:template:deleteBatch'"
            >Thao tác hàng loạt
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <a-button type="default" v-auth="'wordtpl:template:add'" @click="handleGenResume" preIcon="ant-design:plus-outlined"> Tạo hồ sơ </a-button>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--Slot hiển thị lại trường-->
      <template #bodyCell="{ column, record, index, text }"></template>
    </BasicTable>
    <!-- Khu vực biểu mẫu -->
    <EoaWordTemplateModal @register="registerModal" @success="handleSuccess" />

    <BasicModal
      v-bind="$attrs"
      :footer="null"
      :showOkBtn="false"
      :canFullscreen="false"
      wrapClassName="jee-doc-design-modal no-bottom-scroll"
      :style="{ top: '0', padding: '0' }"
      :defaultFullscreen="true"
      @register="designModal"
      destroyOnClose
    >
      <DocDesign :content="wordDesignContent" :upload-file-url="parseFileUrl" @save="handleDesignSave" @download="handleDesignDownload" />
    </BasicModal>

    <BasicModal
      v-bind="$attrs"
      :canFullscreen="false"
      @register="generateModal"
      title="Kiểm tra tạo mẫu"
      :width="528"
      destroyOnClose
      @ok="handleGenOk"
    >
      <a-form :model="genWordData" :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-item class="field-clos" :label="field" :name="field" v-for="field in tplFieldList">
          <a-input v-model:value="genWordData[field]" style="width:90%" :placeholder="'Vui lòng nhập'+field"></a-input>
        </a-form-item>
      </a-form>
    </BasicModal>

    <BasicModal :loading="resumeLoading" v-bind="$attrs" :canFullscreen="false" @register="resumeModal" title="Tạo hồ sơ" :width="628" destroyOnClose @ok="handleGenResumeOk" @cancel="handleGenResumeCancel">
      <a-form :model="genResumeData" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item class="field-clos" label="Thông tin cơ sở" name="content">
          <a-input v-model:value="genResumeData.content" style="width: 90%" placeholder="Vui lòng nhập thông tin cơ bản" />
        </a-form-item>
        <a-form-item class="field-clos" label="Giới thiệu cá nhân" name="profile">
          <a-textarea rows="8" v-model:value="genResumeData.profile" style="width: 90%" placeholder="Vui lòng nhập giới thiệu cá nhân" />
        </a-form-item>
      </a-form>
    </BasicModal>
  </div>
</template>

<script lang="ts" name="wordtpl-eoaWordTemplate" setup>
  import { ref, reactive, computed, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { BasicModal, useModal } from '/src/components/Modal';
  import { useListPage } from '/src/hooks/system/useListPage';
  import EoaWordTemplateModal from './components/EoaWordTemplateModal.vue';
  import { columns, searchFormSchema, superQuerySchema } from './EoaWordTemplate.data';
  import { list, deleteOne, batchDelete, saveOrUpdate, downloadTpl, parseFileUrl, generateWord, generateResume } from './EoaWordTemplate.api';
  import { useUserStore } from '/src/store/modules/user';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { getDateByPicker } from '/src/utils';
  import DocDesign from '/src/components/wordtpl/DocDesign.vue';
  import { BasicForm } from '@/components/Form';
  //Chọn cá nhân hóa ngày tháng
  const fieldPickers = reactive({});
  const queryParam = reactive<any>({});
  const checkedKeys = ref<Array<string | number>>([]);
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  //Đăng ký model
  const [registerModal, { openModal }] = useModal();
  const [designModal, { openModal: openDesignModal, closeModal: closeDesignModal }] = useModal();
  const [generateModal, { openModal: openGenModal, closeModal: closeGenModal }] = useModal();
  const [resumeModal, { openModal: openResumeModal, closeModal: closeResumeModal }] = useModal();
  //Đăng ký dữ liệu table
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      title: 'Quản lý mẫu word',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        //labelWidth: 120,
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      actionColumn: {
        width: 200,
      },
      beforeFetch: (params) => {
        if (params && fieldPickers) {
          for (let key in fieldPickers) {
            if (params[key]) {
              params[key] = getDateByPicker(params[key], fieldPickers[key]);
            }
          }
        }
        return Object.assign(params, queryParam);
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  const wordDesignContent = reactive({});

  const currentTpl = reactive({});
  const genWordData = reactive({});
  const tplFieldList = reactive([]);
  const defaultGenResumeData = {
    content: 'Zhang San; điện thoại: 186xxxxxxxx; email: zhangsan@ex.com',
    profile: 'Có 13 năm kinh nghiệm phát triển, 8 năm kinh nghiệm quản lý dự án, 6 năm kinh nghiệm kiến trúc hệ thống, từng đảm nhiệm vai trò phụ trách cốt lõi trong nhiều sản phẩm cấp nền tảng, có năng lực xây dựng nền tảng từ 0 đến 1, thúc đẩy phát triển từ 1 đến 100.',
  };
  const genResumeData = reactive({ ...defaultGenResumeData });
  const resumeLoading = ref(false);
  let resumeRequestId = 0;

  // Cấu hình truy vấn nâng cao
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * Sự kiện truy vấn nâng cao
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  /**
   * Thêm mới sự kiện
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * Sự kiện tạo hồ sơ
   */
  function handleGenResume() {
    resumeRequestId++;
    resumeLoading.value = false;
    Object.assign(genResumeData, defaultGenResumeData);
    openResumeModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  function handleDesign(record: Recordable) {
    Object.keys(record).map((k) => {
      wordDesignContent[k] = record[k];
    });
    openDesignModal(true, {
      record,
    });
  }

  /**
   * Chi tiết
   */
  function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  function handleGenWord(record: Recordable) {
    extractTplFields(record);
    if(tplFieldList.length > 0){
      // Nếu có trường cần điền, mở popup tạo mẫu
      currentTpl.id = record.id;
      currentTpl.name = record.name;
      Object.keys(genWordData).forEach((key) => delete genWordData[key]);
      openGenModal(true, {
        record,
      });
      return;
    } else {
      // Nếu không có trường cần điền, tạo trực tiếp word
      const params = {
        templateId: record.id,
        data: {},
      };
      generateWord(record.name + '.docx', params);
    }
  }

  function handleGenOk() {
    const params = {
      templateId: currentTpl.id,
      data: genWordData,
    };
    generateWord(currentTpl.name + '.docx', params);
    closeGenModal();
  }

  function handleGenResumeOk() {
    let params = {
      flowId: '1952634605517447170',
      inputParams: genResumeData,
      responseMode: 'blocking',
    };
    const requestId = ++resumeRequestId;
    resumeLoading.value = true;
    generateResume(params, (resp) => {
      if (requestId !== resumeRequestId) {
        return;
      }
      if(resp && resp.success){
        closeResumeModal();
        resumeLoading.value = false;
        reload();
      }else{
        createMessage.error('Tạo hồ sơ thất bại: ' + resp.message);
        resumeLoading.value = false;
      }
    }).catch(() => {
      if (requestId === resumeRequestId) {
        resumeLoading.value = false;
      }
    });
  }

  function handleGenResumeCancel() {
    resumeRequestId++;
    resumeLoading.value = false;
  }

  function extractTplFields(record) {
    const fields = new Set<string>();
    if (record.main) {
      let mainContent = [];
      // Nếu record.main là string
      if (typeof record.main === 'string') {
        mainContent = JSON.parse(record.main);
      } else {
        mainContent = record.main;
      }
      // Trước tiên nối tất cả value
      let contentStr = '';
      mainContent.forEach((item) => {
        if (typeof item.value === 'string' && (!item.type || item.type === '')) {
          contentStr += item.value;
        }
      });
      // Trích xuất biểu thức chính quy thống nhất
      const matches = contentStr.match(/{{(.*?)}}/g);
      if (matches) {
        matches.forEach((m) => {
          const field = m.replace(/[{}]/g, '');
          fields.add(field);
        });
      }
    }
    tplFieldList.splice(0, tplFieldList.length, ...Array.from(fields));
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  /**
   * Callback thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        auth: 'wordtpl:template:edit',
      },
      {
        label: 'Thiết kế',
        onClick: handleDesign.bind(null, record),
        auth: 'wordtpl:template:design',
      },
    ];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Tạo word',
        onClick: handleGenWord.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'wordtpl:template:delete',
      },
    ];
  }

  /**
   * Sự kiện lưu của trình thiết kế
   * @param content
   */
  function handleDesignSave(content) {
    console.log('Dữ liệu biểu mẫu', content);
    saveOrUpdate(content, true)
      .then((data) => {
        // closeDesignModal();
        reload();
      })
      .catch((err) => {
        createMessage.error('Lưu thất bại: ' + err.message);
      });
  }

  /**
   * Sự kiện tải xuống của trình thiết kế
   * @param content
   */
  function handleDesignDownload(content) {
    downloadTpl(content);
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }

  .jee-doc-design-modal {
    &.no-bottom-scroll {
      .scrollbar__bar {
        &.is-horizontal {
          display: none;
        }
      }
    }
    .ant-modal-header {
      padding: 0 !important;
    }
    .ant-modal-body > .scrollbar {
      padding-top: 0;
    }
    .jeecg-modal-content > .scroll-container {
      padding: 0 !important;
    }
  }
</style>
<style lang="less">
  .jee-doc-design-modal .scroll-container {
    height: 100vh !important;
  }
</style>
