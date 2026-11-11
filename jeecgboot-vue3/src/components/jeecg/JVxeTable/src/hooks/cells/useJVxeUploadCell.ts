import { ref, computed, watch } from 'vue';

import {getTenantId, getToken} from '/@/utils/auth';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { JVxeComponent } from '../../types/JVxeComponent';
import { useJVxeComponent } from '../useJVxeComponent';

/**
 * use Component tải lên dùng chung
 * @param props
 * @param options Tùy chọn component, token: có truyền token mặc định hay không, action: đường dẫn tải lên mặc định, multiple: có cho phép nhiều tệp hay không
 */
export function useJVxeUploadCell(props: JVxeComponent.Props, options?) {
  const setup = useJVxeComponent(props);
  const { innerValue, originColumn, handleChangeCommon } = setup;

  const innerFile = ref<any>(null);

  /** upload headers */
  const uploadHeaders = computed(() => {
    let headers = {};
    if ((originColumn.value.token ?? options?.token ?? false) === true) {
      headers['X-Access-Token'] = getToken();
    }
    let tenantId = getTenantId();
    headers['X-Tenant-Id'] = tenantId ? tenantId : '0';
    return headers;
  });

  /** Địa chỉ yêu cầu tải lên */
  const uploadAction = computed(() => {
    if (!originColumn.value.action) {
      return options?.action ?? '';
    } else {
      return originColumn.value.action;
    }
  });
  const hasFile = computed(() => innerFile.value != null);
  const responseName = computed(() => originColumn.value.responseName ?? 'message');
  // update-begin--author:liaozhiyang---date:20250526---for：【issues/9652]vxetable tải lên, tải tệp lên, tải hình ảnh lên thêm đường dẫn tùy chỉnh
  const uploadData = computed(() => {
    const bizPath = originColumn.value.bizPath;
    const result: any = { isup: 1 };
    if (bizPath) {
      result.biz = bizPath;
    }
    return result;
  });
  // update-end--author:liaozhiyang---date:20250526---for：【issues/9652]vxetable tải lên, tải tệp lên, tải hình ảnh lên thêm đường dẫn tùy chỉnh
  watch(
    innerValue,
    (val) => {
      if (val) {
        innerFile.value = val;
      } else {
        innerFile.value = null;
      }
    },
    { immediate: true }
  );

  function handleChangeUpload(info) {
    let { file } = info;
    let value = {
      name: file.name,
      type: file.type,
      size: file.size,
      status: file.status,
      percent: file.percent,
      path: innerFile.value?.path ?? '',
    };
    if (file.response) {
      value['responseName'] = file.response[responseName.value];
    }
    let paths: string[] = [];
    if (options?.multiple && innerFile.value && innerFile.value.path) {
      paths = innerFile.value.path.split(',');
    }
    if (file.status === 'done') {
      if (typeof file.response.success === 'boolean') {
        if (file.response.success) {
          paths.push(file.response[responseName.value]);
          value['path'] = paths.join(',');
          handleChangeCommon(value);
        } else {
          value['status'] = 'error';
          value['message'] = file.response.message || 'Lỗi không xác định';
        }
      } else {
        // Xét trường hợp nếu đặt đường dẫn tải lên action không phải làjeecg-bootbackend, trong trường hợp có thể không trả về thuộc tính success thì mặc định là thành công
        paths.push(file.response[responseName.value]);
        value['path'] = paths.join(',');
        handleChangeCommon(value);
      }
    } else if (file.status === 'error') {
      value['message'] = file.response.message || 'Lỗi không xác định';
    }
    innerFile.value = value;
  }

  function handleClickDownloadFile() {
    let { url, path } = innerFile.value || {};
    if (!url || url.length === 0) {
      if (path && path.length > 0) {
        url = getFileAccessHttpUrl(path.split(',')[0]);
      }
    }
    if (url) {
      window.open(url);
    }
  }

  function handleClickDeleteFile() {
    handleChangeCommon(null);
  }

  return {
    ...setup,
    innerFile,
    uploadAction,
    uploadHeaders,
    uploadData,
    hasFile,
    responseName,
    handleChangeUpload,
    handleClickDownloadFile,
    handleClickDeleteFile,
  };
}

export function fileGetValue(value) {
  if (value && value.path) {
    return value.path;
  }
  return value;
}

export function fileSetValue(value) {
  if (value) {
    let first = value.split(',')[0];
    let name = first.substring(first.lastIndexOf('/') + 1);
    return {
      name: name,
      path: value,
      status: 'done',
    };
  }
  return value;
}
