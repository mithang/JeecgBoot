import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess, resultError, baseUrl } from '../_util';
import { ResultEnum } from '../../src/enums/httpEnum';
const userInfo = {
  name: 'Jeecg',
  userid: '00000001',
  email: 'test@gmail.com',
  signature: 'Biển nạp trăm sông, bao dung vạn vật',
  introduction: 'Mỉm cười, nỗ lực, trân trọng',
  title: 'Chuyên gia tương tác',
  group: 'Tập đoàn kinh doanh XXX – Bộ phận nền tảng XXX – Bộ phận kỹ thuật XXX – UED',
  tags: [
    {
      key: '0',
      label: 'Rất nhiều ý tưởng',
    },
    {
      key: '1',
      label: 'Tập trung vào thiết kế',
    },
    {
      key: '2',
      label: 'Nóng bỏng~',
    },
    {
      key: '3',
      label: 'Chân dài',
    },
    {
      key: '4',
      label: 'Gái Tứ Xuyên',
    },
    {
      key: '5',
      label: 'Biển nạp trăm sông',
    },
  ],
  notifyCount: 12,
  unreadCount: 11,
  country: 'China',
  address: 'Xiamen City 77',
  phone: '0592-268888888',
};

export default [
  {
    url: `${baseUrl}/account/getAccountInfo`,
    timeout: 1000,
    method: 'get',
    response: () => {
      return resultSuccess(userInfo);
    },
  },
  {
    url: `${baseUrl}/user/sessionTimeout`,
    method: 'post',
    statusCode: 401,
    response: () => {
      return resultError();
    },
  },
  {
    url: '/basic-api/user/tokenExpired',
    method: 'post',
    statusCode: 200,
    response: () => {
      return resultError('Token Expired!', { code: ResultEnum.TIMEOUT as number });
    },
  },
] as MockMethod[];
