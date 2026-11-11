<template>
  <a-tree-select
    v-if="show"
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
   * Cấu hình cột phải bao gồm: dict ("Tên bảng,Trường văn bản,Trường lưu trữ"), pidField (tên trường cấp cha),
   * pidValue(Giá trị nút gốc, tùy chọn), hasChildField (có trường nút con hay không, tùy chọn).
   * Hỗ trợ chọn nhiều: Cấu hình cột thêm multiple: true, giá trị lưu trữ là chuỗi phân tách bằng dấu phẩy.
   */
  import { ref, computed, watch, defineComponent } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';

  const API_LOAD_TREE = '/sys/dict/loadTreeData';
  const API_LOAD_ITEM = '/sys/dict/loadDictItem/';

  // Cache nhãn cấp module, key là "tableName:id", ghi khi chọn và khi hiển thị lại,translate.handler Ưu tiên đọc cache
  const _labelCache = new Map<string, string>();

  function _cacheLabels(tableName: string, ids: string[], labels: string[]) {
    ids.forEach((id, i) => _labelCache.set(`${tableName}:${id}`, String(labels[i])));
  }

  export default defineComponent({
    name: 'JVxeTreeSelectCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);

      // cellProps nội bộ dùng {} khai báo, không có kiểu thuộc tính cụ thể; sau khi chuyển đổi thống nhất thì cung cấp cho template dùng trực tiếp, tránh trong template `as` Khẳng định
      const cellAttrs = computed(() => cellProps.value as Record<string, any>);

      // Danh sách nút cây
      const treeData = ref<any[]>([]);
      // Giá trị kèm label đang được chọn (định dạng labelInValue, chọn nhiều thì là mảng)
      const treeValue = ref<any>(null);
      // Dùng để đặt lại component (gắn lại khi dict thay đổi)
      const show = ref(true);

      // Có cho phép chọn nhiều hay không (do cấu hình cột multiple kiểm soát)
      const isMultiple = computed(() => !!originColumn.value?.multiple);

      /**
       * Phân tích tham số liên quan đến cây từ cấu hình cột
       * dict có định dạng "tableName,textField,codeField"
       */
      const treeConfig = computed(() => {
        const col = originColumn.value;
        const dictArr = (col?.dict || '').split(',');
        return {
          tableName: dictArr[0] || '',
          text: dictArr[1] || '',
          code: dictArr[2] || '',
          pidField: col?.pidField || 'pid',
          pidValue: col?.pidValue || '',
          hasChildField: col?.hasChildField || '',
          condition: col?.condition || '',
        };
      });

      // Khi giá trị bên ngoài thay đổi sẽ hiển thị lại label
      watch(
        () => innerValue.value,
        (val) => loadItemByCode(val),
        { immediate: true }
      );

      // dict Tải lại nút gốc khi cấu hình thay đổi
      watch(
        () => treeConfig.value.tableName,
        async (tableName) => {
          if (tableName) {
            treeData.value = [];
            show.value = false;
            await loadRoot();
            show.value = true;
          }
        },
        { immediate: true }
      );

      /** Tải danh sách nút gốc */
      async function loadRoot() {
        const { tableName, text, code, pidField, pidValue, hasChildField, condition } = treeConfig.value;
        if (!tableName) return;
        const res = await defHttp.get(
          { url: API_LOAD_TREE, params: { tableName, text, code, pid: pidValue, pidField, hasChildField, condition } },
          { isTransformResponse: false }
        );
        if (res.success && res.result) {
          treeData.value = res.result.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: !!item.leaf,
          }));
        }
      }

      /** Truy vấn label tương ứng theo giá trị lưu trữ (ID phân tách bằng dấu phẩy), dùng để hiển thị lại */
      async function loadItemByCode(val) {
        if (!val) {
          treeValue.value = isMultiple.value ? [] : { label: null, value: null };
          return;
        }
        const { tableName, text, code } = treeConfig.value;
        if (!tableName) {
          treeValue.value = isMultiple.value ? val.split(',').map((id) => ({ key: id, value: id, label: id })) : { label: val, value: val };
          return;
        }
        const ids = val.split(',');
        // Nếu cache đã trúng toàn bộ, dùng trực tiếp cache để xây dựng treeValue, bỏ qua yêu cầu API
        const cachedLabels = ids.map((id) => _labelCache.get(`${tableName}:${id}`));
        if (cachedLabels.every((l) => l !== undefined)) {
          treeValue.value = isMultiple.value
            ? ids.map((id, i) => ({ key: id, value: id, label: cachedLabels[i] }))
            : { key: val, value: val, label: cachedLabels[0] };
          return;
        }
        const res = await defHttp.get({ url: `${API_LOAD_ITEM}${tableName},${text},${code}`, params: { key: val } }, { isTransformResponse: false });
        if (isMultiple.value) {
          // Đa chọn: Tách ID, ánh xạ từng cái thành {key, value, label}
          treeValue.value = ids.map((id, index) => ({
            key: id,
            value: id,
            label: res.success ? (res.result?.[index] ?? id) : id,
          }));
          if (res.success && res.result?.length) _cacheLabels(tableName, ids, res.result);
        } else {
          const label = res.success && res.result?.length ? res.result[0] : val;
          treeValue.value = { key: val, value: val, label };
          if (res.success && res.result?.length) _cacheLabels(tableName, ids, res.result);
        }
      }

      /** Tải lười không đồng bộ nút con */
      async function asyncLoadTreeData(treeNode) {
        if (treeNode.dataRef.children) return Promise.resolve();
        const { tableName, text, code, pidField, hasChildField, condition } = treeConfig.value;
        const pid = treeNode.dataRef.key;
        const res = await defHttp.get(
          { url: API_LOAD_TREE, params: { tableName, text, code, pid, pidField, hasChildField, condition } },
          { isTransformResponse: false }
        );
        if (res.success) {
          const children = res.result.map((item) => ({
            ...item,
            value: item.key,
            isLeaf: !!item.leaf,
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
        const { tableName } = treeConfig.value;
        if (!value || (Array.isArray(value) && value.length === 0)) {
          // Xóa sạch
          treeValue.value = isMultiple.value ? [] : null;
          handleChangeCommon('');
        } else if (Array.isArray(value)) {
          // Chọn nhiều: mảng → Chuỗi phân tách bằng dấu phẩy; đồng thời cache id->label Để chế độ không chỉnh sửa sử dụng trực tiếp
          treeValue.value = value;
          if (tableName)
            _cacheLabels(
              tableName,
              value.map((item) => item.value),
              value.map((item) => item.label)
            );
          handleChangeCommon(value.map((item) => item.value).join(','));
        } else {
          // Chọn một
          treeValue.value = value;
          if (tableName && value.value != null) _cacheLabels(tableName, [value.value], [value.label]);
          handleChangeCommon(value.value);
        }
      }

      return { cellAttrs, treeData, treeValue, show, isMultiple, asyncLoadTreeData, onChange };
    },
    // [Tăng cường component] Chế độ không chỉnh sửa: ưu tiên đọc cache, tránh yêu cầu API lặp lại
    enhanced: {
      switches: { editRender: true, visible: false },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          if (!value) return '';
          const col = ctx?.context.originColumn.value;
          const dictArr = (col?.dict || '').split(',');
          const tableName = dictArr[0];
          if (!tableName) return value;
          const ids = String(value).split(',');
          const cachedLabels = ids.map((id) => _labelCache.get(`${tableName}:${id}`));
          if (cachedLabels.every((l) => l !== undefined)) {
            return cachedLabels.join(',');
          }
          try {
            const res = await defHttp.get(
              { url: `${API_LOAD_ITEM}${tableName},${dictArr[1] || ''},${dictArr[2] || ''}`, params: { key: value } },
              { isTransformResponse: false }
            );
            if (res.success && res.result?.length) {
              _cacheLabels(tableName, ids, res.result);
              return res.result.join(',');
            }
          } catch {}
          return value;
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
