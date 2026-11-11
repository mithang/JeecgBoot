<template>
  <BasicModal wrapClassName="link-table-config-modal" v-bind="$attrs" title="Cấu hình bản ghi liên kết" @register="registerModal" keyboard :canFullscreen="false" cancelText="Đóng" @ok="handleSubmit">
    <a-spin :spinning="spinningLoading">
        <BasicForm @register="registerForm">
        </BasicForm>
    </a-spin>
  </BasicModal>
</template>

<script>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ref,computed,nextTick} from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import {  omit } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default {
    name: 'LinkTableConfigModal',
    emits: ['success', 'register'],
    components: {
      BasicModal,
      BasicForm
    },
    setup(_p, { emit }) {
      const spinningLoading = ref(false);
      const { createMessage: $message } = useMessage();
      // Có phải cấu hình bản ghi liên kết của khóa ngoại hay không
      //const fkStatus = ref(false);
      const fieldName = ref('')
      // update-begin--author:liaozhiyang---date:20260312---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      const isSubTableOneToMany = ref(false);
      // update-end--author:liaozhiyang---date:20260312---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      let oldValue = {}
      //useModalInner
      const [registerModal,{closeModal}] = useModalInner(async (data) => {
        //fkStatus.value = data.isFk || false;
        console.log('data', data);
        //spinningLoading.value = true;
        isSubTableOneToMany.value = data.isSubTableOneToMany || false;
        oldValue = {...data.record};
        await setFieldsValue({dictTable: data.record.dictTable});
        // Giá trị thiết lập của trường nằm sau tên bảng Ngăn của tên bảngchangeĐặt giá trị của trường thành trống rỗng
        setTimeout(async ()=>{
          let otherValue = omit(data.record, 'dictTable')
          await setFieldsValue(otherValue);
          await clearValidate();
        }, 200);
        fieldName.value = data.fieldName;
      });
      
      const titleFieldName = ref('');
      const imageFieldName = ref('')
      const fieldOptions = ref([]);
      const imageFieldOptions = ref([])
      async function getFieldOptions(tableName){
        if(tableName){
          const url = '/online/cgform/field/listByHeadCode';
          const dataList = await defHttp.get({ url, params:{headCode: tableName} });
          if(dataList && dataList.length>0){
            let arr1 = dataList.filter(item=>item.dbFieldName!='id' && item.dbIsPersist == 1 && item.isShowList == 1);
            console.log('Từ điển trường', arr1)
            if(arr1.length>0){
              fieldOptions.value = arr1.map(item=>{
                return {
                  text: item.dbFieldTxt,
                  value: item.dbFieldName
                }
              });
            }else{
              fieldOptions.value = []
            }
            
            let arr2 = dataList.filter(item=>item.dbFieldName!='id' && item.fieldShowType == 'image' && item.dbIsPersist == 1);
            console.log('Trường hình ảnh', arr2)
            if(arr2.length>0){
              imageFieldOptions.value = arr2.map(item=>{
                return {
                  text: item.dbFieldTxt,
                  value: item.dbFieldName
                }
              });
            }else{
              imageFieldOptions.value = [{text:'Không có trường hình ảnh để lựa chọn', value:'',key:'', disabled: true}]
            }
          }else{
            fieldOptions.value = []
            imageFieldOptions.value = [{text:'Không có trường hình ảnh để lựa chọn', value:'', key:'',disabled: true}]
          }
        }
      }
      async function handleTableChange(tableName){
        titleFieldName.value = '';
        imageFieldName.value = ''
        // Sửa tên bảng, lấy lại trường
        await getFieldOptions(tableName);
      }
      const otherFieldOptions = computed(()=>{
        let arr = fieldOptions.value;
        let titleField = titleFieldName.value;
        let imageField = imageFieldName.value;
        return arr.filter(item=>item.value!=titleField && item.value!=imageField);
      });
      
      
      const formSchemas = [
        {
          label: 'rowKey',
          field: 'rowKey',
          component: 'Input',
          show: false
        },
        {
          label: 'dictField',
          field: 'dictField',
          component: 'Input',
          defaultValue: 'id',
          show: false
        },
        {
          label: 'Mô tả trường',
          field: 'dbFieldTxt',
          component: 'Input',
          required: true
        },
        {
          label: 'Bảng liên kết',
          field: 'dictTable',
          component: 'JSearchSelect',
          required: true,
          componentProps:({ formActionType }) => {
            return {
              dict: 'onl_cgform_head where copy_type = 0,table_txt,table_name',
              pageSize: 10,
              async: true,
              immediateChange: true,
              popContainer: '.link-table-config-modal',
              params:{order: 'desc', column: 'create_time'},
              onChange:async (name)=>{
                // Thiết lập lại giá trị biểu mẫu
                if(oldValue.titleField || oldValue.otherFields){
                  await formActionType.setFieldsValue({
                    titleField: '',
                    otherFields: '',
                    imageField: ''
                  });
                  await formActionType.clearValidate();
                }
                // Truy vấn trường theo tên bảng
                await handleTableChange(name);
              }
            }
          }
        },
        {
          label: 'Trường tiêu đề',
          field: 'titleField',
          component: 'JSearchSelect',
          required: true,
          componentProps:{
            async: false,
            popContainer: '.link-table-config-modal',
            dictOptions: fieldOptions,
            immediateChange: true,
            onChange:(str)=>{
              titleFieldName.value = str;
              oldValue['titleField'] = str;
            }
          }
        },
        {
          label: 'Hình ảnh bìa',
          field: 'imageField',
          component: 'JSearchSelect',
          ifShow: () => !isSubTableOneToMany.value,
          componentProps:{
            async: false,
            popContainer: '.link-table-config-modal',
            dictOptions: imageFieldOptions,
            immediateChange: true,
            onChange:(str)=>{
              imageFieldName.value = str;
              oldValue['imageFieldName'] = str;
            }
          }
        },
        {
          label: 'Trường khác',
          field: 'otherFields',
          component: 'JSelectMultiple',
          ifShow: () => !isSubTableOneToMany.value,
          // update-begin--author:liaozhiyang---date:20240320---for：【QQYUN-8574】Bản ghi liên kết được chọn tối đa6trường
          componentProps: ({ schema, tableAction, formActionType, formModel }) => {
            return {
              popContainer: '.link-table-config-modal',
              options: otherFieldOptions.value,
              onChange: (str) => {
                if (str.split(',').length > 6) {
                  const result = str.split(',');
                  result.pop();
                  const eStr = result.join(',');
                  setTimeout(() => {
                    formModel.otherFields = eStr;
                    oldValue['otherFields'] = eStr;
                  }, 0);
                  $message.warning('Chọn tối đa6trường~');
                } else {
                  oldValue['otherFields'] = str;
                }
                console.info('oldValue', formModel);
              },
            };
          },
          // update-end--author:liaozhiyang---date:20240320---for：【QQYUN-8574】Bản ghi liên kết được chọn tối đa6trường
        },
        
        {
          label: 'Cách hiển thị',
          field: 'showType',
          component: 'Select',
          defaultValue: 'card',
          ifShow: () => !isSubTableOneToMany.value,
          componentProps: {
            options: [
              { label: 'Thẻ (card)', value: 'card' },
              { label: 'Ô dropdown', value: 'select'},
            ],
          }
        },
        {
          label: 'Có chọn nhiều hay không',
          field: 'multiSelect',
          component: 'RadioGroup',
          defaultValue: false,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          }
        },
        {
          label: 'Danh sách chỉ đọc',
          field: 'isListReadOnly',
          component: 'RadioGroup',
          defaultValue: false,
          ifShow: () => !isSubTableOneToMany.value,
          componentProps: {
            options: [
              { label: 'Không', value: false },
              { label: 'là', value: true },
            ],
          },
        }
      ]

      // Cấu hình biểu mẫu
      const [ registerForm, {validate, setFieldsValue, clearValidate, resetFields }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      async function handleSubmit(){
        let data = await validate()
        data['fieldName'] = fieldName.value;
 /*     dbFieldTxt: "Họ tên"
        dictTable: "test"
        multiSelect: false
        otherFields: "create_by,xxx"
        showType: "card",
        imageField: 'imagexx',
        titleField: "namexx"*/
        emit('success', data);
        closeModal();
      }
      
      
      return {
        registerModal,
        spinningLoading,
        registerForm,
        handleSubmit
      };
    },
  };
</script>

<style scoped></style>
