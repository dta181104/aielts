package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.AuthenticationRequest;
import com.example.shopmohinh.dto.request.IntrospectRequest;
import com.example.shopmohinh.dto.request.LogoutRequest;
import com.example.shopmohinh.dto.request.RefeshRequest;
import com.example.shopmohinh.dto.response.AuthenticationResponse;
import com.example.shopmohinh.dto.response.IntrospectResponse;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.InvalidatedToken;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.InvalidatedTokenRepository;
import com.example.shopmohinh.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import com.example.shopmohinh.configuration.EnvConfig;

//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class AuthenticationService {

    UserRepository userRepository;
    UserMapper userMapper;

    InvalidatedTokenRepository invalidatedTokenRepository;

    //@NonFinal để ko tiêm cái này vào contructor
    @NonFinal
    protected static final String SIGNER_KEY = EnvConfig.get("VNP_SIGNER_KEY");

    @NonFinal
    protected static final long VALID_DURATION = 30; // IN SECOND

    @NonFinal
    protected static final long REFRESHABLE_DURATION = 108000 ; // IN SECONDS

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isvalid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isvalid = false;
        }
        return IntrospectResponse.builder()
                .valid(isvalid)
                .build();

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPass(), user.getPass());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);

        UserResponse response = userMapper.toUserResponse(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .user(response)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefeshRequest request) throws ParseException, JOSEException {
        // Kiểm tra hiệu lực của token
        var signJWT = verifyToken(request.getToken(), true);

        //Id của token
        var jit = signJWT.getJWTClaimsSet().getJWTID();

        //Thời gian hiệu lực của Token
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        //Logout token hiện tại
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        //lấy username của acc đang login
        var username = signJWT.getJWTClaimsSet().getSubject();

        //Tìm user theo username
        var user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED)
        );

        //gen lại token mới theo thông tin user
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        //Vì đã mã hóa bằng thuật toán MAC -> verifier bằng MAC
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        //lấy ra thời gian hiệu lực của token
        //nếu isRefresh là tue thì expitTime sẽ dùng để refresh token còn nếu false thì expitTime sẽ được dùng cho việc Authentication
        Date expitTime = (isRefresh)
                /* isRefresh = true => expitTime(thời gian hết hạn) = thời gian lúc đầu của token + với thời gian mà token mới
                 sẽ được cấp sau khi refresh(REFRESHABLE_DURATION) */
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                //isRefresh = false => expitTime(thời gian hết hạn) = thời gian token được cấp ban đầu
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        //trả về true or false
        var verifired = signedJWT.verify(verifier);

        //kiểm tra xem token còn hiệu lực hay ko
        if (!(verifired && expitTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    private String generateToken(User user) {
        //header chứa nội dung của thuật toán mà ta sd
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //Nội dung của token
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("giangdtph40542.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        //token hết hạn sau 1 giờ
                        Instant.now().plus(VALID_DURATION, ChronoUnit.DAYS).toEpochMilli()
                ))
                //ID của token
                .jwtID(UUID.randomUUID().toString())
                //có thể tạo thêm claim custom
                .claim("user ID", user.getId())
                //role của user
                .claim("scope", buildScope(user))
                .build();

        //convert jwtset -> jsonset và Payload nhận
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        //kí token MACSigner(thuật toán kí token)

        try {

            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            //chuyển sang kiểu String
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException();
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getCode());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions()
                            .forEach(permission ->
                                    stringJoiner.add(permission.getCode()));
                }
            });
        }
        return stringJoiner.toString();
    }
}
