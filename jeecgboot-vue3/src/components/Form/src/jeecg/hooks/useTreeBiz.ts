import type { Ref } from 'vue';
import { inject, reactive, ref, computed, unref, watch, nextTick } from 'vue';
import { TreeActionType } from '/@/components/Tree';
import { listToTree } from '/@/utils/common/compUtils';
import { isEqual } from 'lodash-es';
import { defHttp } from "@/utils/http/axios";
import { queryAllParentId } from "/@/api/common/api";

export function useTreeBiz(treeRef, getList, props, realProps, emit) {
  //Nhận tùy chọn hộp thả xuống
  const selectOptions = inject('selectOptions', ref<Array<object>>([]));
  //Nhận giá trị đã chọn
  const selectValues = <object>inject('selectValues', reactive({}));
  // Có đang tải hiển thị lại không
  const loadingEcho = inject<Ref<boolean>>('loadingEcho', ref(false));
  //Tập dữ liệu
  const treeData = ref<Array<object>>([]);
  //Giá trị đã chọn
  const checkedKeys = ref<Array<string | number>>([]);
  //Bản ghi hàng đã chọn
  const selectRows = ref<Array<object>>([]);
  //Có phải chế độ mở hộp thoại không
  const openModal = ref(false);
  //Có liên kết theo cấp bậc không
  const checkStrictly = ref<boolean>(realProps.multiple ? props.checkStrictly : true);
  // Có bật liên kết cha-con hay không, nếu không cho phép chọn nhiều thì luôn hủy liên kết cha-con
  const getCheckStrictly = computed(() => checkStrictly.value);
  // Có phải lần đầu tải và hiển thị lại không, chỉ lần đầu tải mới hiển thị loading
  let isFirstLoadEcho = true;
  let prevSelectValues = [];
  // Danh sách ID nút cha cần mở rộng
  const expandedKeys = ref<Array<string | number>>([]);
  // Có kích hoạt chức năng tự động mở rộng hay không (có thể điều khiển qua props)
  const enableAutoExpand = props.enableAutoExpand !== false;
  /**
   * Lắng nghe sự thay đổi của selectValues
   */
  watch(
    selectValues,
    ({ value: values }: Recordable) => {
      if(!values){
        return;
      }
      // Giải thích logic mã: 【issues/8232] Đặt giá trị component JSelectDept bằng mã không được dịch
      if (values.length > 0) {
        // Ngăn chặn nhiều yêu cầu
        if (isEqual(values, prevSelectValues)) return;
        prevSelectValues = values;
        loadingEcho.value = isFirstLoadEcho;
        isFirstLoadEcho = false;
        onLoadData(null, values.join(',')).finally(() => {
          loadingEcho.value = false;
        });
      }
    },
    { immediate: true }
  );

  /**
   * Lấy đối tượng cây
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      //throw new Error('tree is null!');
      return null;
    }
    return tree;
  }

  /**
   * Lấy nút cha cần mở rộngID
   */
  async function getParentIdsToExpand(selectedIds) {
    if (!selectedIds || selectedIds.length === 0) return [];
    
    try {
      const result = await queryAllParentId({ 
        departId: selectedIds.join(','),
        orgCode: props.params?.orgCode 
      });
      
      if (result) {
        const allParentIds = [];
        // Xử lý cấu trúc Map hoặc Object
        const valuesToProcess = result instanceof Map 
          ? Array.from(result.values()) 
          : Object.values(result);
        
        // Duyệt qua nút cha của tất cả các nút đã chọn
        valuesToProcess.forEach((nodeData: any) => {
          if (nodeData && nodeData.parentIds && Array.isArray(nodeData.parentIds)) {
            // Thêm ID nút cha (không bao gồm nút được chọn)
            const parentIds = nodeData.parentIds.filter(id => !selectedIds.includes(id));
            allParentIds.push(...parentIds);
          }
        });
        
        return [...new Set(allParentIds)]; // Khử trùng lặp
      }
      return [];
    } catch (error) {
      console.warn('Lấy ID nút cha thất bại:', error);
      return [];
    }
  }

  /**
   * Đặt mức mở rộng cây
   */
  function expandTree() {
    nextTick(() => {
      if (props.defaultExpandLevel && props.defaultExpandLevel > 0) {
        getTree().filterByLevel(props.defaultExpandLevel);
      }
      //Đặt mục chọn mặc định của danh sách
      checkedKeys.value = selectValues['value'];
      
      // Nếu có nút cha cần mở rộng, thì mở rộng chúng
      if (expandedKeys.value.length > 0) {
        getTree().setExpandedKeys(expandedKeys.value);
      }
    }).then(() => {
      // Đảm bảo mở rộng lại lần nữa, vì cây có thể chưa được render hoàn toàn
      if (expandedKeys.value.length > 0) {
        setTimeout(() => {
          getTree().setExpandedKeys(expandedKeys.value);
        }, 100);
      }
    });
  }

  /**
   * Chọn nút cây
   */
  function onSelect(keys, info) {
    if (props.checkable == false) {
      checkedKeys.value = props.checkStrictly ? keys.checked : keys;
      const { selectedNodes } = info;
      let rows = <any[]>[];
      selectedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * Chọn nút cây
   */
  function onCheck(keys, info) {
    if(!info){
      return;
    }
    if (props.checkable == true) {
      // Nếu không thể chọn nhiều, thì chỉ giữ lại mục được chọn cuối cùng
      if (!realProps.multiple) {
        if (info.checked) {
          // Giải thích logic mã: Ở chế độ chọn đơn, khi thiết lập rowKey sẽ không thể chọn dữ liệu-
          checkedKeys.value = [info.node.eventKey];
          let rowKey = props.rowKey;
          let temp = info.checkedNodes.find((n) => n[rowKey] === info.node.eventKey);
          selectRows.value = [temp];
        } else {
          checkedKeys.value = [];
          selectRows.value = [];
        }
        return;
      }
      // Giải thích logic mã: 【JHHB-250] Chọn phòng ban thêm một liên kết cấp bậc/Cấu hình độc lập, hiện tại bấm là tick tất cả---
      checkedKeys.value = checkStrictly.value ? keys.checked : keys;
      const { checkedNodes } = info;
      let rows = <any[]>[];
      checkedNodes.forEach((item) => {
        rows.push(item);
      });
      selectRows.value = rows;
    }
  }

  /**
   * Chọn tất cả
   */
  async function checkALL(checkAll) {
    getTree().checkAll(checkAll);
    // Giải thích logic mã: 【issues/394] Thao tác cây phòng ban trực thuộc: tick tất cả không có hiệu lực/【issues/4646] Sau khi tích chọn tất cả phòng ban, nhấn nút xác nhận thì thông tin phòng ban bị mất------------
    await nextTick();
    checkedKeys.value = getTree().getCheckedKeys();
    if(checkAll){
      getTreeRow();
    }else{
      selectRows.value = [];
    }
  }

  /**
   * Lấy danh sách cây
   * @param res
   */
  function getTreeRow() {
    let ids = "";
    if(unref(checkedKeys).length>0){
      ids = checkedKeys.value.join(",");
    }
    getList({ids:ids}).then((res) =>{
      selectRows.value = res;
    })
  }

  /**
   * Mở rộng tất cả
   */
  function expandAll(expandAll) {
    getTree().expandAll(expandAll);
  }

  /**
   * Tải dữ liệu cây
   */
  async function onLoadData(treeNode, ids) {
    let params = {};
    let startPid = '';
    if (treeNode) {
      startPid = treeNode.eventKey;
      // Giải thích logic mã: rowkeyKhông đặt thành id, khi sync bật bất đồng bộ, nhấn cấp trên/cấp dưới không hiển thị------------
      params['pid'] = treeNode.value;
    }
    if (ids) {
      startPid = '';
      params['ids'] = ids;
    }

    if(props.params?.departIds){
      params['departIds'] = props.params.departIds;
    }
    let record = await getList(params);
    let optionData = record;
    //Chỉ hiển thị thông tin công ty (công ty+công ty con)
    if(props.onlyShowCompany){
      record = getCompanyData(record)
    }
    //Có chỉ chọn vị trí phòng ban không
    if (props.izOnlySelectDepartPost) {
      setCompanyDepartCheckable(record);
    }
    //Có viết tắt hay khôngdepartNameAbbr
    if (props.izShowDepartNameAbbr) {
      record = getDepartAbbrData(record);
    }
    if (!props.serverTreeData) {
      //Frontend xử lý dữ liệu thành cấu trúc tree
      record = listToTree(record, props, startPid);
      if (record.length == 0 && treeNode) {
        checkHasChild(startPid, treeData.value);
      }
    }

    if (openModal.value == true) {
      //Tải toàn bộ dữ liệu trong chế độ hộp thoại
      if (!treeNode) {
        treeData.value = record;
      } else {
        return new Promise((resolve: (value?: unknown) => void) => {
          if (!treeNode.children) {
            resolve();
            return;
          }
          const asyncTreeAction: TreeActionType | null = unref(treeRef);
          if (asyncTreeAction) {
            asyncTreeAction.updateNodeByKey(treeNode.eventKey, { children: record });
            asyncTreeAction.setExpandedKeys([treeNode.eventKey, ...asyncTreeAction.getExpandedKeys()]);
          }
          resolve();
          return;
        });
      }
      expandTree();
    } else {
      const options = <any[]>[];
      optionData.forEach((item) => {
        // Giải thích logic mã: issues/I5F3P4 onlineSau khi cấu hình chọn phòng ban rồi chỉnh sửa, khi xem dữ liệu phải hiển thị tên phòng ban, không phải mã phòng ban
        options.push({ label: item[props.labelKey], value: item[props.rowKey] });
      });
      selectOptions.value = options;
    }
  }

  /**
   * Lấy được công ty/Dữ liệu công ty con
   * @param record
   */
  function getCompanyData(record){
    const companyData = record.filter(item=>item.orgCategory && ['1','4'].includes(item.orgCategory));
    return companyData
  }
  /**
   * Lấy được công ty/Dữ liệu công ty con
   * @param record
   */
  function getDepartAbbrData(record){
    const departAbbrData = record;
    departAbbrData.forEach(item=>{
      item.title = item.departNameAbbr || item.title;
    })
    return departAbbrData
  }
  /**
   * Khi tải bất đồng bộ, kiểm tra có chứa nút cấp dưới hay không
   * @param pid Nút cha
   * @param treeArray  treeDữ liệu
   */
  function checkHasChild(pid, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!item.child) {
            item.isLeaf = true;
          }
          break;
        } else {
          checkHasChild(pid, item.children);
        }
      }
    }
  }

  /**
   * Lấy dữ liệu đã chọn
   */
  function getSelectTreeData(success) {
    const options = <any[]>[];
    const values = <any[]>[];
    selectRows.value.forEach((item) => {
      options.push({ label: item[props.labelKey], value: item[props.rowKey] });
    });
    checkedKeys.value.forEach((item) => {
      values.push(item);
    });
    selectOptions.value = options;
    success && success(options, values);
  }

  /**
   * Sự kiện kích hoạt khi popup hiển thị/ẩn
   */
  async function visibleChange(visible) {
    if (visible) {
      //Tải tất cả dữ liệu khi hộp thoại bật lên
      openModal.value = true;
      await onLoadData(null, null);
      
      // Sau khi tải dữ liệu xong, nếu có giá trị đã chọn và đã bật tính năng tự động mở rộng thì mở rộng nút cha
      if (enableAutoExpand && selectValues.value && selectValues.value.length > 0) {
        try {
          const selectedIds = selectValues.value;
          const parentIds = await getParentIdsToExpand(selectedIds);
          
          if (parentIds.length > 0) {
            expandedKeys.value = parentIds;
            
            // Mở rộng chậm lại để đảm bảo cây đã render xong
            nextTick(() => {
              try {
                const tree = getTree();
                if (tree) {
                  tree.setExpandedKeys(parentIds);

                  // Đảm bảo mở rộng lần nữa
                  setTimeout(() => {
                    try {
                      const tree = getTree();
                      if (tree) {
                        tree.setExpandedKeys(parentIds);
                        console.log('Nút cha đã được mở rộng:', parentIds);
                        // Lần thứ ba đảm bảo mở rộng, dùng độ trễ dài hơn
                        setTimeout(() => {
                          try {
                            const tree = getTree();
                            if (tree) {
                              tree.setExpandedKeys(parentIds);
                            }
                          } catch (error) {
                            console.warn('Mở rộng nút cha thất bại:', error);
                          }
                        }, 500);
                      }
                    } catch (error) {
                      console.warn('Mở rộng nút cha thất bại:', error);
                    }
                  }, 200);
                }
              } catch (error) {
                console.warn('Mở rộng nút cha thất bại:', error);
              }
            });
            
          }
        } catch (error) {
          console.warn('Lấy ID nút cha thất bại:', error);
        }
      }
    } else {
      openModal.value = false;
      emit?.('close');
    }
  }

  /**
   * Đặt hiển thị checkbox phòng ban công ty
   * @param record
   */
  function setCompanyDepartCheckable(record) {
    if (record && record.length > 0) {
      for (const item of record) {
        if (item.orgCategory !== '3') {
          item.checkable = false;
          item.selectable = false;
        } else {
          item.checkable = true;
          item.selectable = true;
        }
        if (item.isLeaf) {
          setCompanyDepartCheckable(item.children);
        }
      }
    }
  }

  /**
   * Tìm kiếm vị trí công tác
   *
   * @param value
   */
  async function onSearch(value) {
    if(value){
      let result = await defHttp.get({ url: "/sys/sysDepart/searchBy", params: { keyWord: value, orgCategory: "3",...props.params } });
      if (Array.isArray(result)) {
        treeData.value = result;
      } else {
        treeData.value = [];
      }
    } else {
      treeData.value = [];
      await onLoadData(null, null)
    }
  }

  return [
    {
      visibleChange,
      selectOptions,
      selectValues,
      onLoadData,
      onCheck,
      onSelect,
      checkALL,
      expandAll,
      checkedKeys,
      selectRows,
      treeData,
      getCheckStrictly,
      getSelectTreeData,
      onSearch,
      expandedKeys,
      checkStrictly,
    },
  ];
}
