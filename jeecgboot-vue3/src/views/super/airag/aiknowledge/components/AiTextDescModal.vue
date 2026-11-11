<!--Nhập thủ công text-->
<template>
  <BasicModal title="Chi tiết đoạn văn" destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" :footer="null">
    <div class="p-2">
      <div class="header">
        <a-tag color="#a9c8ff">
          <span>{{hitTextDescData.source}}</span>
        </a-tag>
      </div>
      <div class="content">
        <MarkdownViewer :value="hitTextDescData.content" />
      </div>
    </div>

  </BasicModal>
</template>

<script lang="ts">
  import { ref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModalInner } from '@/components/Modal';

  import BasicForm from '@/components/Form/src/BasicForm.vue';
  import { MarkdownViewer } from '@/components/Markdown';
  import { useGlobSetting } from "@/hooks/setting";

  export default {
    name: 'AiTextDescModal',
    components: {
      MarkdownViewer,
      BasicForm,
      BasicModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      let hitTextDescData = ref<any>({})
      
      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        hitTextDescData.value.source = 'score' + ' ' + data.score.toFixed(2);
        //Thay thế độ rộng hình ảnh
        data.content = replaceImageWith(data.content);
        //Thay thế domainUrl của hình ảnh
        data.content = replaceDomainUrl(data.content);
        hitTextDescData.value.content = data.content;
        setModalProps({ header: '300px' })
      });
      const { domainUrl } = useGlobSetting();
      const replaceImageWith = markdownContent => {
        // Hỗ trợ cách viết đặt width cho hình ảnh ![](/static/jimuImages/screenshot_1617252560523.png =100)
        const regex = /!\[([^\]]*)\]\(([^)]+)=([0-9]+)\)/g;
        return markdownContent.replace(regex, (match, alt, src, width) => {
          let reg = /#\s*{\s*domainURL\s*}/g;
          src = src.replace(reg,domainUrl);
          return `<img src='${src}' alt='${alt}' width='${width}' />`;
        });
      };
      
      //Thay thế domainURL
      const replaceDomainUrl = markdownContent => {
            const regex = /!\[([^\]]*)\]\(.*?#\s*{\s*domainURL\s*}.*?\)/g;
            return markdownContent.replace(regex, (match) => {
              let reg = /#\s*{\s*domainURL\s*}/g;
              return match.replace(reg,domainUrl);
            })
      }
      return {
        registerModal,
        hitTextDescData
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }
  .header {
    font-size: 16px;
    font-weight: bold;
    margin-top: 10px;
  }
  .content {
    margin-top: 20px;
    max-height: 600px;
    overflow-y: auto;
    overflow-x: auto;
  }
  .title-tag {
    color: #477dee;
  }
</style>
