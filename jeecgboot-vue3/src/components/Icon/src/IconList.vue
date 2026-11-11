<template>
  <div class="flex justify-between ml-4 mr-5 mb-1" v-if="isSearch">
    <a-input :placeholder="t('component.icon.search')" v-model:value="searchIconValue" @change="debounceHandleSearchChange" allowClear />
  </div>
  <div v-if="getPaginationList.length > 0">
    <div class="overflow-auto">
      <ul class="px-2 icon-list" style="padding-right: 0">
        <li
          v-for="icon in getPaginationList"
          :key="icon"
          :class="currentSelect === icon ? 'icon-border' : ''"
          class="p-2 w-1/8 cursor-pointer mr-1 mt-1 flex justify-center items-center border border-solid hover:border-primary"
          @click="handleClick(icon)"
          :title="icon"
        >
          <SvgIcon v-if="isSvgMode" :name="icon" />
          <Icon :icon="icon" v-else />
        </li>
      </ul>
    </div>
    <div class="flex py-2 items-center justify-content-right mr-10 mt-5" v-if="getTotal >= pageSize && isPage">
      <Pagination
        showLessItems
        v-model:current="current"
        :page-size-options="pageSizeOptions"
        size="small"
        v-model:pageSize="pageSize"
        v-model:total="getTotal"
        @change="handlePageChange"
      />
    </div>
  </div>
  <template v-else
    ><div class="p-5"><empty /></div>
  </template>
</template>

<script lang="ts" name="icon-list">
  import SvgIcon from '@/components/Icon/src/SvgIcon.vue';
  import Icon from '@/components/Icon/src/Icon.vue';
  import { defineComponent, ref, unref, watchEffect} from 'vue';
  import { useDebounceFn } from '@vueuse/core';
  import { usePagination } from '@/hooks/web/usePagination';
  import { propTypes } from '@/utils/propTypes';
  import { useCopyToClipboard } from '@/hooks/web/useCopyToClipboard';
  import { useI18n } from '@/hooks/web/useI18n';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Empty, Pagination } from 'ant-design-vue';
  export default defineComponent({
    components: {
      SvgIcon,
      Icon,
      Empty,
      Pagination,
    },
    emits: ['update:value'],
    props: {
      currentList: propTypes.any.def([]),
      clearSelect: propTypes.bool.def(false),
      copy: propTypes.bool.def(false),
      isSvgMode: propTypes.bool.def(false),
      isPage: propTypes.bool.def(false),
      isSearch: propTypes.bool.def(false),
      value: propTypes.string.def(''),
    },
    setup(props, { emit }) {
      //Giá trị đã chọn
      const currentSelect = ref('');
      //Số trang
      const current = ref<number>(1);
      //Số bản ghi mỗi trang
      const pageSize = ref<number>(140);
      //Hiển thị phân trang dropdown
      const pageSizeOptions = ref<any>(['10', '20', '50', '100', '140']);
      //Giá trị tìm kiếm thả xuống
      const searchIconValue = ref<string>('');
      const { clipboardRef, isSuccessRef } = useCopyToClipboard(props.value);

      const currentList = ref<any>(props.currentList);
      const { getPaginationList, getTotal, setCurrentPage, setPageSize } = usePagination(currentList, pageSize.value);
      const debounceHandleSearchChange = useDebounceFn(handleSearchChange, 100);
      const { t } = useI18n();
      const { createMessage } = useMessage();
      /**
       * Tìm kiếm
       * @param e
       */
      function handleSearchChange(e: ChangeEvent) {
        const value = e.target.value;
        console.log("value::::",value)
        // Giải thích logic mã: 【issues/4947]Trang chỉnh sửa menu, mẫu chọn biểu tượng menu, chuyển đổi số lượng hiển thị mỗi trang không có hiệu lực------------
        setCurrentPage(1);
        current.value = 1;
        if (!value) {
          currentList.value = props.currentList;
          return;
        }
        currentList.value = props.currentList.filter((item) => item.includes(value));
      }

      /**
       * Nhấp biểu tượng để đặt lại số trang
       */
      function currentSelectClick() {
        setCurrentPage(1);
        setPageSize(140);
        current.value = 1;
        pageSize.value = 140;
        currentList.value = props.currentList;
        searchIconValue.value = '';
      }

      function handlePageChange(page: number, size: number) {
        // Giải thích logic mã: 【issues/4947]Trang chỉnh sửa menu, mẫu chọn biểu tượng menu, chuyển đổi số lượng hiển thị mỗi trang không có hiệu lực------------
        current.value = page;
        pageSize.value = size;
        setPageSize(size);
        setCurrentPage(page);
      }

      /**
       * Sự kiện nhấp biểu tượng
       * @param icon
       */
      function handleClick(icon: string) {
        if (props.clearSelect === true) {
          if (currentSelect.value === icon) {
            currentSelect.value = '';
          } else {
            currentSelect.value = icon;
          }
        } else {
          currentSelect.value = icon;
          if (props.copy) {
            clipboardRef.value = icon;
            if (unref(isSuccessRef)) {
              createMessage.success(t('component.icon.copy'));
            }
          }
        }
        emit('update:value', currentSelect.value);
      }

      /**
       * Lấy biểu tượng
       */
      function getIcon() {
        return currentSelect.value;
      }

      watchEffect(() => {
        currentSelect.value = props.value;
      });

      return {
        searchIconValue,
        debounceHandleSearchChange,
        getTotal,
        getPaginationList,
        handlePageChange,
        handleClick,
        currentSelect,
        t,
        pageSize,
        pageSizeOptions,
        current,
        getIcon,
        currentSelectClick,
      };
    },
  });
</script>

<style scoped lang="less">
  ul li {
    width: 32px;
    height: 32px;
    float: left;
    display: flex;
    margin: 6px;
  }
  ul span {
    font-size: 1.5rem !important;
    padding: 0.2rem;
    margin: 0.3rem;
  }
  .icon-border {
    border: 1px solid rgba(24, 144, 255) !important;
  }
  .justify-content-right {
    justify-content: right;
  }
</style>
