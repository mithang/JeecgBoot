<template>
  <PageWrapper title="Ví dụ về component chi tiết">
    <Description title="Ví dụ cơ bản" :collapseOptions="{ canExpand: true, helpMessage: 'help me' }" :column="3" :data="mockData" :schema="schema" />

    <Description
      class="mt-4"
      title="Ví dụ dọc"
      layout="vertical"
      :collapseOptions="{ canExpand: true, helpMessage: 'help me' }"
      :column="2"
      :data="mockData"
      :schema="schema"
    />

    <Description @register="register" class="mt-4" />
    <Description @register="register1" class="mt-4" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Description, DescItem, useDescription } from '/@/components/Description/index';
  import { PageWrapper } from '/@/components/Page';

  const mockData: Recordable = {
    username: 'test',
    nickName: 'VB',
    age: '123',
    phone: '15695909xxx',
    email: '190848757@qq.com',
    addr: 'Quận Tư Minh, Hạ Môn',
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
      label: 'Số điện thoại liên hệ',
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
    components: { Description, PageWrapper },
    setup() {
      const [register] = useDescription({
        title: 'useDescription',
        data: mockData,
        schema: schema,
      });

      const [register1] = useDescription({
        title: 'Không có viền',
        bordered: false,
        data: mockData,
        schema: schema,
      });

      return { mockData, schema, register, register1 };
    },
  });
</script>
