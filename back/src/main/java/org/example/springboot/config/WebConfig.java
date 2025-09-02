package org.example.springboot.config;

import jakarta.annotation.Resource;
import org.example.springboot.util.FileUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web配置类，用于自定义Spring MVC的行为
 * 主要功能：
 * 1. 配置全局API路径前缀
 * 2. 配置JWT拦截器及其路径规则
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String API_PREFIX = "/api";
    
    // 定义不需要JWT验证的路径
    private static final String[] PUBLIC_PATHS = {
        "/api/user/login",      // 登录接口
        "/api/user/forget",     // 忘记密码接口
        "/api/user/add",        // 用户注册接口
        "/api/user/{id}",       // 用户信息查询接口
        "/api/email/**",        // 邮件相关接口
        "/api/img/**",          // 图片资源接口
        "/api/file/**",       // 文件资源接口
        "/api/common/**",     // 普通文件资源接口
        
        // Swagger和API文档相关路径
        "/api/v3/api-docs/**",
        "/api/swagger-ui.html",
        "/api/swagger-ui/**",
        "/api/doc.html",
        "/api/webjars/**",
        "/api/favicon.ico"
    };

    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置路径匹配规则
     * 为所有带有@RestController注解的控制器类添加统一的路径前缀
     * 这样可以将API接口与其他Web资源区分开
     *
     * @param configurer 路径匹配配置器
     */

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为带有RestController注解的类添加"/api"路径前缀
        // 排除 Knife4j/Swagger 相关的接口（通过包名判断）
        configurer.addPathPrefix("/api", clazz ->
                clazz.isAnnotationPresent(RestController.class) &&
                        !clazz.getPackage().getName().contains("springfox") &&
                        !clazz.getPackage().getName().contains("swagger")&&!clazz.getPackage().getName().contains("doc")
        );
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // 只拦截API请求
                .excludePathPatterns(
                        "/api/user/login",       // 登录接口
                        "/api/user/register",    // 注册接口 
                        "/api/user/forget",      // 忘记密码接口
                        "/api/user/add",         // 用户注册接口
                        "/api/email/**",         // 邮件相关接口
                        "/api/captcha/**",       // 验证码接口
                        "/api/img/**",           // 图片资源
                        "/api/file/**",          // 文件上传接口
                        "/api/common/**",        // 普通文件资源
                        // Swagger和文档相关路径
                        "/api/swagger-ui/**",    
                        "/api/swagger-resources/**", 
                        "/api/v3/api-docs/**",  
                        "/api/doc.html",        
                        "/api/webjars/**"       
                );
    }
    
    /**
     * 配置静态资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRootPath = Paths.get(FileUtil.FILE_BASE_PATH);
        
        // 图片资源
        String imgFolderPath = projectRootPath.resolve("img").toAbsolutePath().toString();
        registry.addResourceHandler("/api/img/**").addResourceLocations("file:" + imgFolderPath + "/");
        
        // 普通文件资源
        String commonFolderPath = projectRootPath.resolve("common").toAbsolutePath().toString();
        registry.addResourceHandler("/api/common/**").addResourceLocations("file:" + commonFolderPath + "/");
        
        // Swagger和API文档资源
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 配置CORS跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
