package org.jeecg.modules.test.sharding.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.test.sharding.entity.ShardingSysLog;
import org.jeecg.modules.test.sharding.service.IShardingSysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Kiểm thử chia cơ sở dữ liệu và bảng
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Tag(name = "Kiểm thử chia cơ sở dữ liệu và bảng")
@RestController
@RequestMapping("/demo/sharding")
public class JeecgShardingDemoController extends JeecgController<ShardingSysLog, IShardingSysLogService> {
    @Autowired
    private IShardingSysLogService shardingSysLogService;

    /**
     * Chia bảng đơn cơ sở dữ liệu —— Chèn
     * @return
     */
    @PostMapping(value = "/insert")
    @Operation(summary = "Chèn chia bảng trên một cơ sở dữ liệu")
    public Result<?> insert() {
        log.info("---------------------------------Chèn chia bảng trên một cơ sở dữ liệu--------------------------------");
        int size = 10;
        for (int i = 0; i < size; i++) {
            ShardingSysLog shardingSysLog = new ShardingSysLog();
            shardingSysLog.setLogContent("Dùng ShardingSphere triển khai chia cơ sở dữ liệu/chia bảng, kiểm thử chèn!");
            shardingSysLog.setLogType(i);
            shardingSysLog.setOperateType(i);
            shardingSysLogService.save(shardingSysLog);
        }
        return Result.OK("Chèn 10 bản ghi vào bảng đã chia của đơn cơ sở dữ liệu hoàn tất!");
    }

    /**
     * Chia bảng đơn cơ sở dữ liệu —— Truy vấn
     * @return
     */
    @PostMapping(value = "/list")
    @Operation(summary = "Truy vấn bảng đã chia của đơn cơ sở dữ liệu")
    public Result<?> list() {
        return Result.OK(shardingSysLogService.list());
    }

    /**
     * Chia cơ sở dữ liệu và bảng - Chèn
     * @return
     */
    @PostMapping(value = "/insert2")
    @Operation(summary = "Chèn dữ liệu chia cơ sở dữ liệu/chia bảng")
    public Result<?> insert2() {
        int start=20;
        int size=30;
        for (int i = start; i <= size; i++) {
            ShardingSysLog shardingSysLog = new ShardingSysLog();
            shardingSysLog.setLogContent("Kiểm thử chia cơ sở dữ liệu và bảng");
            shardingSysLog.setLogType(0);
            shardingSysLog.setOperateType(i);
            shardingSysLogService.save(shardingSysLog);
        }
        return Result.OK("Chèn 10 bản ghi chia cơ sở dữ liệu/chia bảng hoàn tất!");
    }

    /**
     * Chia cơ sở dữ liệu và bảng - Truy vấn
     * @return
     */
    @PostMapping(value = "/list2")
    @Operation(summary = "Truy vấn chia cơ sở dữ liệu/chia bảng")
    public Result<?> list2() {
        return Result.OK(shardingSysLogService.list());
    }

}
