<template>
  <PageWrapper title="Ví dụ xem trước hình ảnh">
    <h1>Có ảnh xem trước</h1>
    <ImagePreview :imageList="imgList" />
    <a-divider />
    <h1>Không có ảnh xem trước</h1>
    <a-button @click="openImg" type="primary">Nhấn để xem trước</a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { createImgPreview, ImagePreview } from '/@/components/Preview/index';
  import { PageWrapper } from '/@/components/Page';
  import { ImageProps } from '/@/components/Preview/src/typing';

  const imgList: ImageProps[] = [
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/login_1658829954004.png', width: 300 },
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/home_index_1658830084684.png', width: 300 },
    { src: 'https://jeecgos.oss-cn-beijing.aliyuncs.com/upload/test/design_1658830200539.png', width: 300 },
    { src: 'https://static.jeecg.com/upload/test/13_1592320121058.png', width: 300 },
    { src: 'https://static.jeecg.com/upload/test/16_1592320251436.png', width: 300 },
  ];
  export default defineComponent({
    components: { PageWrapper, ImagePreview },
    setup() {
      function openImg() {
        const onImgLoad = ({ index, url, dom }) => {
          console.log(`Ảnh thứ ${index + 1} đã tải xong, URL: ${url}`, dom);
        };
        // Có thể dùng PreviewActions trả về bởi createImgPreview để điều khiển logic xem trước, thực hiện các thao tác hay ho tương tự như trình chiếu, tự động xoay
        let imageList = imgList.map<string>((i) => i.src);
        createImgPreview({ imageList: imageList, defaultWidth: 700, rememberState: true, onImgLoad });
      }

      return { imgList, openImg };
    },
  });
</script>
