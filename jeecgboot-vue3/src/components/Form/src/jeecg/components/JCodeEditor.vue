<template>
  <div ref="containerRef" v-bind="boxBindProps">
    <!-- Nút toàn màn hình -->
    <a-icon v-if="fullScreen" class="full-screen-icon" :type="fullScreenIcon" @click="onToggleFullScreen" />
    <textarea ref="textarea" v-bind="getBindValue"></textarea>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, reactive, ref, watch, unref, computed, nextTick } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  // Nhập instance toàn cục
  import _CodeMirror, { EditorFromTextArea } from 'codemirror';
  // Kiểu dáng cốt lõi
  import 'codemirror/lib/codemirror.css';
  // Sau khi import theme vẫn cần chỉ định theme trong options thì mới có hiệu lực
  import 'codemirror/theme/idea.css';
  // Cần import thư viện tô sáng cú pháp cụ thể mới có hiệu ứng tô sáng cú pháp tương ứng
  import 'codemirror/mode/javascript/javascript.js';
  import 'codemirror/mode/css/css.js';
  import 'codemirror/mode/xml/xml.js';
  import 'codemirror/mode/clike/clike.js';
  import 'codemirror/mode/markdown/markdown.js';
  import 'codemirror/mode/python/python.js';
  import 'codemirror/mode/r/r.js';
  import 'codemirror/mode/shell/shell.js';
  import 'codemirror/mode/sql/sql.js';
  import 'codemirror/mode/swift/swift.js';
  import 'codemirror/mode/vue/vue.js';
  // Nhập tài nguyên gấp gọn:Bắt đầu
  import 'codemirror/addon/fold/foldgutter.css';
  import 'codemirror/addon/fold/foldcode.js';
  import 'codemirror/addon/fold/brace-fold.js';
  import 'codemirror/addon/fold/comment-fold.js';
  import 'codemirror/addon/fold/indent-fold.js';
  import 'codemirror/addon/fold/foldgutter.js';
  // Nhập tài nguyên gấp gọn:Kết thúc
  //Tô sáng nền dòng con trỏ, trong cấu hình cũng cần đặt styleActiveLine thànhtrue
  import 'codemirror/addon/selection/active-line.js';
  // Hỗ trợ tự động hoàn thành mã
  import 'codemirror/addon/hint/show-hint.css';
  import 'codemirror/addon/hint/show-hint.js';
  import 'codemirror/addon/hint/anyword-hint.js';
  // Khớp dấu ngoặc
  import 'codemirror/addon/edit/matchbrackets';
  // Placeholder
  import 'codemirror/addon/display/placeholder.js';
  
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { isJsonObjectString } from '/@/utils/is.ts';
  // Gợi ý mã
  import { useCodeHinting } from '../hooks/useCodeHinting';

  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { ThemeEnum } from '/@/enums/appEnum';
  export default defineComponent({
    name: 'JCodeEditor',
    // Không binding thuộc tính của attrs lên thẻ html
    inheritAttrs: false,
    components: {},
    props: {
      value: propTypes.string.def(''),
      height: propTypes.string.def('auto'),
      disabled: propTypes.bool.def(false),
      // Có hiển thị nút toàn màn hình không
      fullScreen: propTypes.bool.def(false),
      // sau khi toàn màn hìnhz-index
      zIndex: propTypes.any.def(1500),
      theme: propTypes.string.def('idea'),
      language: propTypes.string.def(''),
      // Gợi ý mã
      keywords: propTypes.array.def([]),
    },
    emits: ['change', 'update:value', 'ready'],
    setup(props, { emit }) {
      const { getDarkMode } = useRootSetting();
      const containerRef = ref(null);
      const { prefixCls } = useDesign('code-editer');
      const CodeMirror = window.CodeMirror || _CodeMirror;
      const emitData = ref<object>();
      //Giá trị biểu mẫu
      const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const textarea = ref<HTMLTextAreaElement>();
      let coder: Nullable<EditorFromTextArea> = null;
      const attrs = useAttrs();
      const height = ref(props.height);
      const options = reactive({
        // Định dạng thụt lề
        tabSize: 2,
        // Chủ đề, JS của thư viện chủ đề tương ứng cần được import trước
        // Giải thích logic mã: 【QQYUN-8639] thích ứng chủ đề tối
        theme: getDarkMode.value == ThemeEnum.DARK ? 'monokai' : props.theme,
        smartIndent: true, // Có thụt lề thông minh không
        // Hiển thị số dòng
        lineNumbers: true,
        line: true,
        // Bật tính năng liên quan đến gấp mã:Bắt đầu
        foldGutter: true,
        lineWrapping: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-lint-markers'],
        // Bật tính năng liên quan đến gấp mã:Kết thúc
        // Làm nổi bật dòng con trỏ
        styleActiveLine: true,
        // Giải thích logic mã: 【issues/869] Component JCodeEditor khi khởi tạo chưa thiết lậpmode
        mode: props.language,
        // Giải thích logic mã: 【TV360X-898] Bản xem trước sau khi sinh mã đổi thành chỉ đọc
        readOnly: props.disabled,
        // Khớp dấu ngoặc
        matchBrackets: true,
        extraKeys: {
          // Tab: function autoFormat(editor) {
          //   //var totalLines = editor.lineCount();
          //   //editor.autoFormatRange({line:0, ch:0}, {line:totalLines});
          //   setValue(innerValue, false);
          // },
          'Cmd-/': (cm) => comment(cm),
          'Ctrl-/': (cm) => comment(cm),
        },
      });
      // Giá trị lưu trữ nội bộ, ban đầu là props.value
      let innerValue = props.value ?? '';
      // Trạng thái toàn màn hình
      const isFullScreen = ref(false);
      const fullScreenIcon = computed(() => (isFullScreen.value ? 'fullscreen-exit' : 'fullscreen'));
      // Tham số hộp bên ngoài
      const boxBindProps = computed(() => {
        let _props = {
          class: [
            prefixCls,
            'full-screen-parent',
            'auto-height',
            {
              'full-screen': isFullScreen.value,
            },
          ],
          style: {},
        };
        if (isFullScreen.value) {
          _props.style['z-index'] = props.zIndex;
        }
        return _props;
      });
      // Giải thích logic mã: 【QQYUN-5955] Tăng cường JS online, thêm gợi ý mã
      const { codeHintingMount, codeHintingRegistry } = useCodeHinting(CodeMirror, props.keywords, props.language);
      codeHintingRegistry();
      /**
       * Lắng nghe giá trị component
       */
      watch(
        () => props.value,
        () => {
          if (innerValue != props.value) {
            setValue(props.value, false);
          }
        }
      );
      onMounted(() => {
        initialize();
        // Giải thích logic mã: 【QQYUN-8473] Trình chỉnh sửa mã lần đầu tải sẽ bị che khuất
        setTimeout(() => {
          refresh();
          // Khởi tạo + refresh Sau khi hoàn tất tất cả thì thông báo cho component cha
          emit('ready');
        }, 150);
      });

      /**
       * Gán giá trị component
       * @param value
       * @param trigger Có kích hoạt sự kiện change hay không
       */
      function setValue(value: string, trigger = true) {
        if (value && isJsonObjectString(value)) {
          // update-begin--author:liaozhiyang---date:20260617---for:【issues/9699] JS tăng cường viết sai thì trang báo lỗi
          // isJsonObjectString Chỉ phán đoán ký tự đầu và cuối, không thể đảm bảo JSON hợp lệ; khi phân tích thất bại thì giữ nguyên giá trị gốc, tránh nội dung không phải JSON (như mã tăng cường JS) bị lỗi kẹt khi mở
          try {
            value = JSON.stringify(JSON.parse(value), null, 2);
          } catch (e) {
            // JSON không hợp lệ thì hiển thị theo văn bản gốc
          }
        }
        // update-end--author:liaozhiyang---date:20260617---for:【issues/9699] JS tăng cường viết sai thì trang báo lỗi
        coder?.setValue(value ?? '');
        innerValue = value;
        trigger && emitChange(innerValue);
        // Giải thích logic mã: 【QQYUN-9231] trình soạn thảo mã bị che khuất
        setTimeout(() => {
          refresh();
          // Làm mới lại lần nữa để tránh vấn đề che khuất với xác suất nhỏ
          setTimeout(() => {
            refresh();
          }, 600);
        }, 400);
      }

      //Sự kiện thay đổi giá trị trình chỉnh sửa
      function onChange(obj) {
        let value = obj.getValue();
        innerValue = value || '';
        if (props.value != innerValue) {
          emitChange(innerValue);
        }
      }

      function emitChange(value) {
        emit('change', value);
        emit('update:value', value);
      }

      //Khởi tạo component
      function initialize() {
        coder = CodeMirror.fromTextArea(textarea.value!, options);
        //Sự kiện thay đổi giá trị liên kết
        coder.on('change', onChange);
        // Gán giá trị một lần khi khởi tạo thành công
        setValue(innerValue, false);
        // Giải thích logic mã: 【QQYUN-5955] Tăng cường JS online, thêm gợi ý mã
        codeHintingMount(coder);
      }

      // Chuyển đổi trạng thái toàn màn hình
      function onToggleFullScreen() {
        isFullScreen.value = !isFullScreen.value;
        // update-begin--author:liaozhiyang---date:20260713---for:【LHZP-236]Tăng cường onlinejs xem dữ liệu trong lịch sử, khi phóng to để xem, nội dung không thể cuộn
        nextTick(() => {
          setTimeout(() => refresh(), 100);
        });
        // update-end--author:liaozhiyang---date:20260713---for:【LHZP-236]Tăng cường onlinejs xem dữ liệu trong lịch sử, khi phóng to để xem, nội dung không thể cuộn
      }

      // Giải thích logic mã: codeEditorVô hiệu hóa chức năng
      watch(
        () => props.disabled,
        (val) => {
          if (coder) {
            coder.setOption('readOnly', val);
          }
        }
      );
      
      // Hỗ trợ đặt ngôn ngữ động
      watch(()=>props.language, (val)=>{
        if(val && coder){
          coder.setOption('mode', val);
        }
      });

      const getBindValue = Object.assign({}, unref(props), unref(attrs));

      // Giải thích logic mã: VUEN-2480[Bug nghiêm trọng]Vấn đề kiểm thử online vue3 8, vấn đề kiểu dáng tăng cường JS online
      function refresh(){
        if(coder){
          coder.refresh();
        }
      }

      /**
       * 2024-04-01
       * liaozhiyang
       * Chú thích hàng loạt mã
       */
      function comment(cm) {
        var selection = cm.getSelection();
        var start = cm.getCursor('start');
        var end = cm.getCursor('end');
        var isCommented = selection.startsWith('//');
        if (isCommented) {
          // Nếu đã được chú thích thì bỏ chú thích
          cm.replaceRange(selection.replace(/\n\/\/\s/g, '\n').replace(/^\/\/\s/, ''), start, end);
        } else {
          // Thêm chú thích
          cm.replaceRange('// ' + selection.replace(/\n(?=.)/g, '\n// '), start, end);
        }
      }

      return {
        state,
        textarea,
        boxBindProps,
        getBindValue,
        setValue,
        isFullScreen,
        fullScreenIcon,
        onToggleFullScreen,
        refresh,
        containerRef,
      };
    },
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-code-editer';
  .@{prefix-cls} {
    &.auto-height {
      .CodeMirror {
        height: v-bind(height) !important;
        min-height: 100px;
      }
    }

    /* Kiểu toàn màn hình */

    &.full-screen-parent {
      position: relative;

      .full-screen-icon {
        opacity: 0;
        color: black;
        width: 20px;
        height: 20px;
        line-height: 24px;
        background-color: white;
        position: absolute;
        top: 2px;
        right: 2px;
        z-index: 9;
        cursor: pointer;
        transition: opacity 0.3s;
        padding: 2px 0 0 1.5px;
      }

      &:hover {
        .full-screen-icon {
          opacity: 1;

          &:hover {
            background-color: rgba(255, 255, 255, 0.88);
          }
        }
      }

      &.full-screen {
        position: fixed;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        padding: 8px;
        background-color: #f5f5f5;

        .full-screen-icon {
          top: 12px;
          right: 12px;
        }

        .full-screen-child,
        .CodeMirror {
          // update-begin--author:liaozhiyang---date:20260713---for:【LHZP-236]Tăng cường onlinejs xem dữ liệu trong lịch sử, khi phóng to để xem, nội dung không thể cuộn
          height: 100% !important;
          // update-end--author:liaozhiyang---date:20260713---for:【LHZP-236]Tăng cường onlinejs xem dữ liệu trong lịch sử, khi phóng to để xem, nội dung không thể cuộn
          max-height: 100%;
          min-height: 100%;
        }
      }

      .full-screen-child {
        height: 100%;
      }
    }
    
    /** VUEN-2344[vue3] Kiểu này có vấn đề, có nên thêm một đường viền không */
    .CodeMirror{
      border: 1px solid #ddd;
    }
    .CodeMirror pre.CodeMirror-placeholder {
      color: #cacaca;
      font-family: -apple-system,BlinkMacSystemFont,Segoe UI,PingFang SC,Hiragino Sans GB,Microsoft YaHei,Helvetica Neue,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol;
    }
  }
  .CodeMirror-hints.idea,
  .CodeMirror-hints.monokai {
    z-index: 1001;
    max-width: 600px;
    max-height: 300px;
  }
  // Giải thích logic mã: 【QQYUN-8639] thích ứng chủ đề tối
  html[data-theme='dark'] {
    .@{prefix-cls} {
      .CodeMirror {
        border: 1px solid #3a3a3a;
      }
    }
  }
</style>
