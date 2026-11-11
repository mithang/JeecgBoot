package org.jeecg.common.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Một số phương thức về ElasticSearch (tạo chỉ mục, thêm dữ liệu, tìm kiếm, v.v.)
 *
 * @author sunjianlei
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "jeecg.elasticsearch", name = "cluster-nodes")
public class JeecgElasticsearchTemplate {
    /** es Địa chỉ dịch vụ */
    private String baseUrl;
    private final String FORMAT_JSON = "format=json";
    /** Elasticsearch Số phiên bản của */
    private String version = null;

    /**ElasticSearch Số mục tối đa có thể trả về*/
    public static final int ES_MAX_SIZE = 10000;

    /**es7*/
    public static final String IE_SEVEN = "7";

    /**url not found 404*/
    public static final String URL_NOT_FOUND = "404 Not Found";

    public JeecgElasticsearchTemplate(@Value("${jeecg.elasticsearch.cluster-nodes}") String baseUrl, @Value("${jeecg.elasticsearch.check-enabled}") boolean checkEnabled) {
        log.debug("JeecgElasticsearchTemplate BaseURL：" + baseUrl);
        if (StringUtils.isNotEmpty(baseUrl)) {
            this.baseUrl = baseUrl;
            // Kiểm tra xem địa chỉ ES đã cấu hình có hợp lệ không
            if (checkEnabled) {
                try {
                    this.getElasticsearchVersion();
                    log.info("ElasticSearch Kết nối dịch vụ thành công");
                    log.info("ElasticSearch version: " + this.version);
                } catch (Exception e) {
                    this.version = "";
                    log.warn("ElasticSearch Kết nối dịch vụ thất bại, nguyên nhân: cấu hình chưa đạt. Có thể BaseURL chưa được cấu hình hoặc cấu hình sai, cũng có thể dịch vụ Elasticsearch chưa khởi động. Tiếp theo sẽ từ chối thực thi mọi phương thức!");
                }
            }
        }
    }

    /**
     * Lấy thông tin số phiên bản của Elasticsearch, thất bại trả về null
     */
    private void getElasticsearchVersion() {
        if (this.version == null) {
            String url = this.getBaseUrl().toString();
            JSONObject result = RestUtil.get(url);
            if (result != null) {
                JSONObject v = result.getJSONObject("version");
                this.version = v.getString("number");
            }
        }
    }

    public StringBuilder getBaseUrl(String indexName, String typeName) {
        typeName = typeName.trim().toLowerCase();
        return this.getBaseUrl(indexName).append("/").append(typeName);
    }

    public StringBuilder getBaseUrl(String indexName) {
        indexName = indexName.trim().toLowerCase();
        return this.getBaseUrl().append("/").append(indexName);
    }

    public StringBuilder getBaseUrl() {
        return new StringBuilder("http://").append(this.baseUrl);
    }

    /**
     * cat Truy vấn dữ liệu hệ thống ElasticSearch, trả về json
     */
    private <T> ResponseEntity<T> cat(String urlAfter, Class<T> responseType) {
        String url = this.getBaseUrl().append("/_cat").append(urlAfter).append("?").append(FORMAT_JSON).toString();
        return RestUtil.request(url, HttpMethod.GET, null, null, null, responseType);
    }

    /**
     * Tìm kiếm tất cả các chỉ mục
     * <p>
     * Địa chỉ truy vấn:GET http://{baseUrl}/_cat/indices
     */
    public JSONArray getIndices() {
        return getIndices(null);
    }


    /**
     * Tìm kiếm một chỉ mục
     * <p>
     * Địa chỉ truy vấn:GET http://{baseUrl}/_cat/indices/{indexName}
     */
    public JSONArray getIndices(String indexName) {
        StringBuilder urlAfter = new StringBuilder("/indices");
        if (!StringUtils.isEmpty(indexName)) {
            urlAfter.append("/").append(indexName.trim().toLowerCase());
        }
        return cat(urlAfter.toString(), JSONArray.class).getBody();
    }

    /**
     * Chỉ mục có tồn tại không
     */
    public boolean indexExists(String indexName) {
        try {
            JSONArray array = getIndices(indexName);
            return array != null;
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
                return false;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Lấy dữ liệu chỉ mục theo ID, không tìm thấy thì trả về null
     * <p>
     * Địa chỉ truy vấn:GET http://{baseUrl}/{indexName}/{typeName}/{dataId}
     *
     * @param indexName Tên chỉ mục
     * @param typeName  type, một chuỗi tùy ý, dùng để phân loại
     * @param dataId    Dữ liệu id
     * @return
     */
    public JSONObject getDataById(String indexName, String typeName, String dataId) {
        String url = this.getBaseUrl(indexName, typeName).append("/").append(dataId).toString();
        log.info("url:" + url);
        JSONObject result = RestUtil.get(url);
        boolean found = result.getBoolean("found");
        if (found) {
            return result.getJSONObject("_source");
        } else {
            return null;
        }
    }

    /**
     * Tạo chỉ mục
     * <p>
     * Địa chỉ truy vấn:PUT http://{baseUrl}/{indexName}
     */
    public boolean createIndex(String indexName) {
        String url = this.getBaseUrl(indexName).toString();

        /* Kết quả trả về (chỉ để tham khảo)
        "createIndex": {
            "shards_acknowledged": true,
            "acknowledged": true,
            "index": "hello_world"
        }
        */
        try {
            return RestUtil.put(url).getBoolean("acknowledged");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            if (HttpStatus.BAD_REQUEST == ex.getStatusCode()) {
                log.warn("Tạo chỉ mục thất bại:" + indexName + " đã tồn tại, không cần tạo lại");
            } else {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Xóa chỉ mục
     * <p>
     * Địa chỉ truy vấn:DELETE http://{baseUrl}/{indexName}
     */
    public boolean removeIndex(String indexName) {
        String url = this.getBaseUrl(indexName).toString();
        try {
            return RestUtil.delete(url).getBoolean("acknowledged");
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
                log.warn("Xóa chỉ mục thất bại:" + indexName + " không tồn tại, không cần xóa");
            } else {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Lấy ánh xạ trường của chỉ mục (có thể lấy loại trường)
     * <p>
     *
     * @param indexName Tên chỉ mục
     * @param typeName  Tên phân loại
     * @return
     */
    public JSONObject getIndexMapping(String indexName, String typeName) {
        String url = this.getBaseUrl(indexName, typeName).append("/_mapping?").append(FORMAT_JSON).toString();
        // Đối với es 7.x tương thích với phiên bản
        this.getElasticsearchVersion();
        if (oConvertUtils.isNotEmpty(this.version) && this.version.startsWith(IE_SEVEN)) {
            url += "&include_type_name=true";
        }
        log.info("getIndexMapping-url:" + url);
        /*
         * Tham khảo cấu trúc JSON trả về:
         *
         *{
         *    // Tên chỉ mục
         *    "[indexName]": {
         *        "mappings": {
         *            // Tên phân loại
         *            "[typeName]": {
         *                "properties": {
         *                    // Tên trường
         *                    "input_number": {
         *                        // Loại trường
         *                        "type": "long"
         *                    },
         *                    "input_string": {
         *                        "type": "text",
         *                        "fields": {
         *                            "keyword": {
         *                                "type": "keyword",
         *                                "ignore_above": 256
         *                            }
         *                        }
         *                    }
         *                 }
         *            }
         *        }
         *    }
         * }
         */
        try {
            return RestUtil.get(url);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String message = e.getMessage();
            if (message != null && message.contains(URL_NOT_FOUND)) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Lấy ánh xạ trường của chỉ mục, trả về lớp thực thể Java
     *
     * @param indexName
     * @param typeName
     * @return
     */
    public <T> Map<String, T> getIndexMappingFormat(String indexName, String typeName, Class<T> clazz) {
        JSONObject mapping = this.getIndexMapping(indexName, typeName);
        Map<String, T> map = new HashMap<>(5);
        if (mapping == null) {
            return map;
        }
        // Lấy thuộc tính trường
        JSONObject properties = mapping.getJSONObject(indexName)
                .getJSONObject("mappings")
                .getJSONObject(typeName)
                .getJSONObject("properties");
        // Đóng gói thành kiểu java
        for (String key : properties.keySet()) {
            T entity = properties.getJSONObject(key).toJavaObject(clazz);
            map.put(key, entity);
        }
        return map;
    }

    /**
     * Lưu dữ liệu, xem chi tiết:saveOrUpdate
     */
    public boolean save(String indexName, String typeName, String dataId, JSONObject data) {
        return this.saveOrUpdate(indexName, typeName, dataId, data);
    }

    /**
     * Cập nhật dữ liệu, xem chi tiết:saveOrUpdate
     */
    public boolean update(String indexName, String typeName, String dataId, JSONObject data) {
        return this.saveOrUpdate(indexName, typeName, dataId, data);
    }

    /**
     * Lưu hoặc sửa dữ liệu chỉ mục
     * <p>
     * Địa chỉ truy vấn:PUT http://{baseUrl}/{indexName}/{typeName}/{dataId}
     *
     * @param indexName Tên chỉ mục
     * @param typeName  type, một chuỗi tùy ý, dùng để phân loại
     * @param dataId    Dữ liệu id
     * @param data      Dữ liệu cần lưu trữ
     * @return
     */
    public boolean saveOrUpdate(String indexName, String typeName, String dataId, JSONObject data) {
        String url = this.getBaseUrl(indexName, typeName).append("/").append(dataId).append("?refresh=wait_for").toString();
        /* Kết quả trả về (chỉ để tham khảo)
       "createIndexA2": {
            "result": "created",
            "_shards": {
                "total": 2,
                "successful": 1,
                "failed": 0
            },
            "_seq_no": 0,
            "_index": "test_index_1",
            "_type": "test_type_1",
            "_id": "a2",
            "_version": 1,
            "_primary_term": 1
        }
         */

        try {
            // Loại bỏ các giá trị rỗng trong data
            Set<String> keys = data.keySet();
            List<String> emptyKeys = new ArrayList<>(keys.size());
            for (String key : keys) {
                String value = data.getString(key);
                //1, loại bỏ giá trị rỗng
                if (oConvertUtils.isEmpty(value) || "[]".equals(value)) {
                    emptyKeys.add(key);
                }
                //2, loại bỏ giá trị của control tải lên(sẽ khiến đồng bộ ES thất bại và báo lỗi failed to parse field [ge_pic] of type [text] )
                if (oConvertUtils.isNotEmpty(value) && value.indexOf("[{")!=-1) {
                    emptyKeys.add(key);
                    log.info("-------Loại bỏ trường control tải lên------------key: "+ key);
                }
            }
            for (String key : emptyKeys) {
                data.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String result = RestUtil.put(url, data).getString("result");
            return "created".equals(result) || "updated".equals(result);
        } catch (Exception e) {
            log.error(e.getMessage() + "\n-- url: " + url + "\n-- data: " + data.toJSONString());
            //TODO In lỗi trả về của API json
            return false;
        }
    }

    /**
     * Lưu hàng loạt dữ liệu
     *
     * @param indexName Tên chỉ mục
     * @param typeName  type, một chuỗi tùy ý, dùng để phân loại
     * @param dataList  Mảng dữ liệu cần lưu trữ, mỗi dòng dữ liệu phải chứa id
     * @return
     */
    public boolean saveBatch(String indexName, String typeName, JSONArray dataList) {
        String url = this.getBaseUrl().append("/_bulk").append("?refresh=wait_for").toString();
        StringBuilder bodySb = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject data = dataList.getJSONObject(i);
            String id = data.getString("id");
            // Thao tác của dòng đó
            // {"create": {"_id":"${id}", "_index": "${indexName}", "_type": "${typeName}"}}
            JSONObject action = new JSONObject();
            JSONObject actionInfo = new JSONObject();
            actionInfo.put("_id", id);
            actionInfo.put("_index", indexName);
            actionInfo.put("_type", typeName);
            action.put("create", actionInfo);
            bodySb.append(action.toJSONString()).append("\n");
            // Dữ liệu của dòng đó
            data.remove("id");
            bodySb.append(data.toJSONString()).append("\n");
        }
        //System.out.println("+-+-+-: bodySb.toString(): " + bodySb.toString());
        HttpHeaders headers = RestUtil.getHeaderApplicationJson();
        RestUtil.request(url, HttpMethod.PUT, headers, null, bodySb, JSONObject.class);
        return true;
    }

    /**
     * Xóa dữ liệu chỉ mục
     * <p>
     * Địa chỉ yêu cầu:DELETE http://{baseUrl}/{indexName}/{typeName}/{dataId}
     */
    public boolean delete(String indexName, String typeName, String dataId) {
        String url = this.getBaseUrl(indexName, typeName).append("/").append(dataId).toString();
        /* Kết quả trả về (chỉ để tham khảo)
        {
            "_index": "es_demo",
            "_type": "docs",
            "_id": "001",
            "_version": 3,
            "result": "deleted",
            "_shards": {
                "total": 1,
                "successful": 1,
                "failed": 0
            },
            "_seq_no": 28,
            "_primary_term": 18
        }
        */
        try {
            return "deleted".equals(RestUtil.delete(url).getString("result"));
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
                return false;
            } else {
                throw ex;
            }
        }
    }


    /* = = = Các phương thức sau đây về tìm kiếm và điều kiện tìm kiếm = = =*/

    /**
     * Truy vấn dữ liệu
     * <p>
     * Địa chỉ yêu cầu:POST http://{baseUrl}/{indexName}/{typeName}/_search
     */
    public JSONObject search(String indexName, String typeName, JSONObject queryObject) {
        String url = this.getBaseUrl(indexName, typeName).append("/_search").toString();

        log.info("url:" + url + " ,search: " + queryObject.toJSONString());
        JSONObject res = RestUtil.post(url, queryObject);
        log.info("url:" + url + " ,return res: \n" + res.toJSONString());
        return res;
    }

    /**
     * @param source (Bộ lọc nguồn) chỉ định các trường trả về, truyền null sẽ trả về tất cả các trường
     * @param query
     * @param from    Bắt đầu từ bản ghi dữ liệu thứ mấy
     * @param size    Số mục trả về
     * @return { "query": query }
     */
    public JSONObject buildQuery(List<String> source, JSONObject query, int from, int size) {
        JSONObject json = new JSONObject();
        if (source != null) {
            json.put("_source", source);
        }
        json.put("query", query);
        json.put("from", from);
        json.put("size", size);
        return json;
    }

    /**
     * @return { "bool" : { "must": must, "must_not": mustNot, "should": should } }
     */
    public JSONObject buildBoolQuery(JSONArray must, JSONArray mustNot, JSONArray should) {
        JSONObject bool = new JSONObject();
        if (must != null) {
            bool.put("must", must);
        }
        if (mustNot != null) {
            bool.put("must_not", mustNot);
        }
        if (should != null) {
            bool.put("should", should);
        }
        JSONObject json = new JSONObject();
        json.put("bool", bool);
        return json;
    }

    /**
     * @param field Trường cần tìm kiếm
     * @param args  Tham số tìm kiếm, tham khảo: *哈哈* OR *哒* NOT *呵* OR *啊*
     * @return
     */
    public JSONObject buildQueryString(String field, String... args) {
        if (field == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(field).append(":(");
        if (args != null) {
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
        }
        sb.append(")");
        return this.buildQueryString(sb.toString());
    }

    /**
     * @return { "query_string": { "query": query }  }
     */
    public JSONObject buildQueryString(String query) {
        JSONObject queryString = new JSONObject();
        queryString.put("query", query);
        JSONObject json = new JSONObject();
        json.put("query_string", queryString);
        return json;
    }

    /**
     * @param field      Trường tìm kiếm
     * @param min        Giá trị nhỏ nhất
     * @param max        Giá trị lớn nhất
     * @param containMin Trong phạm vi có bao gồm giá trị nhỏ nhất không
     * @param containMax Trong phạm vi có bao gồm giá trị lớn nhất không
     * @return { "range" : { field : { 『 "gt『e』?containMin" : min 』?min!=null , 『 "lt『e』?containMax" : max 』}} }
     */
    public JSONObject buildRangeQuery(String field, Object min, Object max, boolean containMin, boolean containMax) {
        JSONObject inner = new JSONObject();
        if (min != null) {
            if (containMin) {
                inner.put("gte", min);
            } else {
                inner.put("gt", min);
            }
        }
        if (max != null) {
            if (containMax) {
                inner.put("lte", max);
            } else {
                inner.put("lt", max);
            }
        }
        JSONObject range = new JSONObject();
        range.put(field, inner);
        JSONObject json = new JSONObject();
        json.put("range", range);
        return json;
    }

}

