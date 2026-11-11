import { FormProps, FormSchema } from '/@/components/Table';
import { BasicColumn } from '/@/components/Table/src/types/table';

export function getBasicColumns(): BasicColumn[] {
  return [
    {
      title: 'ID',
      dataIndex: 'id',
      fixed: 'left',
      width: 200,
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 150,
      filters: [
        { text: 'Male', value: 'male' },
        { text: 'Female', value: 'female' },
      ],
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      width: 300,
    },
    {
      title: 'Mã',
      dataIndex: 'no',
      width: 150,
      sorter: true,
      defaultHidden: true,
    },
    {
      title: 'Thời gian bắt đầu',
      width: 150,
      sorter: true,
      dataIndex: 'beginTime',
    },
    {
      title: 'Thời gian kết thúc',
      width: 150,
      sorter: true,
      dataIndex: 'endTime',
    },
  ];
}

export function getBasicShortColumns(): BasicColumn[] {
  return [
    {
      title: 'ID',
      width: 150,
      dataIndex: 'id',
      sorter: true,
      sortOrder: 'ascend',
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 120,
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
    },
    {
      title: 'Mã',
      dataIndex: 'no',
      width: 80,
    },
  ];
}

export function getMultipleHeaderColumns(): BasicColumn[] {
  return [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 200,
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 120,
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      sorter: true,
      children: [
        {
          title: 'Mã',
          dataIndex: 'no',
          width: 120,
          filters: [
            { text: 'Male', value: 'male', children: [] },
            { text: 'Female', value: 'female', children: [] },
          ],
        },

        {
          title: 'Thời gian bắt đầu',
          dataIndex: 'beginTime',
          width: 120,
        },
        {
          title: 'Thời gian kết thúc',
          dataIndex: 'endTime',
          width: 120,
        },
      ],
    },
  ];
}

export function getCustomHeaderColumns(): BasicColumn[] {
  return [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 200,
    },
    {
      // title: 'Họ tên',
      dataIndex: 'name',
      width: 120,
      slots: { title: 'customTitle' },
    },
    {
      // title: 'Địa chỉ',
      dataIndex: 'address',
      width: 120,
      slots: { title: 'customAddress' },
      sorter: true,
    },

    {
      title: 'Mã',
      dataIndex: 'no',
      width: 120,
      filters: [
        { text: 'Male', value: 'male', children: [] },
        { text: 'Female', value: 'female', children: [] },
      ],
    },
    {
      title: 'Thời gian bắt đầu',
      dataIndex: 'beginTime',
      width: 120,
    },
    {
      title: 'Thời gian kết thúc',
      dataIndex: 'endTime',
      width: 120,
    },
  ];
}
const renderContent = (filed) => {
  return (record, rowIndex) => {
    const obj: any = {
      children: record[filed],
      attrs: {},
    };
    if (rowIndex === 9) {
      obj.attrs.colSpan = 0;
    }
    return obj;
  };
};
export function getMergeHeaderColumns(): BasicColumn[] {
  return [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 300,
      customCell: renderContent('id'),
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 300,
      customCell: renderContent('name'),
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      colSpan: 2,
      width: 120,
      sorter: true,
      customCell: (record: any, rowIndex) => {
        const obj: any = {
          children: record['address'],
          attrs: {},
        };
        if (rowIndex === 2) {
          obj.attrs.rowSpan = 2;
        }
        if (rowIndex === 3) {
          obj.attrs.colSpan = 0;
        }
        return obj;
      },
    },
    {
      title: 'Mã',
      dataIndex: 'no',
      colSpan: 0,
      filters: [
        { text: 'Male', value: 'male', children: [] },
        { text: 'Female', value: 'female', children: [] },
      ],
      customCell: renderContent('no'),
    },
    {
      title: 'Thời gian bắt đầu',
      dataIndex: 'beginTime',
      width: 200,
      customCell: renderContent('beginTime'),
    },
    {
      title: 'Thời gian kết thúc',
      dataIndex: 'endTime',
      width: 200,
      customCell: renderContent('endTime'),
    },
  ];
}
export const getAdvanceSchema = (itemNumber = 6): FormSchema[] => {
  const arr: any = [];
  for (let index = 0; index < itemNumber; index++) {
    arr.push({
      field: `field${index}`,
      label: `Trường${index}`,
      component: 'Input',
      colProps: {
        xl: 12,
        xxl: 8,
      },
    });
  }
  return arr;
};
export function getFormConfig(): Partial<FormProps> {
  return {
    labelWidth: 100,
    schemas: [
      ...getAdvanceSchema(5),
      {
        field: `field11`,
        label: `SlotVí dụ`,
        component: 'Select',
        slot: 'custom',
        colProps: {
          xl: 12,
          xxl: 8,
        },
      },
    ],
  };
}
export function getBasicData() {
  const data: any = (() => {
    const arr: any = [];
    for (let index = 0; index < 40; index++) {
      arr.push({
        id: `${index}`,
        name: 'John Brown',
        age: `1${index}`,
        no: `${index + 10}`,
        address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
        beginTime: new Date().toLocaleString(),
        endTime: new Date().toLocaleString(),
      });
    }
    return arr;
  })();
  return data;
}

export function getTreeTableData() {
  const data: any = (() => {
    const arr: any = [];
    for (let index = 0; index < 40; index++) {
      arr.push({
        id: `${index}`,
        name: 'John Brown',
        age: `1${index}`,
        no: `${index + 10}`,
        address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
        beginTime: new Date().toLocaleString(),
        endTime: new Date().toLocaleString(),
        children: [
          {
            id: `l2-${index}`,
            name: 'John Brown',
            age: `1${index}`,
            no: `${index + 10}`,
            address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
            beginTime: new Date().toLocaleString(),
            endTime: new Date().toLocaleString(),
          },
          {
            id: `l3-${index}`,
            name: 'John Mary',
            age: `1${index}`,
            no: `${index + 10}`,
            address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
            beginTime: new Date().toLocaleString(),
            endTime: new Date().toLocaleString(),
          },
        ],
      });
    }
    return arr;
  })();

  return data;
}
