<template>
  <div :class="[prefixCls]">
    <JSelectUser v-bind="getProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  //import { JSelectUser } from '/@/components/Form';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { isArray, isEmpty, isString } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeUserSelectCell',
    components: { JSelectUser },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, useCellDesign, originColumn } = useJVxeComponent(props);
      const { prefixCls } = useCellDesign('user-select');

      const selectedValue = computed(() => {
        let val: any = innerValue.value;
        if (val == null) {
          return val;
        }
        if (isEmpty(val)) {
          return [];
        }
        if (isArray(val)) {
          return val;
        }
        if (isString(val)) {
          // @ts-ignore
          return val.split(',');
        }
        return [val];
      });
      // @ts-ignore
      const multiple = computed(() => cellProps.value.multi != false);

      const getProps = computed(() => {
        return {
          ...cellProps.value,
          value: selectedValue.value,
          showButton: false,
          // Không cho phép tìm kiếm
          showSearch: false,
          // Đặt số lượng hiển thị tối đa
          // Giải thích logic mã: 【issues/7661]maxTagCount của component người dùng vxetable được truyền qua nghiệp vụ
          maxTagCount: originColumn.value.maxTagCount ?? 1,
          // Viết lại hiển thị nhắc nhở, bỏ dấu ba chấm
          maxTagPlaceholder: ({ length }) => '+' + length,
          // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết
          isDetailsMode: cellProps.value.disabledTable,
          // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết
        };
      });

      function handleChange(values) {
        handleChangeCommon(values.join(','));
      }

      return {
        prefixCls,
        selectedValue,
        multiple,
        cellProps,
        getProps,
        handleChange,
      };
    },
    enhanced: {
      switches: {
        visible: true,
      },
      translate: {
        enabled: false,
      },
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-vxe-cell-user-select';

  .@{prefix-cls} {
    // Giới hạn độ dài tối đa của tag là 100px, tránh xuống dòng khi chọn option có quá nhiều chữ
    .ant-select .ant-select-selection-overflow-item {
      max-width: 80px;
    }
  }
</style>
