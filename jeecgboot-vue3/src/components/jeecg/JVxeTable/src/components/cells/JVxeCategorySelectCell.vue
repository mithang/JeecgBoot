<template>
  <a-tree-select
    allow-clear
    label-in-value
    show-checked-strategy="SHOW_ALL"
    style="width: 100%"
    :get-popup-container="(node) => node?.parentNode"
    :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="cellAttrs.placeholder || 'Vui lòng chọn'"
    :disabled="cellAttrs.disabled"
    :multiple="isMultiple"
    :load-data="asyncLoadTreeData"
    :value="treeValue"
    :tree-data="treeData"
    @change="onChange"
  />
</template>

<script lang="ts">
  /**
   * Cấu hình cột phải bao gồm: pcode (mã gốc phân loại, ưu tiên) hoặc pid (ID gốc phân loại).
   * Hỗ trợ chọn nhiều: Cấu hình cột thêm multiple: true, giá trị lưu trữ là chuỗi phân tách bằng dấu phẩy.
   */
  import { ref, computed, watch, defineComponent } from 'vue';
  import { loadDictItem, loadTreeData } from '/@/api/common/api';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';

  // Cache nhãn cấp module, key là id của nút phân loại, được ghi khi chọn và khi hiển thị lại,translate.handler Ưu tiên đọc cache
  const _catLabelCache = new Map<string, string>();

  function _cacheCatLabels(ids: string[], labels: string[]) {
    ids.forEach((id, i) => _catLabelCache.set(id, String(labels[i])));
  }

  export default defineComponent({
    name: 'JVxeCategorySelectCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);

      // cellProps nội bộ dùng {} khai báo, không có kiểu thuộc tính cụ thể; sau khi chuyển đổi thống nhất thì cung cấp cho template dùng trực tiếp, tránh trong template `as` Khẳng định
      const cellAttrs = computed(() => cellProps.value as Record<string, any>);

      // Danh sách nút cây
      const treeData = ref<any[]>([]);
      // Giá trị kèm label đang được chọn (định dạng labelInValue, chọn nhiều thì là mảng)
      const treeValue = ref<any>(null);

      // Có cho phép chọn nhiều hay không (do cấu hình cột multiple kiểm soát)
      const isMultiple = computed(() => !!originColumn.value?.multiple);

      /** Phân tích tham số cây phân loại từ cấu hình cột, ưu tiên dùng pcode */
      const catConfig = computed(() => {
        const col = originColumn.value;
        return {
          pcode: col?.pcode || '',
          pid: col?.pid || col?.pidValue || '',
          condition: col?.condition || '',
        };
      });

      // Khi giá trị bên ngoài thay đổi sẽ hiển thị lại label
      watch(() => innerValue.value, (val) => loadItemByCode(val), { immediate: true });

      // Khi cấu hình phân loại thay đổi, tải lại nút gốc
      watch(() => catConfig.value.pcode, () => loadRoot(), { immediate: true });

      /** Tải danh sách nút gốc của phân loại */
      async function loadRoot() {
        const { pcode, pid, condition } = catConfig.value;
        const param = pcode ? { pcode, condition } : { pid: pid || '0', pcode: '0', condition };
        const res = await loadTreeData(param);
        if (res?.length) {
          treeData.value = res.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: item.leaf === true,
          }));
        }
      }

      /** Truy vấn label tương ứng theo giá trị lưu trữ (ID phân tách bằng dấu phẩy), dùng để hiển thị lại */
      async function loadItemByCode(val) {
        if (!val) {
          treeValue.value = isMultiple.value ? [] : { label: null, value: null };
          return;
        }
        const ids = val.split(',');
        // Nếu cache đã trúng toàn bộ, dùng trực tiếp cache để xây dựng treeValue, bỏ qua yêu cầu API
        const cachedLabels = ids.map((id) => _catLabelCache.get(id));
        if (cachedLabels.every((l) => l !== undefined)) {
          treeValue.value = isMultiple.value
            ? ids.map((id, i) => ({ key: id, value: id, label: cachedLabels[i] }))
            : { key: val, value: val, label: cachedLabels[0] };
          return;
        }
        const res = await loadDictItem({ ids: val });
        if (isMultiple.value) {
          // Đa chọn: Tách ID, ánh xạ từng cái thành {key, value, label}
          treeValue.value = ids.map((id, index) => ({
            key: id,
            value: id,
            label: res?.[index] ?? id,
          }));
          if (res?.length) _cacheCatLabels(ids, res);
        } else {
          const label = res?.length ? res[0] : val;
          treeValue.value = { key: val, value: val, label };
          if (res?.length) _cacheCatLabels(ids, res);
        }
      }

      /** Tải lười không đồng bộ nút con */
      async function asyncLoadTreeData(treeNode) {
        if (treeNode.dataRef.children) return Promise.resolve();
        const { condition } = catConfig.value;
        const pid = treeNode.dataRef.key;
        const res = await loadTreeData({ pid, condition });
        if (res?.length) {
          const children = res.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: item.leaf === true,
          }));
          addChildren(pid, children, treeData.value);
          treeData.value = [...treeData.value];
        }
        return Promise.resolve();
      }

      /** Đệ quy chèn nút con vào dưới nút cha tương ứng */
      function addChildren(pid, children, arr) {
        if (!arr?.length) return;
        for (const item of arr) {
          if (item.key === pid) {
            item.children = children.length ? children : undefined;
            if (!children.length) item.isLeaf = true;
            break;
          }
          addChildren(pid, children, item.children);
        }
      }

      /** Sự kiện chọn hoặc xóa */
      function onChange(value) {
        if (!value || (Array.isArray(value) && value.length === 0)) {
          // Xóa sạch
          treeValue.value = isMultiple.value ? [] : null;
          handleChangeCommon('');
        } else if (Array.isArray(value)) {
          // Chọn nhiều: mảng → Chuỗi phân tách bằng dấu phẩy; đồng thời cache id->label Để chế độ không chỉnh sửa sử dụng trực tiếp
          treeValue.value = value;
          _cacheCatLabels(value.map((item) => item.value), value.map((item) => item.label));
          handleChangeCommon(value.map((item) => item.value).join(','));
        } else {
          // Chọn một
          treeValue.value = value;
          if (value.value != null) _cacheCatLabels([value.value], [value.label]);
          handleChangeCommon(value.value);
        }
      }

      return { cellAttrs, treeData, treeValue, isMultiple, asyncLoadTreeData, onChange };
    },
    // [Tăng cường component] Chế độ không chỉnh sửa: ưu tiên đọc cache, tránh yêu cầu API lặp lại
    enhanced: {
      switches: { editRender: true, visible: false },
      translate: {
        enabled: true,
        async handler(value) {
          if (!value) return '';
          const ids = String(value).split(',');
          const cachedLabels = ids.map((id) => _catLabelCache.get(id));
          if (cachedLabels.every((l) => l !== undefined)) {
            return cachedLabels.join(',');
          }
          try {
            const res = await loadDictItem({ ids: value });
            if (res?.length) {
              _cacheCatLabels(ids, res);
              return res.join(',');
            }
          } catch {}
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
