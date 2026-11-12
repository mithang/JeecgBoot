package org.jeecg.modules.demo.test.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: jeecg Kiểm tra demo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
public interface JeecgDemoMapper extends BaseMapper<JeecgDemo> {

    /**
     * Truy vấn dữ liệu danh sách demo theo tên
     * @param name Họ tên
     * @return demo tập hợp
     */
	public List<JeecgDemo> getDemoByName(@Param("name") String name);
	
	/**
	 * Truy vấn dữ liệu danh sách, truyền trực tiếp SQL quyền dữ liệu để lọc dữ liệu
	 * @param page
	 * @param permissionSql
	 * @return
	 */
	public IPage<JeecgDemo> queryListWithPermission(Page<JeecgDemo> page,@Param("permissionSql")String permissionSql);

	/**
	 * Lấy tất cả quyền hợp lệ theo tiền tố
	 * @param permsPrefix
	 * @return
	 */
	public List<String> queryAllAuth(@Param("permsPrefix")String permsPrefix);

	/**
	 * Truy vấn trường đã được ủy quyền của người dùng
	 * @param userId
	 * @param permsPrefix
	 * @return
	 */
	public List<String> queryUserAuth(@Param("userId")String userId,@Param("permsPrefix")String permsPrefix);


	/**
	 * Lấy người tạo
	 * @return
	 */
	List<String> getCreateByList();

}
