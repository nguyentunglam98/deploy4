package com.example.webDemo3.dto.manageTimeTableResponseDto;

import lombok.Data;

/**
 * lamnt98
 * 03/07
 */
@Data
public class AfterInforTimeTableDto {
    private String teacherIdentifier;
    private String classIdentifier;
    private Integer slotId;
    private Integer dayId;
    private String subject;
    private Integer isOddWeek;
    private Integer isAdditional;
}
