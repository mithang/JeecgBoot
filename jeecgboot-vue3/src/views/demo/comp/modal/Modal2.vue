<template>
  <BasicModal @register="register" title="Modal Title" :helpMessage="['Nhắc nhở1', 'Nhắc nhở2']" :okButtonProps="{ disabled: true }">
    <a-button type="primary" @click="closeModal" class="mr-2"> Đóng popup từ bên trong </a-button>
    <a-button type="primary" @click="setModalProps"> Sửa đổi từ bên trongtitle </a-button>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  export default defineComponent({
    components: { BasicModal },
    setup() {
      const [register, { closeModal, setModalProps }] = useModalInner();
      return {
        register,
        closeModal,
        setModalProps: () => {
          setModalProps({ title: 'Modal New Title' });
        },
      };
    },
  });
</script>
