<!-- Trang người đứng đầu phòng ban -->
<template>
  <!--Tham chiếu bảng-->
  <BasicTable @register="registerTable"></BasicTable>
</template>

<script setup lang="ts" name="DepartmentHeadList">
  import { BasicTable } from '@/components/Table';
  import { useListPage } from '@/hooks/system/useListPage';
  import { userColumns } from '@/views/system/depart/depart.data';
  import { getDepartmentHead } from '../depart.api';
  import { computed, watch } from 'vue';
  const props = defineProps({
    data: { require: true, type: Object },
  });
  // Phòng ban hiện tạiid
  const departId = computed(() => props.data?.id);
 
  // Tham số và phương thức dùng chung của trang danh sách
  const { tableContext } = useListPage({
    tableProps: {
      api: getDepartmentHead,
      columns: userColumns,
      canResize: false,
      rowKey: 'id',
      tableSetting: { cacheKey: 'depart_head_list' },
      // Xử lý tham số trước khi gửi yêu cầu
      beforeFetch(params) {
        return Object.assign(params, { departId: departId.value });
      },
      showActionColumn: false,
      immediate: !!departId.value,
    },
  });
  // Đăng ký ListTable
  const [registerTable, { reload }] = tableContext;

  watch(
    () => props.data,
    () => reload()
  );
</script>

<style scoped lang="less"></style>
