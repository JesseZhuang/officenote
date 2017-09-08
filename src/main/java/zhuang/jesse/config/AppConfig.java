package zhuang.jesse.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import zhuang.jesse.OfficeNotes;

@Configuration
@ComponentScan(basePackageClasses = {OfficeNotes.class})
public class AppConfig {
    // @Bean methods here can reference @Bean methods in imported configs
}
