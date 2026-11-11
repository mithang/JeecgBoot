import { CSSProperties, VNodeChild } from 'vue';
import { createTypes, VueTypeValidableDef, VueTypesInterface, toValidableType } from 'vue-types';

export type VueNode = VNodeChild | JSX.Element;

type PropTypes = VueTypesInterface & {
  readonly style: VueTypeValidableDef<CSSProperties>;
  readonly VNodeChild: VueTypeValidableDef<VueNode>;
  // readonly trueBool: VueTypeValidableDef<boolean>;
};
const newPropTypes = createTypes({
  func: undefined,
  bool: undefined,
  string: undefined,
  number: undefined,
  object: undefined,
  integer: undefined,
}) as PropTypes;

// Từ vue-types v5.0 Bắt đầu,extend()Phương thức đã bị bỏ, hiện đã đổi thành cách chính thức khuyến nghịES6+Phương thức https://dwightjack.github.io/vue-types/advanced/extending-vue-types.html#the-extend-method
class _propTypes extends newPropTypes {
  // a native-like validator that supports the `.validable` method
  static get style() {
    return toValidableType('style', {
      type: [String, Object],
    });
  }

  static get VNodeChild() {
    return toValidableType('VNodeChild', {
      type: undefined,
    });
  }
}

//update-begin-author:liaozhiyang date:2026-05-12 for: Sửa class extends vue-types Sau instance,TS Không nhìn thấy .string/.bool/.oneOfType thành viên tĩnh v.v. gây ra trên diện rộng "Property xxx does not exist on type 'typeof propTypes'" Báo lỗi——Ở chỗ export, khẳng định kiểu thành "chính lớp ∪ PropTypes View tĩnh」
const propTypes = _propTypes as unknown as typeof _propTypes & PropTypes;
export { propTypes };
//update-end-author:liaozhiyang date:2026-05-12
