import { Ref } from 'vue';
import DBAttributeTable from '../components/tables/DBAttributeTable.vue';
import PageAttributeTable from '../components/tables/PageAttributeTable.vue';
import CheckDictTable from '../components/tables/CheckDictTable.vue';
import ForeignKeyTable from '../components/tables/ForeignKeyTable.vue';
import IndexTable from '../components/tables/IndexTable.vue';
import QueryTable from '../components/tables/QueryTable.vue';

// Định nghĩa popupformloại của
export namespace CgformModal {
  export type DBAttributeTableType = InstanceType<typeof DBAttributeTable>;
  export type PageAttributeTableType = InstanceType<typeof PageAttributeTable>;
  export type CheckDictTableType = InstanceType<typeof CheckDictTable>;
  export type ForeignKeyTableType = InstanceType<typeof ForeignKeyTable>;
  export type IndexTableType = InstanceType<typeof IndexTable>;
  export type QueryTableType = InstanceType<typeof QueryTable>;

  export type TablesRef = {
    dbTable: Ref<DBAttributeTableType | undefined>;
    pageTable: Ref<PageAttributeTableType | undefined>;
    checkTable: Ref<CheckDictTableType | undefined>;
    fkTable: Ref<ForeignKeyTableType | undefined>;
    idxTable: Ref<IndexTableType | undefined>;
    queryTable: Ref<QueryTableType | undefined>;
  };
}

// Định nghĩa loại trang,normal = Trang thông thường,copy=Trang view
export enum CgformPageType {
  normal,
  copy,
}

/**
 * OnlineLoại cấu hình mở rộng
 */
export type ExtConfigType = Partial<{
  // Có kích hoạt in báo cáo Jimu hay không (0Không,1là)
  reportPrintShow: number,
  // Địa chỉ báo cáo Jimu
  reportPrintUrl: string,
  // Có kích hoạt truy vấn liên hợp hay không (0Không,1là)
  joinQuery: number,
  // Popup có toàn màn hình mặc định hay không (0Không,1là)
  modelFullscreen: number,
  // Độ rộng tối thiểu của popup (px）
  modalMinWidth: string,
  // Có cố định cột thao tác hay không (0Không,1là)
  tableFixedAction: number,
  // Cách cố định cột thao tác
  tableFixedActionType: 'left' | 'right',
  // Có cho phép điều chỉnh độ rộng cột danh sách hay không
  canResizeColumn?: number,
  // Excel Cách thức xuất hình ảnh
  imageExportMode?: 'embed' | 'path',
}>
