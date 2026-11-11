import type { Ref, ComputedRef } from 'vue';
import { ref, computed, nextTick, inject } from 'vue';
import { CgformModal } from '../types';
import { JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
import { VALIDATE_FAILED } from '../cgform.data';
import { pick } from 'lodash-es';

export function useTableSync(columns: Ref<JVxeColumn[]>) {
  const tables = inject<CgformModal.TablesRef>('tables');
  const fullScreenRef = inject<ComputedRef<boolean>>('fullScreenRef');
  const vxetableHeight = inject<ComputedRef<number>>('vxetableHeight');
  const tableRef = ref<JVxeTableInstance>();
  const loading = ref(false);
  const dataSource = ref<Recordable[]>([]);
  // Chiều cao động của bảng
  const tableHeight = computed(() => ({
    // Chiều cao bảng bình thường
    normal: fullScreenRef?.value ? vxetableHeight?.value : 260,
    // Không có toolbar chiều cao bảng của
    noToolbar: fullScreenRef?.value ? vxetableHeight?.value : 320,
  }));

  // Tất cả trường cột của bảng hiện tạikey
  const columnKeys = computed<string[]>(() => ['id'].concat(columns.value.map((col) => col.key)));

  // Bảng khácprops
  const tableProps = computed(() => {
    return {
      // Nhắm vàoOnlineBiểu mẫu tối ưu hóa cho cuộn ảo
      // Cấu hình cuộn ảo,yTrục (số hàng) lớn hơnxxKhi có nhiều bản ghi dữ liệu thì bật cuộn ảo
      // update-begin--author:liaozhiyang---date:20231025---for：【QQYUN-6808】onlineChỉnh sửa nhiều trường bị giật lag
      scrollY: {
        enabled: true,
        gt: 15,
      },
      // Số cột
      scrollX: {
        enabled: true,
        gt: 20,
      },
      // update-begin--author:liaozhiyang---date:20231025---for：【QQYUN-6808】onlineChỉnh sửa nhiều trường bị giật lag
    };
  });

  // Kiểm chứng đồng thời lấy dữ liệu bảng
  async function validateData(activeKey: string) {
    let instance = tableRef.value!;
    let errMap = await instance.fullValidateTable();
    if (errMap) {
      throw { code: VALIDATE_FAILED, activeKey };
    }
    // Lọc bỏ các trường không tồn tại trong bảng hiện tại, để tránh nhiều bảng xung đột
    let tableData = instance.getTableData().map((data) => pick(data, columnKeys.value));
    // Lấy cái bị xóaID
    let deleteIds = instance.getDeleteData().map((d) => d.id);
    return { tableData, deleteIds };
  }

  /**
   * Thiết lập nguồn dữ liệu
   * @param data
   * @param insert
   */
  async function setDataSource(data, insert = false) {
    if (insert) {
      dataSource.value = [];
      await nextTick();
      await tableRef.value!.addOrInsert(data, 0, null, { setActive: false });
      await nextTick();
      tableRef.value!.recalcDisableRows();
    } else {
      dataSource.value = data;
      // update-begin--author:liaozhiyang---date:20240705---for：【TV360X-1762】Giải quyết khi chỉnh sửaidCó thể xóa
      await nextTick();
      tableRef.value!.recalcDisableRows();
      // update-end--author:liaozhiyang---date:20240705---for：【TV360X-1762】Giải quyết khi chỉnh sửaidCó thể xóa
    }
  }

  /**
   * Đồng bộ danh sách, có thể đồng bộ thêm mới, sửa đổi, xóa
   * @param dbTable
   */
  function syncTable(dbTable: Ref<CgformModal.DBAttributeTableType | undefined>) {
    let targetTable = tableRef.value!;
    let sourceTable = dbTable.value!.tableRef!;

    let removeIds = dbTable.value!.getRemoveIds();
    let sourceData = sourceTable.getXTable().internalData.tableFullData;
    let targetData = targetTable.getXTable().internalData.tableFullData;
    // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetableTối ưu
    // Dùng Map Chỉ mục (index) targetData, đưa việc tìm kiếm từ O(N) Giảm xuống O(1)
    const targetMap = new Map<string, Recordable>();
    targetData.forEach((targetValue) => {
      if (targetValue.id) {
        targetMap.set(targetValue.id, targetValue);
      }
    });
    // Dùng Set Chỉ mục (index) removeIds, đưa việc tìm kiếm từ O(K) Giảm xuống O(1)
    const removeIdSet = new Set<string>(removeIds);
    // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1852】Khi thêm mới, xóa tất cả trường rồi thêm một trường, lưu thì báo lỗi
    // Trước tiên thu thập những mục cần xóaID, cuối cùng xóa thống nhất, tránh sửa đổi mảng trong lúc duyệt
    const toRemoveIds: string[] = [];
    // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1852】Khi thêm mới, xóa tất cả trường rồi thêm một trường, lưu thì báo lỗi
    // Thu thập hàng loạt các giá trị cần sửa, cuối cùng gọi một lần duy nhất setValues
    const batchSetValues: { rowKey: string; values: Recordable }[] = [];
    // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
    // Lưu đệm trước ánh xạ giá trị mặc định của cột, tránh duyệt lặp lại khi thêm mỗi hàng columns
    const columnDefaults: { key: string; defaultValue: any }[] = [];
    columns.value.forEach((column) => {
      if (column.key !== 'dbFieldName' && column.key !== 'dbFieldTxt') {
        columnDefaults.push({ key: column.key, defaultValue: column.defaultValue });
      }
    });
    // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
    sourceData.forEach((sourceValue) => {
      const targetValue = targetMap.get(sourceValue.id);
      if (targetValue) {
        // Phán đoán giá trị có bị sửa hay không
        let dbFieldName = targetValue['dbFieldName'];
        let dbFieldTxt = targetValue['dbFieldTxt'];
        if (sourceValue.dbFieldName !== dbFieldName || sourceValue.dbFieldTxt !== dbFieldTxt) {
          // Thu thập các trường sửa đổi, lát nữa đồng bộ hàng loạt
          batchSetValues.push({
            rowKey: targetValue.id,
            values: {
              dbFieldName: sourceValue.dbFieldName,
              dbFieldTxt: sourceValue.dbFieldTxt,
            },
          });
        }
      } else {
        // targetkhông tồn tại trong đó, chứng tỏ là mới thêm
        let record = Object.assign({}, sourceValue);
        // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
        for (const { key, defaultValue } of columnDefaults) {
          if (record[key] == undefined) {
            record[key] = defaultValue;
          }
        }
        // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
        targetTable.addRows(record);
      }
    });
    // Đồng bộ sửa đổi hàng loạt
    if (batchSetValues.length > 0) {
      targetTable.setValues(batchSetValues);
    }
    // Xử lý việc xóa:targethàng tồn tại trong đó nhưng đã bị xóa
    // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1852】Khi thêm mới, xóa tất cả trường rồi thêm một trường, lưu thì báo lỗi
    targetData.forEach((targetValue) => {
      if (targetValue.id && removeIdSet.has(targetValue.id)) {
        toRemoveIds.push(targetValue.id);
      }
    });
    if (toRemoveIds.length > 0) {
      setTimeout(() => {
        toRemoveIds.forEach((id) => targetTable.removeRowsById(id));
      }, 0);
    }
    // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1852】Khi thêm mới, xóa tất cả trường rồi thêm một trường, lưu thì báo lỗi
    // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetableTối ưu
    return nextTick();
  }

  return { tables, tableRef, loading, dataSource, columnKeys, tableHeight, tableProps, syncTable, validateData, setDataSource };
}
