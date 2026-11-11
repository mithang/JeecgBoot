package org.jeecg.common.constant.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Lớp enum cấp bậc
 * 
 * Lưu ý: Enum này chỉ áp dụng cho dự án OA Tianjin Lingang Holdings,Tên và cấp bậc của chức danh đều được viết cứng (cần nhất quán với cấu hình cơ sở dữ liệu)
 * @date 2025-08-26
 * @author scott
 */
public enum PositionLevelEnum {

    // Cấp lãnh đạo (cấp 1-3）
    CHAIRMAN("Chủ tịch", 1, PositionType.LEADER),
    GENERAL_MANAGER("Tổng giám đốc", 2, PositionType.LEADER),
    VICE_GENERAL_MANAGER("Phó tổng giám đốc", 3, PositionType.LEADER),

    // Cấp nhân viên (cấp 4-6）
    MINISTER("Bộ trưởng", 4, PositionType.STAFF),
    VICE_MINISTER("Phó bộ trưởng", 5, PositionType.STAFF),
    STAFF("Nhân viên", 6, PositionType.STAFF);

    private final String name;
    private final int level;
    private final PositionType type;

    PositionLevelEnum(String name, int level, PositionType type) {
        this.name = name;
        this.level = level;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public PositionType getType() {
        return type;
    }

    /**
     * Enum loại cấp bậc
     */
    public enum PositionType {
        STAFF("Cấp nhân viên"),
        LEADER("Cấp lãnh đạo");

        private final String desc;

        PositionType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * Lấy enum theo tên cấp bậc
     * @param name Tên cấp bậc
     * @return Enum cấp bậc
     */
    public static PositionLevelEnum getByName(String name) {
        for (PositionLevelEnum position : values()) {
            if (position.getName().equals(name)) {
                return position;
            }
        }
        return null;
    }

    /**
     * Lấy enum theo cấp độ cấp bậc
     * @param level Cấp độ cấp bậc
     * @return Enum cấp bậc
     */
    public static PositionLevelEnum getByLevel(int level) {
        for (PositionLevelEnum position : values()) {
            if (position.getLevel() == level) {
                return position;
            }
        }
        return null;
    }

    /**
     * Xác định theo tên cấp bậc có phải cấp nhân viên hay không
     * @param name Tên cấp bậc
     * @return true-Cấp nhân viên,false-Không phải cấp nhân viên
     */
    public static boolean isStaffLevel(String name) {
        PositionLevelEnum position = getByName(name);
        return position != null && position.getType() == PositionType.STAFF;
    }

    /**
     * Xác định theo tên cấp bậc có phải cấp lãnh đạo hay không
     * @param name Tên cấp bậc
     * @return true-Cấp lãnh đạo,false-Không phải cấp lãnh đạo
     */
    public static boolean isLeaderLevel(String name) {
        PositionLevelEnum position = getByName(name);
        return position != null && position.getType() == PositionType.LEADER;
    }

    /**
     * So sánh cao thấp cấp độ của hai cấp bậc
     * @param name1 Tên cấp bậc 1
     * @param name2 Tên cấp bậc 2
     * @return Số dương nghĩa là cấp bậc name1 cao hơn, số âm nghĩa là cấp bậc name2 cao hơn, 0 nghĩa là cấp bậc bằng nhau
     */
    public static int compareLevel(String name1, String name2) {
        PositionLevelEnum pos1 = getByName(name1);
        PositionLevelEnum pos2 = getByName(name2);

        if (pos1 == null || pos2 == null) {
            return 0;
        }

        // Số cấp càng nhỏ thì cấp bậc càng cao
        return pos2.getLevel() - pos1.getLevel();
    }

    /**
     * Xác định có phải cấp cao hơn hay không
     * @param currentName Tên cấp bậc hiện tại
     * @param targetName Tên cấp bậc mục tiêu
     * @return true-Cấp bậc mục tiêu cao hơn,false-Cấp bậc mục tiêu không cao hơn cấp bậc hiện tại
     */
    public static boolean isHigherLevel(String currentName, String targetName) {
        return compareLevel(targetName, currentName) > 0;
    }

    /**
     * Lấy tên tất cả cấp nhân viên
     * @return Danh sách tên cấp nhân viên
     */
    public static List<String> getStaffLevelNames() {
        return Arrays.asList(MINISTER.getName(), VICE_MINISTER.getName(), STAFF.getName());
    }

    /**
     * Lấy tên tất cả cấp lãnh đạo
     * @return Danh sách tên cấp lãnh đạo
     */
    public static List<String> getLeaderLevelNames() {
        return Arrays.asList(CHAIRMAN.getName(), GENERAL_MANAGER.getName(), VICE_GENERAL_MANAGER.getName());
    }

    /**
     * Lấy tên tất cả cấp bậc (sắp xếp theo cấp độ)
     * @return Danh sách tên tất cả cấp bậc
     */
    public static List<String> getAllPositionNames() {
        return Arrays.asList(
                CHAIRMAN.getName(), GENERAL_MANAGER.getName(), VICE_GENERAL_MANAGER.getName(),
                MINISTER.getName(), VICE_MINISTER.getName(), STAFF.getName()
        );
    }

    /**
     * Lấy cấp bậc trong phạm vi cấp độ chỉ định
     * @param minLevel Cấp nhỏ nhất
     * @param maxLevel Cấp lớn nhất
     * @return Danh sách tên cấp bậc
     */
    public static List<String> getPositionsByLevelRange(int minLevel, int maxLevel) {
        return Arrays.stream(values())
                .filter(p -> p.getLevel() >= minLevel && p.getLevel() <= maxLevel)
                .map(PositionLevelEnum::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}