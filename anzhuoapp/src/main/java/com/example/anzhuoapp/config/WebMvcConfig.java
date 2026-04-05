package com.example.anzhuoapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 图片存放在 E:\Projects\anzhuo\repair_images\
    // Windows 路径写法：file:///E:/Projects/anzhuo/repair_images/
    private static final String UPLOAD_PATH = "file:E:/Projects/anzhuo/repair_images/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/repair_images/**")
                .addResourceLocations(UPLOAD_PATH);
    }
}