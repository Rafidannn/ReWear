package com.example.application.config;

import com.example.application.model.product.*;
import com.example.application.model.user.*;
import com.example.application.repository.product.CategoryRepository;
import com.example.application.repository.product.ProductRepository;
import com.example.application.repository.user.SchoolRepository;
import com.example.application.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(SchoolRepository schoolRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            School school = schoolRepository.findAll().stream().findFirst().orElseGet(() -> {
                School s = new School("SMKN 24 Jakarta", "20101234", "Jl. Bambu Apus No. 1", "Jakarta Timur");
                return schoolRepository.save(s);
            });

            User seller = userRepository.findByEmail("budi@smkn24.sch.id").orElseGet(() -> {
                User u = new User();
                u.setFullName("Budi Warga SMKN 24");
                u.setEmail("budi@smkn24.sch.id");
                u.setPhone("08123456789");
                u.setPasswordHash("password123");
                u.setRole(Role.BUYER_SELLER);
                u.setSchool(school);
                u.setAccountStatus(AccountStatus.ACTIVE);
                return userRepository.save(u);
            });

            Category pakaian = categoryRepository.findBySlug("pakaian").orElseGet(() ->
                    categoryRepository.save(new Category("Pakaian", "pakaian", "tshirt", 1)));
            Category buku = categoryRepository.findBySlug("buku").orElseGet(() ->
                    categoryRepository.save(new Category("Buku", "buku", "book", 2)));
            Category elektronik = categoryRepository.findBySlug("elektronik").orElseGet(() ->
                    categoryRepository.save(new Category("Elektronik", "elektronik", "laptop", 3)));
            Category peralatan = categoryRepository.findBySlug("peralatan").orElseGet(() ->
                    categoryRepository.save(new Category("Peralatan", "peralatan", "tools", 4)));

            // Products matching the design
            Product p1 = new Product();
            p1.setName("Buku Resep & Alat Pastry Set");
            p1.setDescription("Set lengkap buku resep kue & alat pastry terawat.");
            p1.setPrice(new BigDecimal("85000"));
            p1.setImages("[\"images/buku.jpeg\"]");
            p1.setCategory(buku);
            p1.setSeller(seller);
            p1.setConditionType(ConditionType.BEKAS);
            p1.setSchoolMarket(true);
            p1.setStatus(ProductStatus.ACTIVE);
            p1.setStock(5);
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setName("Colokan Multi-Plug Portable");
            p2.setDescription("Colokan listrik serbaguna cocok untuk lab dan kegiatan belajar.");
            p2.setPrice(new BigDecimal("45000"));
            p2.setImages("[\"images/colokan.webp\"]");
            p2.setCategory(elektronik);
            p2.setSeller(seller);
            p2.setConditionType(ConditionType.BEKAS);
            p2.setSchoolMarket(true);
            p2.setStatus(ProductStatus.ACTIVE);
            p2.setStock(3);
            productRepository.save(p2);

            Product p3 = new Product();
            p3.setName("Kipas Angin Meja Belajar");
            p3.setDescription("Kipas angin portable hemat energi angin kencang untuk meja belajar.");
            p3.setPrice(new BigDecimal("65000"));
            p3.setImages("[\"images/kipas.jpg\"]");
            p3.setCategory(elektronik);
            p3.setSeller(seller);
            p3.setConditionType(ConditionType.BEKAS);
            p3.setSchoolMarket(true);
            p3.setStatus(ProductStatus.ACTIVE);
            p3.setStock(2);
            productRepository.save(p3);

            Product p4 = new Product();
            p4.setName("Set Pulpen Belajar High Quality");
            p4.setDescription("Set pulpen tinta halus cocok untuk ujian dan catatan sekolah.");
            p4.setPrice(new BigDecimal("15000"));
            p4.setImages("[\"images/pulpen.webp\"]");
            p4.setCategory(peralatan);
            p4.setSeller(seller);
            p4.setConditionType(ConditionType.BARU);
            p4.setSchoolMarket(false);
            p4.setStatus(ProductStatus.ACTIVE);
            p4.setStock(10);
            productRepository.save(p4);
        }
    }
}
