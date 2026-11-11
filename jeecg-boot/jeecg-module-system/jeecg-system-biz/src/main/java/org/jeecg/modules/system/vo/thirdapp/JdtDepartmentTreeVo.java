package org.jeecg.modules.system.vo.thirdapp;

import com.jeecg.dingtalk.api.department.vo.Department;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Phòng ban cấu trúc cây DingTalk
 *
 * @author sunjianlei
 */
public class JdtDepartmentTreeVo extends Department {

    private List<JdtDepartmentTreeVo> children;

    public List<JdtDepartmentTreeVo> getChildren() {
        return children;
    }

    public JdtDepartmentTreeVo setChildren(List<JdtDepartmentTreeVo> children) {
        this.children = children;
        return this;
    }

    public JdtDepartmentTreeVo(Department department) {
        BeanUtils.copyProperties(department, this);
    }

    /**
     * Có mục con không
     */
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public String toString() {
        return "JwDepartmentTree{" +
                "children=" + children +
                "} " + super.toString();
    }

    /**
     * Phương thức hỗ trợ tĩnh, chuyển list thành cấu trúc tree
     */
    public static List<JdtDepartmentTreeVo> listToTree(List<Department> allDepartment) {
        // Trước tiên, tìm tất cả các cấp cha
        List<JdtDepartmentTreeVo> treeList = getByParentId(1, allDepartment);
        Optional<Department> departmentOptional = allDepartment.stream().filter(item -> item.getParent_id() == null).findAny();
        Department department = new Department();
        //Kiểm tra xem có tìm thấy dữ liệu không
        if(departmentOptional.isPresent()){
            department = departmentOptional.get();
        }
        getChildrenRecursion(treeList, allDepartment);
        // Giải thích logic mã: 【issues/6017] Khi DingTalk đồng bộ phòng ban thì không có tên phòng ban cấp cao nhất; khi đồng bộ người dùng, người dùng không có thông tin phòng ban---
        JdtDepartmentTreeVo treeVo = new JdtDepartmentTreeVo(department);
        treeVo.setChildren(treeList);
        List<JdtDepartmentTreeVo> list = new ArrayList<>();
        list.add(treeVo);
        return list;
    }

    private static List<JdtDepartmentTreeVo> getByParentId(Integer parentId, List<Department> allDepartment) {
        List<JdtDepartmentTreeVo> list = new ArrayList<>();
        for (Department department : allDepartment) {
            if (parentId.equals(department.getParent_id())) {
                list.add(new JdtDepartmentTreeVo(department));
            }
        }
        return list;
    }

    private static void getChildrenRecursion(List<JdtDepartmentTreeVo> treeList, List<Department> allDepartment) {
        for (JdtDepartmentTreeVo departmentTree : treeList) {
            // Tìm đệ quy cấp con
            List<JdtDepartmentTreeVo> children = getByParentId(departmentTree.getDept_id(), allDepartment);
            if (children.size() > 0) {
                departmentTree.setChildren(children);
                getChildrenRecursion(children, allDepartment);
            }
        }
    }

}
