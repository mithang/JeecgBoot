<template>
  <div :class="boxClass">
    <vxe-toolbar ref="xToolbarRef" :custom="custom">
      <!-- Nút công cụ -->
      <template #buttons>
        <div :class="`${prefixCls}-button div`" :size="btnSize">
          <slot v-if="showPrefix" name="toolbarPrefix" :size="btnSize" />
          <a-button v-if="addBtnCfg.enabled && showAdd" type="primary" :preIcon="addBtnCfg.buttonIcon" :disabled="disabled" :loading="deleting" @click="trigger('add')">
            <span>{{ addBtnCfg.buttonName }}</span>
          </a-button>
          <slot name="toolbarAfterAdd" :size="btnSize" :selectedRowIds="selectedRowIds" />
          <a-button v-if="showSave" preIcon="ant-design:save-outlined" :disabled="disabled" @click="trigger('save')">
            <span>Lưu</span>
          </a-button>
          <template v-if="deleting || selectedRowIds.length > 0">
            <Popconfirm v-if="removeBtnCfg.enabled && showRemove" :title="`Bạn có chắc chắn muốn xóa ${selectedRowIds.length} mục không?`" :disabled="deleting" @confirm="onRemove">
              <a-button :preIcon="removeBtnCfg.buttonIcon" :disabled="disabled" :loading="deleting">
                <span>{{ removeBtnCfg.buttonName }}</span>
              </a-button>
            </Popconfirm>
            <template v-if="showClearSelection">
              <a-button preIcon="ant-design:delete-outlined" @click="trigger('clearSelection')">Xóa lựa chọn</a-button>
            </template>
          </template>
          <slot v-if="showSuffix" name="toolbarSuffix" :size="btnSize" :selectedRowIds="selectedRowIds" />
          <a v-if="showCollapse" style="margin-left: 4px" @click="toggleCollapse">
            <span>{{ collapsed ? 'Mở rộng' : 'Thu gọn' }}</span>
            <Icon :icon="collapsed ? 'ant-design:down-outlined' : 'ant-design:up-outlined'" />
          </a>
        </div>
      </template>
    </vxe-toolbar>
  </div>
</template>

<script lang="ts" setup>
  import { computed, inject, ref, onMounted } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { VxeToolbarInstance } from 'vxe-table';
  import { Icon } from '/@/components/Icon';
  import { propTypes } from '/@/utils/propTypes';

  const props = defineProps({
    size: propTypes.string,
    disabled: propTypes.bool.def(false),
    custom: propTypes.bool.def(false),
    toolbarConfig: propTypes.object,
    disabledRows: propTypes.object,
    hasBtnAuth: propTypes.func,
    selectedRowIds: propTypes.array.def(() => []),
    addBtnCfg: propTypes.object.def(() => ({
      enabled: true,
      buttonIcon: 'ant-design:plus-outlined',
      buttonName: 'Thêm mới',
    })),
    removeBtnCfg: propTypes.object.def(() => ({
      enabled: true,
      buttonIcon: 'ant-design:minus-outlined',
      buttonName: 'Xóa',
    })),
  });
  const emit = defineEmits(['save', 'add', 'remove', 'clearSelection', 'register']);
  const xToolbarRef = ref({} as VxeToolbarInstance);
  const prefixCls = `${inject('prefixCls')}-toolbar`;
  const boxClass = computed(() => [
    prefixCls,
    {
      [`${prefixCls}-collapsed`]: collapsed.value,
    },
  ]);
  // Có thu gọn hay không
  const collapsed = ref(true);
  // Nút đã cấu hình
  const btns = computed(() => {
    let { btn, btns } = props.toolbarConfig || {};
    btns = btn || btns || ['add', 'remove', 'clearSelection'];
    // Loại bỏ nút chưa được cấp quyền
    return btns.filter((btn) => {
      // Mã cấu hình xóa hàng loạt mặc định của hệ thống là batch_delete Tại đây cần tương thích thêm
      if (btn === 'remove') {
        // Giải thích logic mã: VUEN-1162 Nút bảng con chưa được kiểm soát
        return hasBtnAuth(btn) && hasBtnAuth('batch_delete');
      }
      return hasBtnAuth(btn);
    });
  });
  const showAdd = computed(() => btns.value.includes('add'));
  const showSave = computed(() => btns.value.includes('save'));
  const showRemove = computed(() => btns.value.includes('remove'));
  // Slot đã cấu hình
  const slots = computed(() => props.toolbarConfig?.slot || ['prefix', 'suffix']);
  const showPrefix = computed(() => slots.value.includes('prefix'));
  const showSuffix = computed(() => slots.value.includes('suffix'));
  // Có hiển thị nút xóa lựa chọn không
  const showClearSelection = computed(() => {
    if (btns.value.includes('clearSelection')) {
      // Chỉ hiển thị nút xóa lựa chọn khi có dòng bị vô hiệu hóa
      // Vì dòng bị vô hiệu hóa sẽ chặn việc chọn dòng, dẫn đến không thể hủy chọn tất cả
      // return Object.keys(props.disabledRows).length > 0
    }
    return false;
  });
  // Có hiển thị nút mở rộng/thu gọn không
  const showCollapse = computed(() => btns.value.includes('collapse'));
  // Nút size
  const btnSize = computed(() => (props.size === 'tiny' ? 'small' : null));

  onMounted(() => {
    // Đăng ký vxe-toolbar
    emit('register', {
      xToolbarRef,
    });
  });

  // Kiểm tra nút đã được cấp quyền chưa
  function hasBtnAuth(key: string) {
    return props.hasBtnAuth ? props.hasBtnAuth(key) : true;
  }

  /** Kích hoạt sự kiện */
  function trigger(name) {
    emit(name);
  }

  // Chuyển đổi mở rộng/thu gọn
  function toggleCollapse() {
    collapsed.value = !collapsed.value;
  }

  // 【TV360X-1975]Trong thiết kế Online, khi có nhiều trường, do sẽ đồng bộ xóa các bảng khác nên thời gian xóa trở nên dài hơn, vì vậy tăng thêm loading khi xóa, để tránh tưởng rằng nút xóa bấm không có tác dụng
  const deleting = ref(false);

  let deleteTimer: any = null

  function onRemove() {
    trigger('remove')
    deleting.value = true;
    if (deleteTimer) {
      clearTimeout(deleteTimer)
    }
    deleteTimer = setTimeout(() => deleting.value = false, 300);
  }

</script>
