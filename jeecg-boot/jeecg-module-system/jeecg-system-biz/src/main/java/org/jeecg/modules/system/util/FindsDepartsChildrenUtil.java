package org.jeecg.modules.system.util;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * <P>
 * Bảng của phòng ban tương ứng,Xử lý và tìm dữ liệu dạng cây
 * <P>
 * 
 * @Author: Steve
 * @Date: 2019-01-22
 */
public class FindsDepartsChildrenUtil {

	//Thông tin cây phòng ban-Cấu trúc cây
	//private static List<SysDepartTreeModel> sysDepartTreeList = new ArrayList<SysDepartTreeModel>();
	
	//Cây phòng ban id-Cấu trúc cây
    //private static List<DepartIdModel> idList = new ArrayList<>();


    /**
     * queryTreeList (phương thức con) ====1=====
     * Phương thức này chuyển tập hợp list kiểu SysDepart thành tập hợp kiểu SysDepartTreeModel
     */
    public static List<SysDepartTreeModel> wrapTreeDataToTreeList(List<SysDepart> recordList) {
        // Mỗi lần yêu cầu phương thức này,Mỗi lần phương thức này được yêu cầu một lần, đều phải dọn dẹp tập hợp list toàn cục một lần
        //idList.clear();
    	List<DepartIdModel> idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> records = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            SysDepart depart = recordList.get(i);
            records.add(new SysDepartTreeModel(depart));
        }
        List<SysDepartTreeModel> tree = findChildren(records, idList);
        setEmptyChildrenAsNull(tree);
        return tree;
    }

    /**
     * Lấy DepartIdModel
     * @param recordList
     * @return
     */
    public static List<DepartIdModel> wrapTreeDataToDepartIdTreeList(List<SysDepart> recordList) {
        // Mỗi lần yêu cầu phương thức này,Mỗi lần phương thức này được yêu cầu một lần, đều phải dọn dẹp tập hợp list toàn cục một lần
        //idList.clear();
        List<DepartIdModel> idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> records = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            SysDepart depart = recordList.get(i);
            records.add(new SysDepartTreeModel(depart));
        }
        findChildren(records, idList);
        return idList;
    }

    /**
     * queryTreeList (phương thức con) ====2=====
     * Phương thức này tìm và đóng gói nút của lớp cha cấp cao nhất vào tập hợp TreeList
     */
    private static List<SysDepartTreeModel> findChildren(List<SysDepartTreeModel> recordList,
                                                         List<DepartIdModel> departIdList) {

        List<SysDepartTreeModel> treeList = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            SysDepartTreeModel branch = recordList.get(i);
            if (oConvertUtils.isEmpty(branch.getParentId())) {
                treeList.add(branch);
                DepartIdModel departIdModel = new DepartIdModel().convert(branch);
                departIdList.add(departIdModel);
            }
        }
        getGrandChildren(treeList,recordList,departIdList);
        
        //idList = departIdList;
        return treeList;
    }

    /**
     * queryTreeList (phương thức con)====3====
     *Phương thức này tìm tất cả các node con thuộc lớp cha cấp cao nhất và đóng gói vào tập hợp TreeList
     */
    private static void getGrandChildren(List<SysDepartTreeModel> treeList,List<SysDepartTreeModel> recordList,List<DepartIdModel> idList) {

        for (int i = 0; i < treeList.size(); i++) {
            SysDepartTreeModel model = treeList.get(i);
            DepartIdModel idModel = idList.get(i);
            for (int i1 = 0; i1 < recordList.size(); i1++) {
                SysDepartTreeModel m = recordList.get(i1);
                if (m.getParentId()!=null && m.getParentId().equals(model.getId())) {
                    model.getChildren().add(m);
                    DepartIdModel dim = new DepartIdModel().convert(m);
                    idModel.getChildren().add(dim);
                }
            }
            getGrandChildren(treeList.get(i).getChildren(), recordList, idList.get(i).getChildren());
        }

    }
    

    /**
     * queryTreeList (phương thức con) ====4====
     * Phương thức này đặt tập hợp List có nút con rỗng thành giá trị Null
     */
    private static void setEmptyChildrenAsNull(List<SysDepartTreeModel> treeList) {

        for (int i = 0; i < treeList.size(); i++) {
            SysDepartTreeModel model = treeList.get(i);
            if (model.getChildren().size() == 0) {
                model.setChildren(null);
                model.setIsLeaf(true);
            }else{
                setEmptyChildrenAsNull(model.getChildren());
                model.setIsLeaf(false);
            }
        }
        // sysDepartTreeList = treeList;
    }
}
