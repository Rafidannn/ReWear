package com.example.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Mendaftarkan direktori upload foto produk sebagai static resource handler.
 * File yang ditulis ke {project_root}/uploads/ langsung bisa diakses
 * via URL /images/uploads/{filename} tanpa perlu restart server.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Direktori uploads berada di root project, di luar classpath
    // sehingga Vaadin/Vite dev server tidak memblokir akses ke file baru.
    public static final String UPLOAD_BASE_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Pastikan direktori sudah ada
        File uploadDir = new File(UPLOAD_BASE_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Map URL /images/uploads/** → filesystem {project_root}/uploads/
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:" + UPLOAD_BASE_DIR)
                .setCachePeriod(0); // Non-cache agar file baru langsung muncul
    }
}
