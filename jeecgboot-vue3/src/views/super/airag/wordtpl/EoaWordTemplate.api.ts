import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { downloadFile } from '/@/api/common/api';
import { message } from 'ant-design-vue';

const { createConfirm } = useMessage();

enum Api {
  list = '/airag/word/list',
  save = '/airag/word/add',
  edit = '/airag/word/edit',
  deleteOne = '/airag/word/delete',
  deleteBatch = '/airag/word/deleteBatch',
  downloadTpl = '/airag/word/download',
  parseFile = '/airag/word/parse/file',
  generateWord = '/airag/word/generate/word',
  generateResume = '/airag/flow/run',
}
/**
 * Giao diện danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa đơn lẻ
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn hay không',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Tải xuống mẫu
 * @param params
 */
export const downloadTpl = (params) => {
  return downloadFile(Api.downloadTpl, params.name + '.docx', { id: params.id });
};

/**
 * Tạo word
 * @param params
 */
export const generateWord = (fileName, params) => {
  return defHttp
    .post(
      {
        url: Api.generateWord,
        params: params,
        responseType: 'blob',
      },
      { isTransformResponse: false }
    )
    .then((data) => {
      if (!data || data.size === 0) {
        message.warning('Tải xuống tệp thất bại');
        return;
      }
      if (typeof window.navigator.msSaveBlob !== 'undefined') {
        window.navigator.msSaveBlob(new Blob([data]), fileName);
      } else {
        const url = window.URL.createObjectURL(new Blob([data]));
        const link = document.createElement('a');
        link.style.display = 'none';
        link.href = url;
        link.setAttribute('download', fileName);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link); //Sau khi tải xuống hoàn thành loại bỏ phần tử
        window.URL.revokeObjectURL(url); //Giải phóng đối tượng blob
      }
    });
};

/**
 * Tạo hồ sơ
 * @param params
 */
export const generateResume = (params, handleSuccess) => {
  return defHttp
    .post(
      {
        url: Api.generateResume,
        params: params,
        timeout: 120000,
        timeoutErrorMessage: 'Đồng bộ cơ sở dữ liệu quá thời gian chờ, đã tự động làm mới',
      },
      { isTransformResponse: false }
    )
    .then((value) => {
      handleSuccess(value);
    });
};

/**
 * Phân tích địa chỉ tài liệu word
 * @param params
 */
export const parseFileUrl = Api.parseFile;
