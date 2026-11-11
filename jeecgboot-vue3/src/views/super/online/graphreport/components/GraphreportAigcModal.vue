<template>
  <BasicModal v-bind="getProps" destroyOnClose>
    <a-spin wrapperClassName="p-2" :spinning="loading">

      <div class="aiWrap">
        <div class="titleArea">
          <svg
              t="1707100353985"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="4235"
              width="26"
              height="26"
          >
            <path
                d="M512 64C264.8 64 64 264.8 64 512s200.8 448 448 448 448-200.8 448-448S759.2 64 512 64z m32 704h-64v-64h64v64z m11.2-203.2l-5.6 4.8c-3.2 2.4-5.6 8-5.6 12.8v58.4h-64v-58.4c0-24.8 11.2-48 29.6-63.2l5.6-4.8c56-44.8 83.2-68 83.2-108C598.4 358.4 560 320 512 320c-49.6 0-86.4 36.8-86.4 86.4h-64C361.6 322.4 428 256 512 256c83.2 0 150.4 67.2 150.4 150.4 0 72.8-49.6 112.8-107.2 158.4z"
                p-id="4236"
                fill="currentColor"
            ></path>
          </svg>
          <h3>Tạo biểu đồ cần gợi ý chuyên nghiệp? Hãy thửAITạo bảng thông minh</h3>
        </div>
        <p class="tip">Vui lòng chọnOnlinebiểu mẫu, đồng thời nhập nhu cầu biểu đồ, ví dụ: thống kê số lượng nam và nữ mỗi giới</p>
        <a-space class="content" direction="vertical">
          <a-select
              v-model:value="model.cgformTableName"
              placeholder="Vui lòng chọnOnlineBiểu mẫu"
              show-search
              :filter-option="false"
              not-found-content="Không có biểu mẫu phù hợp"
              style="width: 100%"
              @search="handleCgformSearch"
          >
            <template v-for="item of cgformList">
              <a-select-option :value="item.value">{{ item.text }}</a-select-option>
            </template>
            <a-select-option value="2">Nữ</a-select-option>
          </a-select>
          <a-textarea v-model:value.trim="model.prompt" placeholder="Vui lòng nhập nhu cầu biểu đồ" :rows="3"/>
        </a-space>

      </div>
    </a-spin>

    <template #footer>
      <a-button @click="onCancel">Đóng</a-button>
      <a-button type="primary" :loading="loading" preIcon="carbon:ai-generate" @click="onSubmit">
        Tạo ngay lập tức
      </a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
import type {ModalProps} from '/@/components/Modal';
import {BasicModal, useModalInner} from '/@/components/Modal';
import {computed, ref, unref} from 'vue';
import {debounce} from 'lodash-es';
import {useAttrs} from '/@/hooks/core/useAttrs';
import {useMessage} from '/@/hooks/web/useMessage';
import {defHttp} from "@/utils/http/axios";

import {getDictItems} from "@/api/common/api";

enum Api {
  AIGC = '/online/graphreport/head/api/aigc',
}

const props = defineProps();
const emit = defineEmits(['register', 'generate']);
const attrs = useAttrs();

const {createMessage: $message} = useMessage();

// model
const model = ref<Recordable>({
  prompt: '',
  cgformTableName: void 0,
});
// Hiện tại có đang tải hay không
const loading = ref(true);

// Popup đăng ký
const [registerModal, {closeModal, setModalProps}] = useModalInner(() => {
  queryOnlineCgform();
  setLoading(false)
  model.value.prompt = '';
  model.value.cgformTableName = void 0;
});

// Popup cuối cùngprops
const getProps = computed(() => {
  let modalProps: Partial<ModalProps> = {
    width: 600,
    height:  240,
    title: 'Thông quaAiTạo biểu đồ',
    maskClosable: false,
    canFullscreen: false,
    confirmLoading: unref(loading),
  };
  let finalProps: Recordable = {
    ...modalProps,
    ...unref(attrs),
    ...props,
    onCancel: close,
    onRegister: registerModal,
  };
  return finalProps;
});

/** Thiết lập trạng thái tải*/
function setLoading(flag) {
  loading.value = flag;
  setModalProps({confirmLoading: flag});
}

const handleCgformSearch = debounce(function (val: string) {
  queryOnlineCgform(val);
}, 500);

const cgformList = ref<any[]>([]);

function queryOnlineCgform(keywords: string = '') {
  const queryWhere = keywords ? `table_txt like '%25${keywords}%25' and ` : '';
  const where = `${queryWhere}is_db_synch='Y' and (table_type = 1 or table_type = 2)`
  const order = `order by create_time desc`
  getDictItems(`onl_cgform_head,table_txt,table_name,${where} ${order}`).then((res) => {
    cgformList.value = res;
  });
}

async function onSubmit() {
  await onStepGenerate()
}

async function onStepGenerate() {
  const {cgformTableName, prompt} = model.value
  if (!cgformTableName) {
    $message.warning('Vui lòng chọnOnlineBiểu mẫu');
    return
  }
  if (!prompt) {
    $message.warning('Vui lòng nhập nhu cầu biểu đồ');
    return
  }
  setLoading(true);
  try {
    const url = Api.AIGC + '?cgformTableName=' + cgformTableName + '&prompt=' + prompt;
    const {success, result, message} = await defHttp.post({url: url, timeout: 60000}, {isTransformResponse: false})
    if (success) {
      const {code, name, cgrSql} = result;
      if (code && name && cgrSql) {
        emit('generate', result);
        closeModal();
      } else {
        $message.warn('Tạo thất bại,Aiđang lơ đễnh, vui lòng thử lại sau…');
      }
    } else {
      $message.error(message);
    }
  } finally {
    setLoading(false);
  }
}

function onCancel() {
  closeModal();
}

</script>

<style scoped lang="less">

html[data-theme="light"] {
  .aiWrap {
    h3 {
      color: #333;
    }

    .tip {
      color: #666;
    }
  }
}

.aiWrap {
  padding: 30px 20px 20px;

  .titleArea {
    position: relative;
    color: #ffb308;
    width: fit-content;
    margin: 0 auto;

    svg {
      position: absolute;
      left: -30px;
      margin-right: 8px;
    }
  }

  h3 {
    text-align: center;
    margin-bottom: 0;
  }

  .tip {
    text-align: center;
    margin-bottom: 26px;
  }

  .content {
    display: flex;
  }

  .ant-btn {
    margin-left: 16px;
  }
}
</style>
