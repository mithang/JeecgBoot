package org.jeecg.common.util;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.springframework.beans.BeanUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 
 * @Author  张代浩
 *
 */
@Slf4j
public class oConvertUtils {
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return (true);
		}
		if ("".equals(object)) {
			return (true);
		}
		if ("null".equals(object)) {
			return (true);
		}
		if (CommonConstant.STRING_NULL.equals(object)) {
			return (true);
		}
		return (false);
	}
	
	public static boolean isNotEmpty(Object object) {
		if (object != null && !"".equals(object) && !object.equals(CommonConstant.STRING_NULL)) {
			return (true);
		}
		return (false);
	}

	
	/**
	 * Trả về chuỗi đã giải mã decode
	 * 
	 * @param inStr
	 * @return
	 */
	public static String decodeString(String inStr) {
		if (oConvertUtils.isEmpty(inStr)) {
			return null;
		}

		try {
			inStr = URLDecoder.decode(inStr, "UTF-8");
		} catch (Exception e) {
			// Giải quyết:URLDecoder: Illegal hex characters in escape (%) pattern - For input string: "Tự động"
			//e.printStackTrace();
		}
		return inStr;
	}

	public static String decode(String strIn, String sourceCode, String targetCode) {
		String temp = code2code(strIn, sourceCode, targetCode);
		return temp;
	}

	@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static String StrToUTF(String strIn, String sourceCode, String targetCode) {
		strIn = "";
		try {
			strIn = new String(strIn.getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strIn;

	}

	private static String code2code(String strIn, String sourceCode, String targetCode) {
		String strOut = null;
		if (strIn == null || "".equals(strIn.trim())) {
			return strIn;
		}
		try {
			byte[] b = strIn.getBytes(sourceCode);
			for (int i = 0; i < b.length; i++) {
				System.out.print(b[i] + "  ");
			}
			strOut = new String(b, targetCode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return strOut;
	}

	public static int getInt(String s, int defval) {
		if (s == null || "".equals(s)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}

	public static int getInt(String s) {
		if (s == null || "".equals(s)) {
			return 0;
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static int getInt(String s, Integer df) {
		if (s == null || "".equals(s)) {
			return df;
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Integer[] getInts(String[] s) {
		if (s == null) {
			return null;
		}
		Integer[] integer = new Integer[s.length];
		for (int i = 0; i < s.length; i++) {
			integer[i] = Integer.parseInt(s[i]);
		}
		return integer;

	}

	public static double getDouble(String s, double defval) {
		if (s == null || "".equals(s)) {
			return (defval);
		}
		try {
			return (Double.parseDouble(s));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}

	public static double getDou(Double s, double defval) {
		if (s == null) {
			return (defval);
		}
		return s;
	}

	/*public static Short getShort(String s) {
		if (StringUtil.isNotEmpty(s)) {
			return (Short.parseShort(s));
		} else {
			return null;
		}
	}*/

	public static int getInt(Object object, int defval) {
		if (isEmpty(object)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}
	
	public static Integer getInteger(Object object, Integer defval) {
		if (isEmpty(object)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}
	
	public static Integer getInt(Object object) {
		if (isEmpty(object)) {
			return null;
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static int getInt(BigDecimal s, int defval) {
		if (s == null) {
			return (defval);
		}
		return s.intValue();
	}

	public static Integer[] getIntegerArry(String[] object) {
		int len = object.length;
		Integer[] result = new Integer[len];
		try {
			for (int i = 0; i < len; i++) {
				result[i] = new Integer(object[i].trim());
			}
			return result;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String getString(String s) {
		return (getString(s, ""));
	}

	/**
	 * Escape thành mã Unicode
	 * @param s
	 * @return
	 */
	/*public static String escapeJava(Object s) {
		return StringEscapeUtils.escapeJava(getString(s));
	}*/
	
	public static String getString(Object object) {
		if (isEmpty(object)) {
			return "";
		}
		return (object.toString().trim());
	}

	public static String getString(int i) {
		return (String.valueOf(i));
	}

	public static String getString(float i) {
		return (String.valueOf(i));
	}

	/**
	 * Trả về chuỗi thông thường (chỉ giữ lại chữ số, chữ cái và tiếng Trung trong chuỗi)
	 *
	 * @param input
	 * @return
	 */
	public static String getNormalString(String input) {
		if (oConvertUtils.isEmpty(input)) {
			return null;
		}
		String result = input.replaceAll("[^0-9a-zA-Z\\u4e00-\\u9fa5]", "");
		return result;
	}

	public static String getString(String s, String defval) {
		if (isEmpty(s)) {
			return (defval);
		}
		return (s.trim());
	}

	public static String getString(Object s, String defval) {
		if (isEmpty(s)) {
			return (defval);
		}
		return (s.toString().trim());
	}

	public static long stringToLong(String str) {
		Long test = new Long(0);
		try {
			test = Long.valueOf(str);
		} catch (Exception e) {
		}
		return test.longValue();
	}

	/**
	 * Lấy máy cục bộ IP
	 */
	public static String getIp() {
		String ip = null;
		try {
			InetAddress address = InetAddress.getLocalHost();
			ip = address.getHostAddress();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	/**
	 * Phán đoán một lớp có phải kiểu dữ liệu cơ bản không.
	 * 
	 * @param clazz
	 *            Lớp cần phán đoán.
	 * @return true biểu thị là kiểu dữ liệu cơ bản.
	 */
	private static boolean isBaseDataType(Class clazz) throws Exception {
		return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(Character.class) || clazz.equals(Short.class) || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class) || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
	}

	/**
	 * Giải mã base64
	 *
	 * @param base64Str base64 Chuỗi
	 * @return Chuỗi sau khi được mã hoá
	 */
	public static String decodeBase64Str(String base64Str) {
		byte[] byteContent = Base64.decodeBase64(base64Str);
		if (byteContent == null) {
			return null;
		}
		String decodedString = new String(byteContent);
		return decodedString;
	}
	
	
	/**
	 * @param request
	 *            IP
	 * @return IP Address
	 */
	public static String getIpAddrByRequest(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * @return Máy local IP
	 * @throws SocketException
	 */
	public static String getRealIp() throws SocketException {
        // IP cục bộ, nếu chưa cấu hình IP mạng ngoài thì trả về IP này
		String localip = null;
        // Mạng ngoài IP
		String netip = null;

		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
        // Có tìm thấy mạng ngoài không IP
		boolean finded = false;
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
                // Mạng ngoài IP
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
					netip = ip.getHostAddress();
					finded = true;
					break;
				} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                    // Mạng nội bộ IP
				    localip = ip.getHostAddress();
				}
			}
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}

	/**
	 * java Loại bỏ khoảng trắng, ký tự carriage return, ký tự xuống dòng và ký tự tab trong chuỗi
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
		    String reg = "\\s*|\t|\r|\n";
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;

	}

	/**
	 * Kiểm tra xem phần tử có nằm trong mảng không
	 * 
	 * @param child
	 * @param all
	 * @return
	 */
	public static boolean isIn(String child, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}
		for (int i = 0; i < all.length; i++) {
			String aSource = all[i];
			if (aSource.equals(child)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Kiểm tra xem phần tử có nằm trong mảng không
	 *
	 * @param childArray
	 * @param all
	 * @return
	 */
	public static boolean isArrayIn(String[] childArray, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}
		for (String v : childArray) {
			if (!isIn(v, all)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Kiểm tra xem phần tử có nằm trong mảng không
	 *
	 * @param childArray
	 * @param all
	 * @return
	 */
	public static boolean isJsonArrayIn(JSONArray childArray, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}

		List<String> childs = childArray.toJavaList(String.class);
		for (String v : childs) {
			if (!isIn(v, all)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Phán đoán chuỗi có phải định dạng JSON không
	 * @param str
	 * @return
	 */
	public static boolean isJson(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		try {
			com.alibaba.fastjson.JSON.parse(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Lấy đối tượng Map
	 */
	public static Map<Object, Object> getHashMap() {
		return new HashMap<>(5);
	}

	/**
	 * SET Chuyển đổi MAP
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Object, Object> setToMap(Set<Object> setobj) {
		Map<Object, Object> map = getHashMap();
		for (Iterator iterator = setobj.iterator(); iterator.hasNext();) {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iterator.next();
			map.put(entry.getKey().toString(), entry.getValue() == null ? "" : entry.getValue().toString().trim());
		}
		return map;

	}

	public static boolean isInnerIp(String ipAddress) {
		boolean isInnerIp = false;
		long ipNum = getIpNum(ipAddress);
		/**
		 * IP riêng: Loại A 10.0.0.0-10.255.255.255 B Lớp 172.16.0.0-172.31.255.255 C Lớp 192.168.0.0-192.168.255.255 Tất nhiên, còn dải mạng 127 là địa chỉ loopback
		 **/
		long aBegin = getIpNum("10.0.0.0");
		long aEnd = getIpNum("10.255.255.255");
		long bBegin = getIpNum("172.16.0.0");
		long bEnd = getIpNum("172.31.255.255");
		long cBegin = getIpNum("192.168.0.0");
		long cEnd = getIpNum("192.168.255.255");
		String localIp = "127.0.0.1";
		isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || localIp.equals(ipAddress);
		return isInnerIp;
	}

	private static long getIpNum(String ipAddress) {
		String[] ip = ipAddress.split("\\.");
		long a = Integer.parseInt(ip[0]);
		long b = Integer.parseInt(ip[1]);
		long c = Integer.parseInt(ip[2]);
		long d = Integer.parseInt(ip[3]);

		long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
		return ipNum;
	}

	private static boolean isInner(long userIp, long begin, long end) {
		return (userIp >= begin) && (userIp <= end);
	}
	
	/**
	 * Chuyển đổi chuỗi được đặt tên theo kiểu gạch dưới viết hoa thành kiểu camelCase.
	 * Nếu chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi rỗng, trả về chuỗi rỗng.</br>
	 * Ví dụ:hello_world->helloWorld
	 * 
	 * @param name
	 *            Chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi
	 * @return Chuỗi được đặt tên theo kiểu camelCase sau khi chuyển đổi
	 */
	public static String camelName(String name) {
		StringBuilder result = new StringBuilder();
		// Kiểm tra nhanh
		if (name == null || name.isEmpty()) {
			// Không cần chuyển đổi
			return "";
		} else if (!name.contains(SymbolConstant.UNDERLINE)) {
			// Không chứa dấu gạch dưới, chỉ viết thường chữ cái đầu
			// Giải thích logic mã: TASK #2500 【Trình tạo mã】 Chức năng tạo mẫu chung trong phát triển trình tạo mã
			return name.substring(0, 1).toLowerCase() + name.substring(1).toLowerCase();
		}
		// Tách chuỗi gốc bằng dấu gạch dưới
		String[] camels = name.split("_");
		for (String camel : camels) {
			// Bỏ qua dấu gạch dưới ở đầu, cuối chuỗi gốc hoặc dấu gạch dưới kép
			if (camel.isEmpty()) {
				continue;
			}
			// Xử lý đoạn camelCase thực sự
			if (result.length() == 0) {
				// Đoạn camelCase đầu tiên, tất cả chữ cái đều viết thường
				result.append(camel.toLowerCase());
			} else {
				// Các đoạn camelCase khác, viết hoa chữ cái đầu
				result.append(camel.substring(0, 1).toUpperCase());
				result.append(camel.substring(1).toLowerCase());
			}
		}
		return result.toString();
	}
	
	/**
	 * Chuyển đổi chuỗi được đặt tên theo kiểu gạch dưới viết hoa thành kiểu camelCase.
	 * Nếu chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi rỗng, trả về chuỗi rỗng.</br>
	 * Ví dụ:hello_world,test_id->helloWorld,testId
	 * 
	 * @param name
	 *            Chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi
	 * @return Chuỗi được đặt tên theo kiểu camelCase sau khi chuyển đổi
	 */
	public static String camelNames(String names) {
		if(names==null||"".equals(names)){
			return null;
		}
		StringBuffer sf = new StringBuffer();
		String[] fs = names.split(",");
		for (String field : fs) {
			field = camelName(field);
			sf.append(field + ",");
		}
		String result = sf.toString();
		return result.substring(0, result.length() - 1);
	}
	
	/**
	 * Chuyển đổi chuỗi được đặt tên theo kiểu gạch dưới viết hoa thành kiểu camelCase.(chữ cái đầu viết thường)
	 * Nếu chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi rỗng, trả về chuỗi rỗng.</br>
	 * Ví dụ:hello_world->HelloWorld
	 * 
	 * @param name
	 *            Chuỗi được đặt tên theo kiểu gạch dưới viết hoa trước khi chuyển đổi
	 * @return Chuỗi được đặt tên theo kiểu camelCase sau khi chuyển đổi
	 */
	public static String camelNameCapFirst(String name) {
		StringBuilder result = new StringBuilder();
		// Kiểm tra nhanh
		if (name == null || name.isEmpty()) {
			// Không cần chuyển đổi
			return "";
		} else if (!name.contains(SymbolConstant.UNDERLINE)) {
			// Không chứa dấu gạch dưới, chỉ viết thường chữ cái đầu
			return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
		// Tách chuỗi gốc bằng dấu gạch dưới
		String[] camels = name.split("_");
		for (String camel : camels) {
			// Bỏ qua dấu gạch dưới ở đầu, cuối chuỗi gốc hoặc dấu gạch dưới kép
			if (camel.isEmpty()) {
				continue;
			}
			// Các đoạn camelCase khác, viết hoa chữ cái đầu
			result.append(camel.substring(0, 1).toUpperCase());
			result.append(camel.substring(1).toLowerCase());
		}
		return result.toString();
	}
	
	/**
	 * Chuyển đổi tên camelCase thành dấu gạch dưới
	 * @param para
	 * @return
	 */
	public static String camelToUnderline(String para){
	    int length = 3;
        if(para.length()<length){
        	return para.toLowerCase(); 
        }
        StringBuilder sb=new StringBuilder(para);
        //Định vị
        int temp=0;
        //Bắt đầu từ ký tự thứ ba để tránh đặt tên không đúng quy chuẩn 
        for(int i=2;i<para.length();i++){
            if(Character.isUpperCase(para.charAt(i))){
                sb.insert(i+temp, "_");
                temp+=1;
            }
        }
        return sb.toString().toLowerCase(); 
	}

	/**
	 * Số ngẫu nhiên
	 * @param place Định nghĩa số chữ số của số ngẫu nhiên
	 */
	public static String randomGen(int place) {
		String base = "qwertyuioplkjhgfdsazxcvbnmQAZWSXEDCRFVTGBYHNUJMIKLOP0123456789";
		StringBuffer sb = new StringBuffer();
		Random rd = new Random();
		for(int i=0;i<place;i++) {
			sb.append(base.charAt(rd.nextInt(base.length())));
		}
		return sb.toString();
	}
	
	/**
	 * Lấy tất cả thuộc tính của lớp, bao gồm cả lớp cha
	 * 
	 * @param object
	 * @return
	 */
	public static Field[] getAllFields(Object object) {
		Class<?> clazz = object.getClass();
		List<Field> fieldList = new ArrayList<>();
		while (clazz != null) {
			fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}
	
	/**
	  * Chuyển toàn bộ key của map thành chữ thường
	 * @param list
	 * @return
	 */
	public static List<Map<String, Object>> toLowerCasePageList(List<Map<String, Object>> list){
		List<Map<String, Object>> select = new ArrayList<>();
		for (Map<String, Object> row : list) {
			 Map<String, Object> resultMap = new HashMap<>(5);
			 Set<String> keySet = row.keySet(); 
			 for (String key : keySet) { 
				 String newKey = key.toLowerCase(); 
				 resultMap.put(newKey, row.get(key)); 
			 }
			 select.add(resultMap);
		}
		return select;
	}

	/**
	 * Chuyển entityList thành modelList
	 * @param fromList
	 * @param tClass
	 * @param <F>
	 * @param <T>
	 * @return
	 */
	public static<F,T> List<T> entityListToModelList(List<F> fromList, Class<T> tClass){
		if(fromList == null || fromList.isEmpty()){
			return null;
		}
		List<T> tList = new ArrayList<>();
		for(F f : fromList){
			T t = entityToModel(f, tClass);
			tList.add(t);
		}
		return tList;
	}

	public static<F,T> T entityToModel(F entity, Class<T> modelClass) {
		log.debug("entityToModel : Entity Gán giá trị thuộc tính vào Model");
		Object model = null;
		if (entity == null || modelClass ==null) {
			return null;
		}

		try {
			model = modelClass.newInstance();
		} catch (InstantiationException e) {
			log.error("entityToModel : Ngoại lệ khởi tạo (instantiation)", e);
		} catch (IllegalAccessException e) {
			log.error("entityToModel : Ngoại lệ quyền bảo mật", e);
		}
		BeanUtils.copyProperties(entity, model);
		return (T)model;
	}

	/**
	 * Phán đoán list có rỗng không
	 *
	 * @param list
	 * @return true or false
	 * list == null		: true
	 * list.size() == 0	: true
	 */
	public static boolean listIsEmpty(Collection list) {
		return (list == null || list.size() == 0);
	}

	/**
	 * Phán đoán giá trị cũ và giá trị mới có bằng nhau không
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean isEqual(Object oldVal, Object newVal) {
		if (oldVal != null && newVal != null) {
			if (isArray(oldVal)) {
				return equalityOfArrays((Object[]) oldVal, (Object[]) newVal);
			}else if(oldVal instanceof JSONArray){
				if(newVal instanceof JSONArray){
					return equalityOfJSONArray((JSONArray) oldVal, (JSONArray) newVal);
				}else{
					if (isEmpty(newVal) && (oldVal == null || ((JSONArray) oldVal).size() == 0)) {
						return true;
					}
					List<Object> arrayStr = Arrays.asList(newVal.toString().split(","));
					JSONArray newValArray = new JSONArray(arrayStr);
					return equalityOfJSONArray((JSONArray) oldVal, newValArray);
				}
			}else{
				return oldVal.equals(newVal);
			}
			
		} else {
			if (oldVal == null && newVal == null) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Mô tả phương thức: phán đoán một đối tượng có phải là mảng không
	 *
	 * @param obj
	 * @return
	 * @author yaomy
	 * @date 2018 năm 2 tháng 5 ngày buổi chiều 5:03:00
	 */
	public static boolean isArray(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass().isArray();
	}

	/**
	 * Lấy kích thước của tập hợp
	 * 
	 * @param collection
	 * @return
	 */
	public static int getCollectionSize(Collection<?> collection) {
		return collection != null ? collection.size() : 0;
	}
	
	/**
	 * Kiểm tra hai mảng có bằng nhau hay không (phần tử mảng không phân biệt thứ tự)
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfJSONArray(JSONArray oldVal, JSONArray newVal) {
		if (oldVal != null && newVal != null) {
			Object[] oldValArray = oldVal.toArray();
			Object[] newValArray = newVal.toArray();
			return equalityOfArrays(oldValArray,newValArray);
		} else {
			if ((oldVal == null || oldVal.size() == 0) && (newVal == null || newVal.size() == 0)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * So sánh chuỗi có chứa dấu phẩy
	 * QQYUN-5212【Quy trình đơn giản】 Khi kích hoạt theo ngày, component chọn nhiều nhân sự có thứ tự chọn không nhất quán thì không kích hoạt; có lẽ là vấn đề thống nhất, bao gồm cả component chọn nhiều phòng ban
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfStringArrays(String oldVal, String newVal) {
		if(oldVal.equals(newVal)){
			return true;
		}
		if(oldVal.indexOf(",")>=0 && newVal.indexOf(",")>=0){
			String[] arr1 = oldVal.split(",");
			String[] arr2 = newVal.split(",");
			if(arr1.length == arr2.length){
				boolean flag = true;
				Map<String, Integer> map = new HashMap<>();
				for(String s1: arr1){
					map.put(s1, 1);
				}
				for(String s2: arr2){
					if(map.get(s2) == null){
						flag = false;
						break;
					}
				}
				return flag;
			}
		}
		return false;
	}
	
	/**
	 * Kiểm tra hai mảng có bằng nhau hay không (phần tử mảng không phân biệt thứ tự)
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfArrays(Object[] oldVal, Object newVal[]) {
		if (oldVal != null && newVal != null) {
			Arrays.sort(oldVal);
			Arrays.sort(newVal);
			return Arrays.equals(oldVal, newVal);
		} else {
			if ((oldVal == null || oldVal.length == 0) && (newVal == null || newVal.length == 0)) {
				return true;
			} else {
				return false;
			}
		}
	}

//	public static void main(String[] args) {
////		String[] a = new String[]{"1", "2"};
////		String[] b = new String[]{"2", "1"};
//		Integer a = null;
//		Integer b = 1;
//		System.out.println(oConvertUtils.isEqual(a, b));
//	}
	
	/**
	 * Phán đoán list có không rỗng không
	 *
	 * @param list
	 * @return true or false
	 * list == null		: false
	 * list.size() == 0	: false
	 */
	public static boolean listIsNotEmpty(Collection list) {
		return !listIsEmpty(list);
	}

	/**
	 * Đọc nội dung văn bản tĩnh
	 * @param url
	 * @return
	 */
	public static String readStatic(String url) {
		String json = "";
		try {
			//Đổi cách viết khác để giải quyết vấn đề Spring Boot đọc file trong gói jar
			InputStream stream = oConvertUtils.class.getClassLoader().getResourceAsStream(url.replace("classpath:", ""));
			json = IOUtils.toString(stream,"UTF-8");
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return json;
	}

	/**
	 * Chuyển List thành  JSONArray
	 * @return
	 */
	public static JSONArray list2JSONArray(List<String> list){
		if(list==null || list.size()==0){
			return null;
		}
		JSONArray array = new JSONArray();
		for(String str: list){
			array.add(str);
		}
		return array;
	}

	/**
	 * Phán đoán các phần tử trong hai list có hoàn toàn giống nhau không
	 * QQYUN-5326【Luồng đơn giản】Lấy nhân sự tổ chức, đơn/Nhiều điều kiện lọc, không có lọc theo phòng ban
	 * @return
	 */
	public static boolean isEqList(List<String> list1, List<String> list2){
		if(list1.size() != list2.size()){
			return false;
		}
		for(String str1: list1){
			boolean flag = false;
			for(String str2: list2){
				if(str1.equals(str2)){
					flag = true;
					break;
				}
			}
			if(!flag){
				return false;
			}
		}
		return true;
	}


	/**
	 * Phán đoán phần tử trong sourceList có xuất hiện trong targetList không
	 * 
	 * QQYUN-5326【Luồng đơn giản】Lấy nhân sự tổ chức, đơn/Nhiều điều kiện lọc, không có lọc theo phòng ban
	 * @param sourceList Danh sách nguồn, danh sách phần tử cần kiểm tra
	 * @param targetList Danh sách đích, danh sách dùng để khớp
	 * @return Nếu có bất kỳ phần tử nào của sourceList tồn tại trong targetList thì trả về true, nếu không thì trả về false
	 */
	public static boolean isInList(List<String> sourceList, List<String> targetList){
		for(String sourceItem: sourceList){
			boolean flag = false;
			for(String targetItem: targetList){
				if(sourceItem.equals(targetItem)){
					flag = true;
					break;
				}
			}
			if(flag){
				return true;
			}
		}
		return false;
	}

	/**
	 * Phán đoán tất cả phần tử trong sourceList có đều tồn tại trong targetList không
	 * @param sourceList Danh sách nguồn, danh sách phần tử cần kiểm tra
	 * @param targetList Danh sách đích, danh sách dùng để khớp
	 * @return Nếu tất cả phần tử trong sourceList đều tồn tại trong targetList thì trả về true, nếu không thì trả về false
	 */
	public static boolean isAllInList(List<String> sourceList, List<String> targetList){
		if(sourceList == null || sourceList.isEmpty()){
			return true; // Danh sách rỗng được xem là tất cả phần tử đều tồn tại
		}
		if(targetList == null || targetList.isEmpty()){
			return false; // Khi danh sách đích rỗng và danh sách nguồn không rỗng thì trả về false
		}

		for(String sourceItem: sourceList){
			boolean found = false;
			for(String targetItem: targetList){
				if(sourceItem.equals(targetItem)){
					found = true;
					break;
				}
			}
			if(!found){
				return false; // Nếu có bất kỳ phần tử nào không nằm trong danh sách đích thì trả về false
			}
		}
		return true; // Tất cả phần tử đều đã tìm thấy
	}
	
	/**
	 * Tính kích thước tệp rồi chuyển thành MB
	 * @param uploadCount
	 * @return
	 */
	public static Double calculateFileSizeToMb(Long uploadCount){
		double count = 0.0;
		if(uploadCount>0) {
			BigDecimal bigDecimal = new BigDecimal(uploadCount);
			//Quy đổi thành MB
			BigDecimal divide = bigDecimal.divide(new BigDecimal(1048576));
			count = divide.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			return count;
		}
		return count;
	}

	/**
	 * map Chuyển str
	 *
	 * @param map
	 * @return
	 */
	public static String mapToString(Map<String, String[]> map) {
		if (map == null || map.size() == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			sb.append(key).append("=");
			sb.append(values != null ? StringUtils.join(values, ",") : "");
			sb.append("&");
		}

		String result = sb.toString();
		if (result.endsWith("&")) {
			result = result.substring(0, sb.length() - 1);
		}
		return result;
	}

	/**
	 * Phán đoán đối tượng có rỗng không <br/>
	 * Hỗ trợ các đối tượng thuộc nhiều loại khác nhau
	 * for for [QQYUN-10990]AIRAG
	 * @param obj
	 * @return
	 * @author chenrui
	 * @date 2025/2/13 18:34
	 */
	public static boolean isObjectEmpty(Object obj) {
		if (null == obj) {
			return true;
		}

		if (obj instanceof CharSequence) {
			return isEmpty(obj);
		} else if (obj instanceof Map) {
			return ((Map<?, ?>) obj).isEmpty();
		} else if (obj instanceof Iterable) {
			return isObjectEmpty(((Iterable<?>) obj).iterator());
		} else if (obj instanceof Iterator) {
			return !((Iterator<?>) obj).hasNext();
		} else if (isArray(obj)) {
			return 0 == Array.getLength(obj);
		}
		return false;
	}

	/**
	 * iterator Có trống hay không
	 * for for [QQYUN-10990]AIRAG
	 * @param iterator Iterator Đối tượng
	 * @return Có trống hay không
	 */
	public static boolean isEmptyIterator(Iterator<?> iterator) {
		return null == iterator || false == iterator.hasNext();
	}


	/**
	 * Phán đoán đối tượng có không rỗng không
	 * for for [QQYUN-10990]AIRAG
	 * @param object
	 * @return
	 * @author chenrui
	 * @date 2025/2/13 18:35
	 */
	public static boolean isObjectNotEmpty(Object object) {
		return !isObjectEmpty(object);
	}

	/**
	 * Nếu src lớn hơn des thì trả về true
	 * for [QQYUN-10990]AIRAG
	 * @param src
	 * @param des
	 * @return
	 * @author: chenrui
	 * @date: 2018/9/19 15:30
	 */
	public static boolean isGt(Number src, Number des) {
		if (null == src || null == des) {
			throw new IllegalArgumentException("Tham số không được để trống");
		}
		if (src.doubleValue() > des.doubleValue()) {
			return true;
		}
		return false;
	}

	/**
	 * Nếu src lớn hơn hoặc bằng des thì trả về true
	 * for [QQYUN-10990]AIRAG
	 * @param src
	 * @param des
	 * @return
	 * @author: chenrui
	 * @date: 2018/9/19 15:30
	 */
	public static boolean isGe(Number src, Number des) {
		if (null == src || null == des) {
			throw new IllegalArgumentException("Tham số không được để trống");
		}
		if (src.doubleValue() < des.doubleValue()) {
			return false;
		}
		return true;
	}


	/**
	 * Phán đoán có tồn tại không
	 * for [QQYUN-10990]AIRAG
	 * @param obj
	 * @param objs
	 * @param <T>
	 * @return
	 * @author chenrui
	 * @date 2020/9/12 15:50
	 */
	public static <T> boolean isIn(T obj, T... objs) {
		if (isEmpty(objs)) {
			return false;
		}
		for (T obj1 : objs) {
			if (isEqual(obj, obj1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Phán đoán ID tenant có hợp lệ không
	 * @param tenantId
	 * @return
	 */
	public static boolean isEffectiveTenant(String tenantId) {
		return MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && isNotEmpty(tenantId) && !("0").equals(tenantId);
	}

    /**
     * Sao chép các thuộc tính không rỗng của đối tượng nguồn sang đối tượng đích (thuộc tính cùng tên)
     * 
     * @param source Đối tượng nguồn (trang)
     * @param target Đối tượng đích (thực thể cơ sở dữ liệu)
     */
    public static void copyNonNullFields(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        // Lấy mảng tên thuộc tính không rỗng của đối tượng nguồn
        String[] nullPropertyNames = getNullPropertyNames(source);
        // Sao chép: bỏ qua các thuộc tính rỗng của đối tượng nguồn, chỉ ghi đè các thuộc tính không rỗng tương ứng của đối tượng đích
        BeanUtils.copyProperties(source, target, nullPropertyNames);
    }

    /**
     * Lấy mảng tên thuộc tính có giá trị null trong đối tượng nguồn
     * 
     * @param source 
     */
    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        //Lấy thuộc tính của lớp
        PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
        // Lọc ra tên các thuộc tính có giá trị null
        return Stream.of(propertyDescriptors)
                .map(PropertyDescriptor::getName)
                .filter(name -> beanWrapper.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    /**
     * String Chuyển đổi sang kiểu long
     *
     * @param v
     * @param def
     * @return
     */
    public static long getLong(Object v, long def) {
        if (v == null) {
            return def;
        };
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return def;
        }
    }
	
}
