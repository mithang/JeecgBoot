package org.jeecg.common.util.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.vo.Firewall;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SSRF Công cụ kiểm chứng địa chỉ outbound:
 * <ul>
 *     <li>Truy cập chính dịch vụ này (địa chỉ máy cục bộ + cổng mà dịch vụ này đang lắng nghe) thì cho qua;</li>
 *     <li>Trúng danh sách trắng jeecg.firewall.localAddressWhiteList thì cho qua: tên miền, IP, dải CIDR, địa chỉ:cổng (chỉ cho qua cổng đó);</li>
 *     <li>Địa chỉ link-local (bao gồm metadata đám mây 169.254.169.254）、0.0.0.0/8、[::] luôn bị từ chối, dù đã ghi vào danh sách trắng cũng không cho qua;</li>
 *     <li>Các cổng khác trên máy này bị từ chối; dải mạng nội bộ (10/172.16/192.168/100.64/fc00::/7) mặc định cho qua,allowLocalAddress=false thì bị từ chối;</li>
 *     <li>IPv6 Địa chỉ chuyển đổi (NAT64, 6to4, Teredo, tương thích IPv4/ánh xạ) được đánh giá theo IPv4 được nhúng.</li>
 * </ul>
 *
 * @author scott
 * @since 2026-10-27 【GHSA-5g5g-43q2-g87v】【GHSA-2m5w-w7v9-qwx7】【GHSA-hw5p-65q6-qcxr】 Được trích xuất từ SsrfFileTypeFilter, chuyên dùng cho kiểm chứng SSRF
 */
@Slf4j
public class LocalAddressCheckUtils {

    private LocalAddressCheckUtils() {
    }

    /**
     * Kiểm chứng HTTP(S) URL, ngăn chặn tấn công SSRF (chặn tối thiểu, chỉ chặn các mục tiêu thực sự nguy hiểm).
     * Quy tắc:
     * 1. Chỉ cho phép http / https giao thức;
     * 2. Phân tích IP của máy chủ, từ chối loopback (127.x / ::1）、link-local（169.254.x，chứa siêu dữ liệu đám mây 169.254.169.254 / fe80:）、0.0.0.0/8、[::]，
     *    và các địa chỉ chuyển đổi IPv6 nhúng các địa chỉ này (NAT64, 6to4, Teredo, IPv4 tương thích/ánh xạ);
     * 3. Truy cập chính dịch vụ này (địa chỉ máy cục bộ + cổng mà dịch vụ này đang lắng nghe) thì cho qua.
     * 4. Trúng danh sách trắng jeecg.firewall.localAddressWhiteList(tên miền, IP, dải CIDR, địa chỉ:cổng) thì cho qua ngay, địa chỉ link-local,0.0.0.0 ngoại trừ.
     * Lưu ý: Dải mạng riêng RFC1918 (10/172.16/192.168) mặc định cho phép đi qua, tương thích với mạng nội bộ doanh nghiệp MinIO/OSS/dịch vụ tệp và các mục đích hợp pháp khác;
     *    Cấu hình jeecg.firewall.allowLocalAddress=false sau đó mới chặn lại.
     *
     * @param fileUrl HTTP(S) URL
     * @author scott
     * @since 2026-10-27 【GHSA-5g5g-43q2-g87v】 Do SsrfFileTypeFilter.checkSsrfHttpUrl Di chuyển
     */
    public static void checkUrl(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            throw new JeecgBootException("URL không hợp lệ: địa chỉ trống");
        }
        URI uri;
        try {
            uri = new URI(fileUrl);
        } catch (URISyntaxException e) {
            throw new JeecgBootException("URL không hợp lệ: định dạng sai");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new JeecgBootException("URL không hợp lệ: chỉ cho phép http / https Giao thức");
        }
        String host = uri.getHost();
        if (StringUtils.isBlank(host)) {
            throw new JeecgBootException("URL không hợp lệ: tên máy chủ trống");
        }
        // Loại bỏ dấu ngoặc vuông của IPv6
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        try {
            checkAddresses(host, uri);
        } catch (UnknownHostException e) {
            throw new JeecgBootException("URL không hợp lệ: không thể phân tích tên máy chủ");
        }
    }

    /**
     * Phân tích máy chủ và kiểm chứng tất cả kết quả phân tích, ném ra ngoại lệ khi không hợp lệ JeecgBootException
     *
     * @param host Tên máy chủ (IPv6 đã loại bỏ dấu ngoặc vuông)
     * @param uri  Địa chỉ đích (dùng để lấy cổng nhằm xác định có phải đang truy cập chính dịch vụ này hay không)
     * @throws UnknownHostException Không thể phân tích tên máy chủ
     */
    private static void checkAddresses(String host, URI uri) throws UnknownHostException {
        Firewall firewall = getFirewall();
        boolean blockPrivate = firewall != null && Boolean.FALSE.equals(firewall.getAllowLocalAddress());
        List<String> whiteList = firewall == null ? null : firewall.getLocalAddressWhiteList();
        InetAddress[] addresses = InetAddress.getAllByName(host);
        // Truy cập chính dịch vụ này (ví dụ tệp lưu trữ cục bộ http://localhost:8080/jeecg-boot/sys/common/static/xx.png) không chặn
        if (isSelfServiceAddress(addresses, uri)) {
            return;
        }
        int port = getPort(uri);
        // Trúng danh sách trắng thì cho qua ngay (tên miền, IP, dải mạng, địa chỉ:cổng), không phụ thuộc vào công tắc; địa chỉ link-local (metadata đám mây),0.0.0.0 vẫn bị từ chối
        if (isInWhiteList(host, addresses, port, whiteList)) {
            for (InetAddress addr : addresses) {
                if (isNeverAllowedAddress(addr)) {
                    throw new JeecgBootException("URL không hợp lệ: cấm truy cập địa chỉ link-local hoặc địa chỉ bất kỳ " + addr.getHostAddress());
                }
            }
            return;
        }
        String hint = ", nếu cần cho qua, vui lòng cấu hình trong jeecg.firewall.localAddressWhiteList địa chỉ đó, hoặc địa chỉ:cổng (ví dụ " + (host.contains(":") ? "[" + host + "]" : host) + ":" + port + "）";
        for (InetAddress addr : addresses) {
            if (addr.isLoopbackAddress()) {
                throw new JeecgBootException("URL không hợp lệ: Cấm truy cập máy local hoặc địa chỉ link-local " + addr.getHostAddress() + hint);
            }
            checkDangerousAddress(addr, addr);
            if (blockPrivate && isPrivateAddress(addr)) {
                throw new JeecgBootException("URL không hợp lệ: cấm truy cập địa chỉ mạng nội bộ " + addr.getHostAddress() + hint);
            }
        }
    }

    /**
     * Có trúng danh sách trắng hay không: trúng"Địa chỉ:Cổng", hoặc mỗi kết quả phân tích đều trúng tên miền/IP/CIDR mục dải mạng
     */
    private static boolean isInWhiteList(String host, InetAddress[] addresses, int port, List<String> whiteList) {
        if (whiteList == null || whiteList.isEmpty() || addresses.length == 0) {
            return false;
        }
        if (isInHostPortWhiteList(addresses, port, whiteList)) {
            return true;
        }
        for (InetAddress addr : addresses) {
            if (!isInLocalAddressWhiteList(host, addr, whiteList)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cổng đích, khi không ghi cổng thì https là 443, còn lại là 80
     */
    private static int getPort(URI uri) {
        return uri.getPort() != -1 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
    }

    /**
     * Có trúng mục trong danh sách trắng hay không"Địa chỉ:Cổng"mục: cổng phải khớp, và mọi kết quả phân tích của đích đều thuộc về kết quả phân tích địa chỉ của mục đó (khi viết localhost:9999 giờ 127.0.0.1:9999 cũng trúng)
     */
    private static boolean isInHostPortWhiteList(InetAddress[] addresses, int port, List<String> whiteList) {
        if (whiteList == null || whiteList.isEmpty() || addresses.length == 0) {
            return false;
        }
        for (String entry : whiteList) {
            if (StringUtils.isBlank(entry)) {
                continue;
            }
            String[] hostPort = parseHostPort(entry.trim());
            if (hostPort == null || Integer.parseInt(hostPort[1]) != port) {
                continue;
            }
            try {
                List<InetAddress> allowed = Arrays.asList(InetAddress.getAllByName(hostPort[0]));
                if (Arrays.stream(addresses).allMatch(allowed::contains)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("SSRF Cấu hình danh sách trắng không thể phân tích, đã bỏ qua: {}", entry);
            }
        }
        return false;
    }

    /**
     * Phân tích cú pháp"Địa chỉ:Cổng"mục:host:port hoặc [IPv6]:port, trả về {host, port}; IP, CIDR, tên miền, IPv6 không có dấu ngoặc vuông trả về null
     */
    private static String[] parseHostPort(String item) {
        if (item.startsWith("[")) {
            int end = item.indexOf("]:");
            return end > 0 && item.substring(end + 2).matches("\\d{1,5}") ? new String[]{item.substring(1, end), item.substring(end + 2)} : null;
        }
        int idx = item.indexOf(':');
        if (idx <= 0 || idx != item.lastIndexOf(':') || item.contains("/") || !item.substring(idx + 1).matches("\\d{1,5}")) {
            return null;
        }
        return new String[]{item.substring(0, idx), item.substring(idx + 1)};
    }

    /**
     * Địa chỉ mà danh sách trắng cũng không thể cho qua: địa chỉ link-local (bao gồm metadata đám mây 169.254.169.254）、0.0.0.0/8、[::], địa chỉ chuyển đổi IPv6 được đánh giá theo IPv4 được nhúng
     */
    private static boolean isNeverAllowedAddress(InetAddress addr) {
        // ::1 Trên byte có dạng địa chỉ tương thích IPv4 (nhúng 0.0.0.1), thuộc về loopback của máy này, không đánh giá theo địa chỉ được nhúng
        if (addr.isLoopbackAddress()) {
            return false;
        }
        byte[] b = addr.getAddress();
        if (addr.isLinkLocalAddress() || addr.isAnyLocalAddress() || (b.length == 4 && b[0] == 0)) {
            return true;
        }
        for (InetAddress embedded : getEmbeddedIpv4(addr)) {
            if (isNeverAllowedAddress(embedded)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm chứng từng địa chỉ: máy này, địa chỉ link-local (bao gồm metadata đám mây 169.254.169.254）、0.0.0.0/8、[::] đều bị từ chối;
     * IPv6 Địa chỉ chuyển đổi (NAT64, 6to4, Teredo, tương thích IPv4/ánh xạ) lấy IPv4 nhúng bên trong rồi kiểm chứng theo cùng quy tắc
     *
     * @param addr     Địa chỉ đang kiểm chứng
     * @param original Địa chỉ phân giải gốc (dùng cho gợi ý)
     */
    private static void checkDangerousAddress(InetAddress addr, InetAddress original) {
        byte[] b = addr.getAddress();
        boolean thisNetwork = b.length == 4 && b[0] == 0;
        if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isAnyLocalAddress() || thisNetwork) {
            throw new JeecgBootException("URL không hợp lệ: Cấm truy cập máy local hoặc địa chỉ link-local " + original.getHostAddress());
        }
        for (InetAddress embedded : getEmbeddedIpv4(addr)) {
            checkDangerousAddress(embedded, original);
        }
    }

    /**
     * Lấy địa chỉ IPv4 nhúng trong địa chỉ chuyển tiếp IPv6; địa chỉ không phải chuyển tiếp trả về danh sách rỗng
     * <ul>
     *     <li>IPv4 Tương thích ::a.b.c.d、Ánh xạ IPv4 ::ffff:a.b.c.d</li>
     *     <li>NAT64 64:ff9b::/96、64:ff9b:1::/48(lấy 32 bit cuối)</li>
     *     <li>6to4 2002::/16(thứ  3~6 byte)</li>
     *     <li>Teredo 2001:0::/32(địa chỉ máy chủ thứ  5~8 byte; 32 bit cuối của địa chỉ máy khách được đảo bit)</li>
     * </ul>
     */
    private static List<InetAddress> getEmbeddedIpv4(InetAddress addr) {
        List<InetAddress> result = new ArrayList<>();
        if (!(addr instanceof Inet6Address)) {
            return result;
        }
        byte[] b = addr.getAddress();
        try {
            boolean first10Zero = true;
            for (int i = 0; i < 10; i++) {
                if (b[i] != 0) {
                    first10Zero = false;
                    break;
                }
            }
            boolean compatOrMapped = first10Zero && ((b[10] == 0 && b[11] == 0) || (b[10] == (byte) 0xff && b[11] == (byte) 0xff));
            boolean nat64 = b[0] == 0x00 && b[1] == 0x64 && b[2] == (byte) 0xff && b[3] == (byte) 0x9b;
            if (compatOrMapped || nat64) {
                result.add(InetAddress.getByAddress(Arrays.copyOfRange(b, 12, 16)));
            } else if (b[0] == 0x20 && b[1] == 0x02) {
                result.add(InetAddress.getByAddress(Arrays.copyOfRange(b, 2, 6)));
            } else if (b[0] == 0x20 && b[1] == 0x01 && b[2] == 0x00 && b[3] == 0x00) {
                result.add(InetAddress.getByAddress(Arrays.copyOfRange(b, 4, 8)));
                byte[] client = Arrays.copyOfRange(b, 12, 16);
                for (int i = 0; i < client.length; i++) {
                    client[i] = (byte) ~client[i];
                }
                result.add(InetAddress.getByAddress(client));
            }
        } catch (UnknownHostException ignored) {
            // 4 Mảng byte sẽ không kích hoạt
        }
        return result;
    }

    /**
     * Có phải địa chỉ mạng nội bộ không:10/8、172.16/12、192.168/16、100.64/10(NAT cấp nhà mạng)、fc00::/7(địa chỉ cục bộ duy nhất IPv6), địa chỉ chuyển tiếp IPv6 được phán đoán theo IPv4 nhúng bên trong
     */
    private static boolean isPrivateAddress(InetAddress addr) {
        byte[] b = addr.getAddress();
        if (addr.isSiteLocalAddress()) {
            return true;
        }
        if (b.length == 4 && (b[0] & 0xff) == 100 && (b[1] & 0xc0) == 64) {
            return true;
        }
        if (b.length == 16 && (b[0] & 0xfe) == 0xfc) {
            return true;
        }
        for (InetAddress embedded : getEmbeddedIpv4(addr)) {
            if (isPrivateAddress(embedded)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Một địa chỉ có trúng tên miền trong danh sách trắng hay không/IP/CIDR Mục: tên miền khớp chính xác (không phân biệt hoa thường), IP khớp chính xác hoặc khớp dải mạng CIDR
     */
    private static boolean isInLocalAddressWhiteList(String host, InetAddress addr, List<String> whiteList) {
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }
        byte[] target = addr.getAddress();
        for (String entry : whiteList) {
            if (StringUtils.isBlank(entry)) {
                continue;
            }
            String item = entry.trim();
            // "Địa chỉ:Cổng" Mục "địa chỉ:cổng" do isInHostPortWhiteList xử lý
            if (parseHostPort(item) != null) {
                continue;
            }
            if (item.equalsIgnoreCase(host)) {
                return true;
            }
            try {
                String ip = item;
                int prefix = -1;
                if (item.contains("/")) {
                    ip = item.substring(0, item.indexOf('/'));
                    prefix = Integer.parseInt(item.substring(item.indexOf('/') + 1).trim());
                }
                // Chỉ phân giải theo IP dạng literal, mục không phải IP (tên miền) sẽ bị bỏ qua để tránh kích hoạt truy vấn DNS
                if (!ip.matches("^[0-9.]+$") && !ip.contains(":")) {
                    continue;
                }
                byte[] net = InetAddress.getByName(ip).getAddress();
                if (net.length != target.length) {
                    continue;
                }
                int bits = prefix < 0 ? net.length * 8 : prefix;
                if (matchPrefix(net, target, bits)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("SSRF Cấu hình danh sách trắng không thể phân tích, đã bỏ qua: {}", item);
            }
        }
        return false;
    }

    /**
     * So sánh xem bits bit đầu của hai địa chỉ có giống nhau không
     */
    private static boolean matchPrefix(byte[] net, byte[] target, int bits) {
        int fullBytes = bits / 8;
        for (int i = 0; i < fullBytes; i++) {
            if (net[i] != target[i]) {
                return false;
            }
        }
        int rest = bits % 8;
        if (rest == 0) {
            return true;
        }
        int mask = (0xff << (8 - rest)) & 0xff;
        return (net[fullBytes] & mask) == (target[fullBytes] & mask);
    }

    /**
     * Có phải đang truy cập chính dịch vụ này không: kết quả phân giải toàn bộ là địa chỉ máy cục bộ (loopback hoặc IP card mạng của máy cục bộ), và cổng bằng cổng thực tế mà dịch vụ này đang lắng nghe.
     * Cổng lấy từ chính máy chủ (cổng connector của yêu cầu hoặc  server.port), không đọc request header nên không thể giả mạo; các cổng khác trên máy cục bộ (Redis, cơ sở dữ liệu, v.v.) vẫn bị chặn theo quy tắc
     */
    private static boolean isSelfServiceAddress(InetAddress[] addresses, URI uri) {
        if (addresses == null || addresses.length == 0) {
            return false;
        }
        int targetPort = uri.getPort() != -1 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
        Integer selfPort = getSelfServerPort();
        if (selfPort == null || selfPort != targetPort) {
            return false;
        }
        for (InetAddress addr : addresses) {
            if (!isLocalMachineAddress(addr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Có phải địa chỉ máy cục bộ không: địa chỉ loopback, hoặc IP được gắn trên một card mạng nào đó của máy cục bộ (0.0.0.0、multicast không tính)
     */
    private static boolean isLocalMachineAddress(InetAddress addr) {
        if (addr.isLoopbackAddress()) {
            return true;
        }
        if (addr.isAnyLocalAddress() || addr.isMulticastAddress()) {
            return false;
        }
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cổng thực tế dịch vụ này đang lắng nghe: khi có ngữ cảnh yêu cầu thì lấy cổng connector, nếu không thì đọc  server.port(mặc định 8080), nếu lấy thất bại trả về  null
     */
    private static Integer getSelfServerPort() {
        try {
            jakarta.servlet.http.HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            if (request != null) {
                return request.getLocalPort();
            }
        } catch (Exception ignored) {
            // Không có ngữ cảnh yêu cầu (tác vụ định kỳ, luồng bất đồng bộ, v.v.)
        }
        try {
            return SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.port", Integer.class, 8080);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Đọc cấu hình bảo mật nền tảng; môi trường không phải Spring (ví dụ kiểm thử đơn vị) trả về null, xử lý theo giá trị mặc định
     */
    private static Firewall getFirewall() {
        try {
            JeecgBaseConfig config = SpringContextUtils.getBean(JeecgBaseConfig.class);
            return config == null ? null : config.getFirewall();
        } catch (Exception e) {
            return null;
        }
    }
}
