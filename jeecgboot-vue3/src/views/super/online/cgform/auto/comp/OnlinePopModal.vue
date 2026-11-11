<template>
  <BasicModal :width="popModalFixedWidth" :wrapperFooterOffset="26" v-bind="$attrs" :footer="modalFooter" cancelText="Đóng" @register="registerModal" wrapClassName="jeecg-online-pop-modal" @ok="handleSubmit">
        <template #title>
            {{title}}
            <j-modal-tip v-if="showTopTip" :visible="topTipVisible" @save="handleSaveData" @cancel="handleRecover"></j-modal-tip>
        </template>
        
        <online-pop-form
            ref="onlineFormCompRef"
            :id="id"
            :disabled="disableSubmit"
            :form-template="formTemplate"
            :isTree="isTreeForm"
            :pidField="pidFieldName"
            :request="request"
            :isVxeTableData="isVxeTableData"
            @rendered="renderSuccess"
            @success="handleSuccess"
            @data-change="handleDataChange"
            modal-class="jeecg-online-pop-modal"
        >
        </online-pop-form>
    </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, watchEffect, ref, computed, h } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  import OnlinePopForm from './OnlinePopForm.vue';
  import { useAutoModal } from '../../hooks/auto/useAutoModal';
  import JModalTip from '../../extend/linkTable/JModalTip.vue'
  import { Button } from 'ant-design-vue';

  export default defineComponent({
    name: 'OnlinePopModal',
    props: {
      /**có thể là tên bảng có thể làID*/
      id: {
        type: String,
        default: '',
      },
      /*Hiển thị tên trường*/
      showFields:{
        type: Array,
        default: ()=>[],
      },
      /*Ẩn tên trường*/
      hideFields:{
        type: Array,
        default: ()=>[],
      },
      topTip:{
        type: Boolean,
        default: false,
      },
      request:{
        type: Boolean,
        default: true,
      },
      saveClose:{
        type: Boolean,
        default: false
      },
      // Có phải làvxeTableDữ liệu biểu mẫu được mở bằng cách nhấp nút phía trên
      isVxeTableData:{
        type: Boolean,
        default: false
      },
      formTableType:{
        type: String,
        default: '',
      },
      // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
      // CótaskIdchính là quy trình
      taskId: {
        type: String,
      },
      tableName: {
        type: String,
      },
      // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
    },
    components: {
      BasicModal,
      OnlinePopForm,
      JModalTip,
      Button
    },
    emits: ['success', 'register', 'formConfig'],
    setup(props, { emit }) {
      console.log('Vào popup biểu mẫu 》》》》modal');
      
      const {
        title,
        registerModal,
        cgButtonList,
        handleCgButtonClick,
        disableSubmit,
        handleSubmit,
        submitLoading,
        handleCancel,
        handleFormConfig,
        onlineFormCompRef,
        formTemplate,
        isTreeForm,
        pidFieldName,
        renderSuccess,
        formRendered,
        handleSuccess,
        topTipVisible,
        successThenClose,
        isUpdate,
        popModalFixedWidth,
        getFormStatus
      } = useAutoModal(false, { emit });

      // Lắng nghe (watch)idThay đổi Render lại biểu mẫu
      watch(() => props.id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!props.id) {
          return;
        }
        console.log('Render lại biểu mẫu 》》》》modal');
        
        //update-begin-author:taoyan date:2023-4-10 for: issues/4655 onlineBiểu mẫu trực tuyến (một-nhiều), khi thêm mới hoặc chỉnh sửa bản ghi của bảng con, không lấy được thông tin biểu mẫu #4655
        let params = {}
        if(props.formTableType){
          params['tabletype'] = props.formTableType
        }
        // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        if (props.taskId) {
          await handleFormConfig(props.id, params,null,props.taskId, props.tableName );
        } else {
          await handleFormConfig(props.id, params);
        }
        // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        //update-end-author:taoyan date:2023-4-10 for: issues/4655 onlineBiểu mẫu trực tuyến (một-nhiều), khi thêm mới hoặc chỉnh sửa bản ghi của bảng con, không lấy được thông tin biểu mẫu #4655
        
      }
      
      // Kích hoạt nút lưu phía trên
      function handleSaveData() {
        //NếupropscủasaveCloseChưa thiết lập thànhtruethì popup sẽ không đóng
        if(props.saveClose === false){
          successThenClose.value = false;
        }
        handleSubmit();
      }
      
      // Kích hoạt nút hủy phía trên
      function handleRecover(){
        topTipVisible.value = false;
        onlineFormCompRef.value.recoverFormData()
      }
      
      // Kích hoạt khi dữ liệu biểu mẫu thay đổimodalsự kiện
      function handleDataChange(){
        topTipVisible.value = true;
      }
      
      // Chỉ trang chỉnh sửa mới cần hiển thị nút lưu ở trên cùng
      const showTopTip = computed(()=>{
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        if (disableSubmit.value) {
          return false;
        }
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
        if(!isUpdate.value){
          return false;
        }
        return props.topTip
      });

      // Trang chỉnh sửa không có nút ở dưới cùng
      const modalFooter = computed(()=>{
        if(isUpdate.value==true){
          return null;
        }else{
          let flag = submitLoading.value;
          const defaultFooter:any[] = [
            h(Button, { type: 'primary', loading: flag, onClick: handleSubmit },()=>'Xác nhận'),
            h(Button, { onClick:handleCancel },()=>'Đóng')
          ];
          return defaultFooter
        }
      });
      
      const that = {
        title,
        topTipVisible,
        handleSaveData,
        handleRecover,
        onlineFormCompRef,
        renderSuccess,
        registerModal,
        handleSubmit,
        handleSuccess,
        handleCancel,
        formTemplate,
        disableSubmit,
        cgButtonList,
        handleCgButtonClick,
        isTreeForm,
        pidFieldName,
        submitLoading,
        handleDataChange,
        isUpdate,
        showTopTip,
        modalFooter,
        popModalFixedWidth,
        getFormStatus
      };

      return that;
    },
  });
</script>

<style lang="less">
  .jeecg-online-pop-modal {
    > .ant-modal {
      top: 30px !important;
    }
  }
</style>
