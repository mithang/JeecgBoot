import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Văn bản giàu (rich text)
 */
export default class EditorWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JEditor',
      componentProps: {
        //update-begin-author:taoyan date:2022-6-1 for: VUEN-1159 Khi tải lần đầu, nhấp vào ô nhập đầu tiên, con trỏ sẽ chạy sang ô nhập văn bản giàu
        options: {
          auto_focus: false,
        },
        //update-end-author:taoyan date:2022-6-1 for: VUEN-1159 Khi tải lần đầu, nhấp vào ô nhập đầu tiên, con trỏ sẽ chạy sang ô nhập văn bản giàu
        // fileMax:1,
        // showImageUpload:false,
        // width:"966px",
        // height:"200px"
      },
    });
  }
}
