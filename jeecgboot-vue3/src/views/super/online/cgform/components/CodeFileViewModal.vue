<template>
  <BasicModal :height="modalHeight" @register="registerModal" okText="" cancelText="Đóng" :width="1200" :defaultFullscreen="true" :canFullscreen="false" @ok="onDownloadGenerateCode" :wrapClassName="prefixCls">
    <template #title> <info-circle-two-tone /> Xem trước mã trực tuyến </template>
    <div>
      <a-row class="code-gen">
        <div class="left" style="border-right: 1px solid #eee" :style="{width:`calc(${defaultLeftWidthRate}% - 2px)`}">
          <div :style="{ height: height + 'px', overflowY: 'auto' }">
            <a-directory-tree v-if="treeData.length" :defaultExpandAll="true" :tree-data="treeData" @select="showCodeContent"> </a-directory-tree>
          </div>
        </div>
        <div class="resize"/>
        <div class="right" :style="{width:`calc(100% - ${defaultLeftWidthRate}% - 2px)`}">
          <JCodeEditor 
              v-if="activeCodeContent" 
              v-model:value="activeCodeContent"
              theme="idea"
              :language="language" 
              :fullScreen="false" 
              :lineNumbers="true" 
              :height="height + 'px'" 
              :disabled="true"
              :language-change="true">
          </JCodeEditor>
          <a-empty v-else style="margin-top: 50px" description="Vui lòng chọn tệp phía bên trái, hiển thị mã chi tiết" />
        </div>
      </a-row>
    </div>
    <template #footer>
      <a-button @click="handleClose">Đóng</a-button>
      <a-button type="primary" @click="onDownloadGenerateCode">Tải xuống máy cục bộ</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  /**
   * onlineSau khi sinh mã, hỗ trợ xem trước trực tuyến
   */
  import {ref, defineComponent, reactive, onMounted} from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { InfoCircleTwoTone } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { JCodeEditor } from '/@/components/Form';
  import 'codemirror/theme/idea.css';
  import { useDesign } from '/@/hooks/web/useDesign';

  export default defineComponent({
    name: 'CodeFileViewModal',
    components: {
      BasicModal,
      InfoCircleTwoTone,
      JCodeEditor,
    },
    emits: ['download', 'register', 'close'],
    setup(_p, { emit }) {
      const codeList = ref([]);
      const pathKey = ref('');
      const treeData = ref<any[]>([]);
      const expandStatus = ref(false);
      const height = window.innerHeight - 142;
      const language = ref('java');
      const activeCodeContent = ref('');
      let codeMap = reactive({});

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        codeMap = reactive({});
        activeCodeContent.value = '';

        codeList.value = data.codeList;
        pathKey.value = data.pathKey;
        getTreeData();
        dragDiv();
        expandStatus.value = true;
      });
      const { prefixCls } = useDesign('online-codeFileViewModal');

      function getTreeData() {
        let list = getPlainList();
        let root = list[0];
        assembleTree(root, list);
        let treeList: any[] = [];
        const getFinalTreeData = function (root) {
          if (root.children) {
            let children = root.children;
            if (children.length == 1) {
              getFinalTreeData(children[0]);
            } else if (children.length > 1) {
              treeList.push(root);
            }
          }
        };
        getFinalTreeData(root);
        console.log(123, treeList)
        treeData.value = treeList;
        setTimeout(()=>{
          loadFirstFileContent(root)
        }, 300)
      }

      /**
       * Mặc định tải tệp đầu tiên
       * @param root
       */
      async function loadFirstFileContent(root){
        const getFirstFile = function(temp){
          if(temp.isLeaf === true){
            return temp;
          }else{
            if (temp.children) {
              return getFirstFile(temp.children[0])
            }
          }
        }
        let node = getFirstFile(root);
        if(node && node.isLeaf === true){
          let path = node.path;
          if (!codeMap[path]) {
            await loadCode(path);
          }
          language.value = getCodeLanguage(path);
          activeCodeContent.value = codeMap[path];
        }
      }

      function assembleTree(root, list) {
        for (let item of list) {
          if (root.key == item.pid) {
            let children = root.children;
            if (!children) {
              root.children = [];
            }
            root.children.push(item);
            assembleTree(item, list);
          }
        }
      }

      function getAbsolutePath(arr, index){
        let i=0;
        let str = ''
        while(i<=index){
          str+=arr[i];
          i++;
        }
        return str;
      }
      
      function getPlainList() {
        // title key pid
        let list: any[] = [];
        let list2: any[] = [];
        let arr: any[] = codeList.value;
        for (let item of arr) {
          let temp = item.replace(new RegExp('\\\\', 'g'), '/').replace('生成成功：', '').trim();
          if (temp) {
            let arr2 = temp.split('/');
            for (let i = 0; i < arr2.length; i++) {
              let a = arr2[i];
              let id = getAbsolutePath(arr2, i)
              //  let str = getAbsolutePath(arr2, i)
              if (a) {
                let item = {
                  title: a,
                  key: id,
                };
                if (a == 0) {
                } else {
                  let lastKey = getAbsolutePath(arr2, i-1)
                          //arr2[i - 1] + (i - 1);
                  if (lastKey) {
                    item['pid'] = lastKey;
                  } else {
                  }
                }
                if (i == arr2.length - 1) {
                  //Phần tử cuối cùng
                  item['isLeaf'] = true;
                  item['path'] = temp;
                }
                if (list2.indexOf(id) < 0 || i == arr2.length - 1) {
                  list.push(item);
                  list2.push(id);
                }
              }
            }
          }
        }
        return list;
      }

      function handleClose() {
        closeModal();
        emit('close')
      }
      function onDownloadGenerateCode() {
        emit('download');
      }

      function getCodeLanguage(path) {
        if (path.endsWith('xml')) {
          return 'application/xml';
        }
        if (path.endsWith('sql')) {
          return 'text/x-sql';
        }
        if (path.endsWith('vue')) {
          return 'text/x-vue';
        }
        if (path.endsWith('ts')) {
          return 'text/typescript';
        } else {
          return 'text/x-java';
        }
      }

      async function showCodeContent(_selectedKeys, e) {
        let node = e.node.dataRef;
        if (node.isLeaf) {
          let path = node.path;
          if (!codeMap[path]) {
            await loadCode(path);
          }
          language.value = getCodeLanguage(path);
          activeCodeContent.value = codeMap[path];
        }
      }
      function loadCode(path) {
        return new Promise((resolve) => {
          let params = {
            path: encodeURI(path),
            pathKey: pathKey.value
          };
          defHttp.get({ url: '/online/cgform/api/codeView', params }, { isTransformResponse: false }).then((data) => {
            if (!data || data.size === 0) {
              message.warning('Tải tệp xuống thất bại');
              return;
            }else{
              if (data.message) {
                message.warning(data.message);
                return;
              }
            }
            
            let blob = new Blob([data]);
            let reader = new FileReader();
            reader.readAsText(blob, 'utf8');
            reader.onload = function () {
              let content = this.result;
              codeMap[path] = content;
              resolve(1);
            };
          });
        });
      }

      //update-begin---author:wangshuai---date:2025-08-25---for:【QQYUN-13519】Phần xem trước mã này cho phép kéo sang trái/phải---
      //Độ rộng tính từ phía bên trái
      const defaultLeftWidthRate = ref(24)
      //Phía bên trái hiển thị tỷ lệ phần trăm tối thiểu
      const leftMin = ref<number>(14);
      //Phía bên phải hiển thị tỷ lệ phần trăm tối thiểu
      const rightMin = ref<number>(30);
      
      /**
       * divSự kiện kéo thả
       * 
       */
      function dragDiv(){
        let resize:any = document.getElementsByClassName('resize');
        let box:any = document.getElementsByClassName('code-gen');
        let left:any = document.getElementsByClassName('left');
        for (let i = 0; i < resize.length; i++) {
          // Sự kiện nhấn chuột xuống
          resize[i].onmousedown = function (e: any) {
            //toàn bộdivđộ rộng của
            let boxWidth = box[i].offsetWidth;
            //Độ rộng của phía bên trái
            let leftWidth = left[i].offsetWidth;
            // Vị trí bắt đầu
            let startX = e.clientX;
            // Sự kiện kéo chuột
            document.onmousemove = function (e: any) {
              // Vị trí kết thúc
              let endX = e.clientX;
              // Lấy khoảng cách rộng/cao khi kéo chuột: lấy giá trị tuyệt đối
              let distX = Math.abs(endX - startX);
              // Kéo thả sang phải
              if (endX > startX) {
                let moveLate = parseFloat(((leftWidth + distX) / boxWidth * 100).toFixed(4));
                let right = parseFloat('100') - moveLate;
                // Kéo sang phải lớn hơn giá trị tối thiểu của phía bên phải mới cho phép kéo thả
                if (right > rightMin.value) {
                  defaultLeftWidthRate.value = moveLate;
                }
              }
              // Kéo thả sang trái
              if (endX < startX) {
                let moveLate = parseFloat(((leftWidth - distX) / boxWidth * 100).toFixed(4));
                // Kéo sang trái lớn hơn giá trị tối thiểu của phía bên trái mới cho phép kéo thả
                if (moveLate > leftMin.value) {
                  defaultLeftWidthRate.value = moveLate;
                }
              }
            }
            // Sự kiện thả chuột ra
            document.onmouseup = function (e: any) {
              document.onmousemove = null;
              document.onmouseup = null;
              //Khi bạn không cần tiếp tục nhận thông điệp chuột nữa thì nên gọiReleaseCapture()Giải phóng bỏ
              resize[i].releaseCapture && resize[i].releaseCapture();
            }
            //Hàm này thiết lập bắt chuột trong cửa sổ chỉ định thuộc về luồng hiện tại
            resize[i].setCapture && resize[i].setCapture();
            return false;
          }
        }
      }
      //update-end---author:wangshuai---date:2025-08-25---for:【QQYUN-13519】Phần xem trước mã này cho phép kéo sang trái/phải---

      return {
        registerModal,
        codeList,
        onDownloadGenerateCode,
        handleClose,
        treeData,
        showCodeContent,
        activeCodeContent,
        expandStatus,
        height,
        language,
        prefixCls,
        modalHeight:1000,
        defaultLeftWidthRate
      };
    },
  });
</script>

<style lang="less">
  // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-227】Tối ưu kiểu dáng xem trước trực tuyến trong popup sinh mã
  @prefix-cls: ~'@{namespace}-online-codeFileViewModal';
  .@{prefix-cls} {
    .scrollbar__wrap {
      margin-bottom: 0 !important;
    }
    .ant-modal-footer {
      padding-top: 32px;
    }
     //update-begin---author:wangshuai---date:2025-08-25---for:【QQYUN-13519】Phần xem trước mã này cho phép kéo sang trái/phải---
     .code-gen .left {
      position: relative;
      width: calc(24% - 2px);
      height: 100%;
      overflow-x: auto;
    }

    .code-gen .resize {
      position: relative;
      width: 4px;
      color: #f7f7f7;
      background: #f7f7f7;
      cursor: w-resize;
    }

    .code-gen .right {
      position: relative;
      width: calc(76% - 2px);
      height: 100%;
      overflow-x: auto;
    }
    .ant-tree-node-content-wrapper{
      display: flex;
    }
    //update-end---author:wangshuai---date:2025-08-25---for:【QQYUN-13519】Phần xem trước mã này cho phép kéo sang trái/phải---
  }
  // update-end--author:liaozhiyang---date:20240617---for：【TV360X-227】Tối ưu kiểu dáng xem trước trực tuyến trong popup sinh mã
</style>
