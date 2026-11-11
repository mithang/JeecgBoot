import CronParser from 'cron-parser';
import type { ValidatorRule } from 'ant-design-vue/lib/form/interface';

const cronRule: ValidatorRule = {
  validator({}, value) {
    // Không điền thì không kiểm chứng
    if (!value) {
      return Promise.resolve();
    }
    const values: string[] = value.split(' ').filter((item) => !!item);
    if (values.length > 7) {
      return Promise.reject('CronBiểu thức Cron tối đa 7 mục!');
    }
    // Kiểm tra mục thứ 7
    let val: string = value;
    if (values.length === 7) {
      const year = values[6];
      if (year !== '*' && year !== '?') {
        let yearValues: string[] = [];
        if (year.indexOf('-') >= 0) {
          yearValues = year.split('-');
        } else if (year.indexOf('/')) {
          yearValues = year.split('/');
        } else {
          yearValues = [year];
        }
        // Kiểm tra có phải đều là số không
        const checkYear = yearValues.some((item) => isNaN(Number(item)));
        if (checkYear) {
          return Promise.reject('CronTham số biểu thức Cron[năm]Lỗi:' + year);
        }
      }
      // Lấy sáu mục đầu trong đó
      val = values.slice(0, 6).join(' ');
    }
    // 6vị, không có năm
    // 5vị không có giây, năm
    try {
      const iter = CronParser.parseExpression(val);
      iter.next();
      return Promise.resolve();
    } catch (e) {
      return Promise.reject('CronLỗi biểu thức:' + e);
    }
  },
};

export default cronRule.validator;
