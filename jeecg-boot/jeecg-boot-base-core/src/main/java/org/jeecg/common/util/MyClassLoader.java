package org.jeecg.common.util;

import org.jeecg.common.constant.SymbolConstant;

/**
 * @Author  张代浩
 */
public class MyClassLoader extends ClassLoader {
	public static Class getClassByScn(String className) {
		Class myclass = null;
		try {
			//update-begin---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
			myclass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
			//update-end---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(className+" not found!");
		}
		return myclass;
	}

    /**
     * Lấy tên đầy đủ của lớp, bao gồm tên gói
     * @param object
     * @return
     */
	public static String getPackPath(Object object) {
		// Kiểm tra xem tham số người dùng truyền vào có rỗng không
		if (object == null) {
			throw new java.lang.IllegalArgumentException("Tham số không được rỗng!");
		}
		// Lấy tên đầy đủ của lớp, bao gồm tên gói
		String clsName = object.getClass().getName();
		return clsName;
	}

	public static String getAppPath(Class cls) {
		// Kiểm tra xem tham số người dùng truyền vào có rỗng không
		if (cls == null) {
			throw new java.lang.IllegalArgumentException("Tham số không được rỗng!");
		}
		ClassLoader loader = cls.getClassLoader();
		// Lấy tên đầy đủ của lớp, bao gồm tên gói
		String clsName = cls.getName() + ".class";
		// Lấy gói chứa tham số truyền vào
		Package pack = cls.getPackage();
		String path = "";
		// Nếu không phải gói ẩn danh, chuyển tên gói thành đường dẫn
		if (pack != null) {
			String packName = pack.getName();
			String javaSpot="java.";
			String javaxSpot="javax.";
			// Tại đây chỉ đơn giản xác định có phải là thư viện cơ sở Java không, để ngăn người dùng truyền vào thư viện tích hợp sẵn của JDK
			if (packName.startsWith(javaSpot) || packName.startsWith(javaxSpot)) {
				throw new java.lang.IllegalArgumentException("Đừng truyền lớp hệ thống!");
			}
			// Trong tên lớp, loại bỏ phần tên gói để lấy tên tệp của lớp
			clsName = clsName.substring(packName.length() + 1);
			// Xác định xem tên gói có phải là tên gói đơn giản hay không, nếu đúng thì trực tiếp chuyển đổi tên gói thành đường dẫn,
			if (packName.indexOf(SymbolConstant.SPOT) < 0) {
				path = packName + "/";
			} else {
                // Ngược lại, chuyển đổi tên gói thành đường dẫn theo từng thành phần của tên gói
				int start = 0, end = 0;
				end = packName.indexOf(".");
				StringBuilder pathBuilder = new StringBuilder();
				while (end != -1) {
                    pathBuilder.append(packName, start, end).append("/");
					start = end + 1;
					end = packName.indexOf(".", start);
				}
				if(oConvertUtils.isNotEmpty(pathBuilder.toString())){
                    path = pathBuilder.toString();
                }
				path = path + packName.substring(start) + "/";
			}
		}
		// Gọi phương thức getResource của ClassLoader, truyền vào tên tệp lớp chứa thông tin đường dẫn
		java.net.URL url = loader.getResource(path + clsName);
		// Lấy thông tin đường dẫn từ đối tượng URL
		String realPath = url.getPath();
		// Loại bỏ tên giao thức trong thông tin đường dẫn"file:"
		int pos = realPath.indexOf("file:");
		if (pos > -1) {
			realPath = realPath.substring(pos + 5);
		}
		// Loại bỏ phần cuối của thông tin đường dẫn chứa thông tin tệp lớp để lấy đường dẫn nơi lớp tọa lạc
		pos = realPath.indexOf(path + clsName);
		realPath = realPath.substring(0, pos - 1);
		// Nếu tệp lớp được đóng gói vào tệp JAR hoặc tương tự, loại bỏ tên tệp đóng gói JAR tương ứng
		if (realPath.endsWith(SymbolConstant.EXCLAMATORY_MARK)) {
			realPath = realPath.substring(0, realPath.lastIndexOf("/"));
		}
		/*------------------------------------------------------------  
		 ClassLoader phương thức getResource đã sử dụng utf-8 đã mã hóa thông tin đường dẫn, khi đường dẫn  
		  chứa ký tự tiếng Trung và khoảng trắng, nó sẽ chuyển đổi các ký tự này, do đó kết quả thu được thường không phải là thứ chúng ta muốn  
		  đường dẫn thực tế, tại đây đã gọi phương thức decode của URLDecoder để giải mã nhằm thu được  
		  Đường dẫn chứa tiếng Trung và khoảng trắng  
		-------------------------------------------------------------*/
		try {
			realPath = java.net.URLDecoder.decode(realPath, "utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return realPath;
	}// getAppPath Kết thúc định nghĩa
}
