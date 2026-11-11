<template>
    <div ref="tableLinkCardRef">
        <div class="table-link-card">
            <div style="width: 100%; height: 100%">
                <div class="card-button" v-if="showButton">
                    <a-button @click="handleAddRecord"><PlusOutlined />ghi ghi</a-button>
                </div>

                <a-row>
                    <a-col :span="fixedSpan ? fixedSpan : itemSpan" v-for="(record, index) in selectRecords">
                        <div class="card-item" :class="{'disabled-chunk': detail==true}" @click="(e)=>handleClickEdit(e, record)">
                            <!-- -->
                            <div class="card-item-left" :class="{'show-right-image': getImageSrc(record)}">
                                <span class="card-delete" v-if="disabled==false">
                                    <minus-circle-filled @click="(e)=>handleDeleteRecord(e, index)"/>
                                </span>
                                <div class="card-inner">
                                    <div class="card-main-content">{{ getMainContent(record) }}</div>
                                    <div class="other-content">
                                        <a-row>
                                            <a-col :span="columnSpan" v-for="(col) in realShowColumns">
                                                <span class="label ellipsis">{{ col.title }}</span>
                                                <span class="text ellipsis">{{ record[col.dataIndex] }}</span>
                                            </a-col>
                                        </a-row>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="card-item-image" v-if="getImageSrc(record)">
                                <img v-if="getImageSrc(record)" :src="getImageSrc(record)" alt="" @error="handleImageError">
                            </div>
                            
                        </div>
                    </a-col>
                </a-row>
            </div>
        </div>

        <online-pop-list-modal v-if="popListModalShow" @register="registerListModal" :multi="multi" :id="popTableName" :addAuth="auths.add" @success="addCard"></online-pop-list-modal>
        <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa -->
        <online-pop-modal v-if="popFormModalShow" :id="popTableName" @register="registerFormModal" @success="updateCardData" topTip></online-pop-modal>
    </div>
</template>

<script>
  import { propTypes } from '/@/utils/propTypes';
  import { PlusOutlined, MinusCircleFilled } from '@ant-design/icons-vue';
  import { computed, ref, watch, onMounted, defineAsyncComponent } from 'vue'
  import {useLinkTable } from './useLinkTable';
  import { useModal } from '/@/components/Modal';
  const OnlinePopListModal = defineAsyncComponent(() => import('../../auto/comp/OnlinePopListModal.vue'));
  const OnlinePopModal = defineAsyncComponent(() => import('../../auto/comp/OnlinePopModal.vue'));
  import placeholderImage from '/@/assets/images/placeholderImage.png';
  
  export default {
    name: "LinkTableCard",
    props:{
      valueField: propTypes.string.def(''),
      textField: propTypes.string.def(''),
      tableName: propTypes.string.def(''),
      multi: propTypes.bool.def(false),
      value: propTypes.oneOfType([propTypes.string, propTypes.number]),
      // ["Trường biểu mẫu,Trường từ điển bảng","Trường biểu mẫu,Trường từ điển bảng"]
      linkFields: propTypes.array.def([]),
      //Có phải trang vô hiệu hóa hay không
      disabled: propTypes.bool.def(false),
      // Có phải làdetailTrang
      detail: propTypes.bool.def(false),
      imageField: propTypes.string.def(''),
    },
    components:{
      PlusOutlined,
      MinusCircleFilled,
      OnlinePopListModal,
      OnlinePopModal
    },
    emits:['change','update:value'],
    setup(props, {emit}){
      const popTableName = computed(()=>{
        return props.tableName
      });
      const [registerListModal, { openModal: openListModal }] = useModal();
      const [registerFormModal, { openModal: openFormModal }] = useModal();
      // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      // đã thêm vàoonlineBản ghi liên kết một-nhiều và trường bảng khác phải tải lười (lazy load), nếu không việc xây dựng sẽ gọi vòng lặp và báo lỗi
      const popListModalShow = ref(false);
      const popFormModalShow = ref(false);
      // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      
      const selectValue = ref([]);
      const selectRecords = ref([]);
      const tableLinkCardRef = ref(null);
      const fixedSpan = ref(0);

      const showButton = computed(()=>{
        if(props.disabled == true){
          return false;
        }
        if(props.multi===false){
          //Chọn đơn và có giá trị
          if(selectRecords.value.length>0){
            return false;
          }
        }
        return true;
      });
      const {auths, otherColumns,realShowColumns,tableColumns, textFieldArray, transData, loadOne, compareData, formatData, initFormData, getImageSrc,showImage } = useLinkTable(props);
       
      const itemSpan = computed(()=>{
        if(props.multi===true){
          return 12;
        }
        return 24
      });

      const columnSpan = computed(()=>{
        if(props.multi===true){
          return 24;
        }
        return 12
      });


      function getMainContent(record){
        if(record){
          if(textFieldArray.value.length>0){
            let field = textFieldArray.value[0]
            return record[field]
          }
        }
      }
      
      function prevent(e){
        e?.stopPropagation();
        e?.preventDefault();
      }
      
      function handleClickEdit(e, record) {
        prevent(e);
        if(auths.update == false){
          console.error('Người dùng hiện tại không có quyền chỉnh sửa!');
          return;
        }
        if(props.disabled==false){
          popFormModalShow.value = true;
          setTimeout(() => openFormModal(true, {
            isUpdate: true,
            record
          }), 100);
        }
      }
      
      function handleAddRecord(e){
       // prevent(e);
        popListModalShow.value = true;
        setTimeout(() => openListModal(true, {
          // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
          selectedRowKeys: selectRecords.value.map((item) => item.id),
          selectedRows: [...selectRecords.value],
          // update-end--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
        }), 100);
      }

      
      function addCard(data){
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
        let arr = [];
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-43】Khắc phục: bản ghi liên kết có thể thêm dữ liệu trùng lặp
        for(let item of data){
          let temp = {...item}
          transData(temp)
          arr.push(temp)
        }
        selectRecords.value = arr;
        emitValue();
      }
      
      function updateCardData(formData){
        let arr = selectRecords.value
        for(let i=0;i<arr.length;i++){
          if(arr[i].id === formData.id){
            let temp = {...formData}
            transData(temp)
            arr.splice(i, 1, temp)
          }
        }
        selectRecords.value = arr;
        emitValue();
      }

      function handleDeleteRecord(e, index){
        prevent(e);
        let temp = selectRecords.value;
        if(temp && temp.length>index){
          temp.splice(index, 1);
          selectRecords.value = temp
        }
        emitValue();
      }
      
      function emitValue(){
        let arr = selectRecords.value
        let values = [];
        let formData = {}
        let linkFieldArray = props.linkFields;
        if(arr.length>0){
          for(let i=0;i<arr.length;i++){
            values.push(arr[i][props.valueField])
            initFormData(formData, linkFieldArray, arr[i])
          }
        }else{
          initFormData(formData, linkFieldArray)
        }
        let text = values.join(',');
        formatData(formData)
        emit('change', text, formData)
        emit('update:value', text)
      }
      


      watch(()=>props.value, async (val)=>{
        if(val){
          let flag = compareData(selectRecords.value, val);
          if(flag===false){
            let arr = await loadOne(val);
            selectRecords.value = arr;
          }
          //Đảm bảo các giá trị khác của biểu mẫu hiển thị lại thành công
          if(props.linkFields && props.linkFields.length>0){
            emitValue();
          }
        }else{
          selectRecords.value = []
        }
      }, {immediate: true})
      
      onMounted(() => {
        // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-281】Khi độ phân giải nhỏ, chữ của bản ghi liên kết bị hình ảnh chèn mất
        if (tableLinkCardRef.value.offsetWidth < 250) {
          fixedSpan.value = 24;
        }
        // update-end--author:liaozhiyang---date:20240522---for：【TV360X-281】Khi độ phân giải nhỏ, chữ của bản ghi liên kết bị hình ảnh chèn mất
      });
      // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-389】Hình của bản ghi liên kết dạng dropdown và dạng thẻ bị vỡ, hãy cấp một hình mặc định
      const handleImageError = (event) => {
        event.target.src = placeholderImage;
      };
      // update-end--author:liaozhiyang---date:20240529---for：【TV360X-389】Hình của bản ghi liên kết dạng dropdown và dạng thẻ bị vỡ, hãy cấp một hình mặc định

      return {
        popTableName,
        selectRecords,
        popListModalShow,
        popFormModalShow,
        otherColumns,
        realShowColumns,
        showButton,
        selectValue,
        handleAddRecord,
        handleDeleteRecord,
        getMainContent,
        itemSpan,
        columnSpan,
        tableColumns,
        addCard,
        registerListModal,
        registerFormModal,
        handleClickEdit,
        updateCardData,
        getImageSrc,showImage,
        auths,
        tableLinkCardRef,
        fixedSpan,
        placeholderImage,
        handleImageError,
      }
    }
  }
</script>

<style scoped lang="less">
    .table-link-card {
        box-sizing: border-box;
        position: relative;
        width: 100%;
        .card-button {
            margin-bottom: 10px !important;
        }
        .card-item {
            width: calc(100% - 10px);
            display: inline-flex;
            flex-direction: row;
            margin: 0px 10px 10px 0px;
            position: relative;
            border-radius: 3px;
            background-color: rgb(255, 255, 255);
            box-shadow: rgb(0 0 0 / 12%) 0px 1px 4px 0px, rgb(0 0 0 / 12%) 0px 0px 2px 0px;
            cursor: pointer;
            &:hover{
              /*  box-shadow: rgb(0 0 0 / 12%) 0px 4px 12px 0px, rgb(0 0 0 / 12%) 0px 0px 2px 0px;*/
                .card-item-left{
                    .card-delete{
                        display: inline-block;
                    } 
                }
            }
            
            &.disabled-chunk{
                background: none;
                box-shadow:none;
            }
            
            .card-item-image{
                width: 100px;
                padding-right: 8px;
                display: flex;
                flex-direction: column;
                justify-content: center;
                overflow: hidden;
                img{
                    width: 100%;
                }
            }
            
            .card-item-left{
                width: 100%;
                display: inline-block;
                &.show-right-image{
                    width: calc(100% - 100px);
                }
                .card-delete {
                    position: absolute;
                    top: -8px;
                    right: -8px;
                    font-size: 16px;
                    color: #757575;
                    line-height: 1em;
                    overflow: hidden;
                    display: none;
                }

                .card-inner {
                    flex: 1 1 0%;
                    padding: 12px 16px;
                    overflow: hidden;
                    padding-bottom: 10px;

                    .card-main-content {
                        overflow: hidden;
                        text-overflow: ellipsis;
                        vertical-align: top;
                        white-space: nowrap;
                        margin-bottom: 8px;
                        font-weight: 500;
                        font-size: 14px;
                        line-height: 20px;
                        color: rgb(51, 51, 51);
                    }

                    .other-content {
                        .text {
                            font-size: 12px !important;
                            display: inline-block;
                            width: 80%;
                        }
                        .label {
                            max-width: 160px;
                            color: rgb(158, 158, 158);
                            padding-right: 0.7em;
                            display: inline-block;
                        }
                        .ellipsis {
                            overflow: hidden;
                            height: 22px;
                            line-height: 22px;
                            text-overflow: ellipsis;
                            /*   vertical-align: top;*/
                            white-space: nowrap;
                        }
                    }
                }
            }
        }
    }
</style>
