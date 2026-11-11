<template>
  <div class="ding-ding-container" :class="[`${prefixCls}`]">
    <div class="ding-header">
      <ul class="ding-menu-tab">
        <li :class="activeKey === 'ding' ? 'active' : ''" @click="dingLiClick('ding')"><a>Tích hợp DingTalk</a></li>
        <li :class="activeKey === 'wechat' ? 'active' : ''" @click="dingLiClick('wechat')"><a>Tích hợp WeCom</a></li>
        <!--update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-->
        <li :class="activeKey === 'feishu' ? 'active' : ''" @click="dingLiClick('feishu')"><a>Tích hợp Feishu</a></li>
        <!--update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-->
      </ul>
    </div>
    <div v-show="activeKey === 'ding'" class="base-collapse">
      <ThirdAppDingTalkConfigForm />
    </div>
    <div v-show="activeKey === 'wechat'" class="base-collapse">
      <ThirdAppWeEnterpriseConfigForm />
    </div>
    <!--update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-->
    <div v-show="activeKey === 'feishu'" class="base-collapse">
      <ThirdAppFeishuConfigForm />
    </div>
    <!--update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu-->
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import ThirdAppDingTalkConfigForm from './ThirdAppDingTalkConfigForm.vue';
  import ThirdAppWeEnterpriseConfigForm from './ThirdAppWeEnterpriseConfigForm.vue';
  import ThirdAppFeishuConfigForm from './ThirdAppFeishuConfigForm.vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  export default defineComponent({
    name: 'ThirdAppConfigList',
    components: {
      ThirdAppDingTalkConfigForm,
      ThirdAppWeEnterpriseConfigForm,
      ThirdAppFeishuConfigForm,
    },
    setup() {
      const { prefixCls } = useDesign('j-dd-container');

      //đã được chọnkey
      const activeKey = ref<string>('ding');

      /**
       * tabSự kiện nhấp
       * @param key
       */
      function dingLiClick(key) {
        activeKey.value = key;
      }

      return {
        activeKey,
        dingLiClick,
        prefixCls,
      };
    },
  });
</script>

<style lang="less" scoped>
  .ding-ding-container {
    border-radius: 4px;
    height: calc(100% - 80px);
    margin: 16px;
  }
  .ding-header {
    align-items: center;
    /*begin Tương thích chế độ tối*/
    border-bottom: 1px solid @border-color-base;
    /*end Tương thích chế độ tối*/
    box-sizing: border-box;
    display: flex;
    height: 50px;
    justify-content: space-between;
    padding: 0 24px;

    ul {
      margin-bottom: 0;
    }
  }
  .ding-menu-tab {
    display: flex;
    height: 100%;

    li {
      align-items: center;
      border-bottom: 2px solid transparent;
      display: flex;
      height: 100%;
      margin-right: 38px;

      a {
        /*begin Tương thích chế độ tối*/
        color: @text-color !important;
        /*end Tương thích chế độ tối*/
        font-size: 15px;
        font-weight: 700;
      }
    }
  }
  .active {
    border-bottom-color: #2196f3 !important;

    a {
      color: #333 !important;
    }
  }
  .empty-image{
    align-items: center;
    display: flex;
    flex-direction: column;
    height: calc(100% - 50px);
    justify-content: center;
    width: 100%;
  }
</style>

<style lang="less">
  /* update-begin-author:liusq date:20230625 for: [issues/563]Một phần chủ đề tối không hoạt động*/
  @prefix-cls: ~'@{namespace}-j-dd-container';
  /*begin Tương thích chế độ tối*/
  .@{prefix-cls} {
    background: @component-background;

    .ding-header {
      border-bottom: 1px solid @border-color-base;
    }

    .ding-menu-tab {
      li {
        a {
          color: @text-color !important;
        }
      }
    }

    .ant-collapse-borderless {
      background-color: @component-background;
    }

    .ant-collapse{
      background-color: @component-background;
    }
  }
  /*end Tương thích chế độ tối*/
/* update-end-author:liusq date:20230625 for: [issues/563]Một phần chủ đề tối không hoạt động*/
</style>
