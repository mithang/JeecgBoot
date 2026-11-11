//package org.jeecg.modules.ngalain.service.impl;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.common.constant.SymbolConstant;
//import org.jeecg.common.util.oConvertUtils;
//import org.jeecg.modules.ngalain.service.NgAlainService;
//import org.jeecg.modules.system.entity.SysPermission;
//import org.jeecg.modules.system.mapper.SysDictMapper;
//import org.jeecg.modules.system.service.ISysPermissionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//
///**
// * @Description: NgAlainServiceImpl Lớp triển khai
// * @author: jeecg-boot
// */
//@Service("ngAlainService")
//public class NgAlainServiceImpl implements NgAlainService {
//    @Autowired
//    private ISysPermissionService sysPermissionService;
//    @Autowired
//    private SysDictMapper mapper;
//    @Override
//    public JSONArray getMenu(String id) throws Exception {
//        return getJeecgMenu(id);
//    }
//    @Override
//    public JSONArray getJeecgMenu(String id) throws Exception {
//        List<SysPermission> metaList = sysPermissionService.queryByUser(id);
//        JSONArray jsonArray = new JSONArray();
//        getPermissionJsonArray(jsonArray, metaList, null);
//        JSONArray menulist= parseNgAlain(jsonArray);
//        JSONObject jeecgMenu = new JSONObject();
//        jeecgMenu.put("text", "jeecg Menu");
//        jeecgMenu.put("group",true);
//        jeecgMenu.put("children", menulist);
//        JSONArray jeecgMenuList=new JSONArray();
//        jeecgMenuList.add(jeecgMenu);
//        return jeecgMenuList;
//    }
//
//    @Override
//    public List<Map<String, String>> getDictByTable(String table, String key, String value) {
//        return this.mapper.getDictByTableNgAlain(table,key,value);
//    }
//
//    private JSONArray parseNgAlain(JSONArray jsonArray) {
//        JSONArray menulist=new JSONArray();
//        for (Object object : jsonArray) {
//            JSONObject jsonObject= (JSONObject) object;
//            String path= (String) jsonObject.get("path");
//            JSONObject meta= (JSONObject) jsonObject.get("meta");
//            JSONObject menu=new JSONObject();
//            menu.put("text",meta.get("title"));
//            menu.put("reuse",true);
//            if (jsonObject.get("children")!=null){
//                JSONArray child=  parseNgAlain((JSONArray) jsonObject.get("children"));
//                menu.put("children",child);
//                JSONObject icon=new JSONObject();
//                icon.put("type", "icon");
//                icon.put("value", meta.get("icon"));
//                menu.put("icon",icon);
//            }else {
//                menu.put("link",path);
//            }
//            menulist.add(menu);
//        }
//        return menulist;
//    }
//
//    /**
//     *  Lấy mảng JSON menu
//     * @param jsonArray
//     * @param metaList
//     * @param parentJson
//     */
//    private void getPermissionJsonArray(JSONArray jsonArray,List<SysPermission> metaList,JSONObject parentJson) {
//        for (SysPermission permission : metaList) {
//            if(permission.getMenuType()==null) {
//                continue;
//            }
//            String tempPid = permission.getParentId();
//            JSONObject json = getPermissionJsonObject(permission);
//            if(parentJson==null && oConvertUtils.isEmpty(tempPid)) {
//                jsonArray.add(json);
//                if(!permission.isLeaf()) {
//                    getPermissionJsonArray(jsonArray, metaList, json);
//                }
//            }else if(parentJson!=null && oConvertUtils.isNotEmpty(tempPid) && tempPid.equals(parentJson.getString("id"))){
//                if(permission.getMenuType()==0) {
//                    JSONObject metaJson = parentJson.getJSONObject("meta");
//                    if(metaJson.containsKey("permissionList")) {
//                        metaJson.getJSONArray("permissionList").add(json);
//                    }else {
//                        JSONArray permissionList = new JSONArray();
//                        permissionList.add(json);
//                        metaJson.put("permissionList", permissionList);
//                    }
//
//                }else if(permission.getMenuType()==1) {
//                    if(parentJson.containsKey("children")) {
//                        parentJson.getJSONArray("children").add(json);
//                    }else {
//                        JSONArray children = new JSONArray();
//                        children.add(json);
//                        parentJson.put("children", children);
//                    }
//
//                    if(!permission.isLeaf()) {
//                        getPermissionJsonArray(jsonArray, metaList, json);
//                    }
//                }
//            }
//
//
//        }
//    }
//    private JSONObject getPermissionJsonObject(SysPermission permission) {
//        JSONObject json = new JSONObject();
//        //Loại(0: menu cấp một 1: menu con 2: nút)
//        if(CommonConstant.MENU_TYPE_2.equals(permission.getMenuType())) {
//            json.put("action", permission.getPerms());
//            json.put("describe", permission.getName());
//        }else if(CommonConstant.MENU_TYPE_0.equals(permission.getMenuType()) || CommonConstant.MENU_TYPE_1.equals(permission.getMenuType())) {
//            json.put("id", permission.getId());
//            boolean flag = permission.getUrl()!=null&&(permission.getUrl().startsWith(CommonConstant.HTTP_PROTOCOL)||permission.getUrl().startsWith(CommonConstant.HTTPS_PROTOCOL));
//            if(flag) {
//                String url= new String(Base64.getUrlEncoder().encode(permission.getUrl().getBytes()));
//                json.put("path", "/sys/link/" +url.replaceAll("=",""));
//            }else {
//                json.put("path", permission.getUrl());
//            }
//
//            //Quy tắc quan trọng: tên route name (Tạo route qua URL name,Tên route để frontend phát triển, dùng khi chuyển trang)
//            json.put("name", urlToRouteName(permission.getUrl()));
//
//            //Có ẩn route hay không, mặc định đều hiển thị
//            if(permission.isHidden()) {
//                json.put("hidden",true);
//            }
//            //Route tổng hợp
//            if(permission.isAlwaysShow()) {
//                json.put("alwaysShow",true);
//            }
//            json.put("component", permission.getComponent());
//            JSONObject meta = new JSONObject();
//            meta.put("title", permission.getName());
//            if(oConvertUtils.isEmpty(permission.getParentId())) {
//                //Địa chỉ chuyển hướng menu cấp một
//                json.put("redirect",permission.getRedirect());
//                meta.put("icon", oConvertUtils.getString(permission.getIcon(), ""));
//            }else {
//                meta.put("icon", oConvertUtils.getString(permission.getIcon(), ""));
//            }
//            if(flag) {
//                meta.put("url", permission.getUrl());
//            }
//            json.put("meta", meta);
//        }
//
//        return json;
//    }
//    /**
//     * Tạo tên route qua URL (bỏ dấu gạch chéo tiền tố của URL, thay dấu gạch chéo trong nội dung ‘/’ thành-）
//     * Ví dụ: URL = /isystem/role
//     *     RouteName = isystem-role
//     * @return
//     */
//    private String urlToRouteName(String url) {
//        if(oConvertUtils.isNotEmpty(url)) {
//            if(url.startsWith(SymbolConstant.SINGLE_SLASH)) {
//                url = url.substring(1);
//            }
//            url = url.replace("/", "-");
//            return url;
//        }else {
//            return null;
//        }
//    }
//}
