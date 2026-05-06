package cn.edu.nwpu.sonnx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 Web 配置类
 * 用于处理跨域请求，确保前端 IDE 能够顺利调用后端编译器 API
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有来自前端开发服务器的跨域请求
        registry.addMapping("/api/**") // 匹配你定义的编译器接口路径
                .allowedOrigins("*")    // 生产环境建议指定具体域名，开发阶段可设为 *
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}