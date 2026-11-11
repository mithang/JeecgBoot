<template>
  <div v-if="visible">
    <a-alert type="info" class="jeecg-comment-files" style="margin: 0">
      <template #message>
        <span class="j-icon">
          <a-upload multiple v-model:file-list="selectFileList" :showUploadList="false" :before-upload="beforeUpload">
            <span class="inner-button"><upload-outlined />Tải lên</span>
          </a-upload>
        </span>
      </template>
    </a-alert>

    <!-- Tệp đang tải lên -->
    <div class="selected-file-warp" v-if="selectFileList && selectFileList.length > 0">
      <div class="selected-file-list">
        <div class="item" v-for="item in selectFileList">
          <div class="complex">
            <div class="content">
              <!-- Hình ảnh -->
              <div v-if="isImage(item) && !item.exist" class="content-top" style="height: 100%">
                <div class="content-image" :style="{'height':'100%', 'backgroundImage': 'url('+getImageSrc(item)+')'}">
                  <!--  <img style="height: 100%;" :src="getImageSrc(item)">-->
                </div>
              </div>
              <!-- Tệp -->
              <template v-else>
                <div class="content-top">
                  <div class="content-icon" :style="{ background: 'url(' + (isImage(item) ? getImageTypeIcon() : getBackground(item)) + ')  no-repeat' }"></div>
                </div>
                <div class="content-bottom" :title="item.name">
                  <span>{{ item.name }}</span>
                </div>
              </template>
            </div>
            <div class="layer" :class="{'layer-image':isImage(item)}">
              <div class="next" @click="viewImage(item)">
                <div class="text">{{ item.name }} </div>
              </div>
              <div class="buttons">
                <div class="opt-icon">
                  <Tooltip title="Xóa">
                    <delete-outlined @click="handleRemove(item)" />
                  </Tooltip>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="item empty"></div><div class="item empty"></div><div class="item empty"></div> <div class="item empty"></div><div class="item empty"></div><div class="item empty"></div>
      </div>
    </div>
  </div>

</template>

<script lang="ts">
  import { toRaw, watch } from 'vue';
  import { useFileList } from './useComment';
  import { Tooltip } from 'ant-design-vue';
  import { UploadOutlined, FolderOutlined, DownloadOutlined, PaperClipOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import {useModal} from "/@/components/Modal";
  
  export default {
    name: 'UploadChunk',
    components: {
      Tooltip,
      UploadOutlined,
      FolderOutlined,
      DownloadOutlined,
      PaperClipOutlined,
      DeleteOutlined,
    },
    props: {
      visible: {
        type: Boolean,
        default: false,
      },
    },
    emits:['select'],
    setup(_p, {emit}) {
      const { selectFileList, beforeUpload, handleRemove, getBackground, isImage, getImageSrc, viewImage, getImageTypeIcon } = useFileList();

      const [registerModel, { openModal }] = useModal();
      
      function getUploadFileList() {
        let list = toRaw(selectFileList.value);
        console.log(list);
        return list;
      }
      
      function clear(){
        selectFileList.value = [];
      }
      
      watch(()=>selectFileList.value, (arr)=>{
        if(arr && arr.length>0){
          let name = arr[0].name;
          if(name){
            emit('select', name)
          }
        }
      });

      function showFileModal() {
        openModal(true, {})
      }

      function onSelectFileOk(temp) {
        let arr = selectFileList.value;
        // Giải thích logic mã: 【JHHB-524]Chức năng nhắn tin trong lịch trình của tôi, không thể chọn tệp đính kèm từ thư viện tệp
        temp.forEach((item) => {
          arr.push({
            ...item,
            exist: true
          })
        });
        selectFileList.value = arr;
      }

      return {
        selectFileList,
        beforeUpload,
        handleRemove,
        getBackground,
        getUploadFileList,
        clear,
        isImage, 
        getImageSrc, 
        getImageTypeIcon,
        viewImage,
        registerModel,
        showFileModal,
        onSelectFileOk
      };
    },
  };
</script>

<style lang="less" scoped>
  @import 'comment.less';
</style>
