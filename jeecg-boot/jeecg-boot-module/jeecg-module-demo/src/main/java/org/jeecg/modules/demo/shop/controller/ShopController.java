package org.jeecg.modules.demo.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.demo.shop.entity.Order;
import org.jeecg.modules.demo.shop.entity.Product;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * API mô phỏng quản lý sản phẩm
 * Demo nghiệp vụ để AI Agent giúp người dùng tra cứu sản phẩm và mua hàng thông qua công cụ
 * @Author: chenrui
 * @Date: 2025-11-06
 */
@Tag(name = "Quản lý sản phẩm Demo")
@RestController
@RequestMapping("/demo/shop")
@Slf4j
public class ShopController {
    
    /**
     * Lưu trữ dữ liệu sản phẩm(Bộ nhớ)
     */
    private final Map<String, Product> productStore = new ConcurrentHashMap<>();
    
    /**
     * Lưu trữ dữ liệu đơn hàng(Bộ nhớ)
     */
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();
    
    /**
     * Bộ tạo ID đơn hàng
     */
    private final AtomicInteger orderIdGenerator = new AtomicInteger(1000);
    
    /**
     * Khởi tạo dữ liệu sản phẩm
     *
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @PostConstruct
    public void initProducts() {
        // Sản phẩm điện tử
        productStore.put("P001", new Product("P001", "iPhone 15 Pro", new BigDecimal("7999.00"), "Sản phẩm điện tử", "Apple Điện thoại flagship mới nhất,6.1 Màn hình inch,Viền kim loại titan", 50));
        productStore.put("P002", new Product("P002", "MacBook Pro 14", new BigDecimal("14999.00"), "Sản phẩm điện tử", "M3 Pro Chip,16GB Bộ nhớ,512GB Dung lượng lưu trữ", 30));
        productStore.put("P003", new Product("P003", "AirPods Pro 2", new BigDecimal("1899.00"), "Sản phẩm điện tử", "Tai nghe không dây chống ồn chủ động,Hỗ trợ âm thanh không gian", 100));
        productStore.put("P004", new Product("P004", "iPad Air", new BigDecimal("4799.00"), "Sản phẩm điện tử", "10.9 Màn hình LCD inch,M1 Chip", 60));
        
        // Sách
        productStore.put("B001", new Product("B001", "Java Tập công nghệ cốt lõi I", new BigDecimal("119.00"), "Sách", "Java Giáo trình lập trình kinh điển,Phù hợp với người mới bắt đầu và nhà phát triển nâng cao", 200));
        productStore.put("B002", new Product("B002", "Hiểu sâu về hệ thống máy tính", new BigDecimal("139.00"), "Sách", "CSAPP Giáo trình kinh điển,Sách nên đọc về hệ thống máy tính", 150));
        productStore.put("B003", new Product("B003", "Mẫu thiết kế", new BigDecimal("89.00"), "Sách", "Tác phẩm kinh điển về thiết kế phần mềm,GoF Tác phẩm của nhóm bốn tác giả (GoF)", 180));
        
        // Đồ dùng sinh hoạt
        productStore.put("L001", new Product("L001", "Bàn chải điện Xiaomi", new BigDecimal("199.00"), "Đồ dùng sinh hoạt", "Rung sóng âm,IPX7 Chống nước,Thời lượng pin 18 ngày", 300));
        productStore.put("L002", new Product("L002", "Máy sấy tóc Dyson", new BigDecimal("2990.00"), "Đồ dùng sinh hoạt", "Sấy khô tóc nhanh,Điều khiển nhiệt độ thông minh,Bảo vệ tóc", 80));
        productStore.put("L003", new Product("L003", "Bình giữ nhiệt Thermos", new BigDecimal("259.00"), "Đồ dùng sinh hoạt", "Giữ nhiệt chân không,304 Thép không gỉ,Giữ nhiệt 12 giờ", 500));
        
        // Thực phẩm
        productStore.put("F001", new Product("F001", "Hộp quà hạt hỗn hợp Three Squirrels", new BigDecimal("159.00"), "Thực phẩm", "Gói quà lớn hạt hỗn hợp,1500g gói", 400));
        productStore.put("F002", new Product("F002", "Rượu Moutai Feitian 53 độ", new BigDecimal("2899.00"), "Thực phẩm", "Rượu Moutai Quý Châu,500ml", 20));
        productStore.put("F003", new Product("F003", "Hạt cà phê Starbucks", new BigDecimal("128.00"), "Thực phẩm", "Rang vừa,Pike Place,250g", 250));
        
        log.info("Khởi tạo dữ liệu sản phẩm hoàn tất,Tổng cộng{}sản phẩm", productStore.size());
    }
    
    /**
     * Truy vấn danh sách sản phẩm
     *
     * @param category Phân loại sản phẩm(Tùy chọn)
     * @param keyword Từ khóa tìm kiếm(Tùy chọn)
     * @return Danh sách sản phẩm
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Truy vấn danh sách sản phẩm", description = "Hỗ trợ tìm kiếm theo danh mục và từ khóa")
    @GetMapping("/products")
    public Result<List<Product>> getProducts(
            @Parameter(description = "Phân loại sản phẩm") @RequestParam(required = false) String category,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword) {
        
        log.info("Truy vấn danh sách sản phẩm - Phân loại: {}, Từ khóa: {}", category, keyword);
        
        List<Product> products = new ArrayList<>(productStore.values());
        
        // Lọc theo danh mục
        if (category != null && !category.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }
        
        // Lọc theo từ khóa(Tìm kiếm tên và mô tả sản phẩm)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKey = keyword.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchKey) 
                            || p.getDescription().toLowerCase().contains(searchKey))
                    .collect(Collectors.toList());
        }
        
        // Sắp xếp theo giá
        products.sort(Comparator.comparing(Product::getPrice));
        
        log.info("Truy vấn được{}sản phẩm", products.size());
        return Result.OK(products);
    }
    
    /**
     * Truy vấn tồn kho sản phẩm
     *
     * @param productId Sản phẩm ID
     * @return Thông tin tồn kho
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Truy vấn tồn kho sản phẩm", description = "Truy vấn số lượng tồn kho theo ID sản phẩm")
    @GetMapping("/stock")
    public Result<Map<String, Object>> getStock(
            @Parameter(description = "Sản phẩm ID", required = true) @RequestParam String productId) {
        
        log.info("Truy vấn tồn kho sản phẩm - Sản phẩm ID: {}", productId);
        
        Product product = productStore.get(productId);
        if (product == null) {
            return Result.error("Sản phẩm không tồn tại: " + productId);
        }
        
        Map<String, Object> stockInfo = new HashMap<>();
        stockInfo.put("productId", product.getId());
        stockInfo.put("productName", product.getName());
        stockInfo.put("stock", product.getStock());
        stockInfo.put("available", product.getStock() > 0);
        
        return Result.OK(stockInfo);
    }
    
    /**
     * Mua sản phẩm(Đặt hàng)
     *
     * @param productId Sản phẩm ID
     * @param quantity Số lượng mua
     * @param userId Người dùng ID(Tùy chọn)
     * @return Thông tin đơn hàng
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Mua sản phẩm", description = "Tạo đơn hàng,nhưng không trừ tồn kho ngay")
    @PostMapping("/purchase")
    public Result<Order> purchase(
            @Parameter(description = "Sản phẩm ID", required = true) @RequestParam String productId,
            @Parameter(description = "Số lượng mua", required = true) @RequestParam Integer quantity,
            @Parameter(description = "Người dùng ID") @RequestParam(required = false) String userId) {
        
        log.info("Mua sản phẩm - Sản phẩm ID: {}, Số lượng: {}, Người dùng: {}", productId, quantity, userId);
        
        // Kiểm tra tham số
        if (quantity == null || quantity <= 0) {
            return Result.error("Số lượng mua phải lớn hơn 0");
        }
        
        // Truy vấn sản phẩm
        Product product = productStore.get(productId);
        if (product == null) {
            return Result.error("Sản phẩm không tồn tại: " + productId);
        }
        
        // Kiểm tra tồn kho
        if (product.getStock() < quantity) {
            return Result.error("Tồn kho không đủ,Tồn kho hiện tại: " + product.getStock());
        }
        
        // Tạo đơn hàng
        String orderId = "O" + orderIdGenerator.incrementAndGet();
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
        
        Order order = new Order();
        order.setId(orderId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setCreateTime(new Date());
        order.setUserId(userId);
        
        orderStore.put(orderId, order);
        
        log.info("Tạo đơn hàng thành công - Đơn hàng ID: {}, Tổng số tiền: {}", orderId, totalAmount);
        return Result.OK(order);
    }
    
    /**
     * Trừ tồn kho sản phẩm
     *
     * @param orderId Đơn hàng ID
     * @return Kết quả trừ tồn kho
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Trừ tồn kho sản phẩm", description = "Trừ tồn kho của sản phẩm tương ứng theo ID đơn hàng")
    @PostMapping("/stock/deduct")
    public Result<Map<String, Object>> deductStock(
            @Parameter(description = "Đơn hàng ID", required = true) @RequestParam String orderId) {
        
        log.info("Trừ kho - Đơn hàng ID: {}", orderId);
        
        // Truy vấn đơn hàng
        Order order = orderStore.get(orderId);
        if (order == null) {
            return Result.error("Đơn hàng không tồn tại: " + orderId);
        }
        
        // Kiểm tra trạng thái đơn hàng
        if ("paid".equals(order.getStatus())) {
            return Result.error("Đơn hàng đã thanh toán,Tồn kho đã được trừ");
        }
        
        if ("cancelled".equals(order.getStatus())) {
            return Result.error("Đơn hàng đã bị hủy");
        }
        
        // Truy vấn sản phẩm
        Product product = productStore.get(order.getProductId());
        if (product == null) {
            return Result.error("Sản phẩm không tồn tại: " + order.getProductId());
        }
        
        // Kiểm tra tồn kho
        synchronized (product) {
            if (product.getStock() < order.getQuantity()) {
                return Result.error("Tồn kho không đủ,Tồn kho hiện tại: " + product.getStock() + ", Cần: " + order.getQuantity());
            }
            
            // Trừ kho
            int newStock = product.getStock() - order.getQuantity();
            product.setStock(newStock);
            
            // Cập nhật trạng thái đơn hàng
            order.setStatus("paid");
            
            log.info("Trừ tồn kho thành công - Sản phẩm: {}, Số lượng trừ: {}, Tồn kho còn lại: {}", 
                    product.getName(), order.getQuantity(), newStock);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("productId", product.getId());
        result.put("productName", product.getName());
        result.put("deductedQuantity", order.getQuantity());
        result.put("remainingStock", product.getStock());
        result.put("orderStatus", order.getStatus());
        
        return Result.OK(result);
    }
    
    /**
     * Truy vấn chi tiết đơn hàng
     *
     * @param orderId Đơn hàng ID
     * @return Chi tiết đơn hàng
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Truy vấn chi tiết đơn hàng", description = "Truy vấn thông tin đơn hàng theo ID đơn hàng")
    @GetMapping("/order")
    public Result<Order> getOrder(
            @Parameter(description = "Đơn hàng ID", required = true) @RequestParam String orderId) {
        
        log.info("Truy vấn đơn hàng - Đơn hàng ID: {}", orderId);
        
        Order order = orderStore.get(orderId);
        if (order == null) {
            return Result.error("Đơn hàng không tồn tại: " + orderId);
        }
        
        return Result.OK(order);
    }
    
    /**
     * Lấy tất cả danh mục sản phẩm
     *
     * @return Danh sách phân loại
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Lấy danh mục sản phẩm", description = "Lấy danh sách danh mục của tất cả sản phẩm")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        
        Set<String> categories = productStore.values().stream()
                .map(Product::getCategory)
                .collect(Collectors.toSet());
        
        List<String> categoryList = new ArrayList<>(categories);
        categoryList.sort(String::compareTo);
        
        return Result.OK(categoryList);
    }
    
    /**
     * Đặt lại tất cả dữ liệu(Chỉ dùng để kiểm thử)
     *
     * @return Kết quả đặt lại
     * @author chenrui
     * @date 2025/11/6 14:30
     */
    @Operation(summary = "Đặt lại dữ liệu", description = "Xóa tất cả đơn hàng và đặt lại tồn kho sản phẩm(Chỉ dùng để kiểm thử)")
    @PostMapping("/reset")
    public Result<String> reset() {
        
        log.info("Đặt lại dữ liệu sản phẩm và đơn hàng");
        
        orderStore.clear();
        orderIdGenerator.set(1000);
        productStore.clear();
        initProducts();
        
        return Result.OK("Đặt lại dữ liệu thành công");
    }
}
