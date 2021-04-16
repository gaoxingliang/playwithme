package scugxl.playwithme;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.jdbc.*;
import org.springframework.boot.web.servlet.*;
import org.springframework.context.annotation.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PlaywithmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaywithmeApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean filterRegBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("internalFilter");
        return registration;
    }

}
