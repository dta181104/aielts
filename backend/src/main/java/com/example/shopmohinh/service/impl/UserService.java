package com.example.shopmohinh.service.impl;

import com.example.shopmohinh.dto.request.UserCreationRequest;
import com.example.shopmohinh.dto.request.UserUpdateRequest;
import com.example.shopmohinh.dto.response.UserResponse;
import com.example.shopmohinh.entity.CartEntity;
import com.example.shopmohinh.entity.Role;
import com.example.shopmohinh.entity.User;
import com.example.shopmohinh.exception.AppException;
import com.example.shopmohinh.exception.ErrorCode;
import com.example.shopmohinh.mapper.UserMapper;
import com.example.shopmohinh.repository.CartRepository;
import com.example.shopmohinh.repository.RoleRepository;
import com.example.shopmohinh.repository.UserRepository;
import com.example.shopmohinh.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
//Thay thế cho @Autowired
//@RequiredArgsConstructor sẽ tự động tạo contructor của những method đc khai báo là final
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;

    RoleRepository roleRepository;

    UserMapper userMapper;

    FileUploadUtil fileUploadUtil;

    CartRepository cartRepository;

    @Transactional
    public UserResponse createdUser(UserCreationRequest request) {

        // Kiểm tra xem tài khoản với username "admin" đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setCode(this.generateCode());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPass(passwordEncoder.encode(request.getPass()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedDate(now);
        this.validateMailUnique(user.getEmail());
        if(Objects.nonNull(getMyInfo().getId())){
            user.setCreatedBy(getMyInfo().getUsername());
        }else{
            user.setCreatedBy("SYSTEM");
        }
//        user.setAvatar(fileUploadUtil.uploadFile(request.getAvatarFile()));
        user.setAvatar(fileUploadUtil.uploadAvatar(request.getAvatarFile(), request.getUsername()));

        // Lấy role từ request
        Set<Role> roles = getRolesFromRequest(request.getRoles());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        CartEntity cart = new CartEntity();
        cart.setUser(savedUser);
        cart.setCreatedDate(now);
        this.genCodeCart(cart);
        cart.setCreatedBy(savedUser.getUsername());
        cartRepository.save(cart);

        return userMapper.toUserResponse(savedUser);
    }

    private void validateMailUnique(String mail){
        List<UserResponse> userList = this.getUsers();
        for (UserResponse user : userList) {
            if(user.getEmail().equals(mail)){
                throw new AppException(ErrorCode.MAIL_UNIQUE);
            }
        }
    }

    private String generateCode() {
        String lastCode = Optional.ofNullable(userRepository.getTop1())
                .map(User::getCode)
                .orElse("USER000");

        if (lastCode.length() > 6) {
            String prefix = lastCode.substring(0, 6);
            int number = Integer.parseInt(lastCode.substring(6));
            return prefix + (number + 1);
        } else {
            return "USER001";
        }
    }

    private void genCodeCart(CartEntity saveCart){
        CartEntity top1 = cartRepository.getTop1();
        if(top1==null){
            saveCart.setCode("CART1");
        }else{
            String code = top1.getCode();
            saveCart.setCode(code.substring(0,4)+((Integer.parseInt(code.substring(4)))+1));
        }
    }

    private Set<Role> getRolesFromRequest(List<String> roleCodes) {
        Set<Role> roles = new HashSet<>();

        // Nếu roleCodes null hoặc rỗng, gán role mặc định là "USER"
        if (roleCodes == null || roleCodes.isEmpty()) {
            Optional<Role> userRoleOptional = roleRepository.findRoleByCode("USER");
            if (userRoleOptional.isEmpty()) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND); // Nếu không tìm thấy role "USER"
            }
            roles.add(userRoleOptional.get());
        } else {
            // Lấy role từ danh sách roleCodes
            for (String roleCode : roleCodes) {
                Optional<Role> userRoleOptional = roleRepository.findRoleByCode(roleCode);
                if (userRoleOptional.isPresent()) {
                    roles.add(userRoleOptional.get());
                } else {
                    throw new AppException(ErrorCode.ROLE_NOT_FOUND);
                }
            }
        }
        return roles;
    }

    @PreAuthorize("hasAuthority('USER_VIEW')")
//    @PreAuthorize("hasRole('ADMIN')")
    //kiểm tra trc khi vào method nếu thỏa dk thì ms đc chạy method
    public List<UserResponse> getUsers() {
        log.info("In method getUser");
        return userRepository.getAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    //ngược lại với @PreAuthorize
    //kiểm tra ssau khi method chạy xong nếu thỏa dk thì result ms đc trả về
//    @PostAuthorize("hasRole('ADMIN')")

    //kiểm tra username đang đăng nhập có phải là đúng user response ko
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse findUser(String code) {
        log.info("In method get user by id");
        return userMapper.toUserResponse(userRepository.findByCode(code).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        ));
    }

    // lấy thông tin người đang đăng nhập
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        if(name.equals("anonymousUser")){
            name = "admin";
        }
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

//    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public UserResponse userUpdate(String code, UserUpdateRequest request) {
        User user = userRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedDate(now);

        Long id = getMyInfo().getId();
        user.setUpdatedBy(String.valueOf(id));

        if (request.getPass() != null && !request.getPass().isEmpty()) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            user.setPass(passwordEncoder.encode(request.getPass()));
        }

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            user.setAvatar(fileUploadUtil.uploadFile(request.getAvatarFile()));
//            user.setAvatar(fileUploadUtil.uploadAvatar(request.getAvatarFile()));
        }

//        var roles = roleRepository.findAllById(request.getRoles());
//
//        user.setRoles(new HashSet<>(roles));
        var requestedRoles = request.getRoles();

        if (requestedRoles != null) {
//            var roles = roleRepository.findAllById(requestedRoles);
            var roles = roleRepository.findAllByCodeIn(requestedRoles);
            user.setRoles(new HashSet<>(roles));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }


    public UserResponse deleteUser(String code){
        User user = userRepository.findByCode(code).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setDeleted(true);

        return userMapper.toUserResponse(userRepository.save(user));
    }

}
