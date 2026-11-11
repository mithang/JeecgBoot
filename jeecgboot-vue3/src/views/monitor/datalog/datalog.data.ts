import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag, Tooltip } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: 'Tên bảng',
    dataIndex: 'dataTable',
    width: 120,
    align: 'left',
    customRender: ({ text }) => {
      return h(Tag, { color: 'blue' }, () => text);
    },
  },
  {
    title: 'ID dữ liệu',
    dataIndex: 'dataId',
    width: 260,
    align: 'left',
    ellipsis: true,
    customRender: ({ text }) => {
      return h(
        'span',
        { style: 'font-family: Consolas, Monaco, monospace; font-size: 12px; color: #595959' },
        text
      );
    },
  },
  {
    title: 'Số phiên bản',
    dataIndex: 'dataVersion',
    width: 70,
    align: 'center',
    customRender: ({ text }) => {
      return h(Tag, { color: 'green' }, () => 'V' + text);
    },
  },
  {
    title: 'Nội dung dữ liệu',
    dataIndex: 'dataContent',
    ellipsis: true,
    customRender: ({ text }) => {
      if (!text) return '--';
      // Thử định dạng JSON để hiển thị các trường quan trọng
      try {
        const obj = JSON.parse(text);
        const keys = Object.keys(obj);
        const preview = keys
          .slice(0, 3)
          .map((k) => {
            const v = obj[k];
            const val = v === null || v === undefined || v === '' ? '--' : String(v);
            return `${k}: ${val.length > 20 ? val.substring(0, 20) + '...' : val}`;
          })
          .join(' | ');
        const suffix = keys.length > 3 ? ` (+${keys.length - 3} Trường)` : '';
        return h(
          Tooltip,
          { title: JSON.stringify(obj, null, 2), overlayStyle: { maxWidth: '500px', whiteSpace: 'pre-wrap', fontFamily: 'Consolas, monospace', fontSize: '12px' } },
          () => h('span', { style: 'font-size: 12px; color: #595959' }, preview + suffix)
        );
      } catch {
        return text;
      }
    },
  },
  {
    title: 'Người tạo',
    dataIndex: 'createBy',
    sorter: true,
    width: 90,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 180,
    sorter: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'dataTable',
    label: 'Tên bảng',
    component: 'JInput',
    componentProps: {
      placeholder: 'Vui lòng nhập tên bảng',
    },
    colProps: { span: 6 },
  },
  {
    field: 'dataId',
    label: 'ID dữ liệu',
    component: 'JInput',
    componentProps: {
      placeholder: 'Vui lòng nhập ID dữ liệu',
    },
    colProps: { span: 6 },
  },
  {
    field: 'createBy',
    label: 'Người tạo',
    component: 'JSelectUserByDepartment',
    componentProps: {
      rowKey: 'username',
      labelKey: 'realname',
      showButton: false,
      isRadioSelection: true,
    },
    colProps: { span: 6 },
  },
];
