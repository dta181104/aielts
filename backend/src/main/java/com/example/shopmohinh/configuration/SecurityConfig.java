package com.example.shopmohinh.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
//phân quyền bằng method -> UserService
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {"/users/register", "/auth/**"};

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers(HttpMethod.GET, "/product/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/users/register").permitAll()
                                .requestMatchers("/momo/**").permitAll()
                                .requestMatchers("/permissions/**").permitAll()
                                .requestMatchers("/payment/**").permitAll()
                                .requestMatchers("/ghn/**").permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                .anyRequest().authenticated())
                //Xét quyền của user qua token
//       httpSecurity.oauth2ResourceServer(oauth2 ->...) chấp nhận và xác thực quyền qua token OAuth2
                .oauth2ResourceServer(oauth2 ->

//              oauth2.jwt(jwtConfigurer -> ...) chỉ định rằng máy chủ tài nguyên sẽ sử dụng JWT để xác thực token
                                oauth2.jwt(jwtConfigurer ->

//              jwtConfigurer.decoder(jwtDecoder()) tự thiết lập 1 bộ giải mã token tên là jwtDecoder(),
//              dùng để giải mã token
                                                jwtConfigurer.decoder(customJwtDecoder)

//                      Sử dụng thiết lập đã tạo để chuyển đổi đối tượng trong JWT thành 1 đối tượng xác thực trong spring security
                                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                                        .and()

//                      Sử dụng thiết lập đã tạo để đưa ra phản hồi mong muốn khi yêu cầu xác thực thất bại
                                                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                                )
                );

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    //  Tự tạo 1 bộ xác thức JWT cho phép chuyển đổi đối tượng trong JWT thành 1 đối tượng xác thực trong spring security
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        //Chuyển từ SCOPE_ADMIN -> ROLE_ADMIN
        //Khi khai báo cho Prefix là rỗng thì tiền tố sẽ lấy theo token
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
