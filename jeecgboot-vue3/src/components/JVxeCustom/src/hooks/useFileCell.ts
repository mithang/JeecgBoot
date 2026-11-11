import { computed } from 'vue';
import { fileGetValue, fileSetValue, useJVxeUploadCell } from '/@/components/jeecg/JVxeTable/src/hooks/cells/useJVxeUploadCell';
import { uploadUrl } from '/@/api/common/api';
import { JUploadModal, UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
import { useModal } from '/@/components/Modal';
import { JVxeComponent } from '/@/components/jeecg/JVxeTable/src/types/JVxeComponent';
import { Icon } from '/@/components/Icon';
import { Dropdown } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { split } from '/@/utils';

export function useFileCell(props, fileType: UploadTypeEnum, options?) {
  const setup = useJVxeUploadCell(props, { token: true, action: uploadUrl, ...options });

  const { innerFile, handleChangeCommon, originColumn } = setup;
  const [registerModel, { openModal }] = useModal();

  // Cắt tên tệp
  const shortFileName = computed(() => {
    const length = 5;
    const file = innerFile.value;
    if (!file || !file.name) {
      return '';
    }
    if (file.name.length > length) {
      return file.name.substr(0, length);
    }
    return file.name;
  });

  const modalValue = computed(() => {
    if (innerFile.value) {
      if (innerFile.value['url']) {
        return innerFile.value['url'];
      } else if (innerFile.value['path']) {
        return innerFile.value['path'];
      }
    }
    return '';
  });

  const uploadCount = computed(() => {
    const value = modalValue.value;
    return value ? split(value).filter(Boolean).length : 0;
  });

  const maxCount = computed(() => {
    let maxCount = originColumn.value.maxCount;
    // online Mở rộngJSON
    if (originColumn.value && originColumn.value.fieldExtendJson) {
      const json = JSON.parse(originColumn.value.fieldExtendJson);
      maxCount = json.uploadnum ? json.uploadnum : 0;
    }
    return Number(maxCount ?? 0);
  });

  // Nhấp vào nút xem thêm
  function handleMoreOperation() {
    openModal(true, {
      removeConfirm: true,
      mover: true,
      download: true,
      ...originColumn.value.props,
      maxCount: maxCount.value,
      multiple: originColumn.value.props?.multiple ?? maxCount.value !== 1,
      fileType: fileType,
      // update-begin--author:liaozhiyang---date:20250526---for：【issues/9657]Lỗi khi tải lên lại trong lúc chỉnh sửa tải lên hình ảnh của component jvxetable
      action: originColumn.value.action ?? setup.uploadAction,
      // update-end--author:liaozhiyang---date:20250526---for：【issues/9657]Lỗi khi tải lên lại trong lúc chỉnh sửa tải lên hình ảnh của component jvxetable
      // update-begin--author:liaozhiyang---date:20250526---for：【issues/9652]vxetable tải lên, tải tệp lên, tải hình ảnh lên thêm đường dẫn tùy chỉnh
      bizPath: originColumn.value.bizPath ?? 'temp',
      // update-end--author:liaozhiyang---date:20250526---for：【issues/9652]vxetable tải lên, tải tệp lên, tải hình ảnh lên thêm đường dẫn tùy chỉnh
    });
  }

  // Callback tải lên bổ sung
  function onModalChange(path) {
    if (path) {
      if (innerFile.value === null) {
        // Khi tải lên lần đầu từ popup, đồng bộ bổ sung tên tệp và trạng thái hoàn thành
        innerFile.value = fileSetValue(path);
      } else {
        innerFile.value.path = path;
      }
      handleChangeCommon(innerFile.value);
    } else {
      // Giải thích logic mã: [issues/530]JVxeTable củaJVxeTypes.imageloại, không thể xóa toàn bộ hình ảnh đã tải lên
      handleChangeCommon(null);
    }
  }

  return {
    ...setup,
    modalValue,
    uploadCount,
    maxCount,
    shortFileName,
    registerModel,
    onModalChange,
    handleMoreOperation,
  };
}

export const components = {
  Icon,
  Dropdown,
  LoadingOutlined,
  JUploadModal,
};

export const enhanced = {
  switches: { visible: true },
  getValue: (value) => fileGetValue(value),
  setValue: (value) => fileSetValue(value),
} as JVxeComponent.EnhancedPartial;
