<template>
  <PageWrapper title="Giới thiệu">
    <template #headerContent>
      <div class="flex justify-between items-center">
        <span class="flex-1">
          <a :href="GITHUB_URL" target="_blank"> JeecgBoot </a>
          là một nền tảng low-code dựa trên BPM! Kiến trúc tách biệt frontend/backend SpringBoot 2.x, SpringCloud,Ant Design&Vue, Mybatis-plus, Shiro, JWT, hỗ trợ vi dịch vụ (microservices). Bộ sinh mã mạnh mẽ giúp tạo mã frontend và backend chỉ với một cú nhấp, hiện thực hóa phát triển low-code! JeecgBoot dẫn đầu mô hình phát triển low-code mới OnlineCoding-> Bộ sinh mã-> MERGE thủ công, giúp các dự án Java giải quyết 70% công việc trùng lặp, để lập trình viên tập trung hơn vào nghiệp vụ, vừa nhanh chóng nâng cao hiệu quả, tiết kiệm chi phí nghiên cứu phát triển, vừa không mất tính linh hoạt! Hàng loạt năng lực low-code: Biểu mẫu Online, Báo cáo Online, Biểu đồ Online, Thiết kế biểu mẫu, Thiết kế quy trình, Thiết kế báo cáo, Thiết kế màn hình lớn, v.v...
        </span>
      </div>
    </template>
    <Description @register="infoRegister" class="enter-y" />
    <Description @register="register" class="my-4 enter-y" />
    <Description @register="registerDev" class="enter-y" />
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { h } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Description, DescItem, useDescription } from '/@/components/Description/index';
  import { GITHUB_URL, SITE_URL, DOC_URL } from '/@/settings/siteSetting';

  const { pkg, lastBuildTime } = __APP_INFO__;

  const { dependencies, devDependencies, name, version } = pkg;

  const schema: DescItem[] = [];
  const devSchema: DescItem[] = [];

  const commonTagRender = (color: string) => (curVal) => h(Tag, { color }, () => curVal);
  const commonLinkRender = (text: string) => (href) => h('a', { href, target: '_blank' }, text);

  const infoSchema: DescItem[] = [
    {
      label: 'Phiên bản',
      field: 'version',
      render: commonTagRender('blue'),
    },
    {
      label: 'Thời gian biên dịch cuối cùng',
      field: 'lastBuildTime',
      render: commonTagRender('blue'),
    },
    {
      label: 'Địa chỉ tài liệu',
      field: 'doc',
      render: commonLinkRender('Địa chỉ tài liệu'),
    },
    {
      label: 'Địa chỉ xem trước',
      field: 'preview',
      render: commonLinkRender('Địa chỉ xem trước'),
    },
    {
      label: 'Github',
      field: 'github',
      render: commonLinkRender('Github'),
    },
  ];

  const infoData = {
    version,
    lastBuildTime,
    doc: DOC_URL,
    preview: SITE_URL,
    github: GITHUB_URL,
  };

  Object.keys(dependencies).forEach((key) => {
    schema.push({ field: key, label: key });
  });

  Object.keys(devDependencies).forEach((key) => {
    devSchema.push({ field: key, label: key });
  });

  const [register] = useDescription({
    title: 'Phụ thuộc môi trường production',
    data: dependencies,
    schema: schema,
    column: 3,
  });

  const [registerDev] = useDescription({
    title: 'Phụ thuộc môi trường phát triển',
    data: devDependencies,
    schema: devSchema,
    column: 3,
  });

  const [infoRegister] = useDescription({
    title: 'Thông tin dự án',
    data: infoData,
    schema: infoSchema,
    column: 2,
  });
</script>
