<template>
  <a-tree-select
    v-if="show"
    allowClear
    labelInValue
    style="width: 100%"
    :getPopupContainer="(node) => node?.parentNode"
    :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="placeholder"
    :loadData="asyncLoadTreeData"
    :value="treeValue"
    :treeData="treeData"
    :multiple="multiple"
    v-bind="attrs"
    @change="onChange"
    :tree-checkable="treeCheckAble"
    :tagRender="tagRender"
  >
    <template #[name]="data" v-for="name in slotNamesGroup" :key="name">
      <slot :name="name" v-bind="data"></slot>
    </template>
  </a-tree-select>
</template>
<script lang="ts" setup>
  /*
   * Component tải cây không đồng bộ: thông qua việc truyền vào tên bảng, trường hiển thị, trường lưu trữ để tải một control cây
   * <j-tree-select dict="aa_tree_test,aad,id" pid-field="pid" ></j-tree-select>
   * */
  import { ref, watch, unref, nextTick, computed } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isObject, isArray } from '/@/utils/is';
  import { useI18n } from '/@/hooks/web/useI18n';
  enum Api {
    url = '/sys/dict/loadTreeData',
    view = '/sys/dict/loadDictItem/',
  }

  const props = defineProps({
    value: propTypes.string.def(''),
    placeholder: propTypes.string.def('Vui lòng chọn'),
    dict: propTypes.string.def('id'),
    parentCode: propTypes.string.def(''),
    pidField: propTypes.string.def('pid'),
    // Giải thích logic mã: JTreeSelectKhôi phục pidValue của component thành rỗng, nếu không sẽ ảnh hưởng đến ví dụ cây component tùy chỉnh------------
    pidValue: propTypes.string.def(''),
    hasChildField: propTypes.string.def(''),
    converIsLeafVal: propTypes.integer.def(1),
    condition: propTypes.string.def(''),
    multiple: propTypes.bool.def(false),
    loadTriggleChange: propTypes.bool.def(false),
    reload: propTypes.number.def(1),
    // Giải thích logic mã: issues/4173 Online JTreeSelectPhương thức changeOptions của control chưa có hiệu lực
    url: propTypes.string.def(''),
    params: propTypes.object.def({}),
    //Mặc định không có hộp chọn
    treeCheckAble: propTypes.bool.def(false),
    hiddenNodeKey: propTypes.string.def(''),
    //Khi chọn nhiều, render văn bản tag
    tagRender: propTypes.func,
  });
  const attrs = useAttrs();
  const { t } = useI18n();
  const emit = defineEmits(['change', 'update:value']);
  const slots = defineSlots();
  const { createMessage } = useMessage();
  //Dữ liệu thả xuống dạng cây
  const treeData = ref<any[]>([]);
  //Chọn dữ liệu
  const treeValue = ref<any>();
  const tableName = ref<any>('');
  const text = ref<any>('');
  const code = ref<any>('');
  const show = ref<boolean>(true);
  /**
   * Lắng nghe dữ liệu value và khởi tạo
   */
  watch(
    () => props.value,
    () => loadItemByCode(),
    { deep: true, immediate: true }
  );
  /**
   * Lắng nghe thay đổi dict
   */
  // Giải thích logic mã: 【issues/1283]Component JtreeSelect ban đầu đã gọi API hai lần
  watch(
    () => props.dict,
    () => {
      initDictInfo();
      loadRoot();
    }
  );
  // Giải thích logic mã: 【TV360X-87]Khi chỉnh sửa bảng cây không được chọn chính mình và các nút con cháu làm nút cha
  watch(
    () => props.hiddenNodeKey,
    () => {
      if (treeData.value?.length && props.hiddenNodeKey) {
        handleHiddenNode(treeData.value);
        treeData.value = [...treeData.value];
      }
    }
  );

  // Giải thích logic mã: VUEN-1056 15, Nghiêm trọng — Biểu mẫu cây online, khi thêm mới, nút cha bị trống
  watch(
    () => props.reload,
    async () => {
      treeData.value = [];
      // Giải thích logic mã: 【TV360X-88]Khi bảng cây online thêm mới lặp lại, dữ liệu nút cha tải không đầy đủ và nút con đã mở không tải lại
      show.value = false;
      nextTick(() => {
        show.value = true;
      });
      await loadRoot();
    },
    {
      immediate: false,
    }
  );

  /**
   * Lấy dữ liệu dropdown theo code và hiển thị lại
   */
  async function loadItemByCode() {
    if (!props.value || props.value == '0') {
      if(props.multiple){
        treeValue.value = [];
      }else{
        treeValue.value = undefined;
      }
    } else {
      // Giải thích logic mã: issues/4173 Online JTreeSelectPhương thức changeOptions của control chưa có hiệu lực
      if(props.url){
        getItemFromTreeData();
      }else{
        // Giải thích logic mã: 【issues/8093] Sau khi chọn node sẽ chuyển thành mã trước rồi mới hiển thị văn bản label
        if (props.value) {
          if (isArray(treeValue.value)) {
            let isNotRequestTransform = false;
            const value = isArray(props.value) ? props.value : props.value.split(',');
            isNotRequestTransform = value.every((value) => !!treeValue.value.find((item) => item.value === value));
            if (isNotRequestTransform) {
              return;
            }
          } else if (isObject(treeValue.value) && unref(treeValue).label != null) {
            if (props.value == unref(treeValue).value) {
              // Không cần gửi yêu cầu dịch nữa
              return;
            }
          }
        }
        let params = { key: props.value };
        let result = await defHttp.get({ url: `${Api.view}${props.dict}`, params }, { isTransformResponse: false });
        if (result.success) {
          if(props.multiple){
            let values = props.value.split(',');
            treeValue.value = result.result.map((item, index) => ({
              key: values[index],
              value: values[index],
              label: translateTitle(item),
            }));
          }else{
            treeValue.value = { key: props.value, value: props.value, label: translateTitle(result.result[0]) };
          }
          onLoadTriggleChange(result.result[0]);
        }
      }
    }
  }

  function onLoadTriggleChange(text) {
    //Chỉ khi chọn một mới kích hoạt
    if (!props.multiple && props.loadTriggleChange) {
      emit('change', props.value, text);
    }
  }

  /**
   * Dữ liệu khởi tạo
   */
  function initDictInfo() {
    let arr = props.dict?.split(',');
    tableName.value = arr[0];
    text.value = arr[1];
    code.value = arr[2];
  }

  /**
   * Tải dữ liệu cây thả xuống
   */
  async function loadRoot() {
    let params = {
      pid: props.pidValue,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.title = translateTitle(i.title);
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      // Giải thích logic mã: 【TV360X-87]Khi chỉnh sửa bảng cây không được chọn chính mình và các nút con cháu làm nút cha
      handleHiddenNode(res.result);
      treeData.value = [...res.result];
    } else {
      console.log('Kết quả truy vấn nút gốc của cây bất thường', res);
    }
  }

  /**
   * Dịch
   * @param text
   */
  function translateTitle(text) {
    if (text?.includes("t('") && t) {
      return new Function('t', `return ${text}`)(t);
    }
    return text;
  }
  /**
   * Tải dữ liệu bất đồng bộ
   */
  async function asyncLoadTreeData(treeNode) {
    if (treeNode.dataRef.children) {
      return Promise.resolve();
    }
    if (props.url) {
      return Promise.resolve();
    }
    let pid = treeNode.dataRef.key;
    let params = {
      pid: pid,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success) {
      for (let i of res.result) {
        i.title = translateTitle(i.title);
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      // Giải thích logic mã: 【TV360X-87]Khi chỉnh sửa bảng cây không được chọn chính mình và các nút con cháu làm nút cha
      handleHiddenNode(res.result);
      //Thêm nút con
      addChildren(pid, res.result, treeData.value);
      treeData.value = [...treeData.value];
    }
    return Promise.resolve();
  }

  /**
   * Tải nút con
   */
  function addChildren(pid, children, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!children || children.length == 0) {
            item.isLeaf = true;
          } else {
            item.children = children;
          }
          break;
        } else {
          addChildren(pid, children, item.children);
        }
      }
    }
  }

  /**
   * Sự kiện chọn nút cây
   */
  function onChange(value) {
    if (!value) {
      emitValue('');
    } else if (value instanceof Array) {
      emitValue(value.map((item) => item.value).join(','));
    } else {
      emitValue(value.value);
    }
    // Giải thích logic mã: 【issues/8093]Sau khi xóa sẽ chuyển thành mã trước rồi mới hiển thị chữ label
    if (isArray(value)) {
      // Khi chỉnh sửa hoặc xóa, giá trị đã chọn là bất đồng bộ (từ cấp hai trở lên) sẽ không hiển thịlabel
      value.forEach((item) => {
        if (item.label === undefined && item.value != null) {
          const findItem = treeValue.value.find((o) => o.value === item.value);
          if (findItem) {
            item.label = findItem.label;
          }
        }
      });
      treeValue.value = value;
    } else {
      treeValue.value = value;
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  /**
   * Kiểm tra xem cấu hình điều kiện kiểm chứng có sai không
   */
  function validateProp() {
    let mycondition = props.condition;
    return new Promise((resolve, reject) => {
      if (!mycondition) {
        resolve();
      } else {
        try {
          let test = JSON.parse(mycondition);
          if (typeof test == 'object' && test) {
            resolve();
          } else {
            createMessage.error('ComponentJTreeSelect-conditionTruyền giá trị sai, cần một chuỗi json!');
            reject();
          }
        } catch (e) {
          createMessage.error('ComponentJTreeSelect-conditionTruyền giá trị sai, cần một chuỗi json!');
          reject();
        }
      }
    });
  }

  // Giải thích logic mã: issues/4173 Online JTreeSelectPhương thức changeOptions của control chưa có hiệu lực
  watch(()=> [props.url, props.params], async (val)=>{
    if(val){
      await loadRootByUrl();
    }
  });

  /**
   * Tải dữ liệu theo địa chỉ yêu cầu tùy chỉnh
   */
  async function loadRootByUrl(){
    let url = props.url;
    let params = props.params;
    let res = await defHttp.get({ url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.title = translateTitle(i.title ?? i.name);
        i.key = i.value ?? i.id;
        i.isLeaf = !!i.leaf;
      }
      // Giải thích logic mã: 【TV360X-87]Khi chỉnh sửa bảng cây không được chọn chính mình và các nút con cháu làm nút cha
      handleHiddenNode(res.result);
      treeData.value = [...res.result];
    } else {
      console.log('Kết quả truy vấn nút gốc của cây bất thường', res);
    }
  }

  /**
   * Dịch tùy chọn theo dữ liệu cây có sẵn
   */
  function getItemFromTreeData(){
    let data = treeData.value;
    let arr = []
    findChildrenNode(data, arr);
    if(arr.length>0){
      treeValue.value = arr
      onLoadTriggleChange(arr[0]);
    }
  }

  /**
   * Đệ quy tìm nút con
   * @param data
   * @param arr
   */
  function findChildrenNode(data, arr){
    let val = props.value;
    if(data && data.length){
      for(let item of data){
        if(val===item.value){
          arr.push({
            key: item.key,
            value: item.value,
            label: item.label||item.title
          })
        }else{
          findChildrenNode(item.children, arr)
        }
      }
    }
  }

  /**
   * 2024-05-23
   * liaozhiyang
   * Lọc bỏ nút đã chỉ định(Bao gồm cả nút con cháu của nó)
   */
  function handleHiddenNode(data) {
    if (props.hiddenNodeKey && data?.length) {
      for (let i = 0, len = data.length; i < len; i++) {
        const item = data[i];
        if (item.key == props.hiddenNodeKey) {
          data.splice(i, 1);
          i--;
          len--;
          return;
        }
      }
    }
  }
  /**
   * 2024-07-30
   * liaozhiyang
   * 【issues/6953]Component JTreeSelect có thể hỗ trợ tương ứng của antdva-tree-select Slot của component
   */
  const slotNamesGroup = computed(() => {
    const native: string[] = [];
    if (isObject(slots)) {
      for (const name of Object.keys(slots)) {
        native.push(name);
      }
    }
    return native;
  });
  // onCreated
  validateProp().then(() => {
    initDictInfo();
    loadRoot();
    loadItemByCode();
  });
</script>

<style lang="less"></style>
