package org.jeecg.common.util;

import io.netty.util.internal.StringUtil;

/**
 * Quy tắc tạo số thứ tự(Tăng dần theo quy tắc mặc định, số bắt đầu từ 1-99 bắt đầu tăng dần, khi số đến 99 thì tăng dần chữ cái;Không đủ số chữ số thì tăng thêm số chữ số)
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
public class YouBianCodeUtil {

	// Số chữ số(Mặc định tạo số có 3 chữ số)

    /**Đại diện cho số chữ số*/
	private static final int NUM_LENGTH = 2;

	public static final int ZHANWEI_LENGTH = 1+ NUM_LENGTH;

	public static final char LETTER= 'Z';

	/**
	 * Dựa trên code trước đó, lấy code tiếp theo cùng cấp code
	 * Ví dụ:Hiện tại code lớn nhất là D01A04, code tiếp theo là:D01A05
	 * 
	 * @param code
	 * @return
	 */
	public static synchronized String getNextYouBianCode(String code) {
		String newcode = "";
		if (oConvertUtils.isEmpty(code)) {
			String zimu = "A";
			String num = getStrNum(1);
			newcode = zimu + num;
		} else {
			String beforeCode = code.substring(0, code.length() - 1- NUM_LENGTH);
			String afterCode = code.substring(code.length() - 1 - NUM_LENGTH,code.length());
			char afterCodeZimu = afterCode.substring(0, 1).charAt(0);
			Integer afterCodeNum = Integer.parseInt(afterCode.substring(1));
//			org.jeecgframework.core.util.LogUtil.info(after_code);
//			org.jeecgframework.core.util.LogUtil.info(after_code_zimu);
//			org.jeecgframework.core.util.LogUtil.info(after_code_num);

			String nextNum = "";
			char nextZimu = 'A';
			// Kiểm tra trước số có bằng 999*, thì đếm lại từ 1 và tăng dần
			if (afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				nextNum = getNextStrNum(0);
			} else {
				nextNum = getNextStrNum(afterCodeNum);
			}
			// Kiểm tra trước số có bằng 999*, thì chữ cái bắt đầu lại từ A,Tăng dần
			if(afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				nextZimu = getNextZiMu(afterCodeZimu);
			}else{
				nextZimu = afterCodeZimu;
			}

			// Ví dụ Z99, code tiếp theo là Z99A01
			if (LETTER == afterCodeZimu && getMaxNumByLength(NUM_LENGTH) == afterCodeNum) {
				newcode = code + (nextZimu + nextNum);
			} else {
				newcode = beforeCode + (nextZimu + nextNum);
			}
		}
		return newcode;

	}

	/**
	 * Dựa trên code của cha code,Lấy code tiếp theo của cấp dưới code
	 * 
	 * Ví dụ: code của cha CODE:A01
	 *       Hiện tại CODE:A01B03
	 *       Đã lấy được code:A01B04
	 *       
	 * @param parentCode   Cấp trên code
	 * @param localCode    Cùng cấp code
	 * @return
	 */
	public static synchronized String getSubYouBianCode(String parentCode,String localCode) {
		if(localCode!=null && localCode!=""){

//			return parentCode + getNextYouBianCode(localCode);
			return getNextYouBianCode(localCode);

		}else{
			parentCode = parentCode + "A"+ getNextStrNum(0);
		}
		return parentCode;
	}

	

	/**
	 * Đệm số 0 vào các vị trí phía trước của số
	 * 
	 * @param num
	 * @return
	 */
	private static String getNextStrNum(int num) {
		return getStrNum(getNextNum(num));
	}

	/**
	 * Đệm số 0 vào các vị trí phía trước của số
	 * 
	 * @param num
	 * @return
	 */
	private static String getStrNum(int num) {
		String s = String.format("%0" + NUM_LENGTH + "d", num);
		return s;
	}

	/**
	 * Tăng dần để lấy số tiếp theo
	 * 
	 * @param num
	 * @return
	 */
	private static int getNextNum(int num) {
		num++;
		return num;
	}

	/**
	 * Tăng dần để lấy chữ cái tiếp theo
	 * 
	 * @param num
	 * @return
	 */
	private static char getNextZiMu(char zimu) {
		if (zimu == LETTER) {
			return 'A';
		}
		zimu++;
		return zimu;
	}
	
	/**
	 * Lấy giá trị lớn nhất dựa trên số chữ số
	 * @param length
	 * @return
	 */
	private static int getMaxNumByLength(int length){
		if(length==0){
			return 0;
		}
        StringBuilder maxNum = new StringBuilder();
		for (int i=0;i<length;i++){
            maxNum.append("9");
		}
		return Integer.parseInt(maxNum.toString());
	}
	public static String[] cutYouBianCode(String code){
		if(code==null || StringUtil.isNullOrEmpty(code)){
			return null;
		}else{
			//Lấy độ dài chuẩn là numLength+1,Số lượng cắt lấy là code.length/numLength+1
			int c = code.length()/(NUM_LENGTH +1);
			String[] cutcode = new String[c];
			for(int i =0 ; i <c;i++){
				cutcode[i] = code.substring(0,(i+1)*(NUM_LENGTH +1));
			}
			return cutcode;
		}
		
	}
//	public static void main(String[] args) {
//		// org.jeecgframework.core.util.LogUtil.info(getNextZiMu('C'));
//		// org.jeecgframework.core.util.LogUtil.info(getNextNum(8));
//	    // org.jeecgframework.core.util.LogUtil.info(cutYouBianCode("C99A01B01")[2]);
//	}
}
