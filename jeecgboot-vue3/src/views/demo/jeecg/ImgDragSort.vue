<template>
  <div>
    <draggable @end="end" v-model="dataArr" item-key="id" style="display: flex">
      <template #item="{ element }">
        <div class="imgDiv">
          <img :src="element.filePath" preview="index" />
        </div>
      </template>
      <template #footer>
        <a-button @click="sureChange" type="primary" style="margin-top: 100px">Xác nhận</a-button>
      </template>
    </draggable>
    <a-divider>jsonDữ liệu</a-divider>
    <a-row>
      <a-col :span="12">
        <p>Trước khi kéo thả:</p>
        <textarea rows="25" style="width: 780px">{{ oldDateSource }}</textarea>
      </a-col>
      <a-col :span="12">
        <p>Sau khi kéo thả:</p>
        <textarea rows="25" style="width: 780px">{{ newDateSource }}</textarea>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import draggable from 'vuedraggable';
  import { defineComponent, ref, unref } from 'vue';

  const mockData = [
    { id: '000', sort: 0, filePath: 'https://static.jeecg.com/upload/test/1_1588149743473.jpg' },
    { id: '111', sort: 1, filePath: 'https://static.jeecg.com/upload/test/u27356337152749454924fm27gp0_1588149731821.jpg' },
    { id: '222', sort: 2, filePath: 'https://static.jeecg.com/upload/test/u24454681402491956848fm27gp0_1588149712663.jpg' },
    { id: '333', sort: 3, filePath: 'https://static.jeecg.com/temp/国炬软件logo_1606575029126.png' },
    { id: '444', sort: 4, filePath: 'https://static.jeecg.com/upload/test/u8891206113801177793fm27gp0_1588149704459.jpg' },
  ];

  //Tập dữ liệu
  const dataArr = ref(mockData);
  //Dữ liệu gốc
  const oldDateSource = ref(mockData);
  //Dữ liệu sau khi thay đổi
  const newDateSource = ref([]);

  /**
   * Sự kiện kết thúc kéo
   * @param evt
   */
  function end(evt) {
    console.log('Vị trí trước khi kéo' + evt.oldIndex);
    console.log('Vị trí sau khi kéo' + evt.newIndex);
  }

  /**
   * Sự kiện xác nhận thay đổi
   * @param evt
   */
  function sureChange() {
    for (let i = 0; i < unref(dataArr).length; i++) {
      dataArr.value[i].sort = i;
    }
    newDateSource.value = unref(dataArr);
  }
</script>

<style scoped lang="less">
  .imgDiv {
    padding: 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    margin: 0 8px 8px 0;

    img {
      width: 120px;
      height: 120px;
      object-fit: cover;
    }
  }
</style>
