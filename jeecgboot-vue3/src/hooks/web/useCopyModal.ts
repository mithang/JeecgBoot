import { isRef, unref, watch, Ref, ComputedRef } from 'vue';
import Clipboard from 'clipboard';
import { ModalOptionsEx, useMessage } from '/@/hooks/web/useMessage';

/** Popup có nút sao chép */
interface IOptions extends ModalOptionsEx {
  // Văn bản cần sao chép, có thể là một ref Đối tượng, cập nhật động
  copyText: string | Ref<string> | ComputedRef<string>;
}

const COPY_CLASS = 'copy-this-text';
const CLIPBOARD_TEXT = 'data-clipboard-text';

export function useCopyModal() {
  return { createCopyModal };
}

const { createMessage, createConfirm } = useMessage();

/** Tạo popup sao chép */
function createCopyModal(options: Partial<IOptions>) {
  let modal = createConfirm({
    ...options,
    iconType: options.iconType ?? 'info',
    width: options.width ?? 500,
    title: options.title ?? 'Sao chép',
    maskClosable: options.maskClosable ?? true,
    okText: options.okText ?? 'Sao chép',
    okButtonProps: {
      ...options.okButtonProps,
      class: COPY_CLASS,
      [CLIPBOARD_TEXT]: unref(options.copyText),
    } as any,
    onOk() {
      return new Promise((resolve: any) => {
        const clipboard = new Clipboard('.' + COPY_CLASS);
        clipboard.on('success', () => {
          clipboard.destroy();
          createMessage.success('Sao chép thành công');
          resolve();
        });
        clipboard.on('error', () => {
          createMessage.error('Trình duyệt đó không hỗ trợ sao chép tự động');
          clipboard.destroy();
          resolve();
        });
      });
    },
  });

  // Cập nhật động copyText
  if (isRef(options.copyText)) {
    watch(options.copyText, (copyText) => {
      modal.update({
        okButtonProps: {
          ...options.okButtonProps,
          class: COPY_CLASS,
          [CLIPBOARD_TEXT]: copyText,
        } as any,
      });
    });
  }
  return modal;
}
