import {defineStore} from 'pinia';
import {store} from '/@/store';

interface CgformState {
  // Bảng được sửa đổi gần đây, bảng lưu trữid
  changedTables: string[];
}

export const useCgformStore = defineStore('cgform-state', {
  state: (): CgformState => ({
    changedTables: [],
  }),
  getters: {},
  actions: {
    /**
     * Kiểm tra cơ sở dữ liệu đã đồng bộ hay chưa
     * @param tableId Bảngid
     */
    checkIsChanged(tableId: string) {
      return this.changedTables.includes(tableId);
    },
    addChangedTable(tableId: string) {
      this.changedTables.push(tableId);
    },
    removeChangedTable(tableId: string) {
      const index = this.changedTables.findIndex((item) => item === tableId);
      if (index !== -1) {
        this.changedTables.splice(index, 1);
      }
    },
  },
});

// tại setup sử dụng ở bên ngoài
export function useCgformStoreWithOut() {
  return useCgformStore(store);
}
