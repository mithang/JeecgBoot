<template>
  <a-select v-bind="bindProps" @change="onChange" @search="onSearch" />
</template>

<script lang="ts">
  import { propTypes } from '/@/utils/propTypes';
  import { defineComponent, ref, watch, computed } from 'vue';

  // Dropdown có thể nhập (component này tạm thời chưa có ai dùng)
  export default defineComponent({
    name: 'JSelectInput',
    props: {
      options: propTypes.array.def(() => []),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // Tùy chọn options nội bộ
      const options = ref<any[]>([]);
      // Lắng nghe thay đổi của options bên ngoài và ghi đè lên options nội bộ options
      watch(
        () => props.options,
        () => {
          options.value = [...props.options];
        },
        { deep: true, immediate: true }
      );
      // Gộp props và attrs
      const bindProps: any = computed(() =>
        Object.assign(
          {
            showSearch: true,
          },
          props,
          attrs,
          {
            options: options.value,
          }
        )
      );

      function onChange(...args: any[]) {
        deleteSearchAdd(args[0]);
        emit('change', ...args);
        emit('update:value', args[0]);
      }

      function onSearch(value) {
        // Đã tìm thấy mục tương ứng chưa, nếu không tìm thấy thì thêm mục này
        let foundIt =
          options.value.findIndex((option) => {
            return option.value.toString() === value.toString();
          }) !== -1;
        // !!value : không thêm giá trị rỗng
        if (!foundIt && !!value) {
          deleteSearchAdd(value);
          // searchAdd Có phải được thêm qua tìm kiếm không
          options.value.push({ value: value, searchAdd: true });
          //onChange(value,{ value })
        } else if (foundIt) {
          onChange(value);
        }
      }

      // Xóa các mục vô dụng được tạo do tìm kiếm (người dùng nhập)
      function deleteSearchAdd(value = '') {
        let indexes: any[] = [];
        options.value.forEach((option, index) => {
          if (option.searchAdd) {
            if ((option.value ?? '').toString() !== value.toString()) {
              indexes.push(index);
            }
          }
        });
        // Đảo thứ tự rồi xóa mục trong mảng
        for (let index of indexes.reverse()) {
          options.value.splice(index, 1);
        }
      }

      return {
        bindProps,
        onChange,
        onSearch,
      };
    },
  });
</script>

<style scoped></style>
