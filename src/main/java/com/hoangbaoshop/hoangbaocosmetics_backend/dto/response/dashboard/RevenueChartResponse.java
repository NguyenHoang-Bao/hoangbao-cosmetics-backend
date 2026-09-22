package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueChartResponse {

    private String period; // "2026-09-01" hoặc "Tháng 09/2026"
    private Double revenue;
    private Long orderCount;
}
