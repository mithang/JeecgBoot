<template>
  <Description @register="register" class="mt-4" />
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Description, DescItem, useDescription } from '/@/components/Description/index';
  const mockData = {
    username: 'test',
    nickName: 'VB',
    age: '123',
    phone: '15695909xxx',
    email: '190848757@qq.com',
    addr: 'Quận Tư Minh, TP. Hạ Môn',
    sex: 'Nam',
    certy: '3504256199xxxxxxxxx',
    tag: 'orange',
  };
  const schema: DescItem[] = [
    {
      field: 'username',
      label: 'Tên người dùng',
    },
    {
      field: 'nickName',
      label: 'Biệt danh',
      render: (curVal, data) => {
        return `${data.username}-${curVal}`;
      },
    },
    {
      field: 'phone',
      label: 'Điện thoại liên hệ',
    },
    {
      field: 'email',
      label: 'Email',
    },
    {
      field: 'addr',
      label: 'Địa chỉ',
    },
  ];
  export default defineComponent({
    components: { Description },
    setup() {
      const [register] = useDescription({
        title: 'useDescription',
        data: mockData,
        schema: schema,
      });
      return { mockData, schema, register };
    },
  });
</script>
