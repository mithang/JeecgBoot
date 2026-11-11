<template>
  <Dropdown placement="bottomLeft" :overlayClassName="`${prefixCls}-dropdown-overlay`">
    <span :class="[prefixCls, `${prefixCls}--${theme}`]" class="flex">
      <img :class="`${prefixCls}__header`" :src="getAvatarUrl" />
      <span :class="`${prefixCls}__info hidden md:block`">
        <span :class="`${prefixCls}__name  `" class="truncate">
          {{ getUserInfo.realname }}
        </span>
      </span>
    </span>

    <template #overlay>
      <Menu @click="handleMenuClick">
        <MenuItem itemKey="doc" :text="t('layout.header.dropdownItemDoc')" icon="ion:document-text-outline" v-if="getShowDoc" />
        <MenuDivider v-if="getShowDoc" />
        <MenuItem itemKey="account" :text="t('layout.header.dropdownItemSwitchAccount')" icon="ant-design:setting-outlined" />
        <MenuItem itemKey="password" :text="t('layout.header.dropdownItemSwitchPassword')" icon="ant-design:edit-outlined" />
        <MenuItem itemKey="depart" :text="t('layout.header.dropdownItemSwitchDepart')" icon="ant-design:cluster-outlined" />
        <MenuItem itemKey="cache" :text="t('layout.header.dropdownItemRefreshCache')" icon="ion:sync-outline" />
        <!-- <MenuItem
            v-if="getUseLockPage"
            itemKey="lock"
            :text="t('layout.header.tooltipLock')"
            icon="ion:lock-closed-outline"
        />-->
        <MenuItem itemKey="logout" :text="t('layout.header.dropdownItemLoginOut')" icon="ion:power-outline" />
      </Menu>
    </template>
  </Dropdown>
  <LockAction v-if="lockActionVisible" ref="lockActionRef" @register="register" />
  <DepartSelect ref="loginSelectRef" />
  <UpdatePassword v-if="passwordVisible" ref="updatePasswordRef" />
</template>
<script lang="ts">
  // components
  import { Dropdown, Menu } from 'ant-design-vue';

  import { defineComponent, computed, ref, nextTick } from 'vue';

  import { SITE_URL } from '/@/settings/siteSetting';

  import { useUserStore } from '/@/store/modules/user';
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { useGo } from '/@/hooks/web/usePage';
  import headerImg from '/@/assets/images/header.jpg';
  import { propTypes } from '/@/utils/propTypes';
  import { openWindow } from '/@/utils';

  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

  import { refreshCache, queryAllDictItems } from '/@/views/system/dict/dict.api';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { removeAuthCache, setAuthCache } from '/src/utils/auth';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { getRefPromise } from '/@/utils/index';
  import { refreshDragCache, refreshHomeCache } from "@/api/common/api";

  type MenuEvent = 'logout' | 'doc' | 'lock' | 'cache' | 'depart' | 'defaultHomePage' | 'password' | 'account';
  const { createMessage } = useMessage();
  export default defineComponent({
    name: 'UserDropdown',
    components: {
      Dropdown,
      Menu,
      MenuItem: createAsyncComponent(() => import('./DropMenuItem.vue')),
      MenuDivider: Menu.Divider,
      LockAction: createAsyncComponent(() => import('../lock/LockModal.vue')),
      DepartSelect: createAsyncComponent(() => import('./DepartSelect.vue')),
      UpdatePassword: createAsyncComponent(() => import('./UpdatePassword.vue')),
    },
    props: {
      theme: propTypes.oneOf(['dark', 'light']),
    },
    setup() {
      const { prefixCls } = useDesign('header-user-dropdown');
      const { t } = useI18n();
      const { getShowDoc, getUseLockPage } = useHeaderSetting();
      const userStore = useUserStore();
      const go = useGo();
      const passwordVisible = ref(false);
      const lockActionVisible = ref(false);
      const lockActionRef = ref(null);

      const getUserInfo = computed(() => {
        const { realname = '', avatar, desc } = userStore.getUserInfo || {};
        return { realname, avatar: avatar || headerImg, desc };
      });

      const getAvatarUrl = computed(() => {
        let { avatar } = getUserInfo.value;
        if (avatar == headerImg) {
          return avatar;
        } else {
          return getFileAccessHttpUrl(avatar);
        }
      });

      const [register, { openModal }] = useModal();
      /**
       * Logic popup nhiều phòng ban
       */
      const loginSelectRef = ref();
      // Giải thích logic mã: 【QQYUN-6333】 Vấn đề route trống — Lần đầu truy cập tài nguyên quá lớn
      async function handleLock() {
        await getRefPromise(lockActionRef);
        openModal(true);
      }
      // Đăng xuất
      function handleLoginOut() {
        userStore.confirmLoginOut();
      }

      // Mở tài liệu
      function openDoc() {
        openWindow(SITE_URL);
      }

      // Xóa cache
      async function clearCache() {
        const result = await refreshCache();
        const dragRes = await refreshDragCache();
        console.log('dragRes', dragRes);
        await refreshHomeCache();
        await userStore.getUserInfoAction();
        if (result.success) {
          const res = await queryAllDictItems();
          removeAuthCache(DB_DICT_DATA_KEY);
          setAuthCache(DB_DICT_DATA_KEY, res.result);
          createMessage.success(t('layout.header.refreshCacheComplete'));
          // Giải thích logic mã: 【issues/7433】 Vue3 đề xuất tối ưu từ điển dữ liệu
          userStore.setAllDictItems(res.result);
        } else {
          createMessage.error(t('layout.header.refreshCacheFailure'));
        }
      }
      // Chuyển phòng ban
      function updateCurrentDepart() {
        loginSelectRef.value.show();
      }
      // Đổi mật khẩu
      const updatePasswordRef = ref();
      // Giải thích logic mã: 【QQYUN-6333】 Vấn đề route trống — Lần đầu truy cập tài nguyên quá lớn
      async function updatePassword() {
        passwordVisible.value = true;
        await getRefPromise(updatePasswordRef);
        updatePasswordRef.value.show(userStore.getUserInfo.username);
      }
      function handleMenuClick(e: { key: MenuEvent }) {
        switch (e.key) {
          case 'logout':
            handleLoginOut();
            break;
          case 'doc':
            openDoc();
            break;
          case 'lock':
            handleLock();
            break;
          case 'cache':
            clearCache();
            break;
          case 'depart':
            updateCurrentDepart();
            break;
          case 'password':
            updatePassword();
            break;
          case 'account':
            // Giải thích logic mã: Vào trang cài đặt người dùng------------
            go(`/system/usersetting`);
            break;
        }
      }

      return {
        prefixCls,
        t,
        getUserInfo,
        getAvatarUrl,
        handleMenuClick,
        getShowDoc,
        register,
        getUseLockPage,
        loginSelectRef,
        updatePasswordRef,
        passwordVisible,
        lockActionVisible,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-user-dropdown';

  .@{prefix-cls} {
    height: @header-height;
    padding: 0 0 0 10px;
    padding-right: 10px;
    overflow: hidden;
    font-size: 12px;
    cursor: pointer;
    align-items: center;

    img {
      width: 24px;
      height: 24px;
      margin-right: 12px;
    }

    &__header {
      border-radius: 50%;
    }

    &__name {
      font-size: 14px;
    }

    &--dark {
      &:hover {
        background-color: @header-dark-bg-hover-color;
      }
    }

    &--light {
      &:hover {
        background-color: @header-light-bg-hover-color;
      }

      .@{prefix-cls}__name {
        color: @text-color-base;
      }

      .@{prefix-cls}__desc {
        color: @header-light-desc-color;
      }
    }

    &-dropdown-overlay {
      // Giải thích logic mã: 【QQYUN-7512】 Khi di chuột qua tài khoản trên cùng, lần đầu popup vị trí sẽ thay đổi một chút
      width: 160px;
      .ant-dropdown-menu-item {
        min-width: 160px;
      }
    }
  }
  // Giải thích logic mã: 【QQYUN-13013】 Chuyển sang chế độ tiếng Anh, menu thả xuống hơi hẹp
  html[lang="en"] {
    .@{prefix-cls} {
      &-dropdown-overlay {
        width: 175px;
      }
    }
  }
</style>
