package org.jeecg.modules.system.vo.thirdapp;

import com.jeecg.qywx.api.department.vo.Department;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Phòng ban có cấu trúc cây của WeChat Doanh nghiệp
 *
 * @author sunjianlei
 */
public class JwDepartmentTreeVo extends Department {

    private List<JwDepartmentTreeVo> children;

    public List<JwDepartmentTreeVo> getChildren() {
        return children;
    }

    public JwDepartmentTreeVo setChildren(List<JwDepartmentTreeVo> children) {
        this.children = children;
        return this;
    }

    public JwDepartmentTreeVo(Department department) {
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
    public static List<JwDepartmentTreeVo> listToTree(List<Department> allDepartment) {
        // Trước tiên, tìm tất cả các cấp cha
        List<JwDepartmentTreeVo> treeList = getByParentId("1", allDepartment);
        Optional<Department> departmentOptional = allDepartment.stream().filter(item -> "0".equals(item.getParentid())).findAny();
        Department department = new Department();
        //Kiểm tra xem có tìm thấy dữ liệu không
        if(departmentOptional.isPresent()){
            department = departmentOptional.get();
        }
        getChildrenRecursion(treeList, allDepartment);
        // Giải thích logic mã: 【issues/6017] Khi WeChat Doanh nghiệp đồng bộ phòng ban thì không có tên phòng ban cấp cao nhất; khi đồng bộ người dùng, người dùng không có thông tin phòng ban---
        JwDepartmentTreeVo treeVo = new JwDepartmentTreeVo(department);
        treeVo.setChildren(treeList);
        List<JwDepartmentTreeVo> list = new ArrayList<>();
        list.add(treeVo);
        return list;
    }

    private static List<JwDepartmentTreeVo> getByParentId(String parentId, List<Department> allDepartment) {
        List<JwDepartmentTreeVo> list = new ArrayList<>();
        for (Department department : allDepartment) {
            if (parentId.equals(department.getParentid())) {
                list.add(new JwDepartmentTreeVo(department));
            }
        }
        return list;
    }

    private static void getChildrenRecursion(List<JwDepartmentTreeVo> treeList, List<Department> allDepartment) {
        for (JwDepartmentTreeVo departmentTree : treeList) {
            // Tìm đệ quy cấp con
            List<JwDepartmentTreeVo> children = getByParentId(departmentTree.getId(), allDepartment);
            if (children.size() > 0) {
                departmentTree.setChildren(children);
                getChildrenRecursion(children, allDepartment);
            }
        }
    }

}
