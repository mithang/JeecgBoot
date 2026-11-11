<template>
  <PageWrapper title="Ví dụ danh sách thẻ" content="Đóng gói cơ bản">
    <CardList :params="params" :api="demoListApi" @getMethod="getMethod" @delete="handleDel">
      <template #header>
        <Button type="primary" color="error"> Nút1 </Button>
        <Button type="primary" color="success"> Nút2 </Button>
      </template>
    </CardList>
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { CardList } from '/@/components/CardList';
  import { Button } from '/@/components/Button';
  import { PageWrapper } from '/@/components/Page';
  import { demoListApi } from '/@/api/demo/table';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { notification } = useMessage();
  // Kèm theo tham số khi gọi API
  const params = {};

  let reload = () => {};
  // Lấy phương thức fetch nội bộ;
  function getMethod(m: any) {
    reload = m;
  }
  //Sự kiện nút xóa
  function handleDel(id) {
    console.log(id);
    notification.success({ message: `Đã xóa thành công${id}` });
    reload();
  }
</script>
