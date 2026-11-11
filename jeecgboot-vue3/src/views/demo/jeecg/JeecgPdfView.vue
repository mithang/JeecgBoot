<template>
  <a-card :bordered="false">
    <a-row>
      <!-- Cây tệp bên trái -->
      <a-col :span="4" class="clName">
        <a-tree :treeData="treeData" :defaultExpandAll="true" @select="onSelect" style="height: 500px; overflow-y: auto"> </a-tree>
      </a-col>
      <!--Hình thu nhỏ bên phải-->
      <a-col :span="18">
        <div v-for="(file, key) in dataSource" :key="key">
          <a-col :span="24">
            <a-divider orientation="left">{{ file.fileName }}</a-divider>
          </a-col>
          <!-- Khu vực xem trước -->
          <a-col :span="24">
            <template v-if="file.filePdfPath">
              <div @click="pdfPreview(file.title)">
                <img style="width: 80px; height: 80px" src="../../../assets/images/pdf4.jpg" />
              </div>
            </template>
            <template v-else> (Chưa có tài liệu, nhấn"Chọn tệp"hoặc"Quét và tải lên"Tải lên tệp) </template>
          </a-col>
        </div>
      </a-col>
    </a-row>
    <div style="display: none">
      <iframe id="pdfPreviewIframe" :src="url" frameborder="0" width="100%" height="550px" scrolling="auto"></iframe>
    </div>
  </a-card>
</template>

<script lang="ts">
  import { defineComponent, ref, unref, onMounted } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';

  const mockdata = [
    {
      id: '1',
      key: '1',
      title: 'Instance.pdf',
      fileCode: 'shili',
      fileName: 'Instance',
      filePdfPath: 'Instance',
    },
  ];

  export default defineComponent({
    name: 'JeecgPdfView',
    setup() {
      const glob = useGlobSetting();
      const treeData = ref([
        {
          title: 'Tất cả tệp điện tử PDF',
          key: '0-0',
          children: mockdata,
        },
      ]);
      const dataSource = ref(mockdata);
      const allData = ref(mockdata);
      const url = ref(`${glob.domainUrl}/sys/common/pdf/pdfPreviewIframe`);

      /**
       * Mở cửa sổ iframe
       * @param title
       */
      function pdfPreview(title) {
        let iframe = document.getElementById('pdfPreviewIframe');
        let json = { title: title, token: getToken() };
        iframe.contentWindow.postMessage(json, '*');
      }

      // Chọn tệp PDF
      function onSelect(selectedKeys, info) {
        dataSource.value = [];
        if (selectedKeys[0] === undefined || selectedKeys[0] === '0-0') {
          dataSource.value = unref(allData);
        } else {
          dataSource.value.push(info.node.dataRef);
        }
      }

      return {
        url,
        dataSource,
        treeData,
        allData,
        onSelect,
        pdfPreview,
      };
    },
  });
</script>
