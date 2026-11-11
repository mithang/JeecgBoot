<template>
  <JPopup v-bind="popupProps" @focus="handleFocus" />
</template>
<script lang="ts">
  import { computed, defineComponent, ref } from 'vue';
  //import { JPopup } from '/@/components/Form';
  import JPopup from '/@/components/Form/src/jeecg/components/JPopup.vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent, vModel } from '/@/components/jeecg/JVxeTable/utils';
  import { isEmpty } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxePopupCell',
    components: { JPopup },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, row, originColumn, cellProps, handleChangeCommon } = useJVxeComponent(props);
      const groupId = ref<string>('j-vxe-popup');
      const popupProps = computed(() => {
        // Giải thích logic mã: 【issues/5371] Bảng con một-nhiều popup thêm đa chọn
        return {
          ...cellProps.value,
          value: innerValue.value,
          field: originColumn.value.field || originColumn.value.key,
          code: originColumn.value.popupCode,
          fieldConfig: originColumn.value.fieldConfig,
          // orgFields: originColumn.value.orgFields,
          // destFields: originColumn.value.destFields,
          groupId: groupId.value,
          // update-begin--author:liaozhiyang---date:20260203---for:【issues/9212】JVxeTypes.popupSau khi truyền tham số cho thuộc tính param trong, dữ liệu popup trống
          param: originColumn.value.params ?? originColumn.value.param,
          // update-end--author:liaozhiyang---date:20260203---for:【issues/9212】JVxeTypes.popupSau khi truyền tham số cho thuộc tính param trong, dữ liệu popup trống
          sorter: originColumn.value.sorter,
          setFieldsValue: (values) => {
            if (!isEmpty(values)) {
              let popupValue = '';
              Object.keys(values).forEach((key) => {
                let currentValue = values[key];
                // Cột hiện tại gán trực tiếp, các cột khác gán thông qua vModel
                if (key === originColumn.value.key) {
                  popupValue = currentValue;
                } else {
                  vModel(currentValue, row, key);
                }
              });
              handleChangeCommon(popupValue);
            }
          },
        };
      });
      // Giải thích logic mã: 【issues/675] Dữ liệu popup của trường bảng con không cập nhật
      const handleFocus = () => {
        groupId.value = '';
      };
      return {
        handleFocus,
        popupProps,
      };
    },
    // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
