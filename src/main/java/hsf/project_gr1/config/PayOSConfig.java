package hsf.project_gr1.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Data
@Configuration
@ConfigurationProperties(prefix = "payos")
public class PayOSConfig {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String returnUrl;
    private String cancelUrl;

    @Bean
    public PayOS payOS() {
        return new PayOS(
                ClientOptions.builder()
                        .clientId(clientId)
                        .apiKey(apiKey)
                        .checksumKey(checksumKey)
                        .build()
        );
    }
}
