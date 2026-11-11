import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useGlobSetting } from '/@/hooks/setting';

const { createMessage, createWarningModal } = useMessage();
const glob = useGlobSetting();

/**
 * Xuất tệpxlsxcủamime-type
 */
export const XLSX_MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
/**
 * Xuất tệpxlsxhậu tố tệp của
 */
export const XLSX_FILE_SUFFIX = '.xlsx';

export function useMethods() {
  /**
   * Xuấtxls
   * @param name
   * @param url
   * @param params
   * @param isXlsx
   * @param timeout Thời gian timeout (mili giây), mặc định 60000
   */
  async function exportXls(name, url, params, isXlsx = false, timeout = 60000) {
    // Giải thích logic mã: 【JHHB-794]Quản lý người dùng, sau khi chọn tất cả qua các trang, nhấp xuất người dùng không phản hồi---
    if(params?.selections){
      let split = params.selections.split(",");
      if(split && split.length > 100){
        createMessage.warning('Tối đa có thể chọn 100 mục để export!');
        return;
      }
    }
    // Sửa thành trả về gốc response, tiện lấy headers
    const response = await defHttp.get(
      { url: url, params: params, responseType: 'blob', timeout: timeout },
      { isTransformResponse: false, isReturnNativeResponse: true }
    );
    if (!response || !response.data) {
      createMessage.warning('Tải xuống tệp thất bại');
      return;
    }
    // Kiểm tra header trong content-disposition Có chứa không .xlsx
    let isXlsxByHeader = isXlsx;
    const disposition = response.headers && response.headers['content-disposition'];
    if (disposition && disposition.indexOf('.xlsx') !== -1) {
      isXlsxByHeader = true;
    }
    const data = response.data;
    // Giải thích logic mã: XuấtexcelGợi ý thất bại, không xuất---
    let reader = new FileReader()
    reader.readAsText(data, 'utf-8')
    reader.onload = async () => {
      if(reader.result){
        if(reader.result.toString().indexOf("success") !=-1){
          // Giải thích logic mã: 【issues/7738]Trong tệp có"success"Xuất báo lỗi ---
          try {
            const { success, message } = JSON.parse(reader.result.toString());
            if (!success) {
              createMessage.warning('Xuất thất bại, nguyên nhân thất bại:' + message);
            } else {
              exportExcel(name, isXlsxByHeader, data);
            }
            return;
          } catch (error) {
            exportExcel(name, isXlsxByHeader, data);
          }
        }
      }
      exportExcel(name, isXlsxByHeader, data);
    }
  }

  /**
   * Importxls
   * @param data Dữ liệu đã import
   * @param url
   * @param success Callback sau khi thành công
   */
  async function importXls(data, url, success) {
    const isReturn = (fileInfo) => {
      try {
        if (fileInfo.code === 201) {
          let {
            message,
            result: { msg, fileUrl, fileName },
          } = fileInfo;
          let href = glob.uploadUrl + fileUrl;
          createWarningModal({
            title: message,
            centered: false,
            content: `<div>
                                <span>${msg}</span><br/> 
                                <span>Chi tiết cụ thể vui lòng<a href = ${href} download = ${fileName}> Nhấp tải xuống </a> </span> 
                              </div>`,
          });
          // Giải thích logic mã: [VUEN-2827]Import không có quyền, gợi ý lỗi biểu tượng------------
        } else if (fileInfo.code === 500 || fileInfo.code === 510) {
          createMessage.error(fileInfo.message || `${data.file.name} Import thất bại`);
        } else {
          createMessage.success(fileInfo.message || `${data.file.name} Tải lên tệp thành công`);
        }
      } catch (error) {
        console.log('Dữ liệu đã import bất thường', error);
      } finally {
        typeof success === 'function' ? success(fileInfo) : '';
      }
    };
    await defHttp.uploadFile({ url }, { file: data.file }, { success: isReturn });
  }

  return {
    handleExportXls: (name: string, url: string, params?: object, timeout?: number) => exportXls(name, url, params, false, timeout),
    handleImportXls: (data, url, success) => importXls(data, url, success),
    handleExportXlsx: (name: string, url: string, params?: object, timeout?: number) => exportXls(name, url, params, true, timeout),
  };

  /**
   * Xuấtexcel
   * @param name
   * @param isXlsx
   * @param data
   */
  function exportExcel(name, isXlsx, data) {
    if (!name || typeof name != 'string') {
      name = 'Xuất tệp';
    }
    let blobOptions = { type: 'application/vnd.ms-excel' };
    let fileSuffix = '.xls';
    if (isXlsx) {
      blobOptions['type'] = XLSX_MIME_TYPE;
      fileSuffix = XLSX_FILE_SUFFIX;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data], blobOptions), name + fileSuffix);
    } else {
      let url = window.URL.createObjectURL(new Blob([data], blobOptions));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', name + fileSuffix);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //Tải xuống xong thì xóa phần tử
      window.URL.revokeObjectURL(url); //Giải phóngblobĐối tượng
    }
  }
}
