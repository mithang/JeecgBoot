package org.jeecg.common.constant;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: Tỉnh/Thành/Quận huyện
 * @author: jeecg-boot
 */
@Component("pca")
public class ProvinceCityArea {
    // Sử dụng CopyOnWriteArrayList + volatile Giải quyết vấn đề đồng thời trong tình huống lazy loading add / do việc duyệt gây ra ConcurrentModificationException
    // Danh sách sau khi initAreaList hoàn thành sẽ không còn bị sửa đổi nữa,Iterator là dạng snapshot,Đường đọc không cần khóa
    private volatile List<Area> areaList;

    public String getText(String code){
        if(StringUtils.isNotBlank(code)){
            this.initAreaList();
            // Sửa logic bug:Ban đầu || viết sai,areaList==null khi đó sẽ trả về false trước rồi mới gọi size() ném ra NPE,đổi thành &&
            if(this.areaList!=null && this.areaList.size()>0){
                List<String> ls = new ArrayList<String>();
                getAreaByCode(code,ls);
                return String.join("/",ls);
            }
        }
        return "";
    }

    public String getCode(String text){
        if(StringUtils.isNotBlank(text)){
            this.initAreaList();
            if(areaList!=null && areaList.size()>0){
                for(int i=areaList.size()-1;i>=0;i--){
                    // Giải thích logic mã: VUEN-1088 online Import Tỉnh/Thành/Quận huyện Import 后 Nhập dữ liệu 错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
                    String areaText = areaList.get(i).getText();
                    String cityText = areaList.get(i).getAheadText();
                    if(text.indexOf(areaText)>=0 && (cityText!=null && text.indexOf(cityText)>=0)){
                        return areaList.get(i).getId();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Lấy code tỉnh/thành/quận huyện, khớp chính xác
     * @param texts Mảng văn bản: tỉnh, thành phố, quận huyện
     * @return Trả về code của tỉnh/thành/quận huyện code
     */
    public String[] getCode(String[] texts) {
        if (texts == null || texts.length == 0) {
            return null;
        }
        this.initAreaList();
        if (areaList == null || areaList.size() == 0) {
            return null;
        }
        String[] codes = new String[texts.length];
        String code = null;
        for (int i = 0; i < texts.length; i++) {
            String text = texts[i];
            Area area;
            if (code == null) {
                area = getAreaByText(text);
            } else {
                area = getAreaByPidAndText(code, text);
            }
            if (area != null) {
                code = area.id;
                codes[i] = code;
            } else {
                return null;
            }
        }
        return codes;
    }

    /**
     * Lấy theo text area
     * @param text
     * @return
     */
    public Area getAreaByText(String text) {
        for (Area area : areaList) {
            if (text.equals(area.getText())) {
                return area;
            }
        }
        return null;
    }

    /**
     * Lấy đối tượng area qua pid
     * @param pCode Mã cấp cha
     * @param text
     * @return
     */
    public Area getAreaByPidAndText(String pCode, String text) {
        this.initAreaList();
        if (this.areaList != null && this.areaList.size() > 0) {
            for (Area area : this.areaList) {
                if (area.getPid().equals(pCode) && area.getText().equals(text)) {
                    return area;
                }
            }
        }
        return null;
    }

    public void getAreaByCode(String code,List<String> ls){
        for(Area area: areaList){
            if(null != area && area.getId().equals(code)){
                String pid = area.getPid();
                ls.add(0,area.getText());
                getAreaByCode(pid,ls);
            }
        }
    }

    private void initAreaList(){
        // Kiểm tra hai lần + volatile:Đảm bảo danh sách chỉ được khởi tạo bởi một luồng duy nhất,Tránh tình huống đồng thời trong lazy loading add / do việc duyệt gây ra ConcurrentModificationException
        if(this.areaList==null || this.areaList.isEmpty()){
            synchronized (this) {
                if(this.areaList==null || this.areaList.isEmpty()){
                    // CopyOnWriteArrayList  có iterator dạng snapshot,dù đồng thời với các luồng đọc khác cũng không ném ra CME
                    this.areaList = new CopyOnWriteArrayList<Area>();
                    try {
                        String jsonData = oConvertUtils.readStatic("classpath:static/pca.json");
                        JSONObject baseJson = JSONObject.parseObject(jsonData);
                        //Tầng thứ nhất: Tỉnh
                        JSONObject provinceJson = baseJson.getJSONObject("86");
                        for(String provinceKey: provinceJson.keySet()){
                            //System.out.println("===="+provinceKey);
                            Area province = new Area(provinceKey,provinceJson.getString(provinceKey),"86");
                            this.areaList.add(province);
                            //Tầng thứ hai: Thành phố
                            JSONObject cityJson = baseJson.getJSONObject(provinceKey);
                            for(String cityKey:cityJson.keySet()){
                                //System.out.println("-----"+cityKey);
                                Area city = new Area(cityKey,cityJson.getString(cityKey),provinceKey);
                                this.areaList.add(city);
                                //Tầng thứ ba: Quận huyện
                                JSONObject areaJson =  baseJson.getJSONObject(cityKey);
                                if(areaJson!=null){
                                    for(String areaKey:areaJson.keySet()){
                                        //System.out.println("········"+areaKey);
                                        Area area = new Area(areaKey,areaJson.getString(areaKey),cityKey);
                                        // Giải thích logic mã: VUEN-1088 online Import Tỉnh/Thành/Quận huyện Import 后 Nhập dữ liệu 错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
                                        area.setAheadText(cityJson.getString(cityKey));
                                        this.areaList.add(area);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private String jsonRead(File file){
        Scanner scanner = null;
        StringBuilder buffer = new StringBuilder();
        try {
            scanner = new Scanner(file, "utf-8");
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }
        } catch (Exception e) {

        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return buffer.toString();
    }

    class Area{
        String id;
        String text;
        String pid;
        // Dùng để lưu dữ liệu văn bản cấp trên; văn bản cấp trên của quận huyện là dữ liệu của thành phố
        String aheadText;

        public Area(String id,String text,String pid){
            this.id = id;
            this.text = text;
            this.pid = pid;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public String getPid() {
            return pid;
        }

        public String getAheadText() {
            return aheadText;
        }
        public void setAheadText(String aheadText) {
            this.aheadText = aheadText;
        }
    }
}
