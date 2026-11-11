import type { JeecgTag } from './types';
import { shallowRef } from 'vue';

import ToolExecTag from './tool-exec';
import JeecgChart from "./jeecg-chart";

export const jeecgTagMap: Map<string, JeecgTag> = new Map();
// Danh sách tất cả tên nhãn jeecg
export const tagNames: string[] = [];

// Đăng ký nhãn gọi công cụ
useJeecgTag(ToolExecTag);
// Đăng ký nhãn hiển thị biểu đồ
useJeecgTag(JeecgChart);

// Tên class thống nhất của nhãn jeecg
export const JEECG_TAG_CLASS = 'jeecg-tag';

/**
 * Bỏ qua việc phân tích nhãn tùy chỉnh jeecg
 * @param md
 */
export function mdPluginJeecgTag(md: any) {
  // Lưu quy tắc hiển thị html_block gốc
  const htmlBlockOrigin =
    md.renderer.rules.html_block ||
    function (tokens, idx) {
      return tokens[idx].content;
    };

  // Ghi đè quy tắc hiển thị html_block
  md.renderer.rules.html_block = function (tokens, idx) {
    const token = tokens[idx];
    const content = token.content;

    let isJeecgTag = false;
    let tagName = '';

    for (const name of tagNames) {
      // Kiểm tra nội dung có chứa nhãn bắt đầu hoặc nhãn kết thúc của nhãn tùy chỉnh hay không
      const startTag = new RegExp(`<${name}(\\s|>)`, 'i');
      const endTag = new RegExp(`</${name}>`, 'i');
      if (startTag.test(content) || endTag.test(content)) {
        isJeecgTag = true;
        tagName = name;
        break;
      }
    }

    // Nhãn tùy chỉnh jeecg
    if (isJeecgTag) {
      const box = document.createElement('div');
      box.innerHTML = content;
      const tag = box.firstElementChild!;
      tag.classList.add(JEECG_TAG_CLASS);
      return tag.outerHTML;
    }

    // Các nhãn HTML khác hiển thị theo cách mặc định
    return htmlBlockOrigin(tokens, idx);
  };
}

export function useJeecgTag(tag: JeecgTag) {
  if (jeecgTagMap.has(tag.name)) {
    return;
  }
  tag.component = shallowRef(tag.component);
  jeecgTagMap.set(tag.name, tag);
  tagNames.push(tag.name);
}
