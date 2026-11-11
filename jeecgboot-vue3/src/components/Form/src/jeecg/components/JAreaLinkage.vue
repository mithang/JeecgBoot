<template>
  <JCascader
    v-bind="attrs"
    :disabled="disabled"
    :value="cascaderValue"
    :showLastLevelOnly="showLastLevelOnly"
    :options="getOptions"
    @change="handleChange"
  />
</template>
<script lang="ts">
  import { defineComponent, ref, watchEffect, computed } from 'vue';
  import JCascader from './JCascader.vue';
  import { Cascader } from 'ant-design-vue';
  import { provinceAndCityData, regionData, provinceOptions } from '../../utils/areaDataUtil';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray } from '/@/utils/is';

  export default defineComponent({
    name: 'JAreaLinkage',
    components: {
      JCascader,
      Cascader,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.object, propTypes.array, propTypes.string]),
      // Cấp hiển thị: province chỉ hiển thị tỉnh, city hiển thị tỉnh/thành phố, region hiển thị tỉnh/thành phố/quận huyện (ưu tiên hơn showArea)
      displayLevel: propTypes.oneOf(['province', 'city', 'region', 'all']),
      //Có hiển thị quận huyện không
      showArea: propTypes.bool.def(true),
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-707] Sửa lỗi Tỉnh/Thành/Quận huyện ở trạng thái vô hiệu hóa vẫn có thể bấm x để xóa
      disabled: propTypes.bool.def(false),
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-707] Sửa lỗi Tỉnh/Thành/Quận huyện ở trạng thái vô hiệu hóa vẫn có thể bấm x để xóa
      // Lưu trữ dữ liệu (khi all: dữ liệu truyền ra ngoài là mảng;province, city, regiongiá trị truyền ra ngoài là chuỗi)
      saveCode: propTypes.oneOf(['province', 'city', 'region', 'all']).def('all'),
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit }) {
      // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
      const showLastLevelOnly = computed(() => {
        return props.displayLevel === 'province' || props.displayLevel === 'city' || props.displayLevel === 'region';
      });
      // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
      const attrs = useAttrs();
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const cascaderValue = ref<(string | number)[]>([]);
      const getOptions = computed(() => {
        // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
        if (props.displayLevel) {
          if (props.displayLevel === 'all') {
            return regionData;
          } else if (props.displayLevel === 'province') {
            return provinceOptions;
          } else if (props.displayLevel === 'city') {
            return provinceAndCityData;
          } else if (props.displayLevel === 'region') {
            return regionData;
          }
        } else {
          if (props.showArea) {
            return regionData;
          } else {
            return provinceAndCityData;
          }
        }
        // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
      });
      /**
       * Lắng nghe thay đổi của value
       */
      watchEffect(() => {
        // Giải thích logic mã: 【TV360X-1223] tỉnh/thành/quận huyện đổi component mới
        if (props.value) {
          initValue();
        } else {
          cascaderValue.value = [];
        }
      });

      /**
       * Dữ liệu cũ có thể là mã quận huyện (ví dụ 120101), khi displayLevel là province cần hiển thị tỉnh tương ứng,
       */
      function buildDisplayPathFromCode(code, displayLevel) {
        if (!code && code !== 0) return [];
        const str = String(code).trim();
        if (!str) return [];
        const provinceCode = str.length >= 2 ? str.substring(0, 2) + '0000' : str;
        const cityCode = str.length >= 4 ? str.substring(0, 4) + '00' : null;
        const regionCode = str.length >= 6 ? str : null;
        const fullPath = [provinceCode];
        if (cityCode && cityCode !== provinceCode) fullPath.push(cityCode);
        if (regionCode && regionCode !== cityCode) fullPath.push(regionCode);
        if (displayLevel === 'province') return fullPath.slice(0, 1);
        if (displayLevel === 'city') return fullPath.slice(0, 2);
        return fullPath;
      }
      /**
       * Chuyển giá trị chuỗi thành mảng
       */
      function initValue() {
        let value = props.value ? props.value : [];
        // Giải thích logic mã: 【TV360X-501] tỉnh/thành/quận huyện đổi component mới
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          const arr = value.split(',');
          if (props.displayLevel) {
            // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
            const code = arr[0];
            cascaderValue.value = buildDisplayPathFromCode(code, props.displayLevel);
            // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
          } else {
            cascaderValue.value = transform(arr);
          }
        } else if (isArray(value)) {
          if (value.length) {
            if (props.displayLevel) {
              // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
              // Dữ liệu cũ khi saveCode là all thì lưu mảng đường dẫn đầy đủ [Tỉnh,Thành phố,Quận], cắt trực tiếp theo displayLevel
              if (value.length >= 2) {
                const len = props.displayLevel === 'province' ? 1 : props.displayLevel === 'city' ? 2 : Math.min(3, value.length);
                cascaderValue.value = value.slice(0, len);
              } else {
                const code = value[0];
                cascaderValue.value = buildDisplayPathFromCode(code, props.displayLevel);
              }
              // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694] Online hỗ trợ cấu hình riêng Tỉnh, Thành phố, Huyện
            } else {
              cascaderValue.value = transform(value);
            }
          } else {
            cascaderValue.value = [];
          }
        }
      }
      function transform(arr) {
        let result: any = [];
        if (props.saveCode === 'region') {
          const regionCode = arr[0];
          result = [`${regionCode.substring(0, 2)}0000`, `${regionCode.substring(0, 2)}${regionCode.substring(2, 4)}00`, regionCode];
        } else if (props.saveCode === 'city') {
          const cityCode = arr[0];
          result = [`${cityCode.substring(0, 2)}0000`, cityCode];
        } else if (props.saveCode === 'province') {
          const provinceCode = arr[0];
          result = [provinceCode];
        } else {
          result = arr;
        }
        return result;
      }
      /**
       * liaozhiyang
       * 2024-06-17
       * 【TV360X-1224] Giá trị mặc định component Tỉnh/Thành/Quận huyện truyền ra ngoài là chuỗi phân tách bằng dấu phẩy
       * */
      const send = (data) => {
        let result = data;
        if (result) {
          if (props.saveCode === 'all') {
            // Truyền vào là mảng
          } else {
            // Giá trị truyền vào là chuỗi
            result = data.join(',');
          }
        }
        emit('change', result);
        emit('update:value', result);
      };

      function handleChange(arr) {
        // Giải thích logic mã: 【TV360X-501] tỉnh/thành/quận huyện đổi component mới
        if (arr?.length) {
          let result: any = [];
          if (props.saveCode === 'region') {
            // Có thể chỉ có hai cấp (khi chọn Hồng Kông, chỉ có Tỉnh/Quận)
            result = [arr[arr.length - 1]];
          } else if (props.saveCode === 'city') {
            result = [arr[1]];
          } else if (props.saveCode === 'province') {
            result = [arr[0]];
          } else {
            result = arr;
          }
          send(result);
        } else {
          send(arr);
        }
        // emitData.value = args;
        // Việc thay đổi ở trênv-model:valueKhiến dữ liệu đã chọn không hiển thị
        // state.value = result;
      }
      
      return {
        cascaderValue,
        attrs,
        regionData,
        getOptions,
        handleChange,
        showLastLevelOnly,
      };
    },
  });
</script>
