<template>
  <BasicModal wrapClassName="link-table-field-config-modal" v-bind="$attrs" title="Cấu hình trường bảng khác" @register="registerModal" keyboard :canFullscreen="false" cancelText="Đóng" @ok="handleSubmit">
    <a-spin :spinning="spinningLoading">
        <BasicForm @register="registerForm">
        </BasicForm>
    </a-spin>
  </BasicModal>
</template>

<script>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ref,computed } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import {  omit } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';
  
  export default {
    name: 'LinkTableFieldConfigModal',
    emits: ['success', 'register'],
    props: {
      isSubTableOneToMany: {
        type: Boolean,
        default: false
      }
    },
    components: {
      BasicModal,
      BasicForm
    },
    setup(props, { emit }) {
      const spinningLoading = ref(false);
      const linkTableOptions = ref([]);
      const fieldOptions = ref([]);
      let tableAndFieldsMap = {};
      let oldValue = {}
      const { createMessage: $message } = useMessage();
      
      //useModalInner
      const [registerModal,{closeModal}] = useModalInner( async (data) => {
        console.log('data', data);
        oldValue = {...data.record};
        //spinningLoading.value = true;
        tableAndFieldsMap = data.tableAndFieldsMap;
        await initLinkTableOptions()
        await setFieldsValue({dictTable: data.record.dictTable});
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-45】Cập nhật các trường khác trong bản ghi liên kết, trường hiển thị trong trường bảng khác không cập nhật
        if (oldValue.dictTable) {
          handleChange(oldValue.dictTable);
        }
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-45】Cập nhật các trường khác trong bản ghi liên kết, trường hiển thị trong trường bảng khác không cập nhật
        setTimeout(async ()=>{
          let otherValue = omit(data.record, 'dictTable')
          await setFieldsValue(otherValue);
          await clearValidate();
        }, 200);
      });
      
      async function initLinkTableOptions(){
        let fieldNames = Object.keys(tableAndFieldsMap);
        if(!fieldNames || fieldNames.length==0){
          linkTableOptions.value = []
        }else{
          let arr = []
          for(let name of fieldNames){
            arr.push({
              text: tableAndFieldsMap[name].title,
              value: name
            })
          }
          linkTableOptions.value = arr
        }
      }
      
      async function handleChange(fieldName){
        if(fieldName){
          const {table, fields} = tableAndFieldsMap[fieldName];
          if(!table){
            $message.warning('Vui lòng hoàn thiện trường trước['+fieldName+']cấu hình của bản ghi liên kết')
            return;
          }
          const url = '/online/cgform/field/listByHeadCode';
          const dataList = await defHttp.get({ url, params:{headCode: table} });
          if(dataList && dataList.length>0){
            let arr = dataList.map(item=>{
              return {
                text: item.dbFieldTxt,
                value: item.dbFieldName
              }
            });
            console.log('Từ điển trường', arr);
            let fieldArray = fields.split(',')
            // update-begin--author:liaozhiyang---date:20260323---for:【QQYUN-14951】Trường bảng khác một-nhiều cần chọn được tất cả các trường
            if (props.isSubTableOneToMany) {
              fieldOptions.value = arr;
            } else {
              fieldOptions.value = arr.filter(item=>fieldArray.includes(item.value));
            }
            // update-end--author:liaozhiyang---date:20260323---for:【QQYUN-14951】Trường bảng khác một-nhiều cần chọn được tất cả các trường
          }else{
            fieldOptions.value = []
          }
        }
      }
      
      const formSchemas = [
        {
          label: 'rowKey',
          field: 'rowKey',
          component: 'Input',
          show: false
        },
        {
          label: 'Mô tả trường',
          field: 'dbFieldTxt',
          component: 'Input',
          required: true
        },
        {
          label: 'bản ghi liên kết',
          field: 'dictTable',
          component: 'JSearchSelect',
          required: true,
          componentProps:({ formActionType }) => {
            let tempOptions = linkTableOptions.value;
            return {
              async: false,
              popContainer: '.link-table-field-config-modal',
              dictOptions: tempOptions,
              immediateChange: true,
              onChange:async (name)=>{
                if(oldValue.dictText){
                  await formActionType.setFieldsValue({
                    dictText: ''
                  });
                  await formActionType.clearValidate();
                }
                handleChange(name);
              }
            }
          }
        },
        {
          label: 'Trường hiển thị',
          field: 'dictText',
          component: 'JSearchSelect',
          required: true,
          componentProps:{
            async: false,
            popContainer: '.link-table-field-config-modal',
            dictOptions: fieldOptions,
            onChange: (value)=>{
              oldValue['dictText'] = value;
            }
          }
        }
      ]

      // Cấu hình biểu mẫu
      const [ registerForm, {validate, setFieldsValue, clearValidate}] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      async function handleSubmit(){
        const data = await validate()
 /*     dbFieldTxt: "Họ tên"
        dictTable: "test"
        multiSelect: false
        otherFields: "create_by,xxx"
        showType: "card"
        titleField: "namexx"*/
        console.log('handlesub', data);
        emit('success', data)
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
