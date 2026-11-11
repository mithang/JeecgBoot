<!--Hộp chọn phòng ban-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="600px" :minHeight="300" :maxHeight="maxHeight" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <a-input-search v-if="izOnlySelectDepartPost" placeholder="Tìm kiếm theo tên vị trí…" style="margin-bottom: 10px" @search="onSearch" @change="handelSearchChange"/>
      <BasicTree
        ref="treeRef"
        :treeData="treeData"
        :load-data="sync == false ? null : onLoadData"
        v-bind="getBindValue"
        @select="onSelect"
        @check="onCheck"
        :fieldNames="fieldNames"
        :checkedKeys="checkedKeys"
        :expandedKeys="expandedKeys"
        :multiple="multiple"
        :checkStrictly="getCheckStrictly"
        :key="reloadKey"
      >
        <template #title="{ orgCategory, title, orgCode }">
          <TreeIcon :orgCategory="orgCategory" :title="getTitle(orgCategory, title, orgCode)"></TreeIcon>
        </template>
      </BasicTree>
      <!--Phần thao tác cây-->
      <template #insertFooter>
        <a-dropdown placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="multiple" key="1" @click="checkALL(true)">Chọn tất cả</a-menu-item>
              <a-menu-item v-if="multiple" key="2" @click="checkALL(false)">Bỏ chọn tất cả</a-menu-item>
              <a-menu-item key="3" @click="expandAll(true)">Mở rộng tất cả</a-menu-item>
              <a-menu-item key="4" @click="expandAll(false)">Thu gọn tất cả</a-menu-item>
              <a-menu-item v-if="multiple" key="5" @click="toggleCheckStrictly(false)">Liên kết theo cấp bậc</a-menu-item>
              <a-menu-item v-if="multiple" key="6" @click="toggleCheckStrictly(true)">Độc lập theo cấp bậc</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left"> Thao tác cây <Icon icon="ant-design:up-outlined" /> </a-button>
        </a-dropdown>
      </template>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { queryDepartTreeSync, queryTreeList, queryDepartAndPostTreeSync } from '/@/api/common/api';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { treeProps } from '/@/components/Form/src/jeecg/props/props';
  import { BasicTree, TreeActionType } from '/@/components/Tree';
  import { useTreeBiz } from '/@/components/Form/src/jeecg/hooks/useTreeBiz';
  import {propTypes} from "/@/utils/propTypes";
  import { omit } from 'lodash-es';
  import TreeIcon from '@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';
  import { getDepartPathNameByOrgCode } from "@/utils/common/compUtils";

  export default defineComponent({
    name: 'DeptSelectModal',
    components: {
      TreeIcon,
      BasicModal,
      BasicTree,
    },
    props: {
      ...treeProps,
      //Tiêu đề hộp chọn
      modalTitle: {
        type: String,
        default: 'Chọn phòng ban',
      },
      // Giải thích logic mã: 【QQYUN-7678]Nội dung component phòng ban quá nhiều nhưng không có thanh cuộn (đặt một chiều cao tối đa mặc định)
      maxHeight: {
        type: Number,
        default: 500,
      },
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      //Tham số truy vấn
      params: {
        type: Object,
        default: () => ({}),
      },
      //Có hiển thị đường dẫn phòng ban hay không (khi người dùng chọn vị trí chính và vị trí kiêm nhiệm của phòng ban cần hiển thị đầy đủ đường dẫn)
      izShowDepPath: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit }) {
      //Đăng ký popup
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      const treeRef = ref<Nullable<TreeActionType>>(null);
      //Tải câykey
      const reloadKey = ref<number>(Math.random());
      
      // Giải thích logic mã: Loại cảnh báo khi chọn phòng ban không khớp
      let propValue = props.value === ''?[]:props.value;
      // Đảm bảo value truyền cho BasicTree có định dạng mảng
      if (propValue && typeof propValue === 'string') {
        propValue = propValue.split(',');
      }
      // Giải thích logic mã: [issues/538]JSelectDeptComponent bị ảnh hưởng bởi dynamicDisabled
      let temp = Object.assign({}, unref(props), unref(attrs), {value: propValue},{disabled: false});
      const getBindValue = omit(temp, 'multiple');
      
      const queryUrl = getQueryUrl();
      const [{ visibleChange, checkedKeys, getCheckStrictly, getSelectTreeData, onCheck, onLoadData, treeData, checkALL, expandAll, onSelect, onSearch, expandedKeys, checkStrictly }] =
        useTreeBiz(treeRef, queryUrl, getBindValue, props, emit);
      const searchInfo = ref(props.params || {});
      const tree = ref([]);
      //Thay trường key trong treeNode bằng trường tương ứng trong treeData
      const fieldNames = {
        key: props.rowKey,
      };
      // {children:'children', title:'title', key:'key' }
      /**
       * Xác nhận lựa chọn
       */
      function handleOk() {
        getSelectTreeData((options, values) => {
          //Trả về tùy chọn và giá trị đã chọn
          emit('getSelectResult', options, values);
          //Đóng popup
          closeModal();
        });
      }

      /** Phương thức lấy dữ liệu tìm kiếm */
      function getQueryUrl() {
        let queryFn = props.izOnlySelectDepartPost ? queryDepartAndPostTreeSync :props.sync ? queryDepartTreeSync : queryTreeList;
        // Giải thích logic mã: issues/I5F3P4 onlineSau khi cấu hình chọn phòng ban rồi chỉnh sửa, khi xem dữ liệu phải hiển thị tên phòng ban, không phải mã phòng ban
        return (params) => queryFn(Object.assign({}, params, props.params, { primaryKey: props.rowKey }));
      }

      /**
       * Sự kiện thay đổi giá trị tìm kiếm
       * @param value
       */
      function handelSearchChange(value) {
        if(!value.target.value){
          reloadKey.value = Math.random();
        }
      }

      /**
       * Đặt liên kết theo cấp bậc và độc lập theo cấp bậc
       * 
       * @param value
       */
      function toggleCheckStrictly(value) {
        checkStrictly.value = value;
      }

      //Cache tiêu đề
      const titleCache = reactive<Record<string, string>>({});
      
      /**
       * Lấy tiêu đề
       * @param orgCategory
       * @param title
       * @param orgCode
       */
      function getTitle(orgCategory, title, orgCode) {
        if(props.izShowDepPath && orgCategory === '2'){
          const cached = titleCache[orgCode];
          if (cached){
            return cached;
          }
          getDepartPathNameByOrgCode(orgCode,title,"").then(res=>{
            if(res){
              titleCache[orgCode] =  title + "(" + res.substring(0, res.lastIndexOf('/')) + ")";
            }else{
              titleCache[orgCode] =  title;
            }
          });
          return title;
        } else {
          return title;
        }
      }
      
      return {
        tree,
        handleOk,
        searchInfo,
        treeRef,
        treeData,
        onCheck,
        onSelect,
        checkALL,
        expandAll,
        fieldNames,
        checkedKeys,
        expandedKeys,
        register,
        getBindValue,
        getCheckStrictly,
        visibleChange,
        onLoadData,
        onSearch,
        reloadKey,
        handelSearchChange,
        toggleCheckStrictly,
        getTitle,
      };
    },
  });
</script>
<style>
  .svg-company {
    width: 18px;
    height: 18px;
    position: relative;
    top: 1px;
    right: 2px;
  }
  .svg-depart,.svg-post {
    width: 14px;
    height: 16px;
    position: relative;
    right: 2px;
  }
</style>
