package org.jeecg.modules.monitor.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: redis Thông tin
 * @author: jeecg-boot
 */
public class RedisInfo {

	private static Map<String, String> map = new HashMap(5);

	static {
		map.put("redis_version", "Redis Phiên bản máy chủ");
		map.put("redis_git_sha1", "Git SHA1");
		map.put("redis_git_dirty", "Git dirty flag");
		map.put("os", "Redis Hệ điều hành máy chủ lưu trữ của máy chủ Redis");
		map.put("arch_bits", " Kiến trúc (32 hoặc 64 bit)");
		map.put("multiplexing_api", "Redis Cơ chế xử lý sự kiện đang sử dụng");
		map.put("gcc_version", "Phiên bản GCC được sử dụng khi biên dịch Redis");
		map.put("process_id", "Tiến trình máy chủ PID");
		map.put("run_id", "Redis Định danh ngẫu nhiên của máy chủ Redis (dùng cho Sentinel và cụm)");
		map.put("tcp_port", "TCP/IP Lắng nghe cổng");
		map.put("uptime_in_seconds", "Số giây đã trôi qua kể từ khi máy chủ Redis khởi động");
		map.put("uptime_in_days", "Số ngày đã trôi qua kể từ khi máy chủ Redis khởi động");
		map.put("lru_clock", " Đồng hồ tự tăng theo đơn vị phút, dùng để quản lý LRU");
		map.put("connected_clients", "Số lượng client đã kết nối (không bao gồm các client kết nối qua máy chủ phụ thuộc)");
		map.put("client_longest_output_list", "Trong số các máy khách đang kết nối, danh sách đầu ra dài nhất");
		map.put("client_longest_input_buf", "Trong số các máy khách đang kết nối, bộ nhớ đệm đầu vào lớn nhất");
		map.put("blocked_clients", "Số lượng client đang chờ lệnh blocking (BLPOP, BRPOP, BRPOPLPUSH)");
		map.put("used_memory", "Tổng dung lượng bộ nhớ được bộ cấp phát của Redis phân bổ, tính bằng byte");
		map.put("used_memory_human", "Trả về tổng lượng bộ nhớ Redis đã cấp phát theo định dạng con người có thể đọc được");
		map.put("used_memory_rss", "Xét từ góc độ hệ điều hành, trả về tổng dung lượng bộ nhớ mà Redis đã cấp phát (thường gọi là kích thước resident set). Giá trị này khớp với kết quả của các lệnh top, ps");
		map.put("used_memory_peak", " Redis Mức tiêu thụ bộ nhớ đỉnh của Redis((tính bằng byte))");
		map.put("used_memory_peak_human", "Trả về mức đỉnh tiêu thụ bộ nhớ của Redis theo định dạng con người có thể đọc được");
		map.put("used_memory_lua", "Lua Kích thước bộ nhớ mà engine đang sử dụng (tính bằng byte)");
		map.put("mem_fragmentation_ratio", "sed_memory_rss và used_memory tỷ lệ giữa");
		map.put("mem_allocator", "Được chỉ định lúc biên dịch, bộ cấp phát bộ nhớ mà Redis sử dụng. Có thể là libc, jemalloc hoặc tcmalloc");

		map.put("redis_build_id", "redis_build_id");
		map.put("redis_mode", "Chế độ chạy: máy đơn (standalone) hoặc cụm (cluster)");
		map.put("atomicvar_api", "atomicvar_api");
		map.put("hz", "redis Tần suất lập lịch nội bộ (đóng các client timeout, xóa key hết hạn, v.v.); chương trình quy định serverCron chạy 10 lần mỗi giây.");
		map.put("executable", "server thư mục script");
		map.put("config_file", "Thư mục tệp cấu hình");
		map.put("client_biggest_input_buf", "Trong các client đang kết nối, bộ đệm đầu vào lớn nhất; dùng lệnh client list để quan sát qbuf và qbuf-free Giá trị lớn nhất của hai trường");
		map.put("used_memory_rss_human", "Trả về tổng lượng bộ nhớ Redis đã cấp phát theo cách con người có thể đọc được");
		map.put("used_memory_peak_perc", "Tỷ lệ sử dụng bộ nhớ cao nhất");
		map.put("total_system_memory", "Tổng bộ nhớ hệ thống");
		map.put("total_system_memory_human", "Trả về tổng bộ nhớ hệ thống theo cách con người có thể đọc được");
		map.put("used_memory_lua_human", "Trả về kích thước bộ nhớ mà engine Lua đang sử dụng theo cách con người có thể đọc được");
		map.put("maxmemory", "Giới hạn bộ nhớ tối đa, 0 nghĩa là không giới hạn");
		map.put("maxmemory_human", "Trả về bộ nhớ giới hạn tối đa theo cách con người có thể đọc được");
		map.put("maxmemory_policy", "Chính sách xử lý khi vượt quá giới hạn bộ nhớ");
		map.put("loading", "Máy chủ có đang tải tệp persist không");
		map.put("rdb_changes_since_last_save", "Số lệnh ghi kể từ lần tạo file rdb thành công gần nhất, tức là có bao nhiêu lệnh ghi chưa được lưu bền");
		map.put("rdb_bgsave_in_progress", "Máy chủ có đang tạo tệp rdb không");
		map.put("rdb_last_save_time", "Thời gian tính từ lần tạo file rdb thành công gần nhất. Timestamp hiện tại - rdb_last_save_time=Đã bao nhiêu giây chưa tạo thành công tệp rdb");
		map.put("rdb_last_bgsave_status", "Lần persist rdb gần nhất có thành công không");
		map.put("rdb_last_bgsave_time_sec", "Số giây tốn để tạo thành công tệp rdb lần gần nhất");
		map.put("rdb_current_bgsave_time_sec", "Nếu máy chủ đang tạo file rdb thì trường này ghi lại số giây mà thao tác tạo hiện tại đã tiêu tốn");
		map.put("aof_enabled", "Đã bật hay chưa aof");
		map.put("aof_rewrite_in_progress", "Cho biết thao tác rewrite AOF có đang diễn ra hay không");
		map.put("aof_rewrite_scheduled", "rewrite Kế hoạch tác vụ: khi client gửi lệnh bgrewriteaof, nếu tiến trình con rewrite hiện đang chạy thì yêu cầu bgrewriteaof của client sẽ được chuyển thành tác vụ theo kế hoạch và thực thi sau khi tiến trình con aof kết thúc rewrite ");

		map.put("aof_last_rewrite_time_sec", "Thời gian tốn cho lần rewrite AOF gần nhất");
		map.put("aof_current_rewrite_time_sec", "Nếu thao tác rewrite đang diễn ra, ghi lại thời gian đã sử dụng, đơn vị là giây");
		map.put("aof_last_bgrewrite_status", "Trạng thái của thao tác bgrewrite AOF gần nhất");
		map.put("aof_last_write_status", "Trạng thái ghi aof lần trước");

		map.put("total_commands_processed", "redis số lệnh đã xử lý");
		map.put("total_connections_received", "Số kết nối mới được tạo,Nếu số kết nối mới tạo quá nhiều, việc tạo và hủy kết nối quá mức sẽ ảnh hưởng đến hiệu năng, cho thấy tình trạng kết nối ngắn nghiêm trọng hoặc cấu hình connection pool có vấn đề, cần khảo sát cài đặt kết nối trong mã");
		map.put("instantaneous_ops_per_sec", "redis QPS hiện tại, số lệnh Redis thực thi mỗi giây khá realtime trong nội bộ");
		map.put("total_net_input_bytes", "redis Số byte lưu lượng mạng vào");
		map.put("total_net_output_bytes", "redis Số byte lưu lượng mạng ra");

		map.put("instantaneous_input_kbps", "redis lưu lượng mạng vào kps");
		map.put("instantaneous_output_kbps", "redis lưu lượng mạng ra kps");
		map.put("rejected_connections", "Số kết nối bị từ chối; khi số kết nối redis đạt giới hạn maxclients thì số kết nối mới bị từ chối");
		map.put("sync_full", "Số lần đồng bộ toàn phần chính-phụ thành công");

		map.put("sync_partial_ok", "Số lần đồng bộ một phần chính-phụ thành công");
		map.put("sync_partial_err", "Số lần đồng bộ một phần chính-phụ thất bại");
		map.put("expired_keys", "Số lượng key đã hết hạn kể từ khi chạy");
		map.put("evicted_keys", "Đã loại bỏ kể từ khi chạy(Sau khi vượt quá maxmemory)số lượng key đã bị loại bỏ (sau khi vượt quá maxmemory) kể từ khi chạy");
		map.put("keyspace_hits", "Số lần trúng");
		map.put("keyspace_misses", "Số lần không trúng");
		map.put("pubsub_channels", "Số lượng kênh đang sử dụng");
		map.put("pubsub_patterns", "Số lượng mẫu (pattern) đang sử dụng");
		map.put("latest_fork_usec", "Thời gian gần nhất thao tác fork làm nghẽn tiến trình Redis, đơn vị micro giây");
		map.put("role", "Vai trò của instance, là master or slave");
		map.put("connected_slaves", "Số lượng instance slave đang kết nối");
		map.put("master_repl_offset", "Độ lệch đồng bộ chính-phụ,Nếu giá trị này giống với offset phía trên thì nghĩa là master-slave nhất quán, không có độ trễ");
		map.put("repl_backlog_active", "Bộ đệm backlog sao chép có đang bật hay không");
		map.put("repl_backlog_size", "Kích thước bộ đệm tích lũy sao chép");
		map.put("repl_backlog_first_byte_offset", "Kích thước độ lệch trong bộ đệm sao chép");
		map.put("repl_backlog_histlen", "Giá trị này bằng master_repl_offset - repl_backlog_first_byte_offset,giá trị này sẽ không vượt quá repl_backlog_size kích thước");
		map.put("used_cpu_sys", "Cộng dồn tổng thời gian CPU ở chế độ kernel mà tất cả tiến trình chính của Redis chiếm dụng");
		map.put("used_cpu_user", "Cộng dồn tổng thời gian CPU ở chế độ user mà tất cả tiến trình chính của Redis chiếm dụng");
		map.put("used_cpu_sys_children", "Cộng dồn tổng thời gian CPU ở chế độ kernel mà các tiến trình nền chiếm dụng");
		map.put("used_cpu_user_children", "Cộng dồn tổng thời gian CPU ở chế độ user mà các tiến trình nền chiếm dụng");
		map.put("cluster_enabled", "Phiên bản có bật chế độ cụm hay không");
		map.put("db0", "db0 số lượng key đã bị loại bỏ (sau khi vượt quá maxmemory) kể từ khi chạy,Và số lượng key có thời gian sống,thời gian sống trung bình");

	}

	private String key;
	private String value;
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		this.description = map.get(this.key);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "RedisInfo{" + "key='" + key + '\'' + ", value='" + value + '\'' + ", desctiption='" + description + '\'' + '}';
	}
}
