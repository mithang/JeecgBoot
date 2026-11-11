import type { PropType } from 'vue';
import { FileBasicColumn } from './typing';

export const basicProps = {
  helpText: {
    type: String as PropType<string>,
    default: '',
  },
  // Tệp tối đa bao nhiêuMB
  maxSize: {
    type: Number as PropType<number>,
    default: 2,
  },
  // Số lượng tệp tối đa, Infinity là không giới hạn
  maxNumber: {
    type: Number as PropType<number>,
    default: Infinity,
  },
  // Theo hậu tố, hoặc cách khác
  accept: {
    type: Array as PropType<string[]>,
    default: () => [],
  },
  multiple: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  uploadParams: {
    type: Object as PropType<any>,
    default: {},
  },
  api: {
    type: Function as PropType<PromiseFn>,
    default: null,
    required: true,
  },
  name: {
    type: String as PropType<string>,
    default: 'file',
  },
  filename: {
    type: String as PropType<string>,
    default: null,
  },
};

export const uploadContainerProps = {
  value: {
    type: Array as PropType<string[]>,
    default: () => [],
  },
  ...basicProps,
  showPreviewNumber: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  emptyHidePreview: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
};

export const previewProps = {
  value: {
    type: Array as PropType<string[]>,
    default: () => [],
  },
};

export const fileListProps = {
  columns: {
    type: [Array] as PropType<FileBasicColumn[]>,
    default: null,
  },
  actionColumn: {
    type: Object as PropType<FileBasicColumn>,
    default: null,
  },
  dataSource: {
    type: Array as PropType<any[]>,
    default: null,
  },
};
