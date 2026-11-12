/**
 * Khai báo kiểu cho unplugin-icons
 * Cung cấp hỗ trợ kiểu cho đường dẫn ~icons
 */

declare module '~icons/*' {
  import { FunctionalComponent, SVGAttributes } from 'vue';
  const component: FunctionalComponent<SVGAttributes>;
  export default component;
}
