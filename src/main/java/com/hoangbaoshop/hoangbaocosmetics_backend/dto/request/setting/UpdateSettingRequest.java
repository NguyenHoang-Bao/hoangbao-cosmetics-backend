package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.setting;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettingRequest {

    @NotBlank(message = "Giá trị cài đặt không được để trống")
    private String settingValue;
}
