<template>
  <div ref="boxRef" :class="['view-box', {'is-mobile': isMobile}]" :style="boxStyle">
    <template v-if="!loading">
      <!-- Popup chi tiết -->
      <OnlineDetailModal v-if="isDetail" v-bind="getModalProps" :isShare="true">
        <template #footerBtn>
          <a-button @click="emit('goEdit')">Chỉnh sửa dữ liệu này</a-button>
        </template>
      </OnlineDetailModal>
      <!-- Popup chỉnh sửa -->
      <OnlineAutoModal v-else v-bind="getModalProps" :isShare="true"/>
    </template>
  </div>
</template>

<script setup lang="ts">
import type {ModalMethods} from "@/components/Modal";
import {computed, nextTick, ref} from "vue";
import {getRefPromise} from "@/utils";
import {useShareStore} from "../store/shareStore";
import OnlineAutoModal from "../../auto/default/OnlineAutoModal.vue";
import {useOnlineTableContext} from "../../hooks/auto/useOnlineTableContext";
import {useListButton} from "../../hooks/auto/useListButton";
import {useEnhance} from "../../hooks/auto/useEnhance";
import {useTableColumns} from "../../hooks/auto/useTableColumns";
import OnlineDetailModal from "../../auto/default/OnlineDetailModal.vue";

const props = defineProps({
  ID: String,
  formName: String,
  buttonSwitch: Object,
  cgBIBtnMap: Object,
  confirmBtnCfg: Object,
  isUpdate: Boolean,
  isDetail: Boolean,
  isMobile: Boolean,
})
const emit = defineEmits(['ok', 'goEdit'])

const shareStore = useShareStore()

const boxRef = ref()

// boxKiểu dáng
const boxStyle = computed(() => {
  let paddingTop = 20;
  let paddingLeft = 100;
  // Tương thích với màn hình hẹp, kiểu dáng di động, không làm mới động
  if (window.innerWidth < 1200) {
    paddingTop = 0;
    paddingLeft = 0;
  }
  return {
    padding: `${paddingTop}px ${paddingLeft}px`,
  };
});

const getModalProps = computed(() => {
  // Thuộc tính nghiệp vụ
  const titlePrefix = props.isDetail ? 'Chi tiết' : (props.isUpdate ? 'Chỉnh sửa' : 'Thêm mới');
  const modalProps: Recordable = {
    id: props.ID,
    title: titlePrefix + ` [${props.formName}]`,
    onRegister: registerModal,
  }
  if (!props.isDetail) {
    // Tham số thêm mới, chỉnh sửa
    Object.assign(modalProps, {
      cgBIBtnMap: props.cgBIBtnMap,
      buttonSwitch: props.buttonSwitch,
      confirmBtnCfg: props.confirmBtnCfg,
      onSuccess: onSuccess,
      onFormConfig: handleFormConfig,
    });
  }
  // Thuộc tính tùy chỉnh
  Object.assign(modalProps, {
    cancelBtnCfg: {enabled: false},

    width: 1200,
    getContainer: () => boxRef.value,
    mask: false,
    maskClosable: false,
    bodyStyle: {height: bodyHeight.value},
    draggable: false,
    canFullscreen: false,
    closeFunc: closeFunc,
  })

  return modalProps;
});

// Tính toán chiều cao động
const bodyHeight = computed(() => {
  let offset = 140;
  if (props.isMobile) {
    offset = 110;
  }
  return (boxRef.value?.offsetHeight ?? window.innerHeight) - offset + 'px';
});

const {
  loading,
  handleFormConfig,
  onlineTableContext,
  onlineExtConfigJson,
} = useOnlineTableContext();

// Xử lý danh sáchbutton
const {
  registerModal: registerModalOrigin,

  handleAdd,
  handleEdit,
} = useListButton(onlineTableContext, onlineExtConfigJson);

async function registerModal(modalMethod: ModalMethods, uuid: string) {
  registerModalOrigin(modalMethod, uuid);
  await nextTick()
  if (props.isUpdate) {
    const dataRecord = await getDataRecordPromise();
    handleEdit(dataRecord);
  } else {
    handleAdd({});
  }
}

async function getDataRecordPromise() {
  return await getRefPromise(computed(() => shareStore.getDataRecord));
}

// Xử lý BasicTable cấu hình của
const {} = useTableColumns(onlineTableContext, onlineExtConfigJson);
// Xử lý phần tăng cường
const {} = useEnhance(onlineTableContext);

function onSuccess(record: Recordable) {
  let dataId: string;
  if (props.isUpdate) {
    dataId = shareStore.getDataRecord!.id;
  } else {
    dataId = record.flow_submit_id
  }
  if (!dataId) {
    return;
  }
  emit('ok', dataId)
}

function closeFunc() {
  alert('không cho phép đóng')
  return false
}

</script>

<style lang="less">
// noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-auto-desform-data-view';

.view-box {
  height: calc(100vh - 50px);
  background-color: #f0f2f5;

  .jeecg-online-modal, .jeecg-online-detail-modal {
    top: 80px !important;

    > .ant-modal {
      top: 0 !important;
      // Hủy hiệu ứng động
      animation: none !important;

      .jeecg-basic-title {
        // Hủy cử chỉ kéo thả
        cursor: default !important;
      }

      .ant-modal-content {
        // Không có bóng đổ
        box-shadow: none !important;

        // Ẩn góc trên bên phải ×
        > .ant-modal-close {
          display: none;
        }
      }
    }
  }

  &.is-mobile {
    height: 100vh;

    .jeecg-online-modal, .jeecg-online-detail-modal {
      top: 0 !important;
    }
  }

}

</style>
