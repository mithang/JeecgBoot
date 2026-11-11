<template>
    <!-- Vùng nút -->
    <div class="j-super-query-button">
        <a-tooltip v-if="superQueryFlag" :mouseLeaveDelay="0.2">
            <template #title>
                <span>Đang thực thi truy vấn...</span>
                <divider type="vertical" style="background-color: #fff"/>
                <a @click="handleStop">Hủy truy vấn</a>
            </template>
            <a-button-group>
                <a-button type="primary" @click="handleOpen">
                    <AppstoreTwoTone :spin="true"/>
                    <span>{{queryBtnCfg.buttonName}}</span>
                </a-button>
            </a-button-group>
        </a-tooltip>
        <a-button v-else type="primary" :preIcon="queryBtnCfg.buttonIcon" @click="handleOpen">
            <span>{{queryBtnCfg.buttonName}}</span>
        </a-button>
    </div>

    <!-- Popup truy vấn nâng cao -->
    <teleport to="body">
        <BasicModal :title="queryBtnCfg.buttonName + 'Bộ xây dựng (constructor)'" wrap-class-name="j-super-query-modal" :canFullscreen="true" :width="850" @register="registerFormModal" @ok="handleSubmit" @fullScreen="handleFullScreen">
            <template #footer>
                <div style="float: left">
                    <a-button :loading="loading" @click="handleReset">Xóa sạch (làm trống)</a-button>
                    <a-button :loading="loading" @click="handleSave">Lưu truy vấn</a-button>
                </div>

                <a-button key="submit" type="primary" @click="handleSubmit">Thực thi truy vấn</a-button>
                <a-button key="back" @click="handleCancel">Đóng</a-button>
            </template>

            <a-empty v-if="dynamicRowValues.values.length == 0">
                <div slot="description">
                    <span>Không có bất kỳ điều kiện truy vấn nào</span>
                    <a-divider type="vertical"/>
                    <a @click="addOne(-1)">Nhấp thêm mới</a>
                </div>
            </a-empty>

            <a-row :class="'j-super-query-modal-content'">
                <a-col :sm="24" :md="24">
                    <a-row v-show="dynamicRowValues.values.length > 0">
                        <a-col :md="12" :xs="24">
                            <a-form-item label="Chế độ khớp nối" :labelCol="{md: 6,xs:24}" :wrapperCol="{md: 18,xs:24}" style="width: 100%;">
                                <a-select v-model:value="matchType" :getPopupContainer="node=>node?.parentNode" style="width: 100%;">
                                    <a-select-option value="and">AND(mọi điều kiện khớp)</a-select-option>
                                    <a-select-option value="or">OR(bất kỳ một cái nào khớp)</a-select-option>
                                </a-select>
                            </a-form-item>
                        </a-col>
                    </a-row>

                    <a-form v-show="dynamicRowValues.values.length > 0" ref="formRef" :class="'jee-super-query-form'" :model="dynamicRowValues" @finish="onFinish" :style="queryFormStyle">
                        <a-space v-for="(item, index) in dynamicRowValues.values" :key="item.key" style="display: flex; margin-bottom: 8px" :align="item.curLineAlign ? item.align : 'baseline'">

                            <a-form-item class="field-clos" :name="['values', index, 'field']" >
                                <a-tree-select
                                        :popupClassName="getTreePopupClass"
                                        style="width:100%"
                                        placeholder="Vui lòng chọn trường"
                                        v-model:value="item.field"
                                        show-search
                                        tree-node-filter-prop="title"
                                        allow-clear
                                        tree-default-expand-all
                                        :dropdown-style="{ maxHeight: `${fieldTreeSelectHeight}px`, overflow: 'auto' }"
                                        :listHeight="fieldTreeSelectHeight - 20"
                                        @change="handleChangeField(item)"
                                        :tree-data="fieldTreeData">
                                </a-tree-select>
                            </a-form-item>
                            <a-form-item class="rule-clos" :name="['values', index, 'rule']">
                                <a-select style="width:100%" placeholder="Vui lòng chọn quy tắc khớp" v-model:value="item.rule">
                                    <a-select-option :value="qItem.value" v-for="qItem in getQueryCondition(item)" :key="qItem.value">{{qItem.label}}</a-select-option>
                                    <!-- Chuyên dụng trong trình thiết kế biểu mẫu hiện tại -->
                                   <!-- <a-select-option value="empty">rỗng</a-select-option>
                                    <a-select-option value="not_empty">không rỗng</a-select-option>-->
                                    <!-- Chuyên dụng trong trình thiết kế biểu mẫu hiện tại -->
                                </a-select>
                            </a-form-item>
                            <a-form-item class="component-clos" :name="['values', index, 'val']">
                                <online-super-query-val-component style="width:100%" :schema="getSchema(item, index)" :formModel="item" :setFormModel="(key, value)=>{setFormModel(key, value, item)}" @submit="handleSubmit" />
                            </a-form-item>

                            <a-form-item>
                                <a-button @click="addOne(index)" style="margin-right: 6px">
                                    <PlusOutlined #icon/>
                                </a-button>
                                <a-button @click="removeOne(item)">
                                    <MinusCircleOutlined #icon/>
                                </a-button>
                            </a-form-item>
                        </a-space>
                    </a-form>
                </a-col>
            </a-row>
          <!-- Bản ghi truy vấn -->
          <a-card :class="['j-super-query-history-card', {'collapsed': historyCollapsed}]" :bordered="false">
            <template #title><div>truy vấn đã lưu</div></template>
            <a-empty
                v-if="saveTreeData.length === 0"
                class="j-super-query-history-empty"
                :image="simpleImage"
                description="Không có truy vấn đã lưu"
            />
            <a-tree
                v-else
                class="j-super-query-history-tree"
                :treeData="saveTreeData"
                :selectedKeys="[]"
                :show-icon="true"
                @select="handleTreeSelect">
              <template #title="{title}">
                <div>
                  <span :title="title">{{(title.length>10)?(title.substring(0, 10)+'...'):title }}</span>
                  <a-popconfirm title="Xác nhận xóa chứ?" @confirm="handleRemoveSaveInfo(title)">
                    <span class="icon-cancle" @click="(e)=>e.stopPropagation()"><close-circle-outlined /></span>
                  </a-popconfirm>
                </div>
              </template>
              <!-- antd-2được viết như vậy Nâng cấp lên3có thể sẽ thay đổi cách viết -->
              <template #custom>
                <file-text-outlined/>
              </template>
            </a-tree>
            <div class="collapse-box" @click="historyCollapsed=!historyCollapsed">
              <Icon v-if="historyCollapsed" icon="ant-design:caret-left"/>
              <Icon v-else icon="ant-design:caret-right"/>
            </div>
          </a-card>
        </BasicModal>
    </teleport>

    <!-- Popup thông tin đã lưu -->
    <a-modal title="Vui lòng nhập tên để lưu" :open="saveInfo.visible" @cancel="saveInfo.visible=false" @ok="doSaveQueryInfo" :confirmLoading="saveModalLoading">
        <div style="height:80px;line-height:75px;width:100%;text-align: center">
            <a-input v-model:value="saveInfo.title" style="width:90%" placeholder="Vui lòng nhập tên để lưu"></a-input>
        </div>
    </a-modal>
</template>

<script lang="ts">

  import {ref, watch, computed} from 'vue'
  import { BasicModal, useModal } from '/@/components/Modal';
  import { useSuperQuery } from './useSuperQuery';
  import OnlineSuperQueryValComponent from './SuperQueryValComponent.vue';
  import { MinusCircleOutlined, PlusOutlined, FileTextOutlined, CloseCircleOutlined, AppstoreTwoTone } from '@ant-design/icons-vue';
  import { Divider, Empty, Popconfirm } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';
  import { cloneDeep } from 'lodash-es';
  import { useConditionFilter } from '/@/utils/index';
  import { useDesign } from '/@/hooks/web/useDesign';
//const { BasicModal, useModal } = defineAsyncComponent(()=>import('/@/components/Modal'));
  export default{
    name: "OnlineSuperQuery",
    props: {
      config:{
        type: Object,
        default: []
      },
      status:{
        type: Boolean,
        default: false
      },
      online: {
        type: Boolean,
        default: false
      },
      // update-begin--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
      isCustomSave: {
        type: Boolean,
        default: false,
      },
      saveSearchData: {
        type: Array,
        default: () => [],
      },
      save: {
        type: Function,
      },
      // update-end--author:liaozhiyang---date:20240514---for：【issues/6205】Component truy vấn nâng cao bổ sung cách lưu trữ tùy chỉnh cho điều kiện đã lưu
      queryBtnCfg: {
        type: Object,
        default: () => {
          return {
            buttonName: 'Truy vấn nâng cao',
            buttonIcon: 'ant-design:filter-outlined',
          }
        }
      },
    },
    components: {
      BasicModal,
      MinusCircleOutlined,
      PlusOutlined,
      OnlineSuperQueryValComponent,
      FileTextOutlined,
      CloseCircleOutlined,
      AppstoreTwoTone,
      Divider,
      Popconfirm,
    },
    emits:['search'],
    setup(props, {emit}) {

      console.log("-------Khởi tạo  OnlineSuperQueryĐăng ký--------------")
      
      const [registerFormModal, formModal] = useModal()
      const { createMessage: $message } = useMessage();
      const queryFormStyle = ref({});
      const fieldTreeSelectHeight = ref(180);
      const { prefixCls } = useDesign('super-query');
      const treePopupClass = `${prefixCls}-tree-popup`;
      // Truy vấn đã lưu có gập lại hay không
      const historyCollapsed = ref(true)
      let currentQueryInfo = null;
      // -update-begin--author:liaozhiyang---date:20240614---for：【TV360X-76】Điều kiện truy vấn nâng cao phân biệt theo loại control
      const { filterCondition } = useConditionFilter();
      const findFieldItem = (items: any[], field: string | undefined): any => {
        for (const item of items) {
          if (item.value === field) {
            return item;
          }
          if (item.children?.length) {
            const child = findFieldItem(item.children, field);
            if (child) {
              return child;
            }
          }
        }
      };
      const getQueryCondition = (data) => {
        // -update-begin--author:liaozhiyang---date:20260810---for：【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao
        const findItem = findFieldItem(fieldTreeData.value, data.field) ?? {};
        return filterCondition({ view: findItem.originView || findItem.view, fieldType: findItem.fieldType });
        // -update-end--author:liaozhiyang---date:20260810---for：【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao
      };
      // -update-end--author:liaozhiyang---date:20240614---for：【TV360X-76】Điều kiện truy vấn nâng cao phân biệt theo loại control
      /**
       * Sự kiện nút đóng
       */
      function handleCancel() {
        formModal.closeModal();
      }

      /**
       * Sự kiện nút xác nhận
       */
      function handleSubmit() {
        if(props.online === true){
          let dataArray = getQueryInfo(true)
          if(dataArray && dataArray.length>0){
            // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-86】Năm, năm-tháng, tuần truy vấn cho kết quả không chính xác
            transformDateValus(dataArray);
            // update-end--author:liaozhiyang---date:20240517---for：【TV360X-86】Năm, năm-tháng, tuần truy vấn cho kết quả không chính xác
            emit('search', dataArray, matchType.value)
            // update-begin--author:liaozhiyang---date:220230802---for：【QQYUN-5995】Sau hiệu ứng truy vấn nâng cao thì đóng popup
            handleCancel()
            // update-end--author:liaozhiyang---date:220230802---for：【QQYUN-5995】Sau hiệu ứng truy vấn nâng cao thì đóng popup
            currentQueryInfo = dataArray;
          }else{
            $message.warning('Điều kiện trống không thể truy vấn!')
          }
        }else{
          //console.log('handleSubmit', dynamicRowValues.values)
          let dataArray = getQueryInfo(true)
          if(dataArray && dataArray.length>0){
            //update-begin---author:wangshuai---date:2025-07-18---for:【issues/8548】Ngày trong truy vấn nâng cao của mã được sinh ra-Control tháng không hiển thị bình thường được---
            transformDateValus(dataArray);
            //update-end---author:wangshuai---date:2025-07-18---for:【issues/8548】Ngày trong truy vấn nâng cao của mã được sinh ra-Control tháng không hiển thị bình thường được---
            let result = getSuperQueryParams(dataArray);
            //console.log('Truy vấn dữ liệu1', dataArray)
            //console.log('Truy vấn dữ liệu2', result)
            emit('search', result)
            // update-begin--author:wangshuai---date:20251112---for：【issues/9060】superQueryComponent nâng cao, nhấp"sau khi truy vấn"không thể tự động đóng popup
            handleCancel()
            // update-end--author:wangshuai---date:20251112---for：【issues/9060】superQueryComponent nâng cao, nhấp"sau khi truy vấn"không thể tự động đóng popup
          }else{
            $message.warning('Điều kiện trống không thể truy vấn!')
          }
        }
        historyCollapsed.value = true;
      }

      /**
       * 2024-05-17
       * liaozhiyang
       * Đặt lại các thời gian như năm, năm-tháng, tuần về ngày đầu tiên của định dạng hiện tại, vì khi lưu cũng là ngày đầu tiên
       */
      const transformDateValus = (date) => {
        date.forEach((item) => {
          const value = item['val'];
          if (item.type === 'date' && typeof value === 'string' && value != '') {
            // update-begin--author:liusq---date:20260806---for: truy vấn liên hợp bảng con năm/Tháng/Trường tuần sau khi gửi sẽ do@Chuyển thành,, dẫn đến không thể lấy cấu hình mở rộng của trường
            const fieldKey = item.field.includes(',') ? item.field.replace(',', '@') : item.field;
            const obj = fieldProperties.value[fieldKey];
            // update-end--author:liusq---date:20260806---for: truy vấn liên hợp bảng con năm/Tháng/Trường tuần sau khi gửi sẽ do@Chuyển thành,, dẫn đến không thể lấy cấu hình mở rộng của trường
            if (obj) {
              let fieldExtendJson = obj.fieldExtendJson;
              if (fieldExtendJson) {
                fieldExtendJson = JSON.parse(fieldExtendJson);
                if (fieldExtendJson.picker && fieldExtendJson.picker !== 'default') {
                  const picker = fieldExtendJson.picker;
                  if (picker === 'year') {
                    item['val'] = dayjs(value).set('month', 0).set('date', 1).format('YYYY-MM-DD');
                  } else if (picker === 'month') {
                    item['val'] = dayjs(value).set('date', 1).format('YYYY-MM-DD');
                  } else if (picker === 'week') {
                    item['val'] = dayjs(value).startOf('week').format('YYYY-MM-DD');
                  }
                }
              }
            }
          }
        });
      };
      
      function getSuperQueryParams(dataArray){
        let arr:any = []
        for(let item of dataArray){
          let field = item.field;
          let val = item.val
          if(val instanceof Array){
            val = val.join(",")
          }
          arr.push({
            ...item,
            field,
            val
          })
        }
        if(arr.length>0){
          superQueryFlag.value = true;
        }else{
          superQueryFlag.value = false;
        }
        let result = {
          superQueryMatchType: matchType.value,
          superQueryParams: encodeURI(JSON.stringify(arr))
        }
        return result;
      }
      
      function handleStop(){
        let result = getSuperQueryParams([]);
        emit('search', result)
      } 
      /**
       * Sự kiện nút đặt lại
       */
      function handleReset(){
        dynamicRowValues.values = []
        addOne(false);
        let result = getSuperQueryParams([])
        currentQueryInfo = null;
        historyCollapsed.value = true;
        emit('search', result)
      }
      // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-524】Truy vấn nâng cao bổ sung chức năng phóng to
      const handleFullScreen = (val) => {
        // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-810】Sau khi phóng to truy vấn nâng cao, dropdown cây cao hơn một chút, hiển thị nhiều trường hơn
        if (val) {
          const contentHeight = document.documentElement.clientHeight - 165;
          queryFormStyle.value = { maxHeight: `${contentHeight}px` };
          fieldTreeSelectHeight.value = contentHeight * 0.62;
        } else {
          queryFormStyle.value = {};
          fieldTreeSelectHeight.value = 180;
        }
        // update-end--author:liaozhiyang---date:20240603---for：【TV360X-810】Sau khi phóng to truy vấn nâng cao, dropdown cây cao hơn một chút, hiển thị nhiều trường hơn
      };
      // update-end--author:liaozhiyang---date:20240524---for：【TV360X-524】Truy vấn nâng cao bổ sung chức năng phóng to
      const {
        formRef,
        init,
        dynamicRowValues,
        matchType,
        registerModal,

        handleSave,
        doSaveQueryInfo,
        saveInfo,
        saveTreeData,
        handleTreeSelect,
        handleRemoveSaveInfo,
        fieldTreeData,
        addOne,
        removeOne,
        setFormModel,
        getSchema,
        loading,
        getQueryInfo,
        initDefaultValues,
        saveModalLoading,
        fieldProperties,
      } = useSuperQuery(props)

      /*--------------------Vùng nút-beign------------------*/
      const superQueryFlag = ref(false)
      watch(()=>props.status, (val)=>{
        superQueryFlag.value = val;
      }, {immediate: true});
      
      function handleOpen(){
        // update-begin--author:liaozhiyang---date:20240604---for：【TV360X-204】Nội dung đã sửa chưa nhấn xác nhận, khi mở lại phải khôi phục nội dung trước đó
        if (superQueryFlag.value && currentQueryInfo) {
          dynamicRowValues.values = cloneDeep(currentQueryInfo);
          // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-505】Sau khi thao tác thực thi truy vấn, mở lại, khi thao tác xóa điều kiện ở cuối cùng, thực tế lại xóa điều kiện ở đầu tiên
          if (dynamicRowValues.values.length > 0 && !dynamicRowValues.values[0].key) {
            dynamicRowValues.values.forEach((item) => {
              item.key = Math.random().toString(36).slice(2, 18);
            });
          }
          // update-end--author:liaozhiyang---date:20260714---for：【LHZP-505】Sau khi thao tác thực thi truy vấn, mở lại, khi thao tác xóa điều kiện ở cuối cùng, thực tế lại xóa điều kiện ở đầu tiên
          // update-begin--author:liaozhiyang---date:20260707---for：【LHZP-536】Bảng chính-con, chọn trường của bảng con để truy vấn, mở lại popup thì giá trị hiển thị không phải làlabelmà làvalue
          // Khôi phục dấu phẩy trong tên trường của bảng con thành@, để tiệnTreeSelectHiển thị lại đúng
          dynamicRowValues.values.forEach(item => {
            if (item.field && item.field.includes(',')) {
              item.field = item.field.replace(',', '@');
            }
          });
          // update-end--author:liaozhiyang---date:20260707---for：【LHZP-536】Bảng chính-con, chọn trường của bảng con để truy vấn, mở lại popup thì giá trị hiển thị không phải làlabelmà làvalue
        }
        // update-end--author:liaozhiyang---date:20240604---for：【TV360X-204】Nội dung đã sửa chưa nhấn xác nhận, khi mở lại phải khôi phục nội dung trước đó
        formModal.openModal();
        historyCollapsed.value = true;
        addOne(true)
      }
      /*--------------------Vùng nút-end------------------*/


      function getPopupContainer(){
        return document.getElementsByClassName('jee-super-query-form')[0]
      }
      function onFinish(a){
        console.log('onfinish', a)
      }
      /**
       * 2024-06-04
       * liaozhiyang
       * 【TV360X-461】Loại trường làstring, thì mặc định truy vấn mờ
       * */
      function handleChangeField(data) {
        data['val'] = '';
        // -update-begin--author:liaozhiyang---date:20260810---for：【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao
        const findItem = findFieldItem(fieldTreeData.value, data.field);
        if (findItem?.fieldType === 'string' && ['text'].includes(findItem?.originView || findItem?.view)) {
          data['rule'] = 'like';
        } else if (['file', 'image', 'password'].includes(findItem?.originView || findItem?.view)) {
          data['rule'] = 'empty';
        } else {
          // update-begin--author:liaozhiyang---date:20260813---for：【LHZP-442】Component hiện tại không có điều kiện bằng, mặc định sử dụng điều kiện đầu tiên của component hiện tại
          const queryConditions = getQueryCondition(data);
          const defaultCondition = queryConditions.find((item) => item.value === 'eq') || queryConditions[0];
          data['rule'] = defaultCondition?.value;
          // update-end--author:liaozhiyang---date:20260813---for：【LHZP-442】Component hiện tại không có điều kiện bằng, mặc định sử dụng điều kiện đầu tiên của component hiện tại
        }
        // -update-end--author:liaozhiyang---date:20260810---for：【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao
      }

      
      watch(()=>props.config, (val)=>{
        if(val){
          // console.log('123', val)
          // console.log('123', val)
          // console.log('123', val)
          // Object.keys(val).map(k=>{
          //   console.log(k, val[k])
          // })
          // console.log('123', val)
          // console.log('123', val)
          // console.log('123', val)
          init(val);
        }
      }, {immediate: true});
      // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-342】Điều chỉnh kiểu dáng dropdown của trường
      const getTreePopupClass = computed(() => {
        const findItem = fieldTreeData.value.find((item) => item.children);
        return findItem ? `${treePopupClass} containTable` : `${treePopupClass} noTable`;
      });
      // update-end--author:liaozhiyang---date:20240603---for：【TV360X-342】Điều chỉnh kiểu dáng dropdown của trường

      return {
        formRef,
        registerFormModal,
        init,
        handleChangeField,
        dynamicRowValues,
        matchType,
        historyCollapsed,
        registerModal,
        handleSubmit,
        handleCancel,
        handleSave,
        handleReset,
        doSaveQueryInfo,
        saveInfo,
        saveTreeData,
        handleTreeSelect,
        handleRemoveSaveInfo,
        fieldTreeData,
        addOne,
        removeOne,
        setFormModel,
        getSchema,
        loading,
        onFinish,
        getPopupContainer,
        superQueryFlag,
        handleOpen,
        initDefaultValues,
        simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
        saveModalLoading,
        queryFormStyle,
        handleFullScreen,
        fieldTreeSelectHeight,
        getTreePopupClass,
        handleStop,
        getQueryCondition,
      }
    }

  }
</script>

<style lang="less">
  .j-super-query-modal {
    .scrollbar__wrap {
      margin-bottom: 0 !important;
    }
  }
</style>
<style scoped lang="less">
   
    :deep(.jee-super-query-form) {
        max-height: 400px;
        overflow-y: auto;
        min-height: 300px;
        .ant-form-item{
            margin-bottom: 9px;
        }
    }
    
    :deep(.j-super-query-history-tree) {
        .ant-tree-switcher{
            width: 0px;
        }
        .ant-tree-node-content-wrapper{
            width:100%;
            &:hover{
                background-color: #e6f7ff !important;
                border-radius: 0;
            }
        }
        .ant-tree-treenode-switcher-close{
            .ant-tree-title{
                display: inline-block;
                width: calc(100% - 30px);
                >div{
                    display: flex;
                    justify-content: space-between;
                    .icon-cancle{
                        display: none;
                        color: #666666;
                        &:hover{
                            color: black;
                        }
                    }
                }
            }
            &:hover {
                .icon-cancle{
                    display: inline-block !important;
                }
            }
        }
         .ant-card-body{
            padding: 0;
        }
        // Điều chỉnh độ rộng kiểu dáng của truy vấn đã lưu
        .ant-tree-treenode { width: 100%;}
    }

    .j-super-query-history-card {
      position: absolute;
      top: 0;
      right: -0;
      bottom: 0;
      z-index: 2;
      //【QQYUN-7526】Khi di chuột vào hai hàng thì bị dồn thành ba hàng
      width: 180px;
      min-height: 200px;
      box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
      
      
      &.collapsed {
        //【QQYUN-7526】Khi di chuột vào hai hàng thì bị dồn thành ba hàng
        right: -180px;
      }

      .collapse-box {
        position: absolute;
        top: calc(50% - 15px);
        left: -20px;
        width: 20px;
        height: 30px;
        background-color: #fff;
        border: 1px solid #e8e8e8;
        border-left: none;
        border-right-color: #ffffff;
        border-radius: 15px 0 0 15px;
        cursor: pointer;
        transition: all 0.3s;
        z-index: 1;
        display: flex;
        justify-content: center;
        align-items: center;
        box-shadow: -4px 0 6px rgba(0, 0, 0, 0.1);

        &:hover {
          background-color: #f5f5f5;
        }
        .app-iconify {
          font-size: 16px;
          position: relative;
          right: -3px;
          color: #666;
        }
      }

      &.collapsed .app-iconify{
        right: 0;
      }
        
      :deep(.ant-card-body),
      :deep(.ant-card-head){
        padding: 8px;
        min-height: 43px;
        .ant-card-head-title {
          padding: 0;
        }
      }
    }

    /*VUEN-1087 【Di động】Truy vấn nâng cao hiển thị không đầy đủ */
    @media only screen and(max-width: 1050px) {
        :deep(.jee-super-query-form){
            .ant-space{
                flex-direction:column;
                gap: 0 !important;
                margin-bottom: 16px !important;
            }
            .ant-space-item{
                width: 100%;
            }
            .ant-form-item{
                width: 100% !important;
            }
        }
    }
    // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-524】Truy vấn nâng cao bổ sung chức năng phóng to
    .ant-form {
      & > .ant-space {
        width: 100%;
        :deep(& > .ant-space-item) {
          &:nth-child(1) {
            width: 20%;
          }
          &:nth-child(2) {
            width: 15%;
          }
          &:nth-child(3) {
            width: 40%;
          }
        }
      }
    }
    // update-end--author:liaozhiyang---date:20240524---for：【TV360X-524】Truy vấn nâng cao bổ sung chức năng phóng to

    .containTable {
      :deep(.ant-select-tree-treenode) {
        > .ant-select-tree-switcher { display: none;}
      }
    }
</style>
<style lang="less">
  @tree-popup: ~'@{namespace}-super-query-tree-popup';
  .@{tree-popup} {
    // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-342】Điều chỉnh kiểu dáng dropdown của trường
    &.noTable {
      .ant-select-tree-treenode {
        > .ant-select-tree-switcher {
          display: none;
        }
        .ant-select-tree-node-content-wrapper {
          display: block;
          max-width: 100%;
        }
        .ant-select-tree-title {
          width: 100%;
          display: block;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
    // update-end--author:liaozhiyang---date:20240603---for：【TV360X-342】Điều chỉnh kiểu dáng dropdown của trường
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】Khi có bảng con, cấu trúc hóa bảng chính và rút gọn khi quá dài
    &.containTable {
      .ant-select-tree-indent-unit {
        display: none;
      }
      .ant-select-tree-node-content-wrapper {
        display: block;
        max-width: calc(100% - 24px);
      }
      .ant-select-tree-title {
        width: 100%;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1005】Khi có bảng con, cấu trúc hóa bảng chính và rút gọn khi quá dài
  }
</style>
