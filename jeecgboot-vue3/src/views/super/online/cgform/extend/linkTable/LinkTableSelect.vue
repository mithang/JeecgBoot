<template>
    <div class="link-table-select-box" ref="boxRef">
        <a-select
                v-model:value="selectValue"
                style="width: 100%"
                placeholder="Vui lòng chọn"
                option-label-prop="label"
                popupClassName="table-link-select"
                allowClear
                show-search
                v-bind="bindValue"
                :options="selectOptions"
                :filter-option="false"
                :not-found-content="null"
                @search="handleSearch"
                @change="handleChange">
            
            <!-- antD 3.0Sau đó sử dụng cách render nàytag
            <template #tagRender="{ value: val, label, closable, onClose, option }">
                <a-tag :closable="closable" style="margin-right: 3px" @close="onClose">
                    {{ label }}&nbsp;&nbsp;
                    <span role="img" :aria-label="val">{{ option.val }}</span>
                </a-tag>
            </template>
            -->
            <template #option="item">
                <div v-if="!item.value && auths.add" class="opt-add" v-on:click="handleClickAdd">
                    <PlusOutlined/> Bản ghi
                </div>
                <div v-else class="online-select-item">
                    <!-- Sau khi cấu hình mở rộng, có thể thiết lập hình ảnh phía bên trái-->
                    <div class="left-avatar" v-if="showImage">
                        <img v-if="getImageSrc(item)" :src="getImageSrc(item)" alt="" @error="handleImageError">
                        <img v-else :src="placeholderImage" alt="">
                    </div>
                    <div class="right-content">
                        <div class="label" :class="{ noEditBtn: !(editBtnShow && auths.update) }">
                            <EditOutlined v-if="editBtnShow && auths.update" @click="(e)=>handleClickEdit(e, item)"/>
                            {{ item.label }}
                        </div>

                        <div class="others">
                            <div v-for="pro in textFieldArray" class="other-item ellipsis">
                                {{ item[pro] }}
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </a-select>

        <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa -->
        <online-pop-modal v-if="popFormModalShow" :id="popTableName" @register="registerPopModal" @success="getFormData" topTip></online-pop-modal>
    </div>
</template>

<script lang="ts">
  import type {Ref} from "vue";
  import { propTypes } from '/@/utils/propTypes';
  
  import {ref, computed, watch,toRaw, inject, onMounted, onBeforeUnmount, defineAsyncComponent } from 'vue'
  import { PlusOutlined,EditOutlined } from '@ant-design/icons-vue';
  const OnlinePopModal = defineAsyncComponent(() => import('../../auto/comp/OnlinePopModal.vue'));
  import { useModal } from '/@/components/Modal';
  import {useLinkTable } from './useLinkTable';
  import { useDebounceFn } from '@vueuse/core';
  import placeholderImage from '/@/assets/images/placeholderImage.png';
  
  const pageSize = 10;
  
  export default {
    name: "LinkTableSelect",
    components:{
      PlusOutlined,
      EditOutlined,
      OnlinePopModal
    },
    props:{
      valueField: propTypes.string.def(''),
      textField: propTypes.string.def(''),
      tableName: propTypes.string.def(''),
      multi: propTypes.bool.def(false),
      value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
      linkFields: propTypes.array.def([]),
      imageField: propTypes.string.def(''),
      // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
      editBtnShow: propTypes.bool.def(true),
      // update-end--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
    },
    emits:['change','update:value'],
    setup(props, { emit, attrs }){
      // Biểu mẫu hiện tạiid
      const tableId = inject<Ref<string | null>>('tableId', ref(null));
      const boxRef = ref<HTMLDivElement>();
      // Giá trị đã chọn
      const selectValue = ref<string[]>([]);
      const {auths, mainContentField, textFieldArray, selectOptions, reloadTableLinkOptions, addQueryParams, formatData, initFormData, getImageSrc,showImage } = useLinkTable(props);
        
      // Dùng để onlineTrong biểu mẫu Bật ra biểu mẫu khác
      const [registerPopModal, { openModal: openPopModal }] = useModal();
      // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      // đã thêm vàoonlineBản ghi liên kết một-nhiều và trường bảng khác phải tải lười (lazy load), nếu không việc xây dựng sẽ gọi vòng lặp và báo lỗi
      const popFormModalShow = ref(false);
      // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      const popTableName = computed(()=>{
        return props.tableName
      });
    
      const bindValue = computed(()=>{
        if(props.multi === true){
          return {
            // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-988】Vô hiệu hóa kiểu dropdown của component bản ghi liên kết không có hiệu lực
            ...attrs,
            // update-end--author:liaozhiyang---date:20240617---for：【TV360X-988】Vô hiệu hóa kiểu dropdown của component bản ghi liên kết không có hiệu lực
            mode: 'multiple',
          }
        }else{
          return {
            // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-988】Vô hiệu hóa kiểu dropdown của component bản ghi liên kết không có hiệu lực
            ...attrs,
            // update-end--author:liaozhiyang---date:20240617---for：【TV360X-988】Vô hiệu hóa kiểu dropdown của component bản ghi liên kết không có hiệu lực
          }
        }
      });
      
      // Thêm bản ghi
      function handleClickAdd(e){
        // Biểu mẫu thêm mới bằng popup
        e?.stopPropagation();
        e?.preventDefault();
        popFormModalShow.value = true;
        setTimeout(() => openPopModal(true, {}), 100);
      }

      function handleClickEdit(e, record){
        // Biểu mẫu chỉnh sửa bằng popup
        e?.stopPropagation();
        e?.preventDefault();
        if(auths.update == false){
          console.error('Người dùng hiện tại không có quyền chỉnh sửa!');
          return;
        }
        popFormModalShow.value = true;
        setTimeout(() => openPopModal(true, {
          isUpdate: true,
          record
        }), 100);
      }

      const CUSTOM_RELOAD_EVENT = 'custom:online:reload';

      onMounted(() => {
        if (boxRef.value) {
          boxRef.value.addEventListener(CUSTOM_RELOAD_EVENT, handleCustomReload)
        }
      })

      onBeforeUnmount(() => {
        if (boxRef.value) {
          boxRef.value.removeEventListener(CUSTOM_RELOAD_EVENT, handleCustomReload)
        }
      })

      /**
       * Sự kiện làm mới tùy chỉnh, dùng để làm mới các mục dropdown
       */
      function handleCustomReload() {
        reloadTableLinkOptions();
      }

      //Muốn dữ liệu hiển thị lại, trong biểu mẫu phải tồn tạiprops.valueFieldTrường tương ứng
      async function getFormData(data) {
        try {
          // 【QQYUN-7153】Đẩy sự kiện làm mới dữ liệu cho tất cả component bản ghi liên kết dropdown trong vùng truy vấn thuộc biểu mẫu hiện tại
          const queryFormLinkTables = document.querySelectorAll(`.online-list-${tableId.value} .jeecg-basic-table-form-container.online-query-form .link-table-select-box`);
          if (queryFormLinkTables && queryFormLinkTables.length > 0) {
            queryFormLinkTables.forEach((item) => item.dispatchEvent(new Event(CUSTOM_RELOAD_EVENT)))
          }
        } catch (e) {
          console.error(e)
        }

        //1.Làm mới các mục dropdown
        await reloadTableLinkOptions();
        //2.Đặt lại giá trị dropdown
        let temp = data[props.valueField];
        if (props.multi === true) {
          selectValue.value = [temp];
        } else {
          selectValue.value = temp;
        }
        //3.Kích hoạtchangesự kiện
        handleChange(selectValue.value)
      }
      
      function handleSearch(text){
        addQueryParams(text)
        reloadTableLinkOptions();
      }
      
      function handleChange(text){
        emitValue(text);
        if(!text){
          addQueryParams();
          reloadTableLinkOptions();
        }
      }
      
      function emitValue(text){
        let formData = {}
        let linkFieldArray = props.linkFields;
        let textArray = []
        if(!text){
          initFormData(formData, linkFieldArray)
        }else{
          let options = toRaw(selectOptions.value);
          console.log('options>>', options)
          let tempText = toRaw(text);
          if(tempText instanceof Array){
            textArray = [...tempText]
          }else{
            if(props.multi == true){
              textArray = tempText.split(',')
            }else{
              textArray = [tempText]
            }
          }
          let arr = options.filter(i=>textArray.indexOf(i[props.valueField])>=0)
          if(arr && arr.length>0){
            let record = {
              ...arr[0]
            };
            if(arr.length>1){
              for(let i=1;i<arr.length;i++){
                record = hebing(record, arr[i])
              }
            }
            let titleField = mainContentField.value;
            record[titleField] = record.label;
            initFormData(formData, linkFieldArray, record)
          }
        }
        formatData(formData)
        emit('change', textArray.join(',')||'', formData)
        emit('update:value', textArray.join(',')||'')
      }
      
      //Hợp nhất dữ liệu
      function hebing(oldObj, newObj){
        let record = {}
        Object.keys(oldObj).map(k=>{
          record[k] = (oldObj[k]||'')+','+(newObj[k]||'')
        })
        return record;
      }

      watch(()=>props.value, async (val)=>{
        if(val){
          if(props.multi == true){
            selectValue.value = val.split(',')
          }else{
            selectValue.value = val;
          }
          //Đảm bảo các giá trị khác của biểu mẫu hiển thị lại thành công
          if(props.linkFields && props.linkFields.length>0){
            emitValue(val);
          }
        }else{
          selectValue.value = []
        }
      }, {immediate: true})


      //Đảm bảo trang chỉnh sửa Lần đầu vào biểu mẫu có thể hiển thị lại thành công
      watch(()=>selectOptions.value, (val)=>{
        if(val && val.length>0){
          if(props.linkFields && props.linkFields.length>0){
            if(selectValue.value && selectValue.value.length>0){
              emitValue(selectValue.value);
            }
          }
        }
      });
      // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-389】Hình của bản ghi liên kết dạng dropdown và dạng thẻ bị vỡ, hãy cấp một hình mặc định
      const handleImageError = (event) => {
        event.target.src = placeholderImage;
      };
      // update-end--author:liaozhiyang---date:20240529---for：【TV360X-389】Hình của bản ghi liên kết dạng dropdown và dạng thẻ bị vỡ, hãy cấp một hình mặc định

      return {
        boxRef,
        selectValue,
        selectOptions,
        registerPopModal,
        popFormModalShow,
        popTableName,
        textFieldArray,
        handleClickAdd,
        handleClickEdit,
        getFormData,
        handleSearch:  useDebounceFn(handleSearch, 800),
        handleChange,
        bindValue,
        showImage,
        getImageSrc,
        auths,
        placeholderImage,
        handleImageError,
      }
    }
  }
</script>

<style scoped lang="less">
    .table-link-select{
        .opt-add{
            cursor: pointer;
            font-size: 14px;
            margin: 3px 0;
            padding: 0 8px;
            color: #1565c0;
        }
        .online-select-item{
            -webkit-box-sizing: border-box;
            box-sizing: border-box;
            cursor: pointer;
            padding: 0 12px;
            position: relative;
            border-bottom: 1px solid rgba(0,0,0,.06);
            line-height: 22px;
            padding: 6px 40px 6px 12px;
            min-height: 56px;
            display: flex;
            .left-avatar{
                -webkit-box-sizing: border-box;
                box-sizing: border-box;
                width: 44px;
                height: 44px;
                margin-right: 8px;
                >img{
                    width: 44px;
                    height: 44px;
                    border: 1px solid rgba(0, 0, 0, 0.06);
                    border-radius: 4px;
                }
            }
            .right-content{
                overflow: hidden;
                -webkit-flex: 1;
                flex: 1 1 0%;
                -ms-flex: 1;
                -webkit-box-sizing: border-box;
                box-sizing: border-box;
                // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
                .noEditBtn {
                  padding-left: 6px;
                }
                // update-end--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
                .anticon-edit{
                    &:hover{
                        color: #0c8fcf;
                    }
                }

                .label{
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                    display: -webkit-box;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    word-break: break-all;
                    color: rgb(51, 51, 51);
                    font-weight: bold;
                    line-height: 1.5;
                    margin-bottom: 4px;
                }
                .others{
                    margin-left: -7px;
                    box-sizing: border-box;
                    .ellipsis{
                        overflow: hidden;
                        text-overflow: ellipsis;
                        vertical-align: top;
                        white-space: nowrap;
                    }
                    .other-item{
                        -webkit-line-clamp: 3;
                        -webkit-box-orient: vertical;
                        border-right: 1px solid #9e9e9e;
                        color: #757575;
                        display: inline-block;
                        height: 1.2em!important;
                        line-height: 1em;
                        overflow: hidden;
                        padding: 0 7px;
                        text-overflow: ellipsis;
                        word-break: break-all;
                        // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
                        &:first-child,
                        &:last-child {
                          border-right: none;
                        }
                        &:nth-child(2) {
                          padding-left: 0;
                        }
                        // update-end--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
                    }
                }
            }
        }
    }
</style>
