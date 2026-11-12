package org.jeecg.modules.demo.test.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.mapper.JeecgDemoMapper;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: jeecg Kiểm tra demo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
@Service
public class JeecgDemoServiceImpl extends ServiceImpl<JeecgDemoMapper, JeecgDemo> implements IJeecgDemoService {
	@Autowired
	JeecgDemoMapper jeecgDemoMapper;
	
	/**
	 * Kiểm soát giao dịch ở tầng service
	 * Thêm annotation: @Transactional, phương thức được khai báo là một giao dịch độc lập (nếu có ngoại lệ, mọi thao tác DB sẽ được rollback)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void testTran() {
		JeecgDemo pp = new JeecgDemo();
		pp.setAge(1111);
		pp.setName("Giao dịch kiểm thử Thỏ Trắng Nhỏ 1");
		jeecgDemoMapper.insert(pp);
		
		JeecgDemo pp2 = new JeecgDemo();
		pp2.setAge(2222);
		pp2.setName("Giao dịch kiểm thử Thỏ Trắng Nhỏ 2");
		jeecgDemoMapper.insert(pp2);
        //Ngoại lệ tùy chỉnh
		Integer.parseInt("hello");
		
		JeecgDemo pp3 = new JeecgDemo();
		pp3.setAge(3333);
		pp3.setName("Giao dịch kiểm thử Thỏ Trắng Nhỏ 3");
		jeecgDemoMapper.insert(pp3);
		return ;
	}


	/**
	 * Kiểm tra annotation cache:  redis
	 */
	@Override
	@Cacheable(cacheNames = CacheConstant.TEST_DEMO_CACHE, key = "#id")
	public JeecgDemo getByIdCacheable(String id) {
		JeecgDemo t = jeecgDemoMapper.selectById(id);
		System.err.println("---Chưa đọc cache, đọc cơ sở dữ liệu---");
		System.err.println(t);
		return t;
	}


	/**
	 * @Cacheable TTL tùy chỉnh: #60(đơn vị là giây, hiện chỉ hỗ trợ định dạng này)
	 * Chỉ định thời gian hiệu lực của cache là 60 giây bằng annotation
	 * 
	 * Tham khảo blog: https://www.cnblogs.com/h2285409/p/18324396
	 */
	@Override
	@Cacheable(cacheNames = "ceshi:redis:ttl#60", key = "#id")
	public JeecgDemo getByIdCacheableTTL(String id) {
		JeecgDemo t = jeecgDemoMapper.selectById(id);
		System.err.println("---Chưa đọc cache, đọc cơ sở dữ liệu---");
		System.err.println(t);
		return t;
	}


	@Override
	public IPage<JeecgDemo> queryListWithPermission(int pageSize,int pageNo) {
		Page<JeecgDemo> page = new Page<>(pageNo, pageSize);
		//Lấy đoạn SQL quy tắc quyền dữ liệu của yêu cầu hiện tại bằng cách lập trình
		String sql = QueryGenerator.installAuthJdbc(JeecgDemo.class);
		return this.baseMapper.queryListWithPermission(page, sql);
	}

	@Override
	public String getExportFields() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//Ví dụ xuất cột cấu hình quyền
		//1.Tiền tố cấu hình phải trùng với tiền tố cột đã cấu hình trong menu
		List<String> noAuthList = new ArrayList<>();
		List<String> exportFieldsList = new ArrayList<>();
		String permsPrefix = "testdemo:";
		//Truy vấn trường hợp lệ của menu cấu hình
		List<String> allAuth = this.jeecgDemoMapper.queryAllAuth(permsPrefix);
		//Truy vấn trường đã được cấp quyền
		List<String> userAuth = this.jeecgDemoMapper.queryUserAuth(sysUser.getId(),permsPrefix);
		//Liệt kê trường chưa được cấp quyền
		for(String perms : allAuth){
			if(!userAuth.contains(perms)){
				noAuthList.add(perms.substring(permsPrefix.length()));
			}
		}
		//So sánh trường trong lớp entity với trường chưa được cấp quyền, liệt kê trường cần xuất
		Field[] fileds = JeecgDemo.class.getDeclaredFields();
		List<Field> list = new ArrayList(Arrays.asList(fileds));
		for(Field field : list){
			if(!noAuthList.contains(field.getName())){
				exportFieldsList.add(field.getName());
			}
		}
		return exportFieldsList != null && exportFieldsList.size()>0 ? String.join(",", exportFieldsList) : "";
	}

	@Override
	public List<String> getCreateByList() {
		return jeecgDemoMapper.getCreateByList();
	}

}
