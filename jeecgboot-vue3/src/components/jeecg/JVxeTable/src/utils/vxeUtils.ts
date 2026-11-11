/**
 *
 * Lấy nút cha theo tagName
 *
 * @param dom Nút dom cấp một
 * @param tagName Tên thẻ, không phân biệt chữ hoa/thường
 */
export function getParentNodeByTagName(dom: HTMLElement, tagName: string = 'body'): HTMLElement | null {
  if (tagName === 'body') {
    return document.body;
  }
  if (dom.parentElement) {
    if (dom.parentElement.tagName.toLowerCase() === tagName.trim().toLowerCase()) {
      return dom.parentElement;
    } else {
      return getParentNodeByTagName(dom.parentElement, tagName);
    }
  } else {
    return null;
  }
}
