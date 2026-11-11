import { computed, ref, Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { onlineDefaultButton } from '../../cgform.data';
// update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
import { initDictOptions } from '/@/utils/dict';
// update-end--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
//Chỉ hỗ trợ nút tăng cường ở phần bắt đầu
const START_EVENT_ONLY_BUTTONS = ['import', 'export', 'query'];

export function useJavaColumns(btnList: Ref<any[]>) {
  let columns: BasicColumn[] = [
    {
      title: 'Nút trang',
      align: 'center',
      dataIndex: 'buttonCode',
      customRender: ({ text }) => renderButtonText(text, btnList.value),
    },
    {
      title: 'Trạng thái sự kiện',
      align: 'center',
      dataIndex: 'event',
      customRender: ({ text }) => (text == 'start' ? 'Bắt đầu' : 'Kết thúc'),
    },
    {
      title: 'Loại',
      align: 'center',
      dataIndex: 'cgJavaType',
      customRender: ({ text }) => {
        if (text == 'spring') {
          return 'spring-key';
        } else if (text === 'class') {
          return 'java-class';
        } else if (text === 'http') {
          return 'http-api';
        } else {
          return text;
        }
      },
    },
    {
      title: 'Nội dung',
      align: 'center',
      dataIndex: 'cgJavaValue',
    },
    {
      title: 'Có hiệu lực hay không',
      align: 'center',
      dataIndex: 'activeStatus',
      customRender: ({ text }) => {
        if (text == '1') {
          return 'Hợp lệ';
        } else {
          return 'Không hợp lệ';
        }
      },
    },
  ];
  return { columns };
}

export function useJavaFormSchemas(btnList: Ref<any[]>) {
  const formSchemas = computed<FormSchema[]>(() => {
    return [
      {
        label: 'Nút trang',
        field: 'buttonCode',
        component: 'Select',
        componentProps: {
          options: [
            { label: 'Thêm mới', value: 'add' },
            { label: 'Chỉnh sửa', value: 'edit' },
            { label: 'Xóa', value: 'delete' },
            { label: 'Nhập (import)', value: 'import' },
            { label: 'Xuất (export)', value: 'export' },
            { label: 'Truy vấn', value: 'query' },
            ...btnList.value.map((item) => ({ label: item.buttonName, value: item.buttonCode })),
          ],
        },
        defaultValue: 'add',
      },
      {
        label: 'Trạng thái sự kiện',
        field: 'event',
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'Bắt đầu', value: 'start' },
            { label: 'Kết thúc', value: 'end' },
          ],
        },
        defaultValue: 'end',
        ifShow: ({ values, model }) => {
          const isStartEventOnly = START_EVENT_ONLY_BUTTONS.includes(values.buttonCode);
          if (isStartEventOnly) {
            model.event = 'start';
          }
          return !isStartEventOnly;
        },
      },
      {
        label: 'Loại',
        field: 'cgJavaType',
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'spring-key', value: 'spring' },
            { label: 'java-class', value: 'class' },
            { label: 'http-api', value: 'http' },
          ],
        },
        defaultValue: 'spring',
      },
      {
        label: 'Nội dung',
        field: 'cgJavaValue',
        component: 'Input',
        required: true,
      },
      {
        label: 'Có hiệu lực hay không',
        field: 'activeStatus',
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'Hợp lệ', value: '1' },
            { label: 'Không hợp lệ', value: '0' },
          ],
        },
        defaultValue: '1',
      },
    ];
  });

  return { formSchemas };
}

export function useSqlColumns(btnList: Ref<any[]>) {
  let columns: BasicColumn[] = [
    {
      title: 'Nút trang',
      align: 'center',
      dataIndex: 'buttonCode',
      customRender: ({ text }) => renderButtonText(text, btnList.value),
    },
    // update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
    {
      title: 'Nguồn dữ liệu',
      align: 'center',
      dataIndex: 'dbSource',
      width: 140,
      customRender: ({ text }) => (!text ? 'Đi theo nguồn dữ liệu của biểu mẫu' : text === 'master' ? 'Cơ sở dữ liệu chính (master)' : text),
    },
    // update-end--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
    {
      title: 'Tăng cườngSQL',
      align: 'center',
      dataIndex: 'cgbSql',
      ellipsis: true,
    },
  ];
  return { columns };
}

export function useSqlFormSchemas(btnList: Ref<any[]>) {
  // update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
  // Dropdown nguồn dữ liệu: theo nguồn dữ liệu của biểu mẫu (giá trị trống)/ Cơ sở dữ liệu chính (giá trị là master）/ các đa nguồn dữ liệu (giá trị là sys_data_source.code）
  const FOLLOW_FORM_DS = { label: 'Đi theo nguồn dữ liệu của biểu mẫu (mặc định)', value: '' };
  const MASTER_DS = { label: 'Cơ sở dữ liệu chính (master) / Nguồn dữ liệu mặc định', value: 'master' };
  const dbSourceOptions = ref<any[]>([FOLLOW_FORM_DS, MASTER_DS]);
  initDictOptions('sys_data_source,name,code')
    .then((res) => {
      const list = (res || []).map((d: any) => ({ label: d.label ?? d.text ?? d.title, value: d.value }));
      dbSourceOptions.value = [FOLLOW_FORM_DS, MASTER_DS, ...list];
    })
    .catch(() => {});
  // update-end--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
  const formSchemas = computed<FormSchema[]>(() => {
    return [
      {
        label: 'Nút trang',
        field: 'buttonCode',
        component: 'Select',
        componentProps: {
          allowClear: false,
          options: [
            { label: 'Thêm mới', value: 'add' },
            { label: 'Chỉnh sửa', value: 'edit' },
            { label: 'Xóa', value: 'delete' },
            ...btnList.value.map((item) => ({ label: item.buttonName, value: item.buttonCode })),
          ],
        },
        defaultValue: 'add',
      },
      {
        label: 'Tăng cườngSQL',
        field: 'cgbSql',
        component: 'JCodeEditor',
        componentProps: {
          language: 'sql',
          placeholder: 'Vui lòng nhậpSQLCâu lệnh',
          languageChange: false,
          lineNumbers: false,
          fullScreen: true,
          height: '320px',
        },
        defaultValue: '',
      },
      // update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
      {
        label: 'Nguồn dữ liệu',
        field: 'dbSource',
        component: 'Select',
        defaultValue: '',
        componentProps: {
          options: dbSourceOptions.value,
          allowClear: false,
        },
        helpMessage: [
          'Tăng cườngSQLThực thi ở kho nào:',
          '· Đi theo nguồn dữ liệu của biểu mẫu (mặc định): đi theo nguồn dữ liệu được cấu hình của biểu mẫu này (biểu mẫu cũng chưa cấu hình thì đi qua cơ sở dữ liệu chính)',
          '· Cơ sở dữ liệu chính (master) / Nguồn dữ liệu mặc định: tăng cườngSQLcần thao tác sys_* Khi đợi bảng trong cơ sở dữ liệu chính thì chọn nó',
          '· Chỉ định đa nguồn dữ liệu: thực thi tại nguồn dữ liệu đã chọn đó',
        ],
      },
      // update-end--author:jeecg---date:20260513---for：【QQYUN-15337】onlineBiểu mẫuSQLPhần tăng cường hỗ trợ chỉ định nguồn dữ liệu
      {
        label: 'Mô tả',
        field: 'content',
        component: 'InputTextArea',
        defaultValue: '',
      },
    ];
  });

  return { formSchemas };
}

function renderButtonText(text: string, btnList: any[]) {
  let str = '';
  for (let item of onlineDefaultButton) {
    if (item.code === text) {
      str = item.title;
      break;
    }
  }
  if (!str) {
    for (let item of btnList) {
      if (item.buttonCode === text) {
        str = item.buttonName;
        break;
      }
    }
  }
  return str || text;
}
