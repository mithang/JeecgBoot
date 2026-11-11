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
  const navItems = [
    {
      title: 'Tiếp nhận nghiệp vụ',
      icon: 'ion:home-outline',
      color: '#1fdaca',
    },
    {
      title: 'Quản lý nghiệp vụ',
      icon: 'ion:grid-outline',
      color: '#bf0c2c',
    },
    {
      title: 'Quản lý tệp',
      icon: 'ion:layers-outline',
      color: '#e18525',
    },
    {
      title: 'Truy vấn thông tin',
      icon: 'ion:settings-outline',
      color: '#3fb27f',
    }, 
		{
				title: 'Thông báo',
				icon: 'ion:notifications',
				color: '#13b0ff',
		}, 
		{
				title: 'Nhiệm vụ của tôi',
				icon: 'ion:person-stalker',
				color: '#b27315',
		}
  ];

  function goPage() {
    $message.createMessage.success('Theo nghiệp vụ tự xử lý chuyển trang!');
  }
</script>
