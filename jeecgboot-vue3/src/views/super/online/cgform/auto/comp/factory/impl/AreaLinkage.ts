import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Tỉnh/Thành/Quận huyện
 */
export default class PcaWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    // update-begin--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
    const extendData: any = this.getExtendData();
    const componentProps: any = {}
    if (extendData.displayLevel) {
      componentProps.displayLevel = extendData.displayLevel;
      componentProps.saveCode = extendData.displayLevel === 'all' ? 'region' : componentProps.displayLevel;
    }
    // update-end--author:liaozhiyang---date:20260204---for:【QQYUN-14694】onlineHỗ trợ cấu hình độc lập tỉnh, thành phố, quận/huyện
    return Object.assign({}, item, {
      component: 'JAreaLinkage',
      componentProps: {
        saveCode: 'region',
        ...componentProps,
      },
    });
  }
}
