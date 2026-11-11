<template>
  <div>
    <div v-if="isDetailsMode">
      <p class="detailStr" :title="detailStr">{{ detailStr }}</p>
    </div>
    <a-row v-else class="j-select-row" type="flex" :gutter="8">
      <a-col class="left" :class="{ full: !showButton }">
        <!-- Hiển thị hiệu ứng tải -->
        <a-input v-if="loading" readOnly placeholder="Đang tải...">
          <template #prefix>
            <LoadingOutlined />
          </template>
        </a-input>
        <a-select
          v-else
          ref="select"
          v-model:value="selectValues.value"
          :placeholder="placeholder"
          :mode="multiple"
          :open="false"
          :disabled="disabled"
          :options="options"
          :maxTagCount="maxTagCount"
          @change="handleChange"
          style="width: 100%"
          @click="handleSelectClick"
          v-bind="attrs"
          :class="{ 'j-select-biz-wrap': isMultipleMode, 'j-select-biz-has-omitted-tags': hasOmittedTags }"
        >
          <template v-if="isCustomRenderTag" #tagRender="{ label, value, option}">
            <!-- update-begin--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng  -->
            <a-tag class="ant-select-selection-item j-select-biz-tag" style="margin-right: 4px">
              <JLeftEllipsis
                content-class="ant-select-selection-item-content j-select-biz-tag-content"
                :text="getTagFullText(label, value)"
                container-selector=".ant-select-selector"
                :reserve-width="tagReserveWidth"
              />
              <span v-if="!disabled" class="ant-select-selection-item-remove">
                <Icon icon="ant-design:close-outlined" size="12" @click="handleRemoveClick(value)"></Icon>
              </span>
            </a-tag>
            <!-- update-end--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng  -->
          </template>
        </a-select>
      </a-col>
      <a-col v-if="showButton" class="right">
        <a-button v-if="buttonIcon" :preIcon="buttonIcon" type="primary" @click="openModal(true)" :disabled="disabled">
          {{ buttonText }}
        </a-button>
        <a-button v-else type="primary" @click="openModal(true)" :disabled="disabled">
          {{ buttonText }}
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, inject, watch } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { getDepartPathNameByOrgCode } from '@/utils/common/compUtils';
  import JLeftEllipsis from '../JLeftEllipsis.vue';

  export default defineComponent({
    name: 'JSelectBiz',
    components: { LoadingOutlined, JLeftEllipsis },
    inheritAttrs: false,
    props: {
      showButton: propTypes.bool.def(true),
      buttonText: propTypes.string.def('Chọn'),
      disabled: propTypes.bool.def(false),
      placeholder: {
        type: String,
        default: 'Vui lòng chọn',
      },
      // Có hỗ trợ chọn nhiều không, mặc định true
      multiple: {
        type: String,
        default: 'multiple',
      },
      // Có đang tải không
      loading: propTypes.bool.def(false),
      // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-1820] Khi chọn nhiều component phòng ban và vị trí ở vùng truy vấn thì truyềnmaxTagCount
      // Hiển thị tối đa bao nhiêu tag tag
      maxTagCount: propTypes.oneOfType([propTypes.number, propTypes.string]),
      // update-end--author:liaozhiyang---date:20260826---for:【LHZP-1820] Khi chọn nhiều component phòng ban và vị trí ở vùng truy vấn thì truyềnmaxTagCount
      // buttonIcon
      buttonIcon: propTypes.string.def(''),
      // 【TV360X-1002] có phải chế độ chi tiết không
      isDetailsMode: propTypes.bool.def(false),
      //Có render tùy chỉnh tag khôngtag
      isCustomRenderTag: propTypes.bool.def(false),
      rowKey: propTypes.string.def('id'),
    },
    emits: ['handleOpen', 'change'],
    setup(props, { emit, refs }) {
      //Nhận tùy chọn hộp thả xuống
      const options = inject('selectOptions') || ref([]);
      //Nhận giá trị đã chọn
      const selectValues = inject('selectValues') || ref({});
      const attrs = useAttrs();
      const detailStr = ref('');

      //Lưu tên phòng ban
      const departNamePath = ref<Record<string, string>>({});

      const isMultipleMode = computed(() => props.multiple === 'multiple' || props.multiple === 'tags');
      // update-begin--author:liaozhiyang---date:20260811---for：【LHZP-513]Kiểu hiển thị của phòng ban một-nhiều trong online bị chồng chéo
      const hasOmittedTags = computed(
        () => typeof props.maxTagCount === 'number' && Array.isArray(selectValues.value) && selectValues.value.length > props.maxTagCount
      );
      const tagReserveWidth = computed(() => {
        const baseWidth = props.disabled ? 24 : 44;
        return hasOmittedTags.value ? baseWidth + 40 : baseWidth;
      });
      // update-end--author:liaozhiyang---date:20260811---for：【LHZP-513]Kiểu hiển thị của phòng ban một-nhiều trong online bị chồng chéo

      /**
       * Mở hộp popup
       */
      function openModal(isButton) {
        if (props.showButton && isButton) {
          emit('handleOpen');
        }
        if (!props.showButton && !isButton) {
          emit('handleOpen');
        }
      }

      /**
       * Sự kiện thay đổi giá trị hộp thả xuống
       */
      function handleChange(value) {
        selectValues.value = value;
        selectValues.change = true;
        emit('change', value);
      }
      
      /**
       * Render tùy chỉnh tag chọn nhiều (đường dẫn đầy đủ, phần rút gọn giao cho JLeftEllipsis)
       *
       * @param label
       * @param value
       * @param _isEllipsis Tương thích với cách gọi cũ
       */
      function tagRender(label, value, _isEllipsis) {
        return getTagFullText(label, value);
      }

      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng
      // Khi cấp bậc vị trí quá sâu, chữ bên phải đầy đủ, bên trái rút gọn
      function toDisplayText(val) {
        if (val == null || val === '') {
          return '';
        }
        if (typeof val === 'string') {
          return val;
        }
        if (typeof val === 'number' || typeof val === 'boolean') {
          return String(val);
        }
        if (Array.isArray(val)) {
          return val.map((item) => toDisplayText(item)).filter(Boolean).join('');
        }
        if (typeof val === 'object' && typeof val.children === 'string') {
          return val.children;
        }
        return '';
      }

      /** Lấy đầy đủ phòng ban/Đường dẫn vị trí (cập nhật phản ứng sau khi điền lại bất đồng bộ) */
      function getTagFullText(label, value) {
        if (departNamePath.value[value]) {
          return toDisplayText(departNamePath.value[value]);
        }
        const fallback = toDisplayText(label);
        if (props?.rowKey && props?.rowKey === 'orgCode') {
          getDepartPathNameByOrgCode(value, fallback, '').then((data) => {
            departNamePath.value[value] = toDisplayText(data) || fallback;
          });
        } else {
          getDepartPathNameByOrgCode('', fallback, value).then((data) => {
            departNamePath.value[value] = toDisplayText(data) || fallback;
          });
        }
        return fallback;
      }
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng

      /**
       * Mở popup khi nhấn vào hộp chọn
       * Loại trừ trường hợp nhấn nút xóa, tránh kích hoạt nhầm popup khi xóa
       */
      function handleSelectClick(event) {
        if (props.disabled) {
          return;
        }
        // Khi nhấn nút xóa (nút clear tích hợp hoặc nút xóa tag) thì không mở popup
        if (event.target.closest('.ant-select-selection-item-remove') || event.target.closest('.ant-select-clear')) {
          return;
        }
        openModal(false);
      }

      /**
       * tagXóa
       *
       * @param value
       */
      function handleRemoveClick(value) {
        // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-583] Sau khi đặt component phòng ban thành chỉ đọc vẫn có thể xóa
        if (props.disabled) {
          return;
        }
        // update-end--author:liaozhiyang---date:20260714---for：【LHZP-583] Sau khi đặt component phòng ban thành chỉ đọc vẫn có thể xóa
        if (selectValues?.value) {
          let values = selectValues?.value.filter((item) => item !== value);
          handleChange(values);
        }
      }
      // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết
      watch(
        [selectValues, options],
        () => {
          if (props.isDetailsMode) {
            if (Array.isArray(selectValues.value) && Array.isArray(options.value)) {
              const result = options.value.map((item) => item.label);
              detailStr.value = result.join(',');
            }
          }
        },
        { immediate: true }
      );
      // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết

      return {
        attrs,
        selectValues,
        options,
        handleChange,
        openModal,
        detailStr,
        tagRender,
        getTagFullText,
        handleRemoveClick,
        handleSelectClick,
        isMultipleMode,
        hasOmittedTags,
        tagReserveWidth,
      };
    },
  });
</script>
<style lang="less" scoped>
  .j-select-row {
    @width: 82px;
    flex-wrap: nowrap;
    align-items: flex-start;

    .left {
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng
      flex: 1 1 auto;
      width: calc(100% - @width - 8px);
      max-width: calc(100% - @width - 8px);
      min-width: 0;
      // update-end--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng
    }

    .right {
      flex: 0 0 @width;
      width: @width;
    }

    .full {
      width: 100%;
      max-width: 100%;
    }

    :deep(.ant-select-search__field) {
      display: none !important;
    }

    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng
    :deep(.ant-select) {
      max-width: 100%;
    }

    :deep(.ant-select-selector) {
      max-width: 100%;
      height: auto !important;
      min-height: 32px;
      overflow: hidden;
    }

    /* Chọn nhiều: tag xuống dòng mở rộng, không chen chúc trong một dòng */
    :deep(.j-select-biz-wrap .ant-select-selection-overflow) {
      flex-wrap: wrap;
      overflow: visible;
      max-width: 100%;
    }

    :deep(.ant-select-selection-overflow-item) {
      max-width: 100%;
      min-width: 0;
    }

    // update-begin--author:liaozhiyang---date:20260811---for：【LHZP-513]Kiểu hiển thị của phòng ban một-nhiều trong online bị chồng chéo
    :deep(.j-select-biz-has-omitted-tags .ant-select-selection-overflow) {
      flex-wrap: nowrap;
    }

    :deep(.j-select-biz-has-omitted-tags .ant-select-selection-overflow-item) {
      max-width: calc(100% - 48px);
    }

    :deep(.j-select-biz-has-omitted-tags .ant-select-selection-overflow-item-rest),
    :deep(.j-select-biz-has-omitted-tags .ant-select-selection-overflow-item-suffix) {
      max-width: none;
    }
    // update-end--author:liaozhiyang---date:20260811---for：【LHZP-513]Kiểu hiển thị của phòng ban một-nhiều trong online bị chồng chéo

    :deep(.j-select-biz-tag) {
      display: inline-flex !important;
      align-items: center;
      max-width: 100%;
      margin-inline-end: 4px;
      margin-bottom: 2px;
      overflow: hidden;
      box-sizing: border-box;
    }

    :deep(.j-select-biz-tag-content) {
      flex: 1 1 0 !important;
      min-width: 0 !important;
      display: block !important;
      font-size: 14px;
      overflow: hidden;
      white-space: nowrap !important;
      text-overflow: clip !important;
    }

    :deep(.j-select-biz-tag .ant-select-selection-item-remove) {
      flex-shrink: 0;
    }
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-602]Khi cấp bậc vị trí quá sâu, chữ bên phải hiển thị đầy đủ, bên trái rút gọn; chọn nhiều xuống dòng mở rộng
  }
  .detailStr {
    margin: 0;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
