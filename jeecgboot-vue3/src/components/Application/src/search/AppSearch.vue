<script lang="tsx">
  import { defineComponent, ref, unref } from 'vue';
  import { SearchOutlined } from '@ant-design/icons-vue';
  import AppSearchModal from './AppSearchModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';

  export default defineComponent({
    name: 'AppSearch',
    setup() {
      const showModal = ref(false);
      const { t } = useI18n();

      function changeModal(show: boolean) {
        showModal.value = show;
      }

      // Phase 4 Bước 11 (G12): nút tìm kiếm tới được bằng bàn phím.
      function onTriggerKeydown(e: KeyboardEvent) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          changeModal(true);
        }
      }

      return () => {
        return (
          // Phase 4 Bước 5: nút tìm kiếm dạng ô pill có chữ để nổi bật hơn icon trần.
          <div
            class="app-search-trigger"
            tabindex={0}
            role="button"
            aria-label={t('common.searchText')}
            onClick={changeModal.bind(null, true)}
            onKeyDown={onTriggerKeydown}
            title={t('common.searchText')}
          >
            <SearchOutlined />
            <span class="app-search-trigger__text">{t('common.searchText')}</span>
            <AppSearchModal onClose={changeModal.bind(null, false)} visible={unref(showModal)} />
          </div>
        );
      };
    },
  });
</script>
<style lang="less" scoped>
  .app-search-trigger {
    display: flex;
    height: 32px;
    padding: 0 12px;
    margin: 0 8px;
    font-size: 13px;
    color: @text-color-secondary;
    cursor: pointer;
    background-color: @content-bg;
    border: 1px solid @border-color-base;
    border-radius: 8px;
    transition: all 0.2s ease;
    align-items: center;
    gap: 6px;

    &:hover {
      color: @primary-color;
      border-color: @primary-color;
      box-shadow: 0 0 0 2px fade(@primary-color, 12);
    }

    &__text {
      white-space: nowrap;
    }
  }

  // Header tối (mặc định #0A3D91): pill dùng nền/biên trắng mờ để vẫn nổi trên nền xanh đậm.
  :global(.jeecg-layout-header--dark) .app-search-trigger {
    color: rgba(255, 255, 255, 0.85);
    background-color: rgba(255, 255, 255, 0.12);
    border-color: rgba(255, 255, 255, 0.25);

    &:hover {
      color: #fff;
      border-color: rgba(255, 255, 255, 0.6);
      box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.15);
    }
  }

  // Màn nhỏ: thu lại chỉ còn icon để không chiếm chỗ trên header.
  // Phase 4 Bước 10 (G6): giữ vùng chạm tối thiểu 44x44 cho nút tìm kiếm trên mobile.
  @media (max-width: 768px) {
    .app-search-trigger {
      height: 44px;
      min-width: 44px;
      padding: 0 10px;
      margin: 0 2px;
      background-color: transparent;
      border-color: transparent;
      justify-content: center;

      &__text {
        display: none;
      }
    }
  }
</style>
