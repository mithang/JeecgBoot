<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    :width="modalWidth"
    :confirmLoading="confirmLoading"
    okText="Bắt đầu tạo"
    cancelText="Hủy"
    @ok="onSubmit"
    @cancel="onCancel"
    :wrapClassName="wrapClassName"
  >
    <a-spin :spinning="confirmLoading">
      <BasicForm @register="registerForm">
        <!-- update-begin--author:liaozhiyang---date:20240612---for：【TV360X-1057】Trang sinh mã: di chuột vào mã thì hiển thị phần thuyết minh -->
        <template #pageCode="{ model, field }">
          <a-radio-group v-model:value="model[field]">
            <a-tooltip placement="top">
              <template #title>
                <span>Đóng gói sâu biểu mẫu, người dùng chỉ cần định nghĩa trườngjsonlà có thể render biểu mẫu, ưu điểm là đơn giản tiện lợi, nhược điểm là khó mở rộng</span>
              </template>
              <a-radio value="vue3">Biểu mẫu được đóng gói(BasicForm)</a-radio>
            </a-tooltip>
            <a-tooltip placement="top">
              <template #title>
                <span>antdbiểu mẫu gốc của, mọi trường đều cần hardcode, nhược điểm là viết mã rườm rà, ưu điểm là dễ mở rộng</span>
              </template>
             <a-radio value="vue3Native" v-if="!(model.jspMode == 'innerTable' || model.jspMode == 'tab')">Biểu mẫu nguyên bản(a-form)</a-radio>
            </a-tooltip>
          </a-radio-group>
        </template>
        <!-- update-end--author:liaozhiyang---date:20240612---for：【TV360X-1057】Trang sinh mã: di chuột vào mã thì hiển thị phần thuyết minh -->
      </BasicForm>
      <a-card v-if="showSubTable" title="Thông tin bảng con" size="small">
        <JVxeTable ref="subTableRef" rowNumber :maxHeight="580" v-bind="subTable" />
      </a-card>
    </a-spin>
  </BasicModal>
  <FileSelectModal @register="registerFileSelectModal" @select="onFileSelect" />
  <!-- Sau khi tạo tệp mã, popup hiển thị -->
  <code-file-list-modal @register="registerCodeFileListModal"></code-file-list-modal>
</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent,h } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  // TODO import { DisabledAuthFilterMixin } from '@/mixins/DisabledAuthFilterMixin'
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useCodeGeneratorFormSchemas } from '../hooks/useSchemas';
  import { underLine2CamelCase } from '/@/utils/common/compUtils';
  import CodeFileListModal from './CodeFileListModal.vue'
  import FileSelectModal from './FileSelectModal.vue';
  import {message} from "ant-design-vue";
  import { useDesign } from '/@/hooks/web/useDesign';
  import { createLocalStorage } from '/@/utils/cache';
  import { useMessage } from '@/hooks/web/useMessage';
  const $ls = createLocalStorage();
  const { notification } = useMessage();
  
  enum Api {
    tableInfo = '/online/cgform/head/tableInfo',
    codeGenerate = '/online/cgform/api/codeGenerate',
    downGenerateCode = '/online/cgform/api/downGenerateCode',
  }

  export default defineComponent({
    name: 'CodeGenerator',
    components: { BasicForm, BasicModal, FileSelectModal, CodeFileListModal },
    emits: ['register'],
    setup(props) {
      const JEECG_ONL_PROJECT_PATH = 'JEECG_ONL_PROJECT_PATH';
      const JEECG_ONL_PROJECT_NAME = 'JEECG_ONL_PROJECT_NAME';
      const single = ref(true);
      const subTableRef = ref<JVxeTableInstance>();
      const modalWidth = computed(() => (single.value ? 800 : 1200));
      const title = ref('Sinh mã');
      const confirmLoading = ref(false);
      const { prefixCls } = useDesign('code-generator-modal');
      const wrapClassName = prefixCls;
      const code = ref('');
      const metaModel = reactive({
        projectPath: '',
        packageStyle: 'service',
        jspMode: '',
        jformType: '1',
        tableName_tmp: '',
        ftlDescription: '',
        entityName: '',
        codeTypes: 'controller,service,dao,mapper,entity,vue',
      });
      const model = reactive<Recordable>({});
      const jspModeOptions = ref<any[]>([]);
      // Cấu hình bảng con
      const subTable = reactive({
        dataSource: [] as Recordable[],
        columns: [
          {
            title: 'tên bảng con',
            key: 'tableName',
            type: JVxeTypes.input,
            disabled: true,
            validateRules: [{ required: true, message: 'Vui lòng nhập${title}' }],
          },
          {
            title: 'Thực thể bảng con',
            key: 'entityName',
            type: JVxeTypes.input,
            validateRules: [{ required: true, message: 'Vui lòng nhập${title}' }],
          },
          {
            title: 'Thuyết minh chức năng',
            key: 'ftlDescription',
            type: JVxeTypes.input,
            validateRules: [{ required: true, message: 'Vui lòng nhập${title}' }],
          },
        ] as JVxeColumn[],
      });
      const showSubTable = computed<boolean>(() => subTable.dataSource.length > 0);

      const { formSchemas } = useCodeGeneratorFormSchemas(
        props,
        {
          onProjectPathChange,
          onProjectPathSearch,
          jspModeOptions,
        },
        single
      );

      // Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        await resetFields();
        // Bảng chính hiện tạiID
        code.value = data.code;
        confirmLoading.value = false;
        subTable.dataSource = [];
        jspModeOptions.value = [];
        getStoreProjectPath();
        Object.assign(model, metaModel);
        loadData();
      });

      async function loadData() {
        let { main, sub, jspModeList, projectPath } = await defHttp.get({
          url: Api.tableInfo,
          params: { code: code.value },
        });
        //update-begin-author:taoyan date:2022-5-17 for: vue3Không có phong cách cổ điển Mặc định làjvxePhong cách
        let jspModeListForVue3: any[] = [];
        for (let mode of jspModeList) {
          const { code, note } = mode;
          if (code == 'many') {
            //Phong cách cổ điển không cần
          } else {
            jspModeListForVue3.push({
              label: note,
              value: code,
            });
          }
        }
        jspModeOptions.value = jspModeListForVue3;
        if (main.isTree == 'Y') {
          model.jspMode = 'tree';
        } else {
          // Lấy phong cách bảng chính
          if (jspModeListForVue3.find(item => item.value === main.themeTemplate)) {
            model.jspMode = main.themeTemplate
          } else {
            // Nếu không tìm thấy, mặc định chọn cái đầu tiên
            model.jspMode = jspModeListForVue3[0].value;
          }
        }
        //update-end-author:taoyan date:2022-5-17 for: vue3Không có phong cách cổ điển Mặc định làjvxePhong cách
        single.value = main.tableType == 1;
        title.value = 'Sinh mã 【' + main.tableName + '】';
        if (!model.projectPath) {
          model.projectPath = projectPath;
          window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, projectPath);
        }
        // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1054】Cache tên gói khi sinh mã
        const projectName = localStorage.getItem(JEECG_ONL_PROJECT_NAME);
        if (projectName) {
          model.entityPackage = projectName;
        }
        // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1054】Cache tên gói khi sinh mã
        model.jformType = main.tableType + '';
        model.tableName_tmp = main.tableName;
        model.ftlDescription = main.tableTxt;

        let entityNameTemp = underLine2CamelCase(main.tableName);
        model.entityName = entityNameTemp.substring(0, 1).toUpperCase() + entityNameTemp.substring(1);
        await nextTick();
        setFieldsValue(model);
        if (sub && sub.length > 0) {
          subTable.dataSource = sub.map((item) => ({
            tableName: item.tableName,
            entityName: getCamelCase(item.tableName),
            ftlDescription: item.tableTxt,
          }));
        }
      }

      // Sau khi tạo tệp mã, popup hiển thị
      const [registerCodeFileListModal, {openModal: openCodeFileListModal}] = useModal();

      async function onSubmit() {
        try {
          const values = await validate();
          let params = Object.assign({}, values, { code: code.value, tableName: values.tableName_tmp });
          if (showSubTable.value) {
            let errMap = await subTableRef.value!.validateTable();
            if (errMap) {
              return;
            }
            params.subList = subTableRef.value!.getTableData();
          }
          confirmLoading.value = true;
        //  let codeList = await defHttp.post({ url: Api.codeGenerate, params });
          let res:any = await codeGen(params);
          //Mở popup danh sách tệp
          openCodeFileListModal(true, {
            codeList: res.codeList,
            pathKey: res.pathKey,
            tableName: values.tableName_tmp
          });

          //-----------------------------------------------------------------------------------------
          // Mã frontend được sinh trực tiếpvue3Hạng mục, sẽ tự động tải làm mới, cho thời hạn hiệu lực10thông tin nhắc nhở tạm thời trong giây
          const hasViewPath = res.codeList.some(
            s => s.includes('src/views/') || s.includes('src\\views\\')
          );
          if(hasViewPath){
            $ls.set(
              'code.genenrate.success.msg',
              `Bảng 【${values.tableName_tmp}】Sinh mã thành công! Mã frontend đã tự động làm mới,JavaBackend cần khởi động lại mới có hiệu lực!`,
              15
            );
          }else{
            notification.success({
              message: `Bảng 【${values.tableName_tmp}】Sinh mã thành công`,
              description: h('div', [
                '1. Mã frontend vui lòng di chuyển tới VUE3Dự án (hạng mục)', h('br'),
                '2. MenuSQLđặt vàojeecg-system-startcủaflywayThư mục', h('br'),
                '3. Java Backend cần khởi động lại mới có hiệu lực'
              ]),
              duration: 5,
            });
          }
          //-----------------------------------------------------------------------------------------
          
          closeModal();
          // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1054】Cache tên gói khi sinh mã
          localStorage.setItem(JEECG_ONL_PROJECT_NAME, values.entityPackage);
          // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1054】Cache tên gói khi sinh mã
        } catch (e) {
          console.error(e);
        } finally {
          confirmLoading.value = false;
        }
      }
      
       function codeGen(params){
        return new Promise((resolve, reject) => {
          defHttp.post({ url: Api.codeGenerate, params }, {isTransformResponse: false}).then(res=>{
            if(res.success){
              let codeList = res.result;
              let pathKey = res.message;
              resolve({
                codeList,
                pathKey
              })
            }else{
              message.error(res.message);
              reject(res.message)
            }
          })
        })
      }

      function onCancel() {
        closeModal();
      }

      // Đăng ký popup chọn tệp
      const [registerFileSelectModal, fileSelectModal] = useModal();

      function onProjectPathSearch() {
        fileSelectModal.openModal(true, {});
      }

      function onFileSelect(url) {
        window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, url);
        setFieldsValue({ projectPath: url });
      }

      function getCamelCase(val) {
        let temp = underLine2CamelCase(val);
        return temp.substring(0, 1).toUpperCase() + temp.substring(1);
      }

      function getStoreProjectPath() {
        let path = window.localStorage.getItem(JEECG_ONL_PROJECT_PATH);
        if (path) {
          metaModel.projectPath = path;
        }
      }

      function onProjectPathChange(e) {
        if (e.target.value) window.localStorage.setItem(JEECG_ONL_PROJECT_PATH, e.target.value);
      }

      return {
        title,
        modalWidth,
        confirmLoading,
        subTable,
        showSubTable,
        onSubmit,
        onCancel,
        onFileSelect,
        registerFileSelectModal,
        subTableRef,
        registerForm,
        registerModal,
        registerCodeFileListModal,
        wrapClassName
      };
    },
  });
</script>

<style lang="less">
@prefix-cls: ~'@{namespace}-code-generator-modal';
.@{prefix-cls} {
  .jeecg-basic-form .ant-input {font-size: 14px;}
}
</style>
