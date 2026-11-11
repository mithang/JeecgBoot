<template>
  <div ref="editorRootRef" :class="prefixCls" :style="{ width: containerWidth }">
    <Teleport v-if="imgUploadShow" :to="targetElem">
      <ImgUpload
        :fullscreen="fullscreen"
        @uploading="handleImageUploading"
        @loading="handleLoading"
        @done="handleDone"
        v-show="editorRef"
        :disabled="disabled"
      />
    </Teleport>
    <Editor :id="tinymceId" ref="elRef" :disabled="disabled" :init="initOptions" :style="{ visibility: 'hidden' }" v-if="!initOptions.inline"></Editor>
    <slot v-else></slot>
    <ProcessMask ref="processMaskRef" :show="showUploadMask"/>
  </div>
</template>

<script lang="ts">
  import type { RawEditorOptions } from 'tinymce';
  // update-begin--author:copilot---date:20260711---for:Sửa lỗi Rolldown với tương tác CJS nghiêm ngặt khiến tinymce toàn cục chưa được mount
  import tinymce from '/@/utils/tinymceGlobalShim';
  // update-end--author:copilot---date:20260711---for:Sửa lỗi Rolldown với tương tác CJS nghiêm ngặt khiến tinymce toàn cục chưa được mount
  import Editor from '@tinymce/tinymce-vue'
  import 'tinymce/themes/silver';
  import 'tinymce/icons/default/icons';
  import 'tinymce/models/dom';

  // tinymcePlugin có thể import theo nhu cầu của mình
  // Tham khảo thêm plugin:https://www.tiny.cloud/docs/plugins/
  import 'tinymce/plugins/fullscreen';
  import 'tinymce/plugins/link';
  import 'tinymce/plugins/lists';
  import 'tinymce/plugins/preview';
  import 'tinymce/plugins/image';
  import 'tinymce/plugins/media';
  import { defineComponent, computed, nextTick, ref, unref, watch, onDeactivated, onBeforeUnmount, onMounted } from 'vue';
  import ImgUpload from './ImgUpload.vue';
  import ProcessMask from './ProcessMask.vue';
  import {simpleToolbar, menubar, simplePlugins} from './tinymce';
  import { buildShortUUID } from '/@/utils/uuid';
  import { bindHandlers } from './helper';
  import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { isNumber } from '/@/utils/is';
  import { useLocale } from '/@/locales/useLocale';
  import { useAppStore } from '/@/store/modules/app';
  import { uploadFile } from '/@/api/common/api';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { ThemeEnum } from '/@/enums/appEnum';
  import { defHttp } from "@/utils/http/axios";
  const tinymceProps = {
    options: {
      type: Object as PropType<Partial<RawEditorOptions>>,
      default: {},
    },
    value: {
      type: String,
    },

    toolbar: {
      type: [Array as PropType<string[]>, String],
      default: simpleToolbar,
    },
    plugins: {
      type: [Array as PropType<string[]>, String],
      default: simplePlugins,
    },
    menubar: {
      type: [Object, String],
      default: menubar,
    },
    modelValue: {
      type: String,
    },
    height: {
      type: [Number, String] as PropType<string | number>,
      required: false,
      default: 400,
    },
    width: {
      type: [Number, String] as PropType<string | number>,
      required: false,
      default: 'auto',
    },
    showImageUpload: {
      type: Boolean,
      default: true,
    },
    showUploadMask: {
      type: Boolean,
      default: false,
    },
    //Có lấy focus hay không
    autoFocus:{
      type: Boolean,
      default: true,
    }
  };

  export default defineComponent({
    name: 'Tinymce',
    components: { ImgUpload,Editor,ProcessMask },
    inheritAttrs: false,
    props: tinymceProps as any,
    emits: ['change', 'update:modelValue', 'inited', 'init-error'],
    setup(props, { emit, attrs }) {
      console.log("---Tinymce---Khởi tạo---")

      const editorRef = ref<Nullable<any>>(null);
      const fullscreen = ref(false);
      const tinymceId = ref<string>(buildShortUUID('tiny-vue'));
      const elRef = ref<Nullable<HTMLElement>>(null);
      const editorRootRef = ref<Nullable<HTMLElement>>(null);
      const processMaskRef = ref<any>(null);
      const imgUploadShow = ref(false);
      const targetElem = ref<null | HTMLDivElement>(null);

      const { prefixCls } = useDesign('tinymce-container');

      const appStore = useAppStore();

      const tinymceContent = computed(() => props.modelValue);

      const containerWidth = computed(() => {
        const width = props.width;
        if (isNumber(width)) {
          return `${width}px`;
        }
        return width;
      });

      const skinName = computed(() => {
        return appStore.getDarkMode === 'light' ? 'jeecg' : 'oxide-dark';
      });

      const langName = computed(() => {
        const lang = useLocale().getLocale.value;
        return ['zh_CN', 'en'].includes(lang) ? lang : 'zh_CN';
      });

      const initOptions = computed(() => {
        const { height, options, toolbar, plugins, menubar } = props;
        let publicPath = import.meta.env.VITE_PUBLIC_PATH || '/';
        // Giải thích logic mã: 【QQYUN-8571] đường dẫn phát hành không kết thúc bằng/tài nguyên sẽ tải thất bại nếu không kết thúc bằng /
        if (!publicPath.endsWith('/')) {
          publicPath += '/';
        }
        return {
          selector: `#${unref(tinymceId)}`,
          height,
          toolbar,
          menubar: false,
          plugins,
          // Thêm cấu hình dán sau đây
          paste_data_images: true, // Cho phép dán hình ảnh
          paste_as_text: false, // Không dán dưới dạng văn bản thuần
          paste_retain_style_properties: 'all', // Giữ lại tất cả thuộc tính kiểu
          paste_webkit_styles: 'all', // Giữ lại kiểu webkit
          paste_merge_formats: true, // Gộp định dạng
          paste_block_drop: true, // Cho phép kéo thả để dán
          paste_preprocess: (plugin, args) => {
            // Có thể xử lý trước nội dung đã dán tại đây
            //console.log('Nội dung đã dán:', args.content);
          },
          paste_postprocess: (plugin, args) => {
            // Có thể xử lý sau nội dung đã dán tại đây
            //console.log('Nội dung đã xử lý:', args.node);
          },
          // Nới lỏng quy tắc lọc nội dung
          valid_elements: '*[*]',
          extended_valid_elements: '*[*]',
          valid_children: '+body[style]',
          allow_conditional_comments: true,
          allow_html_in_named_anchor: true,

          language_url: publicPath + 'resource/tinymce/langs/' + langName.value + '.js',
          language: langName.value,
          branding: false,
          default_link_target: '_blank',
          link_title: false,
          object_resizing: true,
          toolbar_mode: 'sliding',
          // Giải thích logic mã: 【TV360X-416]Sinh mã bảng đơn, khi biểu mẫu mở ra sẽ focus trước vào component rich text và cuộn đến vị trí của component rich text---
          auto_focus: props.autoFocus,
          // toolbar_groups: true,
          skin: skinName.value,
          skin_url: publicPath + 'resource/tinymce/skins/ui/' + skinName.value,
          images_upload_handler: (blobInfo, process) =>
            new Promise((resolve, reject) => {
            let params = {
              file: blobInfo.blob(),
              filename: blobInfo.filename(),
              data: { biz: 'jeditor', jeditor: '1' },
            };
            const uploadSuccess = (res) => {
              if (res.success) {
                if (res.message == 'local') {
                  const img = 'data:image/jpeg;base64,' + blobInfo.base64();
                      resolve(img);
                } else {
                  let img = getFileAccessHttpUrl(res.message);
                  resolve(img);
                }
              } else {
                  reject('Tải lên thất bại!');
              }
            };
            uploadFile(params, uploadSuccess);
        }),
          // update-begin--author:liaozhiyang---date:20260617---for:【JHHB-1433]Nội dung tin tức cần hỗ trợ chèn video, plugin hiện tại không có thao tác chèn video
          media_live_embeds: true,
          media_alt_source: false,
          media_poster: false,
          media_dimensions: true,
          file_picker_types: 'media',
          file_picker_callback: (callback, value, meta) => {
            if (meta.filetype !== 'media') return;
            const input = document.createElement('input');
            input.setAttribute('type', 'file');
            input.setAttribute('accept', 'video/*');
            input.onchange = () => {
              const file = (input.files && input.files[0]) as File | undefined;
              if (!file) return;
              const params = {
                file,
                filename: file.name,
                data: { biz: 'jeditor', jeditor: '1' },
              };
              const uploadSuccess = (res: any) => {
                if (res && res.success) {
                  const videoUrl = res.message === 'local'
                    ? URL.createObjectURL(file)
                    : getFileAccessHttpUrl(res.message);
                  callback(videoUrl, { title: file.name });
                }
              };
              uploadFile(params, uploadSuccess);
            };
            input.click();
          },
          // update-end--author:liaozhiyang---date:20260617---for:【JHHB-1433]Nội dung tin tức cần hỗ trợ chèn video, plugin hiện tại không có thao tác chèn video
          content_css: publicPath + 'resource/tinymce/skins/ui/' + skinName.value + '/content.min.css',
          ...options,
          setup: (editor: any) => {
            editorRef.value = editor;
            editor.on('init', (e) => {
              initSetup(e);
              // update-begin--author:liaozhiyang---date:20260306---for:【issues/9448]Khi cuộn, nếu dropdown của tinymce đang mở thì ẩn đi
              // Khi cuộn trong popup thì đóng menu dropdown của tinymce, tránh menu dropdown gắn vào body khiến vị trí không bám theo
              bindScrollCloseMenu(editor);
              // update-end--author:liaozhiyang---date:20260306---for:【issues/9448]Khi cuộn, nếu dropdown của tinymce đang mở thì ẩn đi
            });
            //update-begin-author:liusq---date:2025-11-19--for: JHHB-1070 Bảng sao chép từ Word không căn chỉnh được
            // Chức năng căn chỉnh bảng
            initTableAlignment(editor);
            //update-end-author:liusq---date:2025-11-19--for: JHHB-1070 Bảng sao chép từ Word không căn chỉnh được
            // Xử lý sự kiện dán bên trong trình chỉnh sửa, đảm bảo thay thế đúng nội dung đã chọn
            editor.on('paste', async (e: any) => {
              try {
                e.preventDefault();
                e.stopPropagation();
                const clipboardData = e.clipboardData as DataTransfer | undefined;
                if (!clipboardData) return false;

                // Xử lý trước hình ảnh trong clipboard (ảnh chụp màn hình/Dán hình ảnh)
                const items = clipboardData.items as DataTransferItemList | undefined;
                if (items && items.length > 0) {
                  const imageFiles: File[] = [];
                  for (let i = 0; i < items.length; i++) {
                    const it = items[i];
                    if (it && it.type && it.type.indexOf('image') !== -1) {
                      const file = it.getAsFile();
                      if (file) imageFiles.push(file);
                    }
                  }
                  // Nếu có hình ảnh, tải lên từng cái rồi thay thế
                  if (imageFiles.length > 0) {
                    for (const file of imageFiles) {
                      await new Promise<void>((resolve) => {
                        const params = {
                          file,
                          filename: file.name || 'pasted-image.png',
                          data: { biz: 'jeditor', jeditor: '1' },
                        };
                        const uploadSuccess = (res: any) => {
                          try {
                            if (res && res.success) {
                              if (res.message === 'local') {
                                const reader = new FileReader();
                                reader.onload = () => {
                                  const base64 = reader.result as string;
                                  editor.selection.setContent(`<img src="${base64}"/>`);
                                  resolve();
                                };
                                reader.readAsDataURL(file);
                              } else {
                                const imgUrl = getFileAccessHttpUrl(res.message);
                                editor.selection.setContent(`<img src="${imgUrl}"/>`);
                                resolve();
                              }
                            } else {
                              resolve();
                            }
                          } catch (_) {
                            resolve();
                          }
                        };
                        uploadFile(params, uploadSuccess);
                      });
                    }
                    return false;
                  }
                }

                // Xử lý HTML/Văn bản thuần túy
                const pasteContent = clipboardData.getData('text/html') || clipboardData.getData('text');
                if (!pasteContent) return false;
                
                if (pasteContent.includes('<img')) {
                  const processedHtml = await preprocess(pasteContent);
                  editor.selection.setContent(processedHtml);
                } else {
                  editor.selection.setContent(pasteContent);
                }
              } catch (_) {}
              return false;
            });
          },
        };
      });

      const disabled = computed(() => {
        const { options } = props;
        const getdDisabled = options && Reflect.get(options, 'readonly');
        const editor = unref(editorRef);
        // Giải thích logic mã: Đã đặt disabled nhưng tải lên hình ảnh không bị vô hiệu hóa
        if (editor && editor?.setMode) {
          editor.setMode(getdDisabled || attrs.disabled === true ? 'readonly' : 'design');
        }
        if (attrs.disabled === true) {
          return true;
        }
        return getdDisabled ?? false;
      });

      watch(
        () => attrs.disabled,
        () => {
          const editor = unref(editorRef);
          if (!editor) {
            return;
          }
         editor?.setMode && editor.setMode(attrs.disabled ? 'readonly' : 'design');
        }
      );

      onMountedOrActivated(() => {
        if (!initOptions.value.inline) {
          tinymceId.value = buildShortUUID('tiny-vue');
        }
        nextTick(() => {
          setTimeout(() => {
            initEditor();
          }, 30);
        });
      });

      onBeforeUnmount(() => {
        destory();
      });

      onDeactivated(() => {
        destory();
      });

      function destory() {
        if (tinymce !== null) {
          tinymce?.remove?.(unref(initOptions).selector!);
        }
      }

      function initEditor() {
        const el = unref(elRef);
        if (el && el?.style && el?.style?.visibility) {
          el.style.visibility = '';
        }
        tinymce
          .init(unref(initOptions))
          .then((editor) => {
            changeColor();
            emit('inited', editor);
          })
          .catch((err) => {
            emit('init-error', err);
          });
      }

      function initSetup(e) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        const value = props.modelValue || '';

        editor.setContent(value);
        bindModelHandlers(editor);
        bindHandlers(e, attrs, unref(editorRef));
      }

      function setValue(editor: Recordable, val: string, prevVal?: string) {
        if (editor && typeof val === 'string' && val !== prevVal && val !== editor.getContent({ format: attrs.outputFormat })) {
          editor.setContent(val);
        }
      }

      function bindModelHandlers(editor: any) {
        const modelEvents = attrs.modelEvents ? attrs.modelEvents : null;
        const normalizedEvents = Array.isArray(modelEvents) ? modelEvents.join(' ') : modelEvents;

        watch(
          () => props.modelValue,
          (val: string, prevVal: string) => {
            setValue(editor, val, prevVal);
          }
        );

        watch(
          () => props.value,
          (val: string, prevVal: string) => {
            setValue(editor, val, prevVal);
          },
          {
            immediate: true,
          }
        );

        editor.on(normalizedEvents ? normalizedEvents : 'change keyup undo redo', () => {
          const content = editor.getContent({ format: attrs.outputFormat });
          emit('update:modelValue', content);
          emit('change', content);
        });

        editor.on('FullscreenStateChanged', (e) => {
          fullscreen.value = e.state;
        });
      }

      function handleImageUploading(name: string) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        editor.execCommand('mceInsertContent', false, getUploadingImgName(name));
        const content = editor?.getContent() ?? '';
        setValue(editor, content);
      }

      async function handleDone(name: string, url: string) {
        const editor = unref(editorRef);
        if (!editor) {
          return;
        }
        await handleImageUploading(name);
        const content = editor?.getContent() ?? '';
        const val = content?.replace(getUploadingImgName(name), `<img src="${url}"/>`) ?? '';
        setValue(editor, val);
      }

      /**
       * Tính tiến độ tải lên
       * @param file
       * @param fileList
       */
      function handleLoading(fileLength,showMask){
        if(fileLength && fileLength > 0){
          setTimeout(() => {
              props?.showUploadMask && processMaskRef.value.calcProcess(fileLength)
          },100)
        }else{
           props?.showUploadMask && (processMaskRef.value.showMask = showMask);
        }
      }
      function getUploadingImgName(name: string) {
        return `[uploading:${name}]`;
      }
      // Giải thích logic mã: 【TV360X-35] Rich text, tải lên hình ảnh che các nút khác
      let executeCount = 0;
      watch(
        () => props.showImageUpload,
        () => {
          mountElem();
        }
      );
      onMounted(() => {
        mountElem();
      });
      const mountElem = () => {
        if (executeCount > 20) return;
        setTimeout(() => {
          if (targetElem.value) {
            imgUploadShow.value = props.showImageUpload;
          } else {
            const toxToolbar = editorRootRef.value?.querySelector('.tox-toolbar__group');
            if (toxToolbar) {
              const divElem = document.createElement('div');
              divElem.setAttribute('style', `width:64px;height:39px;display:flex;align-items:center;`);
              toxToolbar!.appendChild(divElem);
              targetElem.value = divElem;
              imgUploadShow.value = props.showImageUpload;
              executeCount = 0;
            } else {
              mountElem();
            }
          }
          executeCount++;
        }, 100);
      };
      // Giải thích logic mã: 【QQYUN-8639] thích ứng chủ đề tối
      function changeColor() {
        setTimeout(() => {
          const iframe = editorRootRef.value?.querySelector('iframe');
          const body = iframe?.contentDocument?.querySelector('body');
          if (body) {
            if (appStore.getDarkMode == ThemeEnum.DARK) {
              body.style.color = '#fff';
            } else {
              body.style.color = '#000';
            }
          }
        }, 300);
      }
      watch(
        () => appStore.getDarkMode,
        () => {
          changeColor();
        }
      );

      // Đóng menu dropdown tinymce khi cuộn
      function bindScrollCloseMenu(editor) {
        const root = unref(editorRootRef);
        if (!root) return;
        const scrollParent = getScrollParent(root);
        if (!scrollParent) return;
        const onScroll = () => {
          // Kích hoạt mousedown trên body, tinymce sẽ tự động đóng menu dropdown đang mở
          document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
        };
        scrollParent.addEventListener('scroll', onScroll, { passive: true });
        editor.on('remove', () => {
          scrollParent.removeEventListener('scroll', onScroll);
        });
      }

      function getScrollParent(el: HTMLElement): HTMLElement | null {
        let parent = el.parentElement;
        while (parent && parent !== document.body) {
          const { overflow, overflowY } = getComputedStyle(parent);
          if (/(auto|scroll)/.test(overflow + overflowY)) {
            return parent;
          }
          parent = parent.parentElement;
        }
        return null;
      }

      /**
       * Xử lý địa chỉ liên kết hình ảnh
       *
       * @param pasteContent
       */
      async function preprocess(pasteContent) {
        // Dùng regex trích xuất tất cả thẻ img (bao gồm src và các thuộc tính khác, tránh mất kiểu/altv.v.)
        const imgTagRegex = /<img([^>]+)src="([^">]+)"([^>]*)/g;

        // Thu thập thông tin của tất cả hình ảnh cần thay thế (chỉ mục, src gốc, thẻ đầy đủ)
        const imgReplaceList:any = [];
        let match;
        while ((match = imgTagRegex.exec(pasteContent)) !== null) {
          imgReplaceList.push({
            // Thẻ img đầy đủ (ví dụ<img class="xxx" src="xxx" alt="xxx">）
            fullTag: match[0],
            // Hình ảnh gốcURL
            src: match[2],
            // srcthuộc tính trước (nhưclass="xxx" ）
            prefix: match[1],
            // srcthuộc tính sau (như alt="xxx"）
            suffix: match[3]
          });
        }
   
        // Thay thế tất cả URL hình ảnh bằng địa chỉ máy chủ
        let processedContent = pasteContent;
        // Lấy tên miền hiện tại (giao thức+Tên miền+cổng)
        const currentOrigin = window.location.hostname;
        console.log("Tên miền hiện tại:"+ currentOrigin)
        // Xử lý hình ảnh
        for (const imgInfo of imgReplaceList) {
          try {
            if(imgInfo.src.startsWith('http')){
              // Kiểm tra có phải hình ảnh thuộc tên miền hiện tại hay không
              let isOwnServer = false;
              try {
                const imgHost = new URL(imgInfo.src, window.location.href).hostname;
                isOwnServer = imgHost === currentOrigin;
                console.log("Tên miền trong hình ảnh:" + imgHost)
              } catch (e) {
                // Nếu phân tích URL thất bại, dùng kiểm tra hostname đơn giản làm phương án dự phòng
                isOwnServer = imgInfo.src.includes(window.location.hostname);
                console.log("Tên miền trong hình ảnh:" + window.location.hostname)
              }
              if(!isOwnServer){
                // Hình ảnh mạng không thuộc tên miền hiện tại cần được tải lên
                const filename = getFilenameFromUrl(imgInfo.src) || 'pasted-image.jpg';
                let newUrl = await uploadImage(imgInfo.src, filename);
                if(newUrl){
                  const newImgTag = `<img${imgInfo.prefix}src="${newUrl}"${imgInfo.suffix}`;
                  processedContent = processedContent.replace(imgInfo.fullTag, newImgTag);
                }
              }
              // Nếu là hình ảnh thuộc tên miền hiện tại, không xử lý gì, giữ nguyên
            }
          } catch (error) {}
        }
        return processedContent;
      }
      
      
      /**
       * Lấy tên tệp theo đường dẫn
       * 
       * @param url
       */
      function getFilenameFromUrl(url) {
        return url.split('/').pop().split('?')[0];
      }

      /**
       * Tải lên tệp và trả về đường dẫn tệp
       * 
       * @param fileUrl
       * @param filename
       */
      async function uploadImage(fileUrl, filename) {
        let params = {
          fileUrl: fileUrl,
          filename: filename,
        };
        try {
          let { message } = await defHttp.post({ url: "/sys/common/uploadImgByHttp", params },{ isTransformResponse: false });
          return getFileAccessHttpUrl(message);
        } catch (e) {
          return "";
        }
      }
      //update-begin-author:liusq---date:2025-11-19--for: JHHB-1070 Bảng sao chép từ Word không căn chỉnh được
      /**
       * Xử lý căn giữa bảng
       * @param editor
       */
      function initTableAlignment(editor) {
        // Căn trái bảng - Căn trái có biểu tượng bảng
        editor.ui.registry.addIcon('table-align-left', '<svg width="24" height="24"><rect x="2" y="8" width="16" height="8" fill="currentColor"/><path d="M2 4h20v2H2zm0 14h12v2H2z" fill="currentColor"/></svg>');

        // Căn giữa bảng - Căn giữa với biểu tượng bảng
        editor.ui.registry.addIcon('table-align-center', '<svg width="24" height="24"><rect x="4" y="8" width="16" height="8" fill="currentColor"/><path d="M2 4h20v2H2zm4 14h12v2H6z" fill="currentColor"/></svg>');

        // Căn phải bảng - Căn phải có biểu tượng bảng
        editor.ui.registry.addIcon('table-align-right', '<svg width="24" height="24"><rect x="6" y="8" width="16" height="8" fill="currentColor"/><path d="M2 4h20v2H2zm8 14h12v2H10z" fill="currentColor"/></svg>');

        // Thêm nút căn chỉnh chuyên dụng cho bảng
        editor.ui.registry.addButton('tablealignleft', {
          tooltip: 'Căn trái bảng',
          icon: 'table-align-left',
          onAction: () => {
            const table = getSelectedTable(editor);
            if (table) alignTable(editor, table, 'JustifyLeft');
          }
        });
        editor.ui.registry.addButton('tablealigncenter', {
          tooltip: 'Căn giữa bảng',
          icon: 'table-align-center',
          onAction: () => {
            const table = getSelectedTable(editor);
            if (table) alignTable(editor, table, 'JustifyCenter');
          }
        });

        editor.ui.registry.addButton('tablealignright', {
          tooltip: 'Căn phải bảng',
          icon: 'table-align-right',
          onAction: () => {
            const table = getSelectedTable(editor);
            if (table) alignTable(editor, table, 'JustifyRight');
          }
        });
      }

      // Lấy bảng hiện đang được chọn
      function getSelectedTable(editor) {
        const selectedNode = editor.selection.getNode();
        return selectedNode.closest('table');
      }

      // Hàm căn chỉnh bảng dùng wrapper
      function alignTable(editor, table, command) {
        // Kiểm tra xem bảng đã được đóng gói chưa
        let wrapper = table.parentNode;
        const isWrapped = wrapper && wrapper.classList && wrapper.classList.contains('table-wrapper');

        switch (command) {
          case 'JustifyLeft':
          case 'JustifyRight':
            // Đối với căn trái phải, loại bỏ wrapper (nếu có)
            if (isWrapped) {
              editor.dom.remove(wrapper, true); // Xóa wrapper nhưng giữ lại bảng
            }
            // Đặt trực tiếp float của bảng
            editor.dom.setStyle(table, 'float', command === 'JustifyLeft' ? 'left' : 'right');
            editor.dom.setStyle(table, 'margin', '0');
            break;

          case 'JustifyCenter':
            // Đối với căn giữa, dùng wrapper
            if (!isWrapped) {
              // Tạo wrapper
              wrapper = editor.dom.create('div', {
                'class': 'table-wrapper',
                style: 'text-align: center; margin: 0 auto;'
              });

              // Bọc bảng
              table.parentNode.insertBefore(wrapper, table);
              wrapper.appendChild(table);
            }

            // Đặt kiểu wrapper
            editor.dom.setStyles(wrapper, {
              'text-align': 'center',
              'margin': '0 auto',
              'display': 'block'
            });
            // Xóa float và margin của bảng
            editor.dom.setStyles(table, {
              'float': '',
              'margin': '0',
              'display': 'inline-table' // Giữ đặc tính hiển thị của bảng
            });
            break;

          case 'JustifyFull':
            // Xóa wrapper (nếu có)
            if (isWrapped) {
              editor.dom.remove(wrapper, true);
            }
            editor.dom.setStyle(table, 'width', '100%');
            editor.dom.setStyle(table, 'float', '');
            break;
        }

        // Kích hoạt sự kiện thay đổi
        editor.fire('change');
        editor.undoManager.add();
      }
      //update-end-author:liusq---date:2025-11-19--for: JHHB-1070 Bảng sao chép từ Word không căn chỉnh được 

      return {
        prefixCls,
        containerWidth,
        initOptions,
        tinymceContent,
        elRef,
        tinymceId,
        handleImageUploading,
        handleDone,
        editorRef,
        fullscreen,
        disabled,
        editorRootRef,
        imgUploadShow,
        targetElem,

        handleLoading,
        processMaskRef,
      };
    },
  });
</script>

<style lang="less" scoped>
/* Kiểu wrapper bảng */
.table-wrapper {
  text-align: center;
  margin: 0 auto;
  display: block;
}

/* Đảm bảo hiển thị đúng trong trình chỉnh sửa */
.tox-edit-area__iframe {
  .table-wrapper {
    text-align: center !important;
    margin: 0 auto !important;
    display: block !important;
  }

  .table-wrapper table {
    display: inline-table !important;
    margin: 0 !important;
    float: none !important;
  }
}
</style>

<style lang="less">
  @prefix-cls: ~'@{namespace}-tinymce-container';

  .@{prefix-cls} {
    position: relative;
    line-height: normal;

    textarea {
      z-index: -1;
      visibility: hidden;
    }
    .tox:not(.tox-tinymce-inline) .tox-editor-header {
      padding:0;
    }
    // Giải thích logic mã: 【TV360X-329] Ở trạng thái vô hiệu hóa của rich text, thanh công cụ khi di chuột qua bị mất viền
    .tox .tox-tbtn--disabled,
    .tox .tox-tbtn--disabled:hover,
    .tox .tox-tbtn:disabled,
    .tox .tox-tbtn:disabled:hover {
      background-image: url("data:image/svg+xml;charset=utf8,%3Csvg height='39px' viewBox='0 0 40 39px' width='40' xmlns='http://www.w3.org/2000/svg'%3E%3Crect x='0' y='38px' width='100' height='1' fill='%23d9d9d9'/%3E%3C/svg%3E");
      background-position: left 0;
    }
  }

  html[data-theme='dark'] {
    .@{prefix-cls} {
      .tox .tox-edit-area__iframe {background-color: #141414;}
    }
  }
</style>
