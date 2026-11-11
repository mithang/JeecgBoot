<template>
  <!-- Hộp dropdown phân cấp (cascade) formComponent Tạm thời chỉ ởonlineSử dụng không cung cấp ra bên ngoàiapi -->
  <a-select show-search :filter-option="filterOption" :placeholder="placeholder" :value="selectedValue" @change="handleChange" allowClear style="width: 100%">
    <a-select-option v-for="(item, index) in dictOptions" :key="index" :value="item.store">
      <span style="display: inline-block; width: 100%" :title="item.label">{{ item.label }}</span>
    </a-select-option>
  </a-select>
</template>

<script lang="ts">
  import { defineComponent, watch, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';

  /**Lấy các tùy chọn dropdown*/
  const SELECT_OPTIONS_URL = '/online/cgform/api/querySelectOptions';

  export default defineComponent({
    name: 'OnlineSelectCascade',
    props: {
      table: { type: String, default: '' },
      txt: { type: String, default: '' },
      store: { type: String, default: '' },
      idField: { type: String, default: '' },
      pidField: { type: String, default: '' },
      pidValue: { type: String, default: '-1' },
      origin: { type: Boolean, default: false },
      condition: { type: String, default: '' },
      value: { type: String, default: '' },
      isNumber: { type: Boolean, default: false },
      placeholder: { type: String, default: 'Vui lòng chọn' },
    },
    emits: ['change', 'next'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      // Giá trị đã chọn
      const selectedValue = ref<any>('');
      // Mảng tùy chọn
      const dictOptions = ref<any[]>([]);
      const optionsLoad = ref(true);
      // Sự kiện thay đổi tùy chọn
      function handleChange(value) {
        console.log('handleChange', value);
        // Cái nàyvaluelà Giá trị được lưu trữ Thực tế còn cần lấyidgiá trị
        let temp = value || '';
        emit('change', temp);
        valueChangeThenEmitNext(temp);
      }

      // Nút đầu tiên Tải tùy chọn đi quacondition
      watch(
        () => props.condition,
        (val) => {
          optionsLoad.value = true;
          if (val) {
            loadOptions();
          }
        },
        { immediate: true }
      );

      // Nút bị liên động Tải tùy chọn đi quapidValue
      watch(
        () => props.pidValue,
        (val) => {
          if (val === '-1') {
            dictOptions.value = [];
          } else {
            loadOptions();
          }
        }
      );

      // Hiển thị lại giá trị
      watch(
        () => props.value,
        (newVal, oldVal) => {
          console.log('Sự kiện thay đổi giá trị', newVal, oldVal);
          if (!newVal) {
            // valuekhi không tồn tại--
            selectedValue.value = [];
            if (oldVal) {
              // NếuoldValtồn tại, Cần ném sự kiện lên trên
              emit('change', '');
              emit('next', '-1');
            }
          } else {
            // valuekhi tồn tại
            selectedValue.value = newVal;
          }
          if (newVal && !oldVal) {
            // Có giá trị mới, không có giá trị cũ Gán giá trị khi biểu mẫu tải lần đầu Cần ném một sự kiện ra ngoài Kích hoạt cấp dướioptionstải của
            handleFirstValueSetting(newVal);
          }
        },
        { immediate: true }
      );

      /**
       * Gán giá trị khi tải lần đầu
       */
      async function handleFirstValueSetting(value) {
        if (props.idField === props.store) {
          // NếuidTrường chính là trường lưu trữ Vậy thì có thể không cần gọi yêu cầu
          emit('next', value);
        } else {
          if (props.origin === true) {
            // Nếu là component đầu tiên của component liên động, hãy chờoptionssau khi tải xong, từoptionslấy giá trị trong đó
            await getSelfOptions();
            valueChangeThenEmitNext(value);
          } else {
            // Nếu là component tiếp sau của component liên động, căn cứ vào mục đã chọnvalueTải dữ liệu một lượt
            let arr = await loadValueText();
            valueChangeThenEmitNext(value, arr);
          }
        }
      }

      function loadOptions() {
        let params = getQueryParams();
        if (props.origin === true) {
          params['condition'] = props.condition;
        } else {
          params['pidValue'] = props.pidValue;
        }
        console.log('Tham số yêu cầu', params);
        dictOptions.value = [];
        defHttp.get({ url: SELECT_OPTIONS_URL, params }, { isTransformResponse: false }).then((res) => {
          if (res.success) {
            dictOptions.value = [...res.result];
            console.log('Kết quả yêu cầu', res.result, dictOptions);
          } else {
            $message.warning('Tải dữ liệu component liên động thất bại,Vui lòng kiểm tra cấu hình!');
          }
        });
      }

      function getQueryParams() {
        let params = {
          table: props.table,
          txt: props.txt,
          key: props.store,
          idField: props.idField,
          pidField: props.pidField,
        };
        return params;
      }

      function loadValueText() {
        return new Promise((resolve) => {
          if (!props.value) {
            selectedValue.value = [];
            resolve([]);
          } else {
            let params = getQueryParams();
            if (props.isNumber === true) {
              params['condition'] = `${props.store} = ${props.value}`;
            } else {
              params['condition'] = `${props.store} = '${props.value}'`;
            }
            defHttp.get({ url: SELECT_OPTIONS_URL, params }, { isTransformResponse: false }).then((res) => {
              if (res.success) {
                resolve(res.result);
              } else {
                $message.warning('Tải dữ liệu component liên động thất bại,Vui lòng kiểm tra cấu hình!');
                resolve([]);
              }
            });
          }
        });
      }

      /**
       * Lấy các tùy chọn dropdown
       */
      function getSelfOptions() {
        return new Promise((resolve) => {
          let index = 0;
          (function next(index) {
            if (index > 10) {
              resolve([]);
            }
            let arr = dictOptions.value;
            if (arr && arr.length > 0) {
              resolve(arr);
            } else {
              setTimeout(() => {
                next(index++);
              }, 300);
            }
          })(index);
        });
      }

      /**
       * sau khi giá trị thay đổi Cần ném sự kiện ra ngoài Kích hoạt thay đổi tùy chọn của nút cấp dưới
       */
      function valueChangeThenEmitNext(value, arr: any = []) {
        if (value && value.length > 0) {
          if (!arr || arr.length == 0) {
            arr = dictOptions.value;
          }
          let selected = arr.filter((item) => item.store === value);
          if (selected && selected.length > 0) {
            let id = selected[0].id;
            emit('next', id);
          }
        }
      }

      /**
       * Sàng lọc ô dropdown
       * @param input
       * @param option
       */
      const filterOption = (input: string, option: any) => {
        let labelIf = option.children()[0]?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
        if (labelIf) {
          return true;
        }
        return false;
      };
      
      return {
        selectedValue,
        dictOptions,
        handleChange,
        filterOption,
      };
    },
  });
</script>

<style scoped></style>
