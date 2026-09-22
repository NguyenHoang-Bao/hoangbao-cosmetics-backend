package com.hoangbaoshop.hoangbaocosmetics_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {

    private VietQr vietqr = new VietQr();
    private Sepay sepay = new Sepay();

    @Getter
    @Setter
    public static class VietQr {
        private String bankId = "MBBank";
        private String accountNumber = "0359888999";
        private String accountName = "NGUYEN HOANG BAO";
        private String template = "compact2";
    }

    @Getter
    @Setter
    public static class Sepay {
        private String apiKey = "SEPAY_API_KEY_SECRET";
    }
}
