<!-- Phase 4 Bước 8 (G7) — Pattern pack CRUD: trạng thái rỗng dùng chung cho mọi danh sách.
  Ảnh minh họa nodata.png của repo + mô tả tiếng Việt + nút hành động tùy chọn.
  BasicTable dùng làm trạng thái rỗng mặc định; trang nào tự định nghĩa
  emptyText (slot) hoặc locale.emptyText thì giữ nguyên bản của trang đó. -->
<template>
  <div :class="prefixCls">
    <div :class="`${prefixCls}__image`">
      <slot name="image">
        <img :src="getImage" :alt="getDescription" />
      </slot>
    </div>
    <div :class="`${prefixCls}__description`">
      <slot name="description">{{ getDescription }}</slot>
    </div>
    <div v-if="showAction" :class="`${prefixCls}__action`">
      <slot name="action">
        <Button type="primary" @click="handleAction">{{ actionText }}</Button>
      </slot>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import type { PropType } from 'vue';
  import { computed, useSlots } from 'vue';
  import { Button } from 'ant-design-vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { isFunction } from '/@/utils/is';
  import nodataImg from '/@/assets/images/nodata.png';

  const props = defineProps({
    /** Ảnh minh họa; mặc định nodata.png của repo */
    image: { type: String, default: '' },
    /** Mô tả; mặc định common.noDataText */
    description: { type: String, default: '' },
    /** Nhãn nút hành động (vd: Thêm mới); không truyền thì không hiện nút */
    actionText: { type: String, default: '' },
    /** Hàm chạy khi bấm nút hành động */
    action: { type: Function as PropType<(...args: any[]) => void>, default: undefined },
  });

  const { t } = useI18n();
  const { prefixCls } = useDesign('empty');
  const slots = useSlots();

  const getImage = computed(() => props.image || nodataImg);
  const getDescription = computed(() => props.description || t('common.noDataText'));
  const showAction = computed(() => !!slots.action || (isFunction(props.action) && !!props.actionText));

  function handleAction() {
    if (isFunction(props.action)) {
      props.action();
    }
  }
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-empty';

  .@{prefix-cls} {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 32px 16px;
    text-align: center;

    &__image {
      img {
        width: 120px;
        height: auto;
        opacity: 0.9;
      }
    }

    &__description {
      margin-top: 8px;
      color: @text-color-secondary;
      font-size: 14px;
    }

    &__action {
      margin-top: 16px;
    }
  }
</style>
