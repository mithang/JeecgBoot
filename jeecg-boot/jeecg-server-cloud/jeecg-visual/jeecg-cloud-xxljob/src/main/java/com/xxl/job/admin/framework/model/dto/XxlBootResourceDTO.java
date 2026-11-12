package com.xxl.job.admin.framework.model.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *  XxlBootResource DTO
 *
 *  Created by xuxueli on 2024-08-04
 */
public class XxlBootResourceDTO implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * ID tài nguyên
     */
    private int id;

    /**
     * ID nút cha
     */
    private int parentId;

    /**
     * Tên
     */
    private String name;

    /**
     * Loại: 0-Thư mục, 1-Menu, 2-Nút bấm
     */
    private int type;

    /**
     * Định danh quyền
     */
    private String permission;

    /**
     * Địa chỉ menu
     */
    private String url;

    /**
     * ICON
     */
    private String icon;

    /**
     * Thứ tự
     */
    private int order;

    /**
     * Trạng thái: 0-Bình thường, 1-Vô hiệu hóa
     */
    private int status;

    /**
     * Thời gian thêm mới
     */
    private Date addTime;

    /**
     * Thời gian cập nhật
     */
    private Date updateTime;

    /**
     * child data
     */
    private List<XxlBootResourceDTO> children;

    public XxlBootResourceDTO() {
    }
    public XxlBootResourceDTO(int id, int parentId, String name, int type, String permission, String url, String icon, int order, int status, List<XxlBootResourceDTO> children) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.permission = permission;
        this.url = url;
        this.icon = icon;
        this.order = order;
        this.status = status;
        this.children = children;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<XxlBootResourceDTO> getChildren() {
        return children;
    }

    public void setChildren(List<XxlBootResourceDTO> children) {
        this.children = children;
    }
}
