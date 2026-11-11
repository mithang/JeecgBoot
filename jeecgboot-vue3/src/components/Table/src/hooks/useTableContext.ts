import type { Ref } from 'vue';
import type { BasicTableProps, TableActionType } from '../types/table';
import { provide, inject, ComputedRef } from 'vue';

// Tạo một key Symbol duy nhất cho mỗi instance bảng, tránh xung đột context giữa các component cha-con hoặc các component cùng cấp
// Vue của provide/inject Được truyền theo cây component, dùng Symbol duy nhất có thể đảm bảo mỗi instance BasicTable độc lập
let tableIdCounter = 0;

type Instance = TableActionType & {
  wrapRef: Ref<Nullable<HTMLElement>>;
  getBindValues: ComputedRef<Recordable>;
};

type RetInstance = Omit<Instance, 'getBindValues'> & {
  getBindValues: ComputedRef<BasicTableProps>;
};

export function createTableContext(instance: Instance) {
  // Mỗi lần tạo context đều sinh một Symbol duy nhất mới Symbol
  const key = Symbol(`basic-table-${++tableIdCounter}`);
  provide(key, instance);
  // Đồng thời cung cấp một dấu hiệu nội bộ để component con có thể lấy được key
  provide('__BASIC_TABLE_CONTEXT_KEY__', key);
}

export function useTableContext(): RetInstance {
  // Lấy từ component cha gần nhất context key
  const key = inject<symbol>('__BASIC_TABLE_CONTEXT_KEY__');
  if (!key) {
    throw new Error('useTableContext must be used after createTableContext');
  }
  return inject(key) as RetInstance;
}
