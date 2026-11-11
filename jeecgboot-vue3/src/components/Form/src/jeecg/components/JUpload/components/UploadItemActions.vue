<template>
  <div v-show="download" class="upload-download-handler">
    <a class="download" title="Tải xuống" @click="onDownload">
      <Icon icon="ant-design:download" />
    </a>
  </div>
  <div v-show="canMove" class="upload-mover-handler">
    <a title="Di chuyển lên trước" @click="onMoveForward">
      <Icon icon="ant-design:arrow-left" />
    </a>
    <a title="Di chuyển về sau" @click="onMoveBack">
      <Icon icon="ant-design:arrow-right" />
    </a>
  </div>
</template>

<script lang="ts" setup>
  import { unref, computed, watchEffect } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  const props = defineProps({
    element: { type: HTMLElement, required: true },
    // update-begin--author:liaozhiyang---date:20260831---for:【LHZP-73] Nhắc nhở di chuyển trái/phải sau khi tải lên hàng loạt hình ảnh-1
    container: { type: HTMLElement, required: true },
    // update-end--author:liaozhiyang---date:20260831---for:【LHZP-73] Nhắc nhở di chuyển trái/phải sau khi tải lên hàng loạt hình ảnh-1
    fileList: { type: Object, required: true },
    mover: { type: Boolean, required: true },
    download: { type: Boolean, required: true },
    emitValue: { type: Function, required: true },
  });
  const list = computed(() => unref(props.fileList));
  // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-73] tối ưu nút thao tác tải lên hình ảnh
  const canMove = computed(() => props.mover && list.value.length > 1);
  watchEffect(() => props.element.classList.toggle('has-upload-mover-actions', canMove.value));
  // update-end--author:liaozhiyang---date:20260826---for:【LHZP-73] tối ưu nút thao tác tải lên hình ảnh

  // Di chuyển hình ảnh về phía trước
  function onMoveForward() {
    let index = getIndex();
    if (index === -1) {
      createMessage.warn('Di chuyển thất bại:' + index);
      return;
    }
    if (index === 0) {
      doSwap(index, unref(list).length - 1);
      return;
    }
    doSwap(index, index - 1);
  }

  // Di chuyển hình ảnh về phía sau
  function onMoveBack() {
    let index = getIndex();
    if (index === -1) {
      createMessage.warn('Di chuyển thất bại:' + index);
      return;
    }
    if (index == unref(list).length - 1) {
      doSwap(index, 0);
      return;
    }
    doSwap(index, index + 1);
  }

  function doSwap(oldIndex, newIndex) {
    if (oldIndex !== newIndex) {
      let array: any[] = [...(unref(list) as Array<any>)];
      let temp = array[oldIndex];
      array[oldIndex] = array[newIndex];
      array[newIndex] = temp;
      props.fileList.value = array;
      props.emitValue(array.map((i) => i.response?.message).join(','));
    }
  }

  // update-begin--author:liaozhiyang---date:20260831---for:【LHZP-73] Nhắc nhở di chuyển trái/phải sau khi tải lên hàng loạt hình ảnh-1
  function getIndex() {
    const uploadItems = Array.from(props.container.getElementsByClassName('ant-upload-list-item'));
    const index = uploadItems.indexOf(props.element);
    if (index >= 0 && index < unref(list).length) {
      return index;
    }
    return getIndexByUrl();
  }
  // update-end--author:liaozhiyang---date:20260831---for:【LHZP-73] Nhắc nhở di chuyển trái/phải sau khi tải lên hàng loạt hình ảnh-1

  function getIndexByUrl() {
    const url = props.element?.getElementsByTagName('img')[0]?.src;
    if (url) {
      const fileList: any = unref(list);
      for (let i = 0; i < fileList.length; i++) {
        let current = fileList[i].url;
        const replace = url.replace(window.location.origin, '');
        if (current === replace || encodeURI(current) === replace) {
          return i;
        }
      }
    }
    return -1;
  }

  function onDownload() {
    const url = props.element?.getElementsByTagName('img')[0]?.src;
    window.open(url);
  }
</script>
