<template>
  <template v-if="syncToApp || syncToLocal">
    <JThirdAppDropdown v-if="enabledTypes.wechatEnterprise" type="wechatEnterprise" name="WeCom" v-bind="bindAttrs" v-on="bindEvents" />
    <JThirdAppDropdown v-if="enabledTypes.dingtalk" type="dingtalk" name="DingTalk" v-bind="bindAttrs" v-on="bindEvents" />
  </template>
  <template v-else>Chưa đặt bất kỳ hướng đồng bộ nào</template>
</template>

<script lang="ts" setup>
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import { ref, computed, createVNode, h, resolveComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { backEndUrl, getEnabledTypes, doSyncThirdApp } from './jThirdApp.api';
  import { Modal, Input } from 'ant-design-vue';
  import JThirdAppDropdown from './JThirdAppDropdown.vue';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage, createWarningModal } = useMessage();
  const props = defineProps({
    // Loại đồng bộ, có thể là user,depart
    bizType: {
      type: String,
      required: true,
    },
    // Có cho phép đồng bộ sang bên thứ ba khôngAPP
    syncToApp: Boolean,
    // Có cho phép APP bên thứ ba đồng bộ về local hay không
    syncToLocal: Boolean,
    // Hàng đã chọn
    selectedRowKeys: Array,
  });
  // Khai báoEmits
  const emit = defineEmits(['sync-ok', 'sync-error', 'sync-finally']);

  const enabledTypes = ref({});
  // Thuộc tính liên kết
  const bindAttrs = computed(() => {
    return {
      syncToApp: props.syncToApp,
      syncToLocal: props.syncToLocal,
    };
  });
  // Phương thức liên kết
  const bindEvents = computed(() => {
    return {
      'to-app': onToApp,
      'to-local': onToLocal,
    };
  });

  // Đồng bộ sang bên thứ baApp
  function onToApp(e) {
    doSync(e.type, '/toApp');
  }

  // Đồng bộ về máy local
  function onToLocal(e) {
    doSync(e.type, '/toLocal');
  }

  // Lấy ứng dụng bên thứ ba đã bậtApp
  async function loadEnabledTypes() {
    enabledTypes.value = await getEnabledTypes();
  }

  // Bắt đầu đồng bộ ứng dụng bên thứ baApp
  function doSync(type, direction) {
    let urls = backEndUrl[type];
    if (!(urls && urls[props.bizType])) {
      console.warn('Lỗi cấu hình');
      return;
    }
    let url = urls[props.bizType] + direction;
    let selectedRowKeys = props.selectedRowKeys;
    let content = 'Bạn có chắc chắn muốn bắt đầu đồng bộ tất cả dữ liệu không? Có thể mất khá nhiều thời gian!';

    if (Array.isArray(selectedRowKeys) && selectedRowKeys.length > 0) {
      content = `Bạn có chắc chắn muốn bắt đầu đồng bộ ${selectedRowKeys.length} mục không?`;
    } else {
      selectedRowKeys = [];
    }
    return new Promise((resolve, reject) => {
      const model = Modal.confirm({
        icon: createVNode(ExclamationCircleOutlined),
        title: 'Đồng bộ',
        content,
        onOk: () => {
          model.update({
            keyboard: false,
            okText: 'Đang đồng bộ...',
            cancelButtonProps: { disabled: true },
          });
          let params = { ids: selectedRowKeys.join(',') };
          return defHttp
            .get({ url, params }, { isTransformResponse: false })
            .then((res) => {
              let options = {};
              if (res.result) {
                options = {
                  width: 600,
                  title: res.message,
                  content: () => {
                    let nodes;
                    let successInfo = [`Thông tin thành công như sau:`, renderTextarea(h, res.result.successInfo.map((v, i) => `${i + 1}. ${v}`).join('\n'))];
                    if (res.success) {
                      nodes = [...successInfo, h('br'), `Không có thông tin thất bại!`];
                    } else {
                      nodes = [
                        `Thông tin thất bại như sau:`,
                        renderTextarea(h, res.result.failInfo.map((v, i) => `${i + 1}. ${v}`).join('\n')),
                        h('br'),
                        ...successInfo,
                      ];
                    }
                    return nodes;
                  },
                };
              }
              if (res.success) {
                if (options != null) {
                  Modal.success(options);
                } else {
                  createMessage.warning(res.message);
                }
                emit('sync-ok');
              } else {
                if (options != null) {
                  Modal.warning(options);
                } else {
                  createMessage.warning(res.message);
                }
                emit('sync-error');
              }
            })
            .catch(() => model.destroy())
            .finally(() => {
              resolve();
              emit('sync-finally', {
                type,
                direction,
                isToApp: direction === '/toApp',
                isToLocal: direction === '/toLocal',
              });
            });
        },
        onCancel() {
          resolve();
        },
      });
    });
  }

  function renderTextarea(h, value) {
    return h(
      'div',
      {
        id: 'box',
        style: {
          minHeight: '100px',
          border: '1px solid #d9d9d9',
          fontSize: '14px',
          maxHeight: '250px',
          whiteSpace: 'pre',
          overflow: 'auto',
          padding: '10px',
        },
      },
      value
    );
  }

  // Lấy ứng dụng bên thứ ba đã bậtApp
  loadEnabledTypes();
</script>

<style scoped>
  #box:hover {
    border-color: #40a9ff;
  }
</style>
