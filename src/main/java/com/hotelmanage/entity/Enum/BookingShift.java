package com.hotelmanage.entity.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingShift {

    SANG  ("Ca sáng",  "07:00", "10:00"),
    TRUA  ("Ca trưa",  "11:00", "14:00"),
    CHIEU ("Ca chiều", "14:00", "17:00"),
    TOI   ("Ca tối",   "18:00", "22:00");

    private final String displayName;
    private final String startTime;
    private final String endTime;

    /** Hiển thị đầy đủ: "Ca sáng (07:00 - 10:00)" */
    public String getLabel() {
        return displayName + " (" + startTime + " - " + endTime + ")";
    }
}
