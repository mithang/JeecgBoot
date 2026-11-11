<template>
  <BasicModal @register="registerModal" title="JSTăng cường" :width="800" @fullScreen="handleFullScreenChange">
    <a-tabs v-model:activeKey="enhanceType" @change="onChangeType">
      <a-tab-pane key="form" forceRender>
        <template #tab>
          <div class="titleBox">
            <span class="title">form</span>
            <Tooltip>
              <template #title>
                <span>Biểu mẫujsTài liệu tăng cường</span>
              </template>
              <QuestionCircleOutlined @click="handleGo('form')"/>
            </Tooltip>
          </div>
        </template>
        <JCodeEditor
          v-if="!reloading && enhanceType === 'form'"
          ref="formEditorRef"
          v-model:value="enhanceValues.form"
          language="javascript"
          :fullScreen="true"
          :lineNumbers="false"
          :height="codeEditorHeight"
          :language-change="false"
          @change="onCodeChange"
          :keywords="formKeyWords"
          placeholder="Mẹo gợi ý mã:&#10;Đối tượng toàn cục: this.Gọi thuộc tính hoặc phương thức&#10;Phương thức sự kiện:beforeSubmit、loaded、onlChange、getAction、postAction、putAction、deleteAction、deleteAction、openCustomModalv.v."
        />
      </a-tab-pane>
      <a-tab-pane key="list" forceRender>
        <template #tab>
          <div class="titleBox">
            <span class="title">list</span>
            <Tooltip>
              <template #title>
                <span>Danh sáchjsTài liệu tăng cường</span>
              </template>
              <QuestionCircleOutlined @click="handleGo('list')"/>
            </Tooltip>
          </div>
        </template>
        <JCodeEditor
          v-if="!reloading && enhanceType === 'list'"
          ref="listEditorRef"
          v-model:value="enhanceValues.list"
          language="javascript"
          :fullScreen="true"
          :lineNumbers="false"
          :height="codeEditorHeight"
          :language-change="false"
          @change="onCodeChange"
          :keywords="listKeyWords"
          placeholder="Mẹo gợi ý mã:&#10;Đối tượng toàn cục: this.Gọi thuộc tính hoặc phương thức &#10;Phương thức sự kiện:beforeDelete、beforeEdit、getAction、postAction、putAction、deleteAction、deleteAction、openCustomModalv.v."
        />
      </a-tab-pane>
    </a-tabs>

    <template #footer>
      <a-space>
        <a-button @click="onCancel">Đóng</a-button>
        <a-button type="primary" @click="onSubmit">Xác nhận</a-button>
      </a-space>
      <a-space style="float: left">
        <a-button v-if="showHistory" @click="onShowHistory">Xem phiên bản lịch sử</a-button>
        <a-button v-if="aiTestMode" @click="onGenTestData">Tạo dữ liệu kiểm thử</a-button>
      </a-space>
    </template>

    <EnhanceJsHistory @register="registerEnhanceJsHistory" @apply="onApplyHistory" />
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive, defineComponent } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { JCodeEditor } from '/@/components/Form';
  import { useOnlineTest } from '../../hooks/aitest/useOnlineTest';
  import { useEnhanceStore } from '../../store/enhance';
  import { getEnhanceJsByCode, saveEnhanceJs } from './enhance.api';
  import EnhanceJsHistory from './EnhanceJsHistory.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { keywords } from './codeHinting';
  import { QuestionCircleOutlined } from '@ant-design/icons-vue';
  import { Tooltip } from 'ant-design-vue';
  
  type JCodeEditorType = InstanceType<typeof JCodeEditor>;
  export default defineComponent({
    name: 'EnhanceJs',
    components: { BasicModal, JCodeEditor, EnhanceJsHistory, QuestionCircleOutlined, Tooltip },
    emits: ['register'],
    setup() {
      const { createMessage: $message } = useMessage();
      const enhanceStore = useEnhanceStore();
      const formEditorRef = ref<JCodeEditorType>();
      const listEditorRef = ref<JCodeEditorType>();
      const models = reactive<{ form: Recordable; list: Recordable }>({ form: {}, list: {} });
      const enhanceType = ref('list');
      const code = ref('');
      const showHistory = ref(false);
      const codeChange = ref(false);
      const tableName = ref('');
      const enhanceValues = reactive({ form: '', list: '' });
      // mỗitabChỉ lấy giá trị một lượt
      const getFlag = { form: false, list: false };
      const reloading = ref(false);
      const listKeyWords = [ ...keywords.list, ...keywords.common];
      const formKeyWords = [...keywords.form ,...keywords.common];
      const codeEditorHeight = ref('240px');

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        show(data.row);
      });
      const [registerEnhanceJsHistory, enhanceJsHistory] = useModal();

      const { aiTestMode, genEnhanceJsData } = useOnlineTest({}, {}, null);

      function show(row) {
        code.value = row.id;
        codeChange.value = false;
        tableName.value = row.tableName;
        let enhances = enhanceStore.getEnhanceJs(code.value);
        if (enhances?.length > 0) {
          enhanceType.value = enhances[enhances.length - 1].type;
          showHistory.value = true;
        } else {
          showHistory.value = false;
        }
        getFlag.form = false;
        getFlag.list = false;
        if (!enhanceType.value) {
          onChangeType('form');
        } else {
          onChangeType(enhanceType.value);
        }
        // 【VUEN-48】Trình soạn thảo mã, khi sử dụng trong popup, giao diện hiển thị lộn xộn (giải pháp tham khảoJStăng cường, hiển thị trình soạn thảo có độ trễ)
        reloading.value = true;
        setTimeout(() => (reloading.value = false), 150);
      }

      async function onSubmit() {
        await Promise.all([saveEnhanceJsByType('form'), saveEnhanceJsByType('list')]);
        closeModal();
        $message.success('Lưu thành công');
      }

      async function saveEnhanceJsByType(type) {
        let model = models[type];
        let params = {
          cgJs: enhanceValues[type],
          cgJsType: type,
        };
        // Nếu chưa từng lấy được giá trị, hoặc chưa sửa đổi thì không lưu
        if (!getFlag[type] || model.cgJs === params.cgJs) {
          return;
        }
        let isUpdate = !!model.id;
        if (isUpdate) {
          params = Object.assign({}, model, params);
        }
        await saveEnhanceJs(code.value, params, isUpdate);
        enhanceStore.addEnhanceJs({
          code: code.value,
          str: params.cgJs,
          type: params.cgJsType,
          date: new Date().getTime(),
        });
      }

      function onCancel() {
        closeModal();
      }

      async function onChangeType(type) {
        enhanceType.value = type;
        try {
          if (!getFlag[type]) {
            let result = await getEnhanceJsByCode(code.value, type);
            Object.assign(models[type], { id: null }, result);
            enhanceValues[type] = models[type].cgJs;
            getFlag[type] = true;
          }
        } catch (e) {
          console.error(e);
        }
        //update-begin-author:taoyan date:2022-10-18 for: VUEN-2480【Nghiêm trọngbug】online vue3Vấn đề của kiểm thử 8、online jsVấn đề kiểu dáng phần tăng cường
        setTimeout(()=>{
          if(type=='list'){
            listEditorRef.value.refresh();
          }else{
            formEditorRef.value.refresh();
          }
        }, 150);
        //update-end-author:taoyan date:2022-10-18 for: VUEN-2480【Nghiêm trọngbug】online vue3Vấn đề của kiểm thử 8、online jsVấn đề kiểu dáng phần tăng cường
      }

      function onShowHistory() {
        enhanceJsHistory.openModal(true, {
          code: code.value,
          type: enhanceType.value,
        });
      }

      // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-235】Khắc phụcjsPhần tăng cường: năm của phiên bản lịch sử không hiển thị và các ứng dụng mới thêm
      function onApplyHistory({ type, str }) {
        enhanceType.value = type;
        enhanceValues[type] = str;
        codeChange.value = true;
        getFlag[type] = true;
        // Bắt buộc render lại trình soạn thảo, đảm bảo giá trị sau khi áp dụng hiển thị đúng
        reloading.value = true;
        setTimeout(() => (reloading.value = false), 150);
      }
      // update-end--author:liaozhiyang---date:20260713---for：【LHZP-235】Khắc phụcjsPhần tăng cường: năm của phiên bản lịch sử không hiển thị và các ứng dụng mới thêm

      function onCodeChange(value) {
        if (enhanceValues[enhanceType.value] != value) {
          codeChange.value = true;
          enhanceValues[enhanceType.value] = value;
        }
      }

      // Tạo dữ liệu kiểm thử
      function onGenTestData() {
        if (enhanceType.value === 'form') {
          genEnhanceJsData(tableName.value, enhanceType.value, formEditorRef.value!);
        } else {
          genEnhanceJsData(tableName.value, enhanceType.value, listEditorRef.value!);
        }
      }
      // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-8865】onlinejsTự tăng mạnh: tiêu đề cộng thêmiconLiên kết tới địa chỉ tài liệu
      const handleGo = (params) => {
        window.open(`https://help.jeecg.com/java/online/enhanceJs/${params}`);
      };
      // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-8865】onlinejsTự tăng mạnh: tiêu đề cộng thêmiconLiên kết tới địa chỉ tài liệu
      // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1251】onlinePhóng to tối đa phần tăng cường, khung mã sẽ lớn theo
      const handleFullScreenChange = (zoom) => {
        if (zoom) {
          codeEditorHeight.value = `${document.documentElement.clientHeight - 250}px`;
        } else {
          codeEditorHeight.value = `240px`;
        }
      };
      // update-end--author:liaozhiyang---date:20240617---for：【TV360X-1251】onlinePhóng to tối đa phần tăng cường, khung mã sẽ lớn theo
      return {
        formEditorRef,
        listEditorRef,
        reloading,
        enhanceValues,
        enhanceType,
        showHistory,
        aiTestMode,
        tableName,
        genEnhanceJsData,
        onGenTestData,
        onChangeType,
        onCodeChange,
        onShowHistory,
        onApplyHistory,
        onSubmit,
        onCancel,
        registerModal,
        registerEnhanceJsHistory,
        listKeyWords,
        formKeyWords,
        handleGo,
        codeEditorHeight,
        handleFullScreenChange,
      };
    },
  });
</script>

<style lang="less" scoped>
  .titleBox {
    position: relative;
    .title {
      padding: 0 6px;
    }
    .anticon {
      position: absolute;
      top: 3px;
      margin-right: 0;
      color: rgba(0, 0, 0, 0.5);
      &:hover {
        color: @primary-color;
      }
    }
  }
  html[data-theme="dark"] {
    .titleBox { 
      .anticon {
       color: rgba(255, 255, 255, 0.8);
      }
    }
  }
</style>
