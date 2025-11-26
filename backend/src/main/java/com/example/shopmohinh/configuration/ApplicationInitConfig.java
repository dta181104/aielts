package com.example.shopmohinh.configuration;

import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.entity.Role;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.repository.RoleRepository;
import com.example.shopmohinh.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

//@Configuration sẽ tự động chạy khi sạc application
@Configuration
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
//Để có thể logger được
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    RoleRepository roleRepository;

    //ApplicationRunner sẽ đc khởi chạy mỗi khi application sạc lên
    //mục đích ở đây là auto khởi tạo 1 user admin nếu chưa có admin khi sạc application
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){

        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                User account = User.builder()
                        .username("admin")
                        .pass(passwordEncoder.encode("admin"))
                        .email("giangdaica10x@gmail.com")
                        .name("admin")
                        .build();

                Set<Role> roles = new HashSet<>();
                roles.add(roleRepository.findRoleByCode("ADMIN").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));
//                roles.add(roleRepository.findRoleByCode("STAFF").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));
                roles.add(roleRepository.findRoleByCode("USER").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)));

                account.setRoles(roles); // Thiết lập quyền cho tài khoản

                // Tạo mã tài khoản tự động
                if (userRepository.getTop1() == null) {
                    account.setCode("ACC1");
                } else {
                    //Lấy giá trị code đầu tiên
                    String lastCode = userRepository.getTop1().getCode();

                    // Đảm bảo độ dài mã tài khoản đủ để cắt
                    if (lastCode.length() > 3) {
                        String prefix = lastCode.substring(0, 3); // 3 ký tự đầu
                        int number = Integer.parseInt(lastCode.substring(3)); // Phần số sau
                        account.setCode(prefix + (number + 1)); // Tạo mã mới
                    } else {
                        // Nếu mã cũ quá ngắn, sử dụng mã mặc định
                        account.setCode("ACC1");
                    }
                }
                System.out.println("Mã tài khoản mới: " + account.getCode());

                userRepository.save(account);
                log.warn("admin was has been created with default username: admin, password: admin, please change it");
            }
        };
    }
}
