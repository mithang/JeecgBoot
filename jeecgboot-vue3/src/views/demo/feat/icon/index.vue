<template>
  <PageWrapper title="Ví dụ sử dụng biểu tượng">
    <CollapseContainer title="IconCách dùng biểu tượng iconfiy trong component (không khuyến khích dùng, tất cả biểu tượng trong dự án đều được build trong một file js và tải ngay ở màn hình đầu tiên, không được tách vào chuck, không phải import theo nhu cầu)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <Icon icon="ion:layers-outline" :size="30" />
        <Icon icon="ion:bar-chart-outline" :size="30" />
        <Icon icon="ion:tv-outline" :size="30" />
        <Icon icon="ion:settings-outline" :size="30" />
        <Icon icon="ion:language" :size="30" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="Khuyến nghị dùng trực tiếp component gốc iconify (tách vào chunk, import theo nhu cầu)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconifyonLayersOutline class="text-30px" />
        <IconifyIonBarChartOutline class="text-30px" />
        <IconifyIonTvOutline class="text-30px" />
        <IconifyIonSettingsOutline class="text-30px" />
        <IconIonLanguage class="text-30px" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="Antv IconSử dụng (Chỉ cần import component tương ứng theo nhu cầu là được)">
      <div class="flex justify-around">
        <GithubFilled :style="{ fontSize: '30px' }" />
        <QqCircleFilled :style="{ fontSize: '30px' }" />
        <WechatFilled :style="{ fontSize: '30px' }" />
        <AlipayCircleFilled :style="{ fontSize: '30px' }" />
        <IeCircleFilled :style="{ fontSize: '30px' }" />
        <TaobaoCircleFilled :style="{ fontSize: '30px' }" />
        <CodepenCircleFilled :style="{ fontSize: '30px' }" />
      </div>
    </CollapseContainer>

    <CollapseContainer title="svg Ảnh sprite" class="my-5">
      <div class="flex justify-around flex-wrap">
        <SvgIcon name="test" size="32" />
        <template v-for="item in 6" :key="item">
          <SvgIcon :name="`dynamic-avatar-${item}`" size="32" />
        </template>
      </div>
    </CollapseContainer>

    <CollapseContainer title="Bộ chọn biểu tượng(Iconify)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconPicker />
      </div>
    </CollapseContainer>

    <CollapseContainer title="Bộ chọn biểu tượng(Svg)" class="my-5">
      <div class="flex justify-around flex-wrap">
        <IconPicker mode="svg" />
      </div>
    </CollapseContainer>

    <Alert
      show-icon
      message="Khuyên dùng component Iconify"
      description="IconComponent về cơ bản bao gồm tất cả các biểu tượng,Trong trang web dưới đây bạn có thể tra cứu bất kỳ biểu tượng nào bạn muốn. Và khi đóng gói chỉ đóng gói các biểu tượng đã dùng."
    />
    <a-button type="link" @click="toIconify"> Iconify Tổng hợp biểu tượng </a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { CollapseContainer } from '/@/components/Container/index';
  import { Alert } from 'ant-design-vue';
  import {
    QqCircleFilled,
    GithubFilled,
    WechatFilled,
    AlipayCircleFilled,
    IeCircleFilled,
    TaobaoCircleFilled,
    CodepenCircleFilled,
  } from '@ant-design/icons-vue';

  import { Icon, IconPicker, SvgIcon } from '/@/components/Icon/index';

  import { openWindow } from '/@/utils';
  import { PageWrapper } from '/@/components/Page';
  import IconIonLanguage from '~icons/ion/language'
  export default defineComponent({
    components: {
      PageWrapper,
      CollapseContainer,
      GithubFilled,
      QqCircleFilled,
      WechatFilled,
      AlipayCircleFilled,
      IeCircleFilled,
      TaobaoCircleFilled,
      CodepenCircleFilled,
      Icon,
      Alert,
      IconPicker,
      SvgIcon,
      IconIonLanguage,
    },
    setup() {
      return {
        toIconify: () => {
          openWindow('https://iconify.design/');
        },
      };
    },
  });
</script>
