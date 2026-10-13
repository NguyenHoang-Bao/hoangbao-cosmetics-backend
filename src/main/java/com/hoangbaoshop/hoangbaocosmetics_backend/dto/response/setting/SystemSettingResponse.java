package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.SettingDataType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettingResponse {
    private String settingKey;
    private String settingValue;
    private SettingDataType dataType;
    private String description;
    private Boolean isPublic;
    private LocalDateTime updatedAt;
}
