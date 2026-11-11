package org.jeecg.codegenerate;

import java.util.ArrayList;
import java.util.List;

import org.jeecgframework.codegenerate.generate.impl.CodeGenerateOneToMany;
import org.jeecgframework.codegenerate.generate.pojo.onetomany.MainTableVo;
import org.jeecgframework.codegenerate.generate.pojo.onetomany.SubTableVo;

/**
 * Lối vào trình sinh mã [Một-nhiều]
 * 
 * [Chế độ GUI có chức năng yếu hơn, vui lòng ưu tiên sử dụng sinh mã Online]
 * @Author 张代浩
 * @site www.jeecg.com
 * 
 */
public class JeecgOneToMainUtil {

	/**
	 * Một-nhiều (Bảng cha-con) Mô hình dữ liệu, phương thức sinh
	 * @param args
	 */
	public static void main(String[] args) {
		//Bước 1: Đặt cấu hình bảng chính
		MainTableVo mainTable = new MainTableVo();
        //Tên bảng
		mainTable.setTableName("jeecg_order_main");
        //Tên thực thể
		mainTable.setEntityName("GuiTestOrderMain");
        //Tên package
		mainTable.setEntityPackage("gui");
        //Mô tả
		mainTable.setFtlDescription("GUI Quản lý đơn hàng");
		
		//Bước 2: Đặt cấu hình tập hợp bảng con
		List<SubTableVo> subTables = new ArrayList<SubTableVo>();
		//[1].Bảng con 1
		SubTableVo po = new SubTableVo();
        //Tên bảng
		po.setTableName("jeecg_order_customer");
        //Tên thực thể
		po.setEntityName("GuiTestOrderCustom");
        //Tên package
		po.setEntityPackage("gui");
        //Mô tả
		po.setFtlDescription("Chi tiết khách hàng");
		//Cấu hình tham số khóa ngoại của bảng con
		/*Giải thích: 
		 * a) Bảng con tham chiếu ID khóa chính của bảng chính làm khóa ngoại, trường khóa ngoại bắt buộc phải kết thúc bằng _ID;
		 * b) Tên trường khóa ngoại của bảng chính và bảng con phải giống nhau (trừ ID khóa chính);
		 * c) Nhiều trường khóa ngoại thì dùng dấu phẩy để phân tách;
		*/
		po.setForeignKeys(new String[]{"order_id"});
		subTables.add(po);
		//[2].Bảng con 2
		SubTableVo po2 = new SubTableVo();
        //Tên bảng
		po2.setTableName("jeecg_order_ticket");
        //Tên thực thể
		po2.setEntityName("GuiTestOrderTicket");
        //Tên package
		po2.setEntityPackage("gui");
        //Mô tả
		po2.setFtlDescription("Chi tiết sản phẩm");
		//Cấu hình tham số khóa ngoại của bảng con
		/*Giải thích: 
		 * a) Bảng con tham chiếu ID khóa chính của bảng chính làm khóa ngoại, trường khóa ngoại bắt buộc phải kết thúc bằng _ID;
		 * b) Tên trường khóa ngoại của bảng chính và bảng con phải giống nhau (trừ ID khóa chính);
		 * c) Nhiều trường khóa ngoại thì dùng dấu phẩy để phân tách;
		*/
		po2.setForeignKeys(new String[]{"order_id"});
		subTables.add(po2);
		mainTable.setSubTables(subTables);
		
		//Bước 3: Một-nhiều (Bảng cha-con) Mô hình dữ liệu, Tạo mã
		try {
			new CodeGenerateOneToMany(mainTable,subTables).generateCodeFile(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
