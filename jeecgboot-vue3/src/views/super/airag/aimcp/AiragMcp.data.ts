import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Biểu tượng',
    align: "center",
    dataIndex: 'icon'
  },
  {
    title: 'Tên',
    align: "center",
    dataIndex: 'name'
  },
  {
    title: 'Mô tả',
    align: "center",
    dataIndex: 'descr'
  },
  {
    title: 'Loại mcp (sse: loại sse; stdio: loại tiêu chuẩn)',
    align: "center",
    dataIndex: 'type'
  },
  {
    title: 'Điểm cuối máy chủ (loại SSE là URL, loại stdio là lệnh)',
    align: "center",
    dataIndex: 'endpoint'
  },
  {
    title: 'Header yêu cầu (loại sse), biến môi trường (loại stdio)',
    align: "center",
    dataIndex: 'headers'
  },
  {
    title: 'Danh sách công cụ',
    align: "center",
    dataIndex: 'tools'
  },
  {
    title: 'Trạng thái (enable=kích hoạt, disable=Vô hiệu hóa)',
    align: "center",
    dataIndex: 'status'
  },
  {
    title: 'Có đồng bộ hay không',
    align: "center",
    dataIndex: 'synced'
  },
  {
    title: 'Siêu dữ liệu',
    align: "center",
    dataIndex: 'metadata'
  },
  {
    title: 'id tenant',
    align: "center",
    dataIndex: 'tenantId'
  },
];

// Dữ liệu truy vấn nâng cao
export const superQuerySchema = {
  icon: {title: 'Biểu tượng ứng dụng',order: 0,view: 'text', type: 'string',},
  name: {title: 'Tên',order: 1,view: 'text', type: 'string',},
  descr: {title: 'Mô tả',order: 2,view: 'text', type: 'string',},
  type: {title: 'Loại mcp (sse: loại sse; stdio: loại tiêu chuẩn)',order: 3,view: 'text', type: 'string',},
  endpoint: {title: 'Điểm cuối máy chủ (loại SSE là URL, loại stdio là lệnh)',order: 4,view: 'textarea', type: 'string',},
  headers: {title: 'Header yêu cầu (loại sse), biến môi trường (loại stdio)',order: 5,view: 'textarea', type: 'string',},
  tools: {title: 'Danh sách công cụ',order: 6,view: 'textarea', type: 'string',},
  status: {title: 'Trạng thái (enable=kích hoạt, disable=Vô hiệu hóa)',order: 7,view: 'text', type: 'string',},
  synced: {title: 'Có đồng bộ hay không',order: 8,view: 'number', type: 'number',},
  metadata: {title: 'Siêu dữ liệu',order: 9,view: 'textarea', type: 'string',},
  tenantId: {title: 'id tenant',order: 10,view: 'text', type: 'string',},
};
