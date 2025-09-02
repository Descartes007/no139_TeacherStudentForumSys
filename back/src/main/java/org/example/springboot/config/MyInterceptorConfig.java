package org.example.springboot.config;

import jakarta.annotation.Resource;
import org.example.springboot.util.FileUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MyInterceptorConfig implements WebMvcConfigurer {
    
    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/forget",
                        "/api/email/**",
                        "/api/captcha/**",
                        "/api/img/**",
                        "/api/file/**",
                        "/api/swagger-ui/**",
                        "/api/swagger-resources/**",
                        "/api/v3/api-docs/**",
                        "/api/doc.html",
                        "/api/webjars/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRootPath = null;
        projectRootPath = Paths.get(FileUtil.FILE_BASE_PATH);
        String imgFolderPath = projectRootPath.resolve("img").toAbsolutePath().toString();
        registry.addResourceHandler("/api/img/**").addResourceLocations("file:" + imgFolderPath + "/");
        
        // 添加对common文件夹的静态资源映射
        String commonFolderPath = projectRootPath.resolve("common").toAbsolutePath().toString();
        registry.addResourceHandler("/api/common/**").addResourceLocations("file:" + commonFolderPath + "/");

        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
