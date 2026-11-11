import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  list = '/sys/message/sysMessageTemplate/list',
  delete = '/sys/message/sysMessageTemplate/delete',
  deleteBatch = '/sys/message/sysMessageTemplate/deleteBatch',
  exportXls = 'sys/message/sysMessageTemplate/exportXls',
  importXls = 'sys/message/sysMessageTemplate/importExcel',
  save = '/sys/message/sysMessageTemplate/add',
  edit = '/sys/message/sysMessageTemplate/edit',
  // Gửi kiểm tra
  send = '/sys/message/sysMessageTemplate/sendMsg',
}

export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa hàng loạt
 * @param params
 * @param confirm
 */
export const deleteBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'Xóa',
        content: 'Bạn có chắc chắn muốn xóa không?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * Lưu hoặc thay đổi mẫu tin nhắn
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (unref(isUpdate)) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * Gửi tin nhắn kiểm tra
 * @param params
 */
export const sendMessageTest = (params) => defHttp.post({ url: Api.send, params });
