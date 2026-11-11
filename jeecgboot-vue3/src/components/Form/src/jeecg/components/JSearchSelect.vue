<template>
  <!--Tìm kiếm thả xuống từ điển không đồng bộ-->
  <a-select
    v-if="async"
    v-bind="attrs"
    v-model:value="selectedAsyncValue"
    showSearch
    labelInValue
    allowClear
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="isDictTable ? false : filterOption"
    :notFoundContent="loading ? undefined : null"
    @focus="handleAsyncFocus"
    @search="loadData"
    @change="handleAsyncChange"
    @popupScroll="handlePopupScroll"
    :mode="multiple?'multiple':''"
    @select="handleSelect"
    @deselect="handleDeSelect"
  >
    <template #notFoundContent>
      <a-spin size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d?.value" :value="d?.value">{{ d?.text }}</a-select-option>
  </a-select>
  <!--Tìm kiếm dropdown từ điển-->
  <a-select
    v-else
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="filterOption"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{overflow: {adjustY: adjustY }}"
    @change="handleChange"
    :mode="multiple?'multiple':''"
    @select="handleSelect"
    @deselect="handleDeSelect"
  >
    <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d?.value" :value="d?.value">
      <span :class="[useDicColor && d.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && d.color}` }">{{ d?.text || d?.label }}</span>
    </a-select-option>
  </a-select>
</template>

<script lang="ts">
  import { useDebounceFn } from '@vueuse/core';
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';
  import { defHttp } from '/@/utils/http/axios';
  import { debounce } from 'lodash-es';
  import { setPopContainer } from '/@/utils';
  import { isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'JSearchSelect',
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number]),
      dict: propTypes.string,
      dictOptions: {
        type: Array,
        default: () => [],
      },
      async: propTypes.bool.def(false),
      placeholder: propTypes.string,
      popContainer: propTypes.string,
      pageSize: propTypes.number.def(10),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      //Mặc định bật điều chỉnh vị trí tràn trục Y, vì vậy khi không gian hiển thị không đủ, vị trí của hộp thả xuống sẽ tự động dịch lên trên, khiến ô nhập của Select bị che khuất. Cần lưu ý, mặc định là không gian hiển thị, chứ không phải không gian sở hữu
      // Giải thích logic mã: [issue/286]Vấn đề hộp tìm kiếm thả xuống bị che khuất
      adjustY:propTypes.bool.def(true),
      //Có kích hoạt ngay sau khi có giá trị khôngchange
      immediateChange: propTypes.bool.def(false),
      //Hỗ trợ truyền tham số truy vấn, ví dụ thông tin sắp xếp
      params:{
        type: Object,
        default: ()=>{}
      },
      //Có phải chọn nhiều không
      multiple:{
        type: Boolean,
        default: false
      },
      useDicColor: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, refs }) {
      const options = ref<any[]>([]);
      const loading = ref(false);
      // Giải thích logic mã: 【issues/897] Component JSearchSelect thêmclass/styleKiểu không có hiệu lực
      const attrs = useAttrs({'excludeDefaultKeys': false});
      const selectedValue = ref([]);
      const selectedAsyncValue = ref([]);
      const lastLoad = ref(0);
      // Có tải theo value khôngtext
      const loadSelectText = ref(true);
      // Bất đồng bộ(Bảng từ điển) - Sẽ dùng khi tải bằng cuộn
      let isHasData = true;
      let scrollLoading = false;
      let pageNo = 1;
      let searchKeyword = '';

      // Có phải bảng từ điển không
      const isDictTable = computed(() => {
        if (props.dict) {
          return props.dict.split(',').length >= 2
        }
        return false;
      })

      /**
       * Lắng nghe danh mụccode
       */
      watch(() => props.dict, () => {
        if (!props.dict) {
          return
        }
        if (isDictTable.value) {
          initDictTableData();
        } else {
          initDictCodeData();
        }
      }, {immediate: true});

      /**
       * Lắng nghevalue
       */
      watch(
        () => props.value,
        (val) => {
          if (val || val === 0) {
            initSelectValue();
          } else {
            selectedValue.value = [];
            selectedAsyncValue.value = [];
          }
        },
        { immediate: true }
      );
      /**
       * Lắng nghedictOptions
       */
      watch(
        () => props.dictOptions,
        (val) => {
          if (val && val.length >= 0) {
            options.value = [...val];
          }
        },
        { immediate: true }
      );
      /**
       * Truy vấn dữ liệu bất đồng bộ
       */
      const loadData = debounce(async function loadData(value) {
        if (!isDictTable.value) {
          return;
        }
        // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
        pageNo = 1;
        isHasData = true;
        searchKeyword = value;
 
        lastLoad.value += 1;
        const currentLoad = unref(lastLoad);
        options.value = [];
        loading.value = true;
        let keywordInfo = getKeywordParam(value);
        // Giải thích logic mã: [QQYUN-8800]JSearchSelectSự kiện search sẽ được kích hoạt khi nhập tiếng Trung chưa ghép chữ xong, dẫn đến SQL injection ở backend #6049------------
        keywordInfo = keywordInfo.replaceAll("'", '');
        // Định dạng code từ điển:table,text,code
        defHttp
          .get({
            url: `/sys/dict/loadDict/${props.dict}`,
            params: { keyword: keywordInfo, pageSize: props.pageSize, pageNo },
          })
          .then((res) => {
            loading.value = false;
            if (res && res.length > 0) {
              if (currentLoad != unref(lastLoad)) {
                return;
              }
              options.value = res;
              // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
              pageNo++;
            } else {
              // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
              pageNo == 1 && (isHasData = false);
            }
          });
      }, 300);
      /**
       * Khởi tạovalue
       */
      function initSelectValue() {
        // Giải thích logic mã: Component tìm kiếm dropdown mỗi lần chọn giá trị sẽ kích hoạt sự kiện lắng nghe value, kích hoạt phương thức này, nhưng thực tế không cần
        if (loadSelectText.value === false) {
          loadSelectText.value = true;
          return;
        }
        let { async, value, dict } = props;
        if (async) {
          if (!selectedAsyncValue || !selectedAsyncValue.key || selectedAsyncValue.key !== value) {
            defHttp.get({ url: `/sys/dict/loadDictItem/${dict}`, params: { key: value } }).then((res) => {
              if (res && res.length > 0) {
                //Kiểm tra component có phải chọn nhiều không
                if(props.multiple){
                  if(value){
                    let arr: any = [];
                    //Đa chọn trả về theo cách nối bằng dấu phẩy
                    let values = value.toString().split(',');
                    for (let i = 0; i < res.length; i++) {
                      let obj = {
                        key: values[i],
                        label: res[i],
                      };
                      arr.push(obj);
                      selectedValue.value.push(obj.key);
                    }
                    selectedAsyncValue.value = arr;
                  }
                } else {
                  let obj = {
                    key: value,
                    label: res,
                  };
                  if (props.value == value) {
                    selectedAsyncValue.value = { ...obj };
                  }
                  // Giải thích logic mã: Giá trị thay đổi sẽ kích hoạt sự kiện change--Dùng cho trang cấu hình bản ghi liên kết online
                  if(props.immediateChange == true){
                    emit('change', props.value);
                  }
                }
              }
            });
          }
        } else {
          // Giải thích logic mã: 【issues/8101]Component dict ở frontend gây tràn bộ nhớ: component tìm kiếm hỗ trợ chọn nhiều---
          if(!props.multiple){
            selectedValue.value = value.toString();
            if(props.immediateChange == true){
              emit('change', value.toString());
            }
          }else{
            //Trong trường hợp đa chọn cần chuyển thành mảng
            selectedValue.value = value.toString().split(',');
          }
        }
      }

      /**
       * Khởi tạo dữ liệu thả xuống của từ điển
       */
      async function initDictTableData() {
        let { dict, async, dictOptions, pageSize } = props;
        if (!async) {
          //Nếu tập hợp mục từ điển có dữ liệu
          if (dictOptions && dictOptions.length > 0) {
            options.value = dictOptions;
          } else {
            //Theo từ điểnCode, Khởi tạo mảng từ điển
            let dictStr = '';
            if (dict) {
              let arr = dict.split(',');
              if (arr[0].indexOf('where') > 0) {
                let tbInfo = arr[0].split('where');
                dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
              } else {
                dictStr = dict;
              }
              //Theo từ điểnCode, Khởi tạo mảng từ điển
              const dictData = await initDictOptions(dictStr);
              options.value = dictData;
            }
          }
        } else {
          if (!dict) {
            console.error('Component tìm kiếm chưa cấu hình mục từ điển');
          } else {
            // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
            pageNo = 1;
            isHasData = true;
            searchKeyword = '';

            //Bất đồng bộ ban đầu cũng tải một ít dữ liệu
            loading.value = true;
            let keywordInfo = getKeywordParam('');
            defHttp
              .get({
                url: `/sys/dict/loadDict/${dict}`,
                params: { pageSize: pageSize, keyword: keywordInfo, pageNo },
              })
              .then((res) => {
                loading.value = false;
                if (res && res.length > 0) {
                  options.value = res;
                  // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
                  pageNo++;
                } else {
                  // Giải thích logic mã: 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
                  pageNo == 1 && (isHasData = false);
                }
              });
          }
        }
      }

      /**
       * Truy vấn từ điển dữ liệu
       */
      async function initDictCodeData() {
        options.value = await initDictOptions(props.dict);
      }

      /**
       * Sự kiện thay đổi đồng bộ
       * */
      function handleChange(value) {
        //Chọn nhiều cũng sẽ kích hoạt sự kiện change, cần kiểm tra nếu là chọn nhiều thì không cần gán giá trị
        if(!props.multiple){
          selectedValue.value = value;
          callback();
        }
      }
      /**
       * Sự kiện thay đổi bất đồng bộ
       * */
      function handleAsyncChange(selectedObj) {
          // Trong trường hợp đơn chọn dùng sự kiện change
          if(!props.multiple){
            if (selectedObj) {
              selectedAsyncValue.value = selectedObj;
              selectedValue.value = selectedObj.key;
            } else {
              selectedAsyncValue.value = null;
              selectedValue.value = null;
              options.value = null;
              loadData('');
            }
            callback();
            // Khi nhấn x để xóa cần đặt loadSelectTexttrue
            selectedObj ?? (loadSelectText.value = true);
          }
      }

      /**
       * Sự kiện chọn giá trị bất đồng bộ
       * @param selectedObj
       */
      function handleSelect(selectedObj){
        let key = selectedObj;
        if(props.async){
          key = selectedObj.key;
        }
        //Trong trường hợp đa chọn dùng sự kiện select
        if(props.multiple && key){
          //Chỉ khi không đồng bộ mới cần thao tác thêm giá trị trong mảng selectedValue, trong trường hợp đồng bộ thì đi thẳng thao tác cập nhật giá trị
          if(props.async){
            selectedValue.value.push(key);
          }
          selectedObj ?? (loadSelectText.value = true);
          callback();
        }
      }
      
      /**
       * Sự kiện bỏ chọn giá trị không đồng bộ
       * @param selectedObj
       */
      function handleDeSelect(selectedObj){
        let key = selectedObj;
        if(props.async){
          key = selectedObj.key;
        }
        //Trong trường hợp đa chọn dùng sự kiện select
        if(props.multiple){
          //Chỉ khi không đồng bộ mới cần thao tác xóa giá trị trong mảng selectedValue, trong trường hợp đồng bộ thì đi thẳng thao tác cập nhật giá trị
          if(props.async){
            let findIndex = selectedValue.value.findIndex(item => item === key);
            if(findIndex != -1){
              selectedValue.value.splice(findIndex,1);
            }
          }
          selectedObj ?? (loadSelectText.value = true);
          callback();
        }
      }
      
      /**
       *Phương thức callback
       * */
      function callback() {
        loadSelectText.value = false;
        //Chọn đơn thực hiện trực tiếp thao tác cập nhật giá trị
        if(!props.multiple){
          emit('change', unref(selectedValue));
          emit('update:value', unref(selectedValue));
        } else {
          //Đa chọn cần chuyển mảng thành chuỗi
          emit('change', unref(selectedValue).join(","));
          emit('update:value', unref(selectedValue).join(","));
        }
      }
      /**
       * Lọc lựa chọnoption
       */
      function filterOption(input, option) {
        // Giải thích logic mã: issues/218 Ô tìm kiếm dropdown của biểu mẫu mọi chức năng tìm kiếm không có hiệu lực
        let value = '', label = '';
        try {
          value = option.value;
          label = option.children()[0].children;
        }catch (e) {
          console.log('Lấy mục dropdown thất bại', e)
        }
        let str = input.toLowerCase();
        return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
      }

      function getParentContainer(node) {
        // Giải thích logic mã: getPopupContainerLuôn có giá trị khiến logic của popContainer mãi không được thực thi, hãy chuyển nó lên phía trước để kiểm tra
        if (props.popContainer) {
          // Giải thích logic mã: 【QQYUN-9339]Khi có nhiều popup modal đều chứa component chọn nhiều trong danh mục thả xuống và component tìm kiếm thả xuống, khi mở một modal khác thì options của component không hiển thị
          return setPopContainer(node, props.popContainer);
        } else {
          if (typeof props.getPopupContainer === 'function') {
            return props.getPopupContainer(node);
          } else {
            return node?.parentNode;
          }
        }
      }

      //Lấy tham số từ khóa, hỗ trợ đặt thông tin sắp xếp
      function getKeywordParam(text){
        // Nếu đã đặt thông tin sắp xếp thì cần ghi thông tin sắp xếp, thêm sau từ khóa [orderby:create_time,desc]
        if(props.params && props.params.column && props.params.order){
          let temp = text||''
          
          // Giải thích logic mã: /issues/4905 Khi cấu hình trường của trình tạo biểu mẫu, chọn trường liên kết, khi thực hiện cấu hình nâng cao, không thể tải danh sách cơ sở dữ liệu, báo lỗi kiểm chứng chữ ký Sgin! #4905
          temp = temp+'[orderby:'+props.params.column+','+props.params.order+']'
          return encodeURI(temp);
          
        }else{
          return text;
        }
      }
      // Giải thích logic mã: 【TV360X-26]Sau khi chọn mục trong control tìm kiếm dropdown rồi bấm lại dropdown thì nên hiển thị các mục dropdown ban đầu, thay vì chỉ hiển thị kết quả đã chọn
      const handleAsyncFocus = () => {
        // Giải thích logic mã: 【issues/6681] truy vấn không đồng bộ không có hiệu lực
        if ((isObject(selectedAsyncValue.value) || selectedAsyncValue.value?.length) && isDictTable.value && props.async) {
          // Giải thích logic mã: 【TV360X-2062]Sau khi chọn dữ liệu trang thứ hai trong tìm kiếm dropdown, lần nhấn đầu tiên(nhận focus)Thanh cuộn chưa khôi phục về vị trí ban đầu và dữ liệu sẽ tải dữ liệu trang thứ hai(Chỉ nên tải dữ liệu của trang đầu tiên)
          options.value = [];
          initDictTableData();
        }
        // Giải thích logic mã: 【TV360X-2348]Khi nhận focus, mục options hiển thị nội dung trang đầu tiên (giải quyết việc khi thêm mới hiển thị nội dung không phải trang đầu tiên)
        if (Array.isArray(selectedAsyncValue.value) && selectedAsyncValue.value.length === 0 && isDictTable.value && props.async) {
          if (pageNo > 2) {
            options.value = [];
            initDictTableData();
          }
        }
        attrs.onFocus?.();
      };

      /**
       * 2024-07-30
       * liaozhiyang
       * 【TV360X-1898]Component JsearchSelect khi truyền vào định dạng bảng từ điển thì hỗ trợ tải cuộn
       * */
      const handlePopupScroll = async (e) => {
        // Chỉ bảng từ điển mới hỗ trợ tải khi cuộn
        if (isDictTable.value) {
          const { target } = e;
          const { scrollTop, scrollHeight, clientHeight } = target;
          if (!scrollLoading && isHasData && scrollTop + clientHeight >= scrollHeight - 10) {
            scrollLoading = true;
            let keywordInfo = getKeywordParam(searchKeyword);

            defHttp
              .get({ url: `/sys/dict/loadDict/${props.dict}`, params: { pageSize: props.pageSize, keyword: keywordInfo, pageNo } })
              .then((res) => {
                loading.value = false;
                if (res?.length > 0) {
                  // Ngăn trường hợp mã nguồn mở chỉ cập nhật code frontend mà chưa cập nhật code backend (nếu dữ liệu đầu tiên của trang 1 và trang 2 giống nhau thì tức là code backend chưa được cập nhật, không có phân trang)
                  if (JSON.stringify(res[0]) === JSON.stringify(options.value[0])) {
                    isHasData =  false;
                    return;
                  }
                  options.value.push(...res);
                  pageNo++;
                } else {
                  isHasData = false;
                }
              })
              .finally(() => {
                scrollLoading = false;
              })
              .catch(() => {
                pageNo != 1 && pageNo--;
              });
          }
        }
      };

      return {
        attrs,
        options,
        loading,
        isDictTable,
        selectedValue,
        selectedAsyncValue,
        loadData: useDebounceFn(loadData, 800),
        getParentContainer,
        filterOption,
        handleChange,
        handleAsyncChange,
        handleAsyncFocus,
        handlePopupScroll,
        handleSelect,
        handleDeSelect,
      };
    },
  });
</script>

<style scoped>
.colorText {
  display: inline-block;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
  border-radius: 8px;
  background-color: red;
  color: #fff;
  font-size: 12px;
}
</style>
