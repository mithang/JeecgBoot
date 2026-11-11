<template>
  <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" wrapClassName="ai-model-modal">
    <div class="modal">
      <div class="header">
        <span class="header-title">
          <span v-if="dataIndex ==='list' || dataIndex ==='add'" :class="dataIndex === 'list' ? '' : 'add-header-title pointer'" @click="goToList">
            Chọn nhà cung cấp
            <a-tooltip title="Tài liệu nhà cung cấp" v-if="dataIndex ==='list'">
              <a style="color: #333333" href="https://help.jeecg.com/aigc/guide/model/#2-%E4%BE%9B%E5%BA%94%E5%95%86%E9%80%89%E6%8B%A9" target="_blank">
                <Icon style="position:relative;left: -2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          <span v-if="dataIndex === 'add'" class="add-header-title"> > </span>
          <span v-if="dataIndex === 'add'" style="color: #1f2329">Thêm {{ providerName }}</span>
        </span>

        <a-select v-if="dataIndex === 'list'" :bordered="false" class="header-select" size="small" v-model:value="modelType" @change="handleChange">
          <a-select-option v-for="item in modelTypeOption" :value="item.value">{{ item.text }}</a-select-option>
        </a-select>
      </div>
      <div class="model-content" v-if="dataIndex === 'list'">
        <a-row :span="24">
          <a-col :xxl="12" :xl="12" :lg="12" :md="12" :sm="12" :xs="24" v-for="item in modelTypeList">
            <a-card class="model-card" @click="handleClick(item)">
              <div class="model-header">
                <div class="flex">
                  <img :src="getImage(item.value)" class="header-img" />
                  <div class="header-text">{{ item.title }}</div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
      <a-tabs v-model:activeKey="activeKey" v-if="dataIndex === 'add' || dataIndex === 'edit'">
        <a-tab-pane :key="1">
          <template #tab>
            <span style="display: flex">
            Thông tin cơ sở
            <a-tooltip title="Tài liệu thông tin cơ bản">
              <a @click.stop style="color: unset" href="https://help.jeecg.com/aigc/guide/model/#31-%E5%A1%AB%E5%86%99%E5%9F%BA%E7%A1%80%E4%BF%A1%E6%81%AF" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          </template>
          <div class="model-content">
            <BasicForm @register="registerForm">
              <template #modelType="{ model, field }">
                <a-select v-model:value="model[field]" @change="handleModelTypeChange" :disabled="modelTypeDisabled">
                  <a-select-option v-for="item in modelTypeAddOption" :value="item">
                    <span v-if="item === 'LLM'">Mô hình ngôn ngữ</span>
                    <span v-else-if="item === 'EMBED'">Mô hình vector</span>
                    <span v-else-if="item === 'IMAGE'">Mô hình hình tượng</span>
                  </a-select-option>
                </a-select>
              </template>

              <template #extraParams="{ model, field }">
                <a-input v-model:value="model[field]" readonly placeholder="Nhấp nút bên phải để chỉnh sửa tham số JSON">
                  <template #suffix>
                    <FullscreenOutlined style="cursor: pointer;" @click="openExtraParamsModal" />
                  </template>
                </a-input>
                <div style="margin-top: 4px; color: #999; font-size: 12px; line-height: 1.5;">
                  Theo từng nhà sản xuất hỗ trợ các tham số sau (các khóa khác bỏ qua):<br/>
                  • Qwen:<code>"image_url":"Địa chỉ hình ảnh"</code>(các mô hình thị giác như qwen-vl-ocr), <code>"incremental_output":true</code>(các mô hình như qwen3.5-plus cần tăng dần dạng streaming), <code>"enable_thinking":true</code>(Công tắc suy nghĩ)<br/>
                  • DeepSeek:<code>"thinking":{"type":"enabled"}</code>(công tắc của các mô hình suy luận hỗn hợp như deepseek-v4-pro), <code>"reasoning_effort":"high"</code>(Cường độ suy nghĩ, "high"/"max")
                  <!--update-begin---author:wangshuai---date:20261012---for：【issues/9884】Vấn đề khi dùng mô hình Volcano Ark, không tắt được chế độ suy nghĩ, thời gian phản hồi quá chậm, mỗi mô hình dường như tham số không giống nhau?-->
                  <br/>• Volcengine Ark:<code>"thinking":{"type":"disabled"}</code>(dòng doubao-seed mặc định bật suy nghĩ sâu, sau khi tắt bằng disabled thì phản hồi nhanh hơn), <code>"reasoning_effort":"minimal"</code>(Cường độ suy nghĩ, "minimal"/"low"/"medium"/"high", minimal là không suy nghĩ)
                  <!--update-end---author:wangshuai---date:20261012---for：【issues/9884】Vấn đề khi dùng mô hình Volcano Ark, không tắt được chế độ suy nghĩ, thời gian phản hồi quá chậm, mỗi mô hình dường như tham số không giống nhau?-->
                </div>
                <a-modal v-model:open="extraParamsVisible" title="Chỉnh sửa tham số bổ sung" width="600px" @ok="saveExtraParams" destroyOnClose>
                  <JCodeEditor v-model:value="extraParamsTemp"  language="javascript" fullScreen height="500px" />
                </a-modal>
              </template>

              <template #modelName="{ model, field }">
                <AutoComplete v-model:value="model[field]" :options="modelNameAddOption" :filter-option="filterOption">
                  <template #option="{ value, label, descr, type }">
                    <a-tooltip placement="right" color="#ffffff" :overlayInnerStyle="{ color:'#646a73' }">
                      <template #title>
                        <div v-html="getTitle(descr)"></div>
                      </template>
                      <div style="display: flex;justify-content: space-between;">
                        <span>{{label}}</span>
                        <div>
                          <a-tag v-if="type && type.split(',').includes('text')" color="#E8D7C3">Văn bản</a-tag>
                          <!--update-begin---author:wangshuai---date:20261013---for：【issues/9879】Menu thả xuống mô hình cơ sở phân biệt mô hình tạo ảnh từ văn bản và mô hình tạo ảnh từ hình ảnh-->
                          <a-tag v-if="type && type.split(',').includes('imageGen')" color="#FFEBD3">Tạo ảnh từ văn bản</a-tag>
                          <a-tag v-if="type && type.split(',').includes('imageEdit')" color="#D9CCF2">Tạo ảnh từ ảnh</a-tag>
                          <!--update-end---author:wangshuai---date:20261013---for：【issues/9879】Menu thả xuống mô hình cơ sở phân biệt mô hình tạo ảnh từ văn bản và mô hình tạo ảnh từ hình ảnh-->
                          <a-tag v-if="type && type.split(',').includes('image')" color="#C3D9DC">Phân tích hình tượng</a-tag>
                          <a-tag v-if="type && type.split(',').includes('vector')" color="#D4E0D8">Vector</a-tag>
                          <a-tag v-if="type && type.split(',').includes('embeddings')" color="#FFEBD3">Nhúng văn bản</a-tag>
                        </div>
                      </div>
                    </a-tooltip>
                  </template>
                </AutoComplete>
              </template>
            </BasicForm>
            <a-alert v-if="!modelActivate" message="Mô hình chưa kích hoạt, vui lòng kích hoạt mô hình hiện tại qua nút「Lưu và kích hoạt」phía dưới" type="warning" show-icon />
          </div>
        </a-tab-pane>
        <a-tab-pane :key="2"  v-if="modelParamsShow">
          <template #tab>
            <span style="display: flex">
            Cấu hình nâng cao
            <a-tooltip title="Tài liệu cấu hình nâng cao">
              <a @click.stop style="color: unset" href="https://help.jeecg.com/aigc/guide/model/#32-%E9%85%8D%E7%BD%AE%E9%AB%98%E7%BA%A7%E5%8F%82%E6%95%B0" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </span>
          </template>
          <AiModelSeniorForm ref="modelParamsRef" :modelParams="modelParams"></AiModelSeniorForm>
        </a-tab-pane>
      </a-tabs>

    </div>
    <template v-if="dataIndex === 'add' || dataIndex === 'edit'" #footer>
      <a-button @click="cancel">Đóng</a-button>
      <a-button @click="test" v-if="modelActivate" :loading="testLoading" type="default">Kiểm tra kết nối</a-button>
      <a-button @click="save" type="primary" ghost="true">Lưu</a-button>
      <a-button @click="test(false)" v-if="!modelActivate" :loading="testLoading" type="primary" >Lưu và kích hoạt</a-button>
    </template>
    <template v-else #footer> </template>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { initDictOptions } from '@/utils/dict';
  import model from './model.json';
  import { AutoComplete } from 'ant-design-vue';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { useForm } from '@/components/Form';
  import { formSchema, imageList } from '../model.data';
  import { editModel, queryById, saveModel, testConn } from '../model.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  const {createMessage: $message, createConfirm} = useMessage();
  import { FullscreenOutlined } from '@ant-design/icons-vue';
  import AiModelSeniorForm from './AiModelSeniorForm.vue';
  import JCodeEditor from '/@/components/Form/src/jeecg/components/JCodeEditor.vue';
  import { cloneDeep } from "lodash-es";
  export default {
    name: 'AddModelModal',
    components: {
      BasicForm,
      BasicModal,
      AiModelSeniorForm,
      AutoComplete,
      JCodeEditor,
      FullscreenOutlined,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Dữ liệu loại ai
      const modelTypeData = ref<any>([]);
      //Hộp thả xuống loại mô hình
      const modelTypeOption = ref<any>([]);
      //Trạng thái vô hiệu hóa loại mô hình
      const modelTypeDisabled = ref<boolean>(false);
      //Loại mô hình
      const modelType = ref<string>('all');
      //Nhà cung cấp mô hình
      const modelTypeList = ref<any>([]);
      //list:trang chọn nhà cung cấp, add thêm và sửa
      const dataIndex = ref<string>('list');
      //Tên nhà cung cấp
      const providerName = ref<string>('');
      //Thêm option của loại mô hình
      const modelTypeAddOption = ref<any>([]);
      //Thêm option của tên mô hình
      const modelNameAddOption = ref<any>([]);
      //Dữ liệu mô hình
      const modelData = ref<any>({});
      //key tương ứng khi chuyển tab
      const activeKey = ref<number>(1);
      //Tham số mô hình
      const modelParams = ref<any>({});
      //Có hiển thị tham số mô hình hay không
      const modelParamsShow = ref<boolean>(false);
      //ref tham số mô hình
      const modelParamsRef = ref();
      //Trạng thái loading của nút kiểm tra
      const testLoading = ref<boolean>(false);
      //Mô hình đã kích hoạt hay chưa
      const modelActivate = ref<boolean>(false);
      //Tham số đặc biệt
      const extraParamsVisible = ref<boolean>(false);
      const extraParamsTemp = ref<string>('');
      //update-begin---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
      //Mô hình đang chỉnh sửa trước đây có cấu hình tham số bổ sung hay không, khi xóa tham số bổ sung và lưu sẽ cần dùng
      const hadExtraParams = ref<boolean>(false);
      //update-end---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------

      /**
       * Mở popup chỉnh sửa tham số đặc biệt
       */
      function openExtraParamsModal() {
        const formVal = getFieldsValue();
        let val = formVal.extraParams || '';
        if (val) {
          try {
            val = JSON.stringify(JSON.parse(val), null, 2);
          } catch (e) {}
        }
        extraParamsTemp.value = val;
        extraParamsVisible.value = true;
      }

      /**
       * Lưu tham số đặc biệt
       */
      function saveExtraParams() {
        const val = extraParamsTemp.value;
        if (val) {
          try {
            JSON.parse(val);
          } catch (e) {
            $message.error('Định dạng JSON không đúng, vui lòng kiểm tra');
            return;
          }
        }
        setFieldsValue({ extraParams: val });
        extraParamsVisible.value = false;
      }

      const getImage = (name) => {
        return imageList.value[name];
      };
      //Sự kiện tìm kiếm văn bản tự động điền
      const filterOption = (input: string, option: any)=>{
        return option.value.toUpperCase().indexOf(input.toUpperCase()) >= 0;
      }

      //Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, getFieldsValue, validate, clearValidate }] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        layout: 'vertical',
        wrapperCol: { span: 24 },
        labelCol: { span: 24 },
      });

      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        activeKey.value = 1;
        modelParamsShow.value = false;
        // 【LHZP-1487】Khi mở popup mới, đặt lại loading của kiểm tra kết nối, tránh trạng thái còn sót lại sau khi yêu cầu chậm trước đó đóng
        testLoading.value = false;
        //update-begin---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
        hadExtraParams.value = false;
        //update-end---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
        if(dataIndex.value !== 'list') {
          //Đặt lại biểu mẫu
          await resetFields();
        }
        setModalProps({ minHeight: 500 });
        if (data.id) {
          dataIndex.value = 'edit';
          let values = await queryById({ id: data.id });
          if (values) {
            if(values.result.credential){
              let credential = JSON.parse(values.result.credential);
              if(credential.secretKey){
                values.result.secretKey = credential.secretKey;
              }
              if(credential.apiKey){
                values.result.apiKey = credential.apiKey;
              }
              if (credential.httpVersionOne) {
                values.result.httpVersionOne = credential.httpVersionOne;
              }
            }
            let provider = values.result.provider;
            let data = model.data.filter((item) => {
              return item.value.includes(provider);
            });
            if (data && data.length > 0) {
              modelTypeAddOption.value = data[0].type;
              modelNameAddOption.value = data[0][values.result.modelType];
            }
            if(values.result.modelType && values.result.modelType === 'LLM'){
              modelParamsShow.value = true;
            }
            if (values.result.activateFlag) {
              modelActivate.value = true;
            }else{
              modelActivate.value = false;
            }
            if(values.result.modelParams){
              let allParams = JSON.parse(values.result.modelParams);
              if (allParams.extraParams) {
                values.result.extraParams = JSON.stringify(allParams.extraParams, null, 2);
                delete allParams.extraParams;
                //update-begin---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
                hadExtraParams.value = true;
                //update-end---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
              }
              modelParams.value = allParams;
            }
            modelTypeDisabled.value = true;
            //Gán giá trị biểu mẫu
            await setFieldsValue({
              ...values.result,
            });
            //Khởi tạo nhà cung cấp mô hình
            initModelProvider();
          }
        } else {
          modelTypeDisabled.value = false;
          modelType.value = 'all';
          //Khởi tạo nhà cung cấp mô hình
          initModelProvider();
          dataIndex.value = 'list';
          modelNameAddOption.value = [];
          modelActivate.value = false;
        }
      });

      //Khởi tạo loại mô hình
      initModelTypeOption();
      
      /**
       * Khởi tạo từ điển loại mô hình
       */
      function initModelTypeOption() {
        initDictOptions('model_type').then((data) => {
          modelTypeOption.value = cloneDeep(data);
          //update-begin---author:wangshuai---date:2025-03-04---for: Giải quyết việc làm mới tab trang một lần là thêm một tùy chọn loại tất cả---
          if(data[0].value != 'all'){
            modelTypeOption.value.unshift({
              text: 'Tất cả các loại',
              value: 'all',
            });
          }
          //update-end---author:wangshuai---date:2025-03-04---for: Giải quyết việc làm mới tab trang một lần là thêm một tùy chọn loại tất cả---
        });
      }

      /**
       * Sự kiện chọn giá trị của hộp thả xuống
       * @param value
       */
      function handleChange(value) {
        if ('all' == value) {
          modelTypeList.value = sortModelProviders(model.data);
          return;
        }
        let data = model.data.filter((item) => {
          return item.type.includes(value);
        });
        modelTypeList.value = sortModelProviders(data);
      }

      /**
       * Hiển thị theo độ hot và thứ tự phân loại nhà cung cấp
       */
      function sortModelProviders(data) {
        return [...data].sort((first, second) => first.sort - second.sort);
      }

      /**
       * Khởi tạo nhà cung cấp mô hình
       */
      function initModelProvider() {
        modelTypeList.value = sortModelProviders(model.data);
      }

      /**
       * Sự kiện nhấp nhà cung cấp
       *
       * @param item
       */
      function handleClick(item) {
        dataIndex.value = 'add';
        modelData.value = item;
        providerName.value = item.title;
        modelTypeAddOption.value = item.type;
        setTimeout(()=>{
          setFieldsValue({ 'provider': item.value, 'baseUrl': item.baseUrl })
        },100)
      }

      /**
       * Lưu
       */
      async function save() {
        try {
          setModalProps({ confirmLoading: true });
          let values = await validate();
          let credential = {
            apiKey: values.apiKey,
            secretKey: values.secretKey,
            httpVersionOne: values.httpVersionOne,
          }
          let params = {};
          if(modelParamsRef.value){
            let seniorParams = modelParamsRef.value.emitChange();
            if(seniorParams){
              params = { ...seniorParams };
            }
          }
          if (values.extraParams) {
            try {
              params.extraParams = JSON.parse(values.extraParams);
            } catch(e) {}
          }
          if (Object.keys(params).length > 0) {
            values.modelParams = JSON.stringify(params);
          }
          //update-begin---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
          if (values.id && !values.extraParams && hadExtraParams.value) {
            if (!modelParamsRef.value) {
              params = { ...modelParams.value };
            }
            values.modelParams = JSON.stringify(params);
          }
          //update-end---author:wangshuai---date:20261012---for：【issues/9884】Xóa tham số bổ sung, lưu không có hiệu lực-----------
          delete values.extraParams;
          if(modelActivate.value){
            values.activateFlag = 1
          }else{
            values.activateFlag = 0;
          }
          values.credential = JSON.stringify(credential);
          //Thêm mới
          if (!values.id) {
            values.provider = modelData.value.value;
            await saveModel(values);
            closeModal();
            emit('success');
          } else {
            await editModel(values);
            closeModal();
            emit('success');
          }
        }catch(e){
          if(e.hasOwnProperty('errorFields')){
            activeKey.value = 1;
          }
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      /**
       * Hủy
       */
      function cancel() {
        dataIndex.value = 'list';
        closeModal();
        emit('success');
      }

      /**
       * Kiểm tra kết nối
       */
      async function test(onlyTest = false) {
        try {
          testLoading.value = true;
          let values = await validate();
          let credential = {
            apiKey: values.apiKey,
            secretKey: values.secretKey,
            httpVersionOne: values.httpVersionOne,
          };
          let params = {};
          if (modelParamsRef.value) {
            //update-begin---author:wangshuai---date:2026-03-20---for:【issues/8】Lưu và kích hoạt mô hình qwen-vl-ocr báo lỗi---
            let seniorParams = modelParamsRef.value.emitChange();
            if (seniorParams) {
              params = { ...seniorParams };
            //update-end---author:wangshuai---date:2026-03-20---for:【issues/8】Lưu và kích hoạt mô hình qwen-vl-ocr báo lỗi---
            }
          }
          if (values.extraParams) {
            try {
              params.extraParams = JSON.parse(values.extraParams);
            } catch(e) {}
          }
          if (Object.keys(params).length > 0) {
            values.modelParams = JSON.stringify(params);
          }
          delete values.extraParams;
          values.credential = JSON.stringify(credential);
          if (!values.provider) {
            values.provider = modelData.value.value;
          }
          //Kiểm tra
          await testConn(values).then(async (result) => {
            if(onlyTest){
              $message.success('Kiểm tra kết nối thành công');
              return true;
            }
            modelActivate.value = true;
            await save();
          });
        } catch (e) {
          if (e.hasOwnProperty('errorFields')) {
            activeKey.value = 1;
          }
        } finally {
          testLoading.value = false;
        }
      }

      /**
       * Sự kiện chọn loại mô hình
       * @param value
       */
      async function handleModelTypeChange(value) {
        await setFieldsValue({ modelName: '' });
        await clearValidate('modelName');
        await setFieldsValue({
          modelName: modelData.value[value+'DefaultValue']
        })
        modelNameAddOption.value = modelData.value[value];
        if(value === 'LLM'){
          modelParamsShow.value = true;
        }else{
          modelParamsShow.value = false;
        }
        if(value === "IMAGE" && modelData.value.baseImageUrl){
          setFieldsValue({ 'baseUrl': modelData.value.baseImageUrl })
        } else if(modelData.value.baseUrl) {
          setFieldsValue({ 'baseUrl': modelData.value.baseUrl })
        }
      }

      /**
       * Chọn nhà cung cấp
       */
      function goToList() {
        if (dataIndex.value === 'add') {
          dataIndex.value = 'list';
        }
      }

      /**
       * Lấy tiêu đề
       * @param title
       */
      function getTitle(title) {
        if(!title){
          return "Tạm thời chưa có nội dung mô tả";
        }
        return title.replaceAll("\n","<br>")
      }
      
      return {
        registerModal,
        modelTypeData,
        modelTypeOption,
        modelType,
        handleChange,
        modelTypeList,
        getImage,
        handleClick,
        dataIndex,
        providerName,
        save,
        cancel,
        registerForm,
        handleModelTypeChange,
        modelTypeAddOption,
        modelNameAddOption,
        goToList,
        modelTypeDisabled,
        activeKey,
        modelParams,
        modelParamsShow,
        modelActivate,
        modelParamsRef,
        filterOption,
        getTitle,
        test,
        testLoading,
        extraParamsVisible,
        extraParamsTemp,
        openExtraParamsModal,
        saveExtraParams,
      };
    },
  };
</script>

<style scoped lang="less">
  .modal {
    padding: 12px 20px 20px 20px;
    .header {
      padding: 0 24px 24px 0;
      display: flex;
      justify-content: space-between;
      .header-title {
        font-size: 16px;
        font-weight: bold;
      }
      .header-select {
        margin-right: 10px;
      }
      .add-header-title {
        color: #646a73;
      }
    }
    .model-content {
      .model-header {
        position: relative;
        font-size: 14px;
        .header-img {
          width: 32px;
          height: 32px;
          margin-right: 12px;
          object-fit: contain;
        }
        .header-text {
          width: calc(100% - 80px);
          overflow: hidden;
          align-content: center;
        }
      }
    }
    .model-card {
      margin-right: 10px;
      margin-bottom: 10px;
      cursor: pointer;
    }
  }
  :deep(.ant-card .ant-card-body) {
    padding: 12px;
  }

  .pointer {
    cursor: pointer;
  }
  
  :deep(.jeecg-basic-modal-close){
    span{
      margin-left: 0 !important;
    }
  }
</style>
<style lang="less">
.ai-model-modal{
  .jeecg-basic-modal-close > span{
    margin-left: 0 !important;
  }
}
</style>
