import { BasicColumn } from '/@/components/Table';
import dayjs from 'dayjs';
import _get from 'lodash.get';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: 'Thời gian yêu cầu',
    dataIndex: 'timestamp',
    width: 50,
    customRender({ text }) {
      return dayjs(text).format('YYYY-MM-DD HH:mm:ss');
    },
  },
  {
    title: 'Phương thức yêu cầu',
    dataIndex: 'request.method',
    width: 20,
    customRender({ record, column }) {
      let value = _get(record, column.dataIndex!);
      let color = '';
      if (value === 'GET') {
        color = '#87d068';
      }
      if (value === 'POST') {
        color = '#2db7f5';
      }
      if (value === 'PUT') {
        color = '#ffba5a';
      }
      if (value === 'DELETE') {
        color = '#ff5500';
      }
      return h(Tag, { color }, () => value);
    },
  },
  {
    title: 'URL yêu cầu',
    dataIndex: 'request.uri',
    width: 200,
    customRender({ record, column }) {
      return _get(record, column.dataIndex!);
    },
  },
  {
    title: 'Trạng thái phản hồi',
    dataIndex: 'response.status',
    width: 50,
    customRender({ record, column }) {
      let value = _get(record, column.dataIndex!);
      let color = '';
      if (value < 200) {
        color = 'pink';
      } else if (value < 201) {
        color = 'green';
      } else if (value < 399) {
        color = 'cyan';
      } else if (value < 403) {
        color = 'orange';
      } else if (value < 501) {
        color = 'red';
      }
      return h(Tag, { color }, () => value);
    },
  },
  {
    title: 'Thời gian xử lý yêu cầu',
    dataIndex: 'timeTaken',
    width: 50,
    customRender({ record, column }) {
      let value = _get(record, column.dataIndex!);
      let color = 'red';
      if (value < 500) {
        color = 'green';
      } else if (value < 1000) {
        color = 'cyan';
      } else if (value < 1500) {
        color = 'orange';
      }
      return h(Tag, { color }, () => `${value} ms`);
    },
    sorter: true,
  },
];
