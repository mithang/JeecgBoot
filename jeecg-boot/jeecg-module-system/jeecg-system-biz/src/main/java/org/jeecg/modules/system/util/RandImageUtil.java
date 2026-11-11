package org.jeecg.modules.system.util;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Lớp tiện ích mã xác minh đăng nhập
 * @date  2025-09-11
 * @author AI tối ưu hóa
 */
public class RandImageUtil {

    // Khối khởi tạo tĩnh, giải quyết vấn đề cấu hình phông chữ trong môi trường không có giao diện
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public static final String KEY = "JEECG_LOGIN_KEY";

    /** Chiều rộng hình ảnh mã xác minh */
    private static final int WIDTH = 105;
    
    /** Chiều cao hình ảnh mã xác minh */
    private static final int HEIGHT = 35;
    
    /** Số lượng đường nhiễu */
    private static final int INTERFERENCE_LINE_COUNT = 200;
    
    /** Độ rộng đường nhiễu */
    private static final int LINE_WIDTH = 2;
    
    /** Định dạng hình ảnh */
    private static final String IMG_FORMAT = "JPEG";
    
    /** base64 Tiền tố hình ảnh */
    private static final String BASE64_PREFIX = "data:image/jpg;base64,";
    
    /** Khoảng cách ký tự */
    private static final int CHAR_SPACING = 23;
    
    /** Cỡ chữ */
    private static final int FONT_SIZE = 24;
    
    /** Độ lệch trục Y của ký tự */
    private static final int CHAR_Y_OFFSET = 26;
    
    /** Độ lệch bắt đầu trục X của ký tự */
    private static final int CHAR_X_OFFSET = 8;

    /**
     * Xuất trực tiếp hình ảnh mã xác thực qua response
     * 
     * @param response HTTP Đối tượng phản hồi
     * @param verifyCode Chuỗi mã xác minh
     * @throws IOException Ngoại lệ đầu ra
     */
    public static void generate(HttpServletResponse response, String verifyCode) throws IOException {
        if (response == null || verifyCode == null || verifyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tham số không được để trống");
        }
        
        try {
            BufferedImage image = createVerifyCodeImage(verifyCode);
            ImageIO.write(image, IMG_FORMAT, response.getOutputStream());
        } catch (Exception e) {
            throw new IOException("Tạo hình ảnh mã xác minh thất bại", e);
        }
    }

    /**
     * Tạo chuỗi base64 của mã xác minh
     * 
     * @param verifyCode Chuỗi mã xác minh
     * @return base64 Chuỗi hình ảnh đã mã hóa base64
     * @throws IOException Ngoại lệ khi tạo
     */
    public static String generate(String verifyCode) throws IOException {
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã xác minh không được để trống");
        }
        
        try {
            BufferedImage image = createVerifyCodeImage(verifyCode);
            
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
                ImageIO.write(image, IMG_FORMAT, byteStream);
                byte[] bytes = byteStream.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(bytes).trim();
                // Dọn dẹp ký tự xuống dòng
                base64 = base64.replaceAll("[\r\n]", "");
                return BASE64_PREFIX + base64;
            }
        } catch (Exception e) {
            throw new IOException("Tạo mã xác minh base64 thất bại", e);
        }
    }

    /**
     * Tạo hình ảnh mã xác minh
     * 
     * @param verifyCode Chuỗi mã xác minh
     * @return Hình ảnh mã xác minh
     */
    private static BufferedImage createVerifyCodeImage(String verifyCode) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = null;
        
        try {
            graphics = (Graphics2D) image.getGraphics();
            
            // Đặt chất lượng hiển thị đồ họa
            setupRenderingHints(graphics);
            
            // Vẽ nền
            drawBackground(graphics);
            
            // Vẽ viền
            drawBorder(graphics);
            
            // Lấy bộ tạo số ngẫu nhiên an toàn
            SecureRandom random = createSecureRandom();
            
            // Vẽ đường nhiễu
            drawInterferenceLines(graphics, random);
            
            // Vẽ ký tự mã xác minh
            drawVerifyCodeText(graphics, verifyCode);
            
        } catch (Exception e) {
            // Nếu vẽ thất bại, tạo hình ảnh lỗi đơn giản
            return createErrorImage(verifyCode);
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return image;
    }

    /**
     * Đặt chất lượng hiển thị đồ họa
     */
    private static void setupRenderingHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Vẽ nền trắng
     */
    private static void drawBackground(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * Vẽ viền
     */
    private static void drawBorder(Graphics2D graphics) {
        graphics.setColor(Color.GRAY);
        graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
    }

    /**
     * Tạo trình tạo số ngẫu nhiên an toàn
     */
    private static SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }

    /**
     * Vẽ đường nhiễu
     */
    private static void drawInterferenceLines(Graphics2D graphics, SecureRandom random) {
        for (int i = 0; i < INTERFERENCE_LINE_COUNT; i++) {
            graphics.setColor(getRandomColor(150, 200, random));
            
            // Đảm bảo đường nhiễu nằm trong viền
            int x1 = random.nextInt(WIDTH - LINE_WIDTH - 1) + 1;
            int y1 = random.nextInt(HEIGHT - LINE_WIDTH - 1) + 1;
            int x2 = x1 + random.nextInt(LINE_WIDTH);
            int y2 = y1 + random.nextInt(LINE_WIDTH);
            
            graphics.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Vẽ văn bản mã xác minh
     */
    private static void drawVerifyCodeText(Graphics2D graphics, String verifyCode) {
        graphics.setColor(Color.BLACK);
        Font font = createSafeFont();
        graphics.setFont(font);
        
        for (int i = 0; i < verifyCode.length(); i++) {
            char character = verifyCode.charAt(i);
            int x = i * CHAR_SPACING + CHAR_X_OFFSET;
            graphics.drawString(String.valueOf(character), x, CHAR_Y_OFFSET);
        }
    }

    /**
     * Tạo phông chữ an toàn, tránh vấn đề cấu hình phông chữ
     */
    private static Font createSafeFont() {
        // Dùng tên phông chữ logic, có thể dùng được trên mọi nền tảng
        String[] safeFontNames = {Font.SERIF, Font.SANS_SERIF, Font.MONOSPACED, "Dialog"};
        
        for (String fontName : safeFontNames) {
            try {
                Font font = new Font(fontName, Font.BOLD, FONT_SIZE);
                if (font.getFamily() != null) {
                    return font;
                }
            } catch (Exception e) {
                // Tiếp tục thử phông chữ tiếp theo
            }
        }
        
        // Phương án dự phòng cuối cùng
        return new Font(Font.MONOSPACED, Font.BOLD, FONT_SIZE);
    }

    /**
     * Tạo hình ảnh lỗi (dùng khi vẽ bình thường thất bại)
     */
    private static BufferedImage createErrorImage(String verifyCode) {
        BufferedImage errorImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = null;
        
        try {
            graphics = (Graphics2D) errorImage.getGraphics();
            
            // Nền trắng
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            
            // Viền đen
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
            
            // Thử vẽ mã xác minh
            try {
                graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
                graphics.setColor(Color.BLUE);
                for (int i = 0; i < verifyCode.length(); i++) {
                    graphics.drawString(String.valueOf(verifyCode.charAt(i)), 
                                      i * CHAR_SPACING + CHAR_X_OFFSET, CHAR_Y_OFFSET);
                }
            } catch (Exception fontException) {
                // Nếu ngay cả phông chữ cơ bản cũng thất bại, hiển thị ERROR
                graphics.setColor(Color.RED);
                graphics.drawString("ERROR", 10, 20);
            }
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return errorImage;
    }

    /**
     * Lấy màu ngẫu nhiên trong phạm vi được chỉ định
     * 
     * @param minColorValue Giá trị màu tối thiểu
     * @param maxColorValue Giá trị màu tối đa
     * @param random Bộ tạo số ngẫu nhiên
     * @return Màu ngẫu nhiên
     */
    private static Color getRandomColor(int minColorValue, int maxColorValue, Random random) {
        // Đảm bảo giá trị màu nằm trong phạm vi hợp lệ
        int min = Math.max(0, Math.min(minColorValue, 255));
        int max = Math.max(min, Math.min(maxColorValue, 255));
        
        int range = max - min;
        if (range == 0) {
            return new Color(min, min, min);
        }
        
        int red = min + random.nextInt(range);
        int green = min + random.nextInt(range);
        int blue = min + random.nextInt(range);
        
        return new Color(red, green, blue);
    }
}
