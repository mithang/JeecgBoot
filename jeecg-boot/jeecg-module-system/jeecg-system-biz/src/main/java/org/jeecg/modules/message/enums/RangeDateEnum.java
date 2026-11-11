package org.jeecg.modules.message.enums;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Dùng để truy vấn dữ liệu tin nhắn 【vue3】
 * Điều kiện truy vấn thông báo hệ thống phiên bản mới
 * @Author taoYan
 * @Date 2022/8/19 20:41
 **/
@Slf4j
@EnumDict("rangeDate")
public enum RangeDateEnum {

    JT("jt", "Hôm nay"),
    ZT("zt", "Hôm qua"),
    QT("qt", "Hôm kia"),
    BZ("bz","Tuần này"),
    SZ("sz", "Tuần trước"),
    BY("by", "Tháng này"),
    SY("sy", "Tháng trước"),
    SEVENDAYS("7day", "7 ngày"),
    ZDY("zdy", "Ngày tùy chỉnh");

    String key;

    String title;

    RangeDateEnum(String key, String title){
        this.key = key;
        this.title = title;
    }

    /**
     * Lấy dữ liệu danh mục
     * @return
     */
    public static List<DictModel> getDictList(){
        List<DictModel> list = new ArrayList<>();
        DictModel dictModel = null;
        for(RangeDateEnum e: RangeDateEnum.values()){
            dictModel = new DictModel();
            dictModel.setValue(e.key);
            dictModel.setText(e.title);
            list.add(dictModel);
        }
        return list;
    }

    /**
     * Lấy giá trị thời gian trong phạm vi theo key
     * @param key
     * @return
     */
    public static Date[] getRangeArray(String key){
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        Date[] array = new Date[2];
        boolean flag = false;
        if(JT.key.equals(key)){
            //Hôm nay
        } else if(ZT.key.equals(key)){
            //Hôm qua
            calendar1.add(Calendar.DAY_OF_YEAR, -1);
            calendar2.add(Calendar.DAY_OF_YEAR, -1);
        } else if(QT.key.equals(key)){
            //Hôm kia
            calendar1.add(Calendar.DAY_OF_YEAR, -2);
            calendar2.add(Calendar.DAY_OF_YEAR, -2);
        } else if(BZ.key.equals(key)){
            //Tuần này
            calendar1.set(Calendar.DAY_OF_WEEK, 2);

            calendar2.add(Calendar.WEEK_OF_MONTH,1);
            calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(SZ.key.equals(key)){
            //Thứ Hai tuần này trừ đi một tuần
            calendar1.set(Calendar.DAY_OF_WEEK, 2);
            calendar1.add(Calendar.WEEK_OF_MONTH, -1);

            // Thứ Hai tuần này trừ đi một ngày
            calendar2.set(Calendar.DAY_OF_WEEK, 2);
            calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(BY.key.equals(key)){
            //Tháng này
            calendar1.set(Calendar.DAY_OF_MONTH, 1);

            calendar2.set(Calendar.DAY_OF_MONTH, 1);
            calendar2.add(Calendar.MONTH, 1);
            calendar2.add(Calendar.DAY_OF_MONTH, -1);
        } else if(SY.key.equals(key)){
            //Ngày đầu tiên của tháng này trừ một tháng
            calendar1.set(Calendar.DAY_OF_MONTH, 1);
            calendar1.add(Calendar.MONTH, -1);

            //Ngày đầu tiên của tháng này trừ một ngày
            calendar2.set(Calendar.DAY_OF_MONTH, 1);
            calendar2.add(Calendar.DAY_OF_MONTH, -1);
        } else if (SEVENDAYS.key.equals(key)){
            //Ngày đầu tiên của 7 ngày
            calendar1.setTime(new Date());
            calendar1.add(Calendar.DATE, -7);
        }else{
            flag = true;
        }
        if(flag){
            return null;
        }
        // Thời gian bắt đầu 00:00:00 Thời gian kết thúc 23:59:59
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        array[0] = calendar1.getTime();
        array[1] = calendar2.getTime();
        return array;
    }
    
    public String getKey(){
        return this.key;
    }

}
