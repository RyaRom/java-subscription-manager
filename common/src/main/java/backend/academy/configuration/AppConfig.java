package backend.academy.configuration;

import java.util.Arrays;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Log4j2
public class AppConfig {
    @Bean
    public EnvType envType(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        log.info("Current profiles: {}", Arrays.toString(profiles));
        if (profiles.length < 1) {
            return EnvType.UNKNOWN;
        }
        return EnvType.getFromType(profiles[0]);
    }
}
