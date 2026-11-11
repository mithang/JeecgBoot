import { UploadApiResult } from './model/uploadModel';
import { defHttp } from '/@/utils/http/axios';
import { UploadFileParams } from '/#/axios';
import { useGlobSetting } from '/@/hooks/setting';

const { uploadUrl = '' } = useGlobSetting();

/**
 * @description: Upload interface
 */
export function uploadApi(params: UploadFileParams, onUploadProgress: (progressEvent: ProgressEvent) => void) {
  return defHttp.uploadFile<UploadApiResult>(
    {
      url: `${uploadUrl}/sys/common/upload`,
      onUploadProgress,
    },
    params,
    // update-begin--author:liaozhiyang---date:20260804---for: 【LHZP-1385]Sửa lỗi ví dụ tải lên
    // Trả về phản hồi đầy đủ của API upload, tránh bên gọi lấy được undefined Bị coi là thất bại
    { isReturnResponse: true }
    // update-end--author:liaozhiyang---date:20260804---for: 【LHZP-1385]Sửa lỗi ví dụ tải lên
  );
}
/**
 * @description: Upload interface
 */
export function uploadImg(params: UploadFileParams, onUploadProgress: (progressEvent: ProgressEvent) => void) {
  return defHttp.uploadFile<UploadApiResult>(
    {
      url: `${uploadUrl}/sys/common/upload`,
      onUploadProgress,
    },
    params,
    { isReturnResponse: true }
  );
}
