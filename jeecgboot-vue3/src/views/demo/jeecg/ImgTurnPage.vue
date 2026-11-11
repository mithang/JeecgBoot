<template>
  <div style="min-width: 800px">
    <a-row>
      <!-- Cây tệp bên trái -->
      <a-col :span="6">
        <a-tree
          showLine
          :treeData="treeData"
          :expandedKeys="[expandedKeys[0]]"
          :selectedKeys="selectedKeys"
          :style="{ height: '500px', 'border-right': '2px solid #c1c1c1', 'overflow-y': 'auto' }"
          @expand="onExpand"
          @select="onSelect"
        ></a-tree>
      </a-col>

      <!--Hình thu nhỏ bên phải-->
      <a-col :span="18">
        <a-row style="margin-top: 10px; padding-left: 2%">
          <a-col :span="24" style="margin-bottom: 10px">
            <a-button @click="prev" preIcon="ant-design:left-outlined" type="primary">Trang trước</a-button>
            <a-button @click="next" preIcon="ant-design:right-outlined" style="margin-left: 8px" type="primary">Trang tiếp theo</a-button>
            <span style="margin-left: 100px; font-weight: bolder">{{ navName }}</span>
          </a-col>
          <a-col :span="24">
            <img :src="imgUrl" preview />
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { ref, unref, onMounted } from 'vue';
  //mockDữ liệu
  const mockdata = [
    {
      title: 'Trang 1',
      key: '0-0',
      children: [
        {
          title: '1trang',
          key: '0-0-0',
          imgUrl: 'https://static.jeecg.com/upload/test/1_1588149743473.jpg',
        },
        {
          title: '2trang',
          key: '0-0-1',
          imgUrl: 'https://static.jeecg.com/upload/test/u27356337152749454924fm27gp0_1588149731821.jpg',
        },
      ],
    },
    {
      title: 'Trang 2',
      key: '0-1',
      children: [
        {
          title: '1trang',
          key: '0-1-0',
          imgUrl: 'https://static.jeecg.com/upload/test/u24454681402491956848fm27gp0_1588149712663.jpg',
        },
        {
          title: '2trang',
          key: '0-1-1',
          imgUrl: 'https://static.jeecg.com/upload/test/u8891206113801177793fm27gp0_1588149704459.jpg',
        },
      ],
    },
    {
      title: 'Trang 3',
      key: '0-2',
      children: [
        {
          title: '1trang',
          key: '0-2-0',
          imgUrl: 'https://static.jeecg.com/upload/test/1374962_1587621329085.jpg',
        },
      ],
    },
  ];
  /**
   * Dữ liệu dạng cây bên trái
   */
  const treeData = ref(mockdata);
  //đã chọnkey
  const selectedKeys = ref([]);
  //đã mở rộngkey
  const expandedKeys = ref([]);
  const sort = ref(0);
  //Liên kết hình ảnh
  const imgUrl = ref('');
  //Tiêu đề số trang
  const navName = ref('');
  //Bộ sưu tập hình ảnh
  const imgList = ref([]);

  onMounted(getImgList);

  /**
   * Tải tập hợp hình ảnh
   */
  function getImgList() {
    var count = 0;
    for (var i = 0; i < unref(treeData).length; i++) {
      for (var j = 0; j < unref(treeData)[i].children.length; j++) {
        imgList.value.push({
          key: unref(treeData)[i].children[j].key,
          pkey: unref(treeData)[i].key,
          sort: count++,
          imgUrl: unref(treeData)[i].children[j].imgUrl,
          navName: unref(treeData)[i].title + '/' + unref(treeData)[i].children[j].title,
        });
      }
    }
    setValue(imgList.value[unref(sort)]);
  }
  /**
   * Sự kiện chọn nút
   */
  function onSelect(selectedKeys, info) {
    for (var i = 0; i < unref(imgList).length; i++) {
      if (unref(imgList)[i].key === selectedKeys[0]) {
        sort.value = unref(imgList)[i].sort;
        setValue(unref(imgList)[i]);
        break;
      }
    }
  }
  /**
   * Sự kiện mở rộng nút
   */
  function onExpand(expandedKey) {
    expandedKeys.value = [];
    if (expandedKey !== null && expandedKey !== '') {
      expandedKeys.value[0] = expandedKey[1];
    }
  }
  /**
   * Trang trước
   */
  function prev() {
    if (unref(sort) === 0) {
      sort.value = unref(imgList).length - 1;
    } else {
      sort.value = sort.value - 1;
    }
    setValue(unref(imgList)[unref(sort)]);
  }
  /**
   * Trang tiếp theo
   */
  function next() {
    if (unref(sort) === unref(imgList).length - 1) {
      sort.value = 0;
    } else {
      sort.value = unref(sort) + 1;
    }
    setValue(unref(imgList)[unref(sort)]);
  }

  // Đặt giá trị nút được kiểm soát
  function setValue(value) {
    selectedKeys.value = [];
    imgUrl.value = value.imgUrl;
    selectedKeys.value[0] = value.key;
    expandedKeys.value[0] = value.pkey;
    navName.value = value.navName;
  }
</script>
