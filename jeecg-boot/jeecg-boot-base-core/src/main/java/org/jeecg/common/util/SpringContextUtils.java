package org.jeecg.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.ServiceNameConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description: spring Lớp công cụ ngữ cảnh
 * @author: jeecg-boot
 */
@Lazy(false)
@Component
public class SpringContextUtils implements ApplicationContextAware {

	/**
	 * Thể hiện đối tượng ngữ cảnh
	 */
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtils.applicationContext = applicationContext;
	}

	/**
	 * Lấy applicationContext
	 *
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	  * Lấy HttpServletRequest
	 */
	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	/**
	 * Lấy HttpServletResponse
	 */
	public static HttpServletResponse getHttpServletResponse() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	}

	/**
	*  Lấy đường dẫn gốc của dự án basePath
	*/
	public static String getDomain(){
		HttpServletRequest request = getHttpServletRequest();
		StringBuffer url = request.getRequestURL();
		//1.Trong trường hợp microservice, lấy của gateway basePath
		String basePath = request.getHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH);
		if(oConvertUtils.isNotEmpty(basePath)){
        //update-begin---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------
			String validated = CommonUtils.validateGatewayBasePathForDomain(basePath);
			if(validated != null){
				return validated;
			}
        //update-end---author:wangshuai ---date:20260616  for：【issues/9695】Kiểm chứng X_GATEWAY_BASE_PATH Ngăn chặn SSRF header injection-----------
		}
		String domain = url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
		//2.【Tương thích】 Sau khi xác thực SSL,request.getScheme()vấn đề không lấy được https
		// https://blog.csdn.net/weixin_34376986/article/details/89767950
		String scheme = request.getHeader(CommonConstant.X_FORWARDED_SCHEME);
		//update-begin---author:scott ---date:20261013  for：【jimureport/issues/4786】Khi reverse proxy HTTPS chỉ truyền X-Forwarded-Proto thì tài nguyên trang vẫn là http://, màn hình trắng do Mixed Content-----------
		if(oConvertUtils.isEmpty(scheme)){
			// Khi proxy nhiều cấp có dạng "https, http", lấy cái đầu tiên (bước nhảy gần trình duyệt nhất)
			String proto = request.getHeader(CommonConstant.X_FORWARDED_PROTO);
			scheme = oConvertUtils.isNotEmpty(proto) ? proto.split(",")[0].trim() : null;
		}
		//update-end---author:scott ---date:20261013  for：【jimureport/issues/4786】Khi reverse proxy HTTPS chỉ truyền X-Forwarded-Proto thì tài nguyên trang vẫn là http://, màn hình trắng do Mixed Content-----------
		if(scheme!=null && !request.getScheme().equals(scheme)){
			domain = domain.replace(request.getScheme(),scheme);
		}
		return domain;
	}

	public static String getOrigin(){
		HttpServletRequest request = getHttpServletRequest();
		return request.getHeader("Origin");
	}
	
	/**
	 * Lấy thông qua name Bean.
	 *
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	/**
	 * Lấy thông qua class Bean.
	 *
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * Thông qua name,và trả về đối tượng được chỉ định theo Clazz Bean
	 *
	 * @param name
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}
}
