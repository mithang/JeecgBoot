<template>
  <div>
    <textarea ref="textarea">
Mặt trời lặn sau núi, sông Hoàng Hà đổ ra biển.
Muốn nhìn xa ngàn dặm, phải lên thêm một tầng lầu.
        </textarea
    >
  </div>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, reactive } from 'vue';
  // Nhập instance toàn cục
  import _CodeMirror from 'codemirror';

  // Kiểu dáng cốt lõi
  import 'codemirror/lib/codemirror.css';
  // Sau khi import theme vẫn cần chỉ định theme trong options thì mới có hiệu lực
  import 'codemirror/theme/cobalt.css';

  // Cần import thư viện tô sáng cú pháp cụ thể mới có hiệu ứng tô sáng cú pháp tương ứng
  // codemirror Thực ra chính thức hỗ trợ thông qua /addon/mode/loadmode.js và /mode/meta.js để tải động thư viện tô sáng cú pháp tương ứng
  // Nhưng vue dường như không thể tải động JS tương ứng sau khi instance được khởi tạo, nên tại đây mới import trước JS tương ứng
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
  // Thử lấy instance toàn cục

  export default defineComponent({
    components: {},
    setup() {
      const CodeMirror = window.CodeMirror || _CodeMirror;

      const textarea = ref(null);
      const options = reactive({
        // Định dạng thụt lề
        tabSize: 2,
        // Chủ đề, JS của thư viện chủ đề tương ứng cần được import trước
        theme: 'cobalt',
        // Hiển thị số dòng
        lineNumbers: true,
        line: true,
      });
      onMounted(() => {
        init();
      });

      function init() {
        CodeMirror.fromTextArea(textarea.value, options);
      }

      return {
        textarea,
      };
    },
  });
</script>

<style lang="css" scoped></style>
