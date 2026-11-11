<!--Chọn người dùng theo phòng ban-->
<template>
  <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="1200px" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
    <a-row :gutter="10">
      <a-col :md="7" :sm="24" style="height: 613px;overflow: auto ">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <!--Tổ chức-->
          <BasicTree
            ref="treeRef"
            :style="{ minWidth: '250px' }"
            selectable
            @select="onDepSelect"
            :load-data="loadChildrenTreeData"
            :treeData="departTree"
            :selectedKeys="selectedDepIds"
            :expandedKeys="expandedKeys"
            :clickRowToExpand="false"
          >
            <template #title="{ orgCategory, title }">
              <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
            </template>
          </BasicTree>
        </a-card>
      </a-col>
      <a-col :md="17" :sm="24">
        <a-card :style="{ minHeight: '613px', overflow: 'auto' }">
          <!--Danh sách người dùng-->
          <BasicTable ref="tableRef" v-bind="getBindValue" :searchInfo="searchInfo" :api="getTableList" :rowSelection="rowSelection" :defSort="{ column: '', order: '' }"></BasicTable>
        </a-card>
      </a-col>
    </a-row>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, unref, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTree } from '/@/components/Tree/index';
  import { queryTreeList, getTableList as getTableListOrigin } from '/@/api/common/api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useSelectBiz } from '/@/components/Form/src/jeecg/hooks/useSelectBiz';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { queryDepartTreeSync as queryDepartTreeSyncOrigin } from '/@/views/system/depart/depart.api';
  import { selectProps } from '/@/components/Form/src/jeecg/props/props';
  import TreeIcon from '@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';
  export default defineComponent({
    name: 'UserSelectByDepModal',
    components: {
      TreeIcon,
      //Tại đây cần tải bất đồng bộBasicTable
      BasicModal,
      BasicTree,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      ...selectProps,
      //Tiêu đề hộp chọn
      modalTitle: {
        type: String,
        default: 'Chọn người dùng theo phòng ban',
      },
    },
    emits: ['register', 'getSelectResult'],
    setup(props, { emit, refs }) {
      const tableRef = ref();
      const treeRef = ref();
      //Đăng ký popup
      const [register, { closeModal }] = useModalInner(async (data) => {
        await queryDepartTree();
      });
      const attrs = useAttrs();
      const departTree = ref([]);
      const selectedDepIds = ref([]);
      const expandedKeys = ref([]);
      const searchInfo = {};
      /**
       *Cấu hình bảng
       */
      const tableProps = {
        columns: [
          {
            title: 'Tài khoản người dùng',
            dataIndex: 'username',
            width: 180,
          },
          {
            title: 'Họ tên người dùng',
            dataIndex: 'realname',
            width: 180,
          },
          {
            title: 'Giới tính',
            dataIndex: 'sex_dictText',
            width: 80,
          },
          {
            title: 'Số điện thoại',
            dataIndex: 'phone',
            customRender:( { record, text })=>{
              if(record.izHideContact && record.izHideContact === '1'){
                return '/';
              }
              return text;
            }
            // width: 50,
          },
        ],
        useSearchForm: true,
        canResize: false,
        showIndexColumn: false,
        striped: true,
        bordered: true,
        size: 'small',
        formConfig: {
          //labelWidth: 200,
          autoSubmitOnEnter: true,
          baseColProps: {
            xs: 24,
            sm: 8,
            md: 6,
            lg: 8,
            xl: 6,
            xxl: 8,
          },
          // Giải thích logic mã: [issues/5514]Trang component hiển thị lệch vị trí
          actionColOptions: {
              xs: 24,
              sm: 12,
              md: 12,
              lg: 12,
              xl: 8,
              xxl: 8,
          },
          schemas: [
            {
              label: 'Tài khoản',
              field: 'username',
              component: 'Input',
            },
            {
              label: 'Họ tên',
              field: 'realname',
              component: 'Input',
            },
          ],
          resetFunc: customResetFunc,
        },
      };
      const getBindValue = Object.assign({}, unref(props), unref(attrs), tableProps);
      const [{ rowSelection, visibleChange, indexColumnProps, getSelectResult, reset }] = useSelectBiz(getTableList, getBindValue);

      function getTableList(params) {
        params = parseParams(params);
        return getTableListOrigin({...params});
      }

      function queryDepartTreeSync(params) {
        params = parseParams(params);
        return queryDepartTreeSyncOrigin({...params});
      }

      /**
       * Phân tích tham số
       * @param params
       */
      function parseParams(params) {
        if (props?.params) {
          return {
            ...params,
            ...props.params,
          }
        }
        return params;
      }

      /**
       * Tải dữ liệu dạng cây
       */
      function queryDepartTree() {
        queryDepartTreeSync().then((res) => {
          if (res) {
            departTree.value = res;
            // Mở rộng nút cha theo mặc định
            //expandedKeys.value = unref(departTree).map(item => item.id)
          }
        });
      }
      /**
       * Tải phòng ban cấp con
       */
      async function loadChildrenTreeData(treeNode) {
        try {
          const result = await queryDepartTreeSync({
            pid: treeNode.eventKey,
          });
          const asyncTreeAction = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: result });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
        } catch (e) {
          console.error(e);
        }
        return Promise.resolve();
      }
      /**
       * Nhấp nút cây,Lọc ra người dùng tương ứng
       */
      function onDepSelect(keys) {
        if (keys[0] != null) {
          if (unref(selectedDepIds)[0] !== keys[0]) {
            selectedDepIds.value = [keys[0]];
          }
          searchInfo['departId'] = unref(selectedDepIds).join(',');
          tableRef.value.reload();
        }
      }
      /**
       * Phương thức đặt lại tùy chỉnh
       * */
      async function customResetFunc() {
        console.log('Tìm kiếm tùy chỉnh');
        //Xóa nút cây
        selectedDepIds.value = [];
        //Xóa điều kiện truy vấn
        searchInfo['departId'] = '';
        //Xóa mục đã chọn
        reset();
      }
      /**
       * Xác nhận lựa chọn
       */
      function handleOk() {
        getSelectResult((options, values) => {
          //Trả về tùy chọn và giá trị đã chọn
          emit('getSelectResult', options, values);
          //Đóng popup
          closeModal();
        });
      }

      return {
        //config,
        handleOk,
        searchInfo,
        register,
        indexColumnProps,
        visibleChange,
        getBindValue,
        rowSelection,

        departTree,
        selectedDepIds,
        expandedKeys,
        treeRef,
        tableRef,
        getTableList,
        onDepSelect,
        loadChildrenTreeData,
      };
    },
  });
</script>

<style scoped lang="less"></style>
