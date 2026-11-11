package org.jeecg.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * mybatis Interceptor, tự động inject người tạo, thời gian tạo, người sửa, thời gian sửa
 * @Author scott
 * @Date  2019-01-19
 *
 */
@Slf4j
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		String sqlId = mappedStatement.getId();
		log.debug("------sqlId------" + sqlId);
		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
		Object parameter = invocation.getArgs()[1];
		log.debug("------sqlCommandType------" + sqlCommandType);

		if (parameter == null) {
			return invocation.proceed();
		}
		if (SqlCommandType.INSERT == sqlCommandType) {
			LoginUser sysUser = this.getLoginUser();
			Field[] fields = oConvertUtils.getAllFields(parameter);
			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("createBy".equals(field.getName())) {
						field.setAccessible(true);
						Object localCreateBy = field.get(parameter);
						field.setAccessible(false);
						if (localCreateBy == null || "".equals(localCreateBy)) {
							if (sysUser != null) {
								// Tài khoản của người đăng nhập
								field.setAccessible(true);
								field.set(parameter, sysUser.getUsername());
								field.setAccessible(false);
							}
						}
					}
					// Inject thời gian tạo
					if ("createTime".equals(field.getName())) {
						field.setAccessible(true);
						Object localCreateDate = field.get(parameter);
						field.setAccessible(false);
						if (localCreateDate == null || "".equals(localCreateDate)) {
							field.setAccessible(true);
							field.set(parameter, new Date());
							field.setAccessible(false);
						}
					}
					//Inject mã phòng ban
					if ("sysOrgCode".equals(field.getName())) {
						field.setAccessible(true);
						Object localSysOrgCode = field.get(parameter);
						field.setAccessible(false);
						if (localSysOrgCode == null || "".equals(localSysOrgCode)) {
							// Lấy thông tin người dùng đăng nhập
							if (sysUser != null) {
								field.setAccessible(true);
								field.set(parameter, sysUser.getOrgCode());
								field.setAccessible(false);
							}
						}
					}

					//------------------------------------------------------------------------------------------------
					//Inject tenant ID (có bật cách ly dữ liệu đa tenant của module quản lý hệ thống hay không 【Chế độ đa tenant SAAS】)
					if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
						if (TenantConstant.TENANT_ID.equals(field.getName())) {
							field.setAccessible(true);
							Object localTenantId = field.get(parameter);
							field.setAccessible(false);
							if (localTenantId == null) {
								field.setAccessible(true);

								String tenantId = TenantContext.getTenant();
								//Nếu lấy tenant ID qua thread bị trống thì lấy tenant qua request của yêu cầu hiện tại (yêu cầu bị shiro loại khỏi interceptor sẽ không lấy được tenant ID)
								if(oConvertUtils.isEmpty(tenantId) && MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
									try {
										tenantId = TokenUtils.getTenantIdByRequest(SpringContextUtils.getHttpServletRequest());
									} catch (Exception e) {
										//e.printStackTrace();
									}
								}

								if (field.getType().equals(String.class)) {
									// Loại trường là String
									field.set(parameter, tenantId);
								} else {
									// Loại trường không phải là String
									field.set(parameter, oConvertUtils.getInt(tenantId, 0));
								}
								field.setAccessible(false);
							}
						}
					}
					//------------------------------------------------------------------------------------------------
					
				} catch (Exception e) {
				}
			}
		}
		if (SqlCommandType.UPDATE == sqlCommandType) {
			LoginUser sysUser = this.getLoginUser();
			Field[] fields = null;
			if (parameter instanceof ParamMap) {
				ParamMap<?> p = (ParamMap<?>) parameter;
				// Giải thích logic mã: Cập nhật hàng loạt báo lỗi issues/IZA3Q--
                String et = "et";
				if (p.containsKey(et)) {
					parameter = p.get(et);
				} else {
					parameter = p.get("param1");
				}

				// Giải thích logic mã: Khi cập nhật trường chỉ định thì báo lỗi issues/#516-
				if (parameter == null) {
					return invocation.proceed();
				}

				fields = oConvertUtils.getAllFields(parameter);
			} else {
				fields = oConvertUtils.getAllFields(parameter);
			}

			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("updateBy".equals(field.getName())) {
						//Lấy thông tin người dùng đăng nhập
						if (sysUser != null) {
							// Tài khoản đăng nhập
							field.setAccessible(true);
							field.set(parameter, sysUser.getUsername());
							field.setAccessible(false);
						}
					}
					if ("updateTime".equals(field.getName())) {
						field.setAccessible(true);
						field.set(parameter, new Date());
						field.setAccessible(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
	}

    /**
     * Lấy người dùng đăng nhập
     * @return
     */
	private LoginUser getLoginUser() {
		LoginUser sysUser = null;
		try {
			sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
		} catch (Exception e) {
			//e.printStackTrace();
			sysUser = null;
		}
		return sysUser;
	}

}
