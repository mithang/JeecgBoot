<template>
  <Card title="Điều hướng nhanh" v-bind="$attrs">
    <template v-for="item in navItems" :key="item">
      <CardGrid @click="goPage">
        <span class="flex flex-col items-center">
          <Icon :icon="item.icon" :color="item.color" size="20" />
          <span class="text-md mt-2">{{ item.title }}</span>
        </span>
      </CardGrid>
    </template>
  </Card>
</template>
<script lang="ts" setup>
  import { Card } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';

  const CardGrid = Card.Grid;
  const $message = useMessage();
  // Phase 4 Step 7 (G5): màu icon theo bảng màu thương hiệu "Xanh Jeecg".
  const navItems = [
    {
      title: 'Tiếp nhận nghiệp vụ',
      icon: 'ion:home-outline',
      color: '#0B57D0',
    },
    {
      title: 'Quản lý nghiệp vụ',
      icon: 'ion:grid-outline',
      color: '#2E6FE8',
    },
    {
      title: 'Quản lý tệp',
      icon: 'ion:layers-outline',
      color: '#5B8DEF',
    },
    {
      title: 'Truy vấn thông tin',
      icon: 'ion:settings-outline',
      color: '#F4502A',
    },
		{
				title: 'Thông báo',
				icon: 'ion:notifications',
				color: '#34A853',
		},
		{
				title: 'Nhiệm vụ của tôi',
				icon: 'ion:person-stalker',
				color: '#F5A623',
		}
  ];

  function goPage() {
    $message.createMessage.success('Theo nghiệp vụ tự xử lý chuyển trang!');
  }
</script>
