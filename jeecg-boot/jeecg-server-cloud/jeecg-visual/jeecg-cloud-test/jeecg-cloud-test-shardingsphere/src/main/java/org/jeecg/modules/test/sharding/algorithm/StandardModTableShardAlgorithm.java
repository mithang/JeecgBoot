package org.jeecg.modules.test.sharding.algorithm;


import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Dùng để xử lý khi sử dụng một khóa duy nhất
 * Dựa theo giá trị của trường phân mảnh và sharding-count thực hiện phép chia lấy dư
 * SQL Trong câu lệnh có>，>=, <=，<，=, toán tử IN và BETWEEN AND đều có thể áp dụng chiến lược phân mảnh này.
 *
 * @author zyf
 */
public class StandardModTableShardAlgorithm implements StandardShardingAlgorithm<Integer> {
    private Properties props = new Properties();


    /**
     * Dùng để xử lý=phân mảnh của = và IN
     *
     * @param collection           Tập hợp các phân mảnh đích(Tên bảng)
     * @param preciseShardingValue Thông tin liên quan đến bảng logic
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> preciseShardingValue) {

        for (String name : collection) {
            Integer value = preciseShardingValue.getValue();
            //Dựa theo giá trị thực hiện phép chia lấy dư, thu được một giá trị đích
            if (name.indexOf(value % 2+"") > -1) {
                return name;
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Dùng để xử lý phân mảnh BETWEEN AND, nếu không cấu hình RangeShardingAlgorithm, BETWEEN AND trong SQL sẽ được xử lý theo định tuyến toàn bộ cơ sở dữ liệu
     *
     * @param collection
     * @param rangeShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Integer> rangeShardingValue) {

        return collection;
    }

    /**
     * Thuật toán phân mảnh tương ứng (sharding-algorithms) loại
     *
     * @return
     */
    @Override
    public String getType() {
        return "STANDARD_MOD";
    }
}