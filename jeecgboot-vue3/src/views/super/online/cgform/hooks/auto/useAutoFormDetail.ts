
import { nextTick } from 'vue';

/***
 * Ngữ cảnh biểu mẫu
 */
export function useOnlineFormDetailContext() {
  const that = {};
  const CONTEXT_DESCRIPTION = {
    setFieldsValue: '<m> Thiết lập giá trị của control biểu mẫu',
    getFieldsValue: '<m> Lấy giá trị của control biểu mẫu',
    sh: '<p> Trạng thái hiển thị/ẩn của control biểu mẫu',
    isUpdate: '<p> Phán đoán có phải chế độ chỉnh sửa hay không',
    isDetail: '<p> Phán đoán có phải chế độ chi tiết hay không',
  };
  const onlineFormDetailContext = new Proxy(CONTEXT_DESCRIPTION, {
    get(_target: any, prop: string): any {
      return Reflect.get(that, prop);
    },
  });

  function addObject2Context(prop, object) {
    that[prop] = object;
  }

  function resetContext(context) {
    Object.keys(context).map((k) => {
      that[k] = context[k];
    });
  }
  addObject2Context('$nextTick', nextTick);
  addObject2Context('addObject2Context', addObject2Context);
  return { onlineFormDetailContext, addObject2Context, resetContext };
}
