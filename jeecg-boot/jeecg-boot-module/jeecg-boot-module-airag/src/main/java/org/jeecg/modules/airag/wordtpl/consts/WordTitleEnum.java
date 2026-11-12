package org.jeecg.modules.airag.wordtpl.consts;

import lombok.Getter;

/**
 * @author chenrui
 * @ClassName: TitleLevelEnum
 * @Description: Cấp độ tiêu đề
 * @date 2024 năm ngày 4 tháng 5 07:38:30
 */
@Getter
public enum WordTitleEnum {

    FIRST("first", "Tiêu đề 1"),
    SECOND("second", "Tiêu đề 2"),
    THIRD("third", "Tiêu đề 3"),
    FOURTH("fourth", "Tiêu đề 4"),
    FIFTH("fifth", "Tiêu đề 5"),
    SIXTH("sixth", "Tiêu đề 6");

    WordTitleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    final String code;

    final String name;

}
