package by.iyunski.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

    @Bean
    public OpenAPI profileOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("Library API")
                        .description("Library application. Test task for applying on trainee java developer position in Digital Chief.")
                        .version("v1.0.0")
                        .license(
                                (new License())
                                        .name("Apache 2.0")
                                        .url("http://springdoc.org")));
    }
}
