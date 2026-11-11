<template>
  <div v-if="cgformRef != null" :class="[prefixCls]">
    <template v-if="showResult!=='none'">
      <a-result
          status="success"
          :title="`${isUpdate ? 'Chỉnh sửa': 'Thêm mới'}Thành công`"
          subTitle="Vui lòng chọn thao tác tiếp theo"
      >
        <template #extra>
          <a-space>
            <a-button type="primary" @click="onAddContinue">
              <span>Tiếp tục{{ isUpdate ? 'Chỉnh sửa' : 'Thêm mới' }}</span>
            </a-button>
            <!-- Có quyền xem chi tiết: nút chính là 【Xem dữ liệu】, các thao tác khác đặt ở dropdown -->
            <a-space-compact v-if="canView" block>
              <a-button @click="onOtherAction({key:'view'})">
                <span>Xem</span>
                <span v-if="!isUpdate">Mới</span>
                <span>Dữ liệu</span>
              </a-button>
              <a-dropdown v-if="otherAction" :trigger="['click', 'hover']">
                <a-button preIcon="ant-design:down">
                </a-button>
                <template #overlay>
                  <a-menu @click="onOtherAction">
                    <a-menu-item :key="otherAction.key">{{ otherAction.label }}</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space-compact>
            <!-- Không có quyền xem chi tiết: các thao tác khác trực tiếp trải phẳng thành nút -->
            <a-button v-else-if="otherAction" @click="onOtherAction({key: otherAction.key})">{{ otherAction.label }}</a-button>
            <a-button @click="onClose">Đóng</a-button>
          </a-space>
        </template>
      </a-result>
    </template>
    <!-- Trang bảng đơn -->
    <!--    <template v-else-if="isSingle">-->
    <!--    </template>-->
    <!-- Trang bảng chính-con -->
    <template v-else>
      <SingleView
          :ID="ID"
          :formName="formName"
          :isUpdate="isUpdate"
          :isDetail="isDetail"
          :isMobile="getIsMobile"
          :buttonSwitch="buttonSwitch"
          :cgBIBtnMap="cgBIBtnMap"
          :confirmBtnCfg="confirmBtnCfg"

          @ok="onOk"
          @goEdit="goEdit"
      />
    </template>
    <div v-show="false">
      <OnlineAutoList ref="listRef"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue';
import {useShareStore} from "../store/shareStore";
import OnlineAutoList from "../../auto/default/OnlineAutoList.vue";
import {useDesign} from "@/hooks/web/useDesign";

import SingleView from "./SingleView.vue";
import {useGo} from "@/hooks/web/usePage";
import {useAppInject} from "@/hooks/web/useAppInject";
import {parseExtConfigJson} from "../../util/utils";

const {prefixCls} = useDesign('online-share-view-box');

const props = defineProps({
  isUpdate: Boolean,
  isDetail: Boolean,
})

const {getIsMobile} = useAppInject();

const shareStore = useShareStore()

const cgformRef = computed(() => shareStore.getCgformRecord);
const ID = computed(() => cgformRef.value?.id);
const formName = computed(() => cgformRef.value?.tableTxt);
// Có phải bảng đơn hay không
// const isSingle = computed(() => cgformRef.value?.tableType == 1);

const listRef = ref()
const buttonSwitch = computed(() => listRef.value?.buttonSwitch);
const cgBIBtnMap = computed(() => listRef.value?.cgBIBtnMap);
const confirmBtnCfg = computed(() => listRef.value?.getFormConfirmButtonCfg);

// update-begin--author:liaozhiyang---date:20261026---for：【LHZP-29】Nút căn cứ theo chi tiết/Chỉnh sửa/Hiển thị quyền thêm mới
/**
 * Có quyền của một nút tích hợp sẵn nào đó hay không
 * @param keys Mã nút, nhiều mã cần thỏa mãn đồng thời
 */
function hasBtnAuth(...keys: string[]) {
  return keys.every((key) => buttonSwitch.value?.[key] === true && cgBIBtnMap.value?.[key]?.enabled !== false);
}

// Các thao tác mà liên kết ngoài cho phép:add、edit、detail
const externalLinkActions = computed<string[]>(() => {
  if (!cgformRef.value) {
    return [];
  }
  const extJson = parseExtConfigJson(cgformRef.value);
  return (extJson.externalLinkActions || '').split(',').filter((item) => !!item);
});
// Liên kết ngoài có cho phép một thao tác nào đó hay không
function hasExtAction(action: string) {
  return externalLinkActions.value.includes(action);
}

// Chi tiết: cần quyền nút chi tiết, và liên kết ngoài cho phép xem chi tiết
const canView = computed(() => hasBtnAuth('detail') && hasExtAction('detail'));
// dropdown/Các thao tác dạng phẳng khác: sau khi sửa là 【Thêm dữ liệu】, sau khi thêm mới là 【Chỉnh sửa dữ liệu mới】
const otherAction = computed(() => {
  if (props.isUpdate) {
    return hasBtnAuth('add') && hasExtAction('add') ? { key: 'add', label: 'Thêm dữ liệu' } : null;
  }
  return hasBtnAuth('edit', 'update') && hasExtAction('edit') ? { key: 'edit', label: 'Chỉnh sửa dữ liệu mới' } : null;
});
// update-end--author:liaozhiyang---date:20261026---for：【LHZP-29】Nút căn cứ theo chi tiết/Chỉnh sửa/Hiển thị quyền thêm mới

const showResult = ref<'none' | 'ok'>('none');
const lastActionDataId = ref<string>();

function onOk(dataId: string) {
  showResult.value = 'ok';
  lastActionDataId.value = dataId;
  if (!props.isUpdate) {
    shareStore.setDataRecord(null);
  }
}

// Tiếp tục thêm
function onAddContinue() {
  // window.location.reload();
  showResult.value = 'none';
  lastActionDataId.value = '';
}

const go = useGo();

function onOtherAction({key}) {
  const dataId = lastActionDataId.value!;
  if (key === 'add') {
    goDataAction('add', '');
  } else if (key === 'view') {
    goDetail(dataId);
  } else if (key === 'edit') {
    goEdit(dataId);
  }
  lastActionDataId.value = '';
  showResult.value = 'none';
}

function goEdit(dataId: string) {
  goDataAction('u', dataId);
}

function goDetail(dataId: string) {
  goDataAction('d', dataId);
}

function goDataAction(action, dataId) {
  if (action === 'add') {
    go(`/online/cgform/share/${ID.value}/${action}`);
    return;
  }
  if (!dataId) {
    dataId = shareStore.getDataRecord?.id;
  }

  go(`/online/cgform/share/${ID.value}/${action}/${dataId}`);
}

function onClose() {
  window.close()
  setTimeout(() => alert('Trang hiện tại không thể đóng bằng nút, vui lòng đóng thủ công'), 1000)
}

</script>

<style scoped lang="less">

</style>
