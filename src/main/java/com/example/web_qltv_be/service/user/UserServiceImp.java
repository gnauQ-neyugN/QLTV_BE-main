package com.example.web_qltv_be.service.user;

import com.example.web_qltv_be.dao.LibraryCardRepository;
import com.example.web_qltv_be.dao.RoleRepository;
import com.example.web_qltv_be.dao.UserRepository;
import com.example.web_qltv_be.entity.LibraryCard;
import com.example.web_qltv_be.entity.Notification;
import com.example.web_qltv_be.entity.Role;
import com.example.web_qltv_be.entity.User;
import com.example.web_qltv_be.security.JwtResponse;
import com.example.web_qltv_be.service.JWT.JwtService;
import com.example.web_qltv_be.service.UploadImage.UploadImageService;
import com.example.web_qltv_be.service.email.EmailService;
import com.example.web_qltv_be.service.util.Base64ToMultipartFileConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private LibraryCardRepository libraryCardRepository;
    private final ObjectMapper objectMapper;

    public UserServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<?> register(User user) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(new Notification("Username đã tồn tại."));
        }

        // Kiểm tra email
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(new Notification("Email đã tồn tại."));
        }

        // Mã hoá mật khẩu
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);

        user.setAvatar("");

        // Tạo mã kích hoạt cho người dùng
        user.setActivationCode(generateActivationCode());
        user.setEnabled(false);

        // Tạo mới thẻ thư viện
        LibraryCard libraryCard = new LibraryCard();
        libraryCard.setActivated(false);
        libraryCard = libraryCardRepository.save(libraryCard);
        user.setLibraryCard(libraryCard);

        // Cho role mặc định
        List<Role> roleList = new ArrayList<>();
        Optional<Role> role = roleRepository.findByNameRole("CUSTOMER");
        roleList.add(role.get());
        user.setListRoles(roleList);
        // Lưu vào database
        userRepository.save(user);

        // Gửi email cho người dùng để kích hoạt
        sendEmailActivation(user.getEmail(),user.getActivationCode());

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @Override
    public ResponseEntity<?> save(JsonNode userJson, String option) {
        try {
            User user = objectMapper.treeToValue(userJson, User.class);

            if (!option.equals("update")) {
                if (userRepository.existsByUsername(user.getUsername())) {
                    return ResponseEntity.badRequest().body(new Notification("Username đã tồn tại."));
                }

                if (userRepository.existsByEmail(user.getEmail())) {
                    return ResponseEntity.badRequest().body(new Notification("Email đã tồn tại."));
                }
            }

            // Parse ngày sinh
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Instant instant = Instant.from(formatter.parse(formatStringByJson(String.valueOf(userJson.get("dateOfBirth")))));
            java.sql.Date dateOfBirth = new java.sql.Date(Date.from(instant).getTime());
            user.setDateOfBirth(dateOfBirth);

            if (option.equals("add")) {
                // Tạo thẻ thư viện
                LibraryCard libraryCard = new LibraryCard();
                libraryCard.setCardNumber(user.getIdentifierCode());
                libraryCard.setIssuedDate(java.sql.Date.valueOf(LocalDate.now()));
                libraryCard.setExpiryDate(java.sql.Date.valueOf(LocalDate.now().plusDays(365)));
                libraryCard.setStatus("Đang hoạt động");
                libraryCard.setActivated(true);
                libraryCard = libraryCardRepository.save(libraryCard);
                user.setLibraryCard(libraryCard);

                // Gán role mặc định
                Optional<Role> role = roleRepository.findByNameRole("CUSTOMER");
                if (role.isEmpty()) {
                    return ResponseEntity.badRequest().body(new Notification("Không tìm thấy role CUSTOMER"));
                }
                user.setListRoles(List.of(role.get()));

                // Mặc định chưa kích hoạt
                user.setEnabled(true);
            } else {
                // ==== ⚠️ Đây là đoạn cần thêm để fix lỗi mất dữ liệu ====
                Optional<User> userOldOpt = userRepository.findById(user.getIdUser());
                if (userOldOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(new Notification("Người dùng không tồn tại."));
                }

                User userOld = userOldOpt.get();
                user.setLibraryCard(userOld.getLibraryCard());

                if (user.getListRoles() == null || user.getListRoles().isEmpty()) {
                    user.setListRoles(userOld.getListRoles());
                }
            }

            // Mã hoá mật khẩu nếu được truyền
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                Optional<User> userTemp = userRepository.findById(user.getIdUser());
                user.setPassword(userTemp.get().getPassword());
            }

            // Avatar
            String avatar = formatStringByJson(String.valueOf((userJson.get("avatar"))));
            if (avatar.length() > 500) {
                MultipartFile avatarFile = Base64ToMultipartFileConverter.convert(avatar);
                String avatarURL = uploadImageService.uploadImage(avatarFile, "User_" + user.getIdUser());
                user.setAvatar(avatarURL);
            }

            userRepository.save(user);
            return ResponseEntity.ok("thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi xử lý người dùng."));
        }
    }

    @Override
    public ResponseEntity<?> delete(int id) {
        try{
            Optional<User> user = userRepository.findById(id);

            if (user.isPresent()) {
                String imageUrl = user.get().getAvatar();

                if (imageUrl != null) {
                    uploadImageService.deleteImage(imageUrl);
                }

                userRepository.deleteById(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("thành công");
    }

    @Override
    public ResponseEntity<?> changePassword(JsonNode userJson) {
        try{
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String newPassword = formatStringByJson(String.valueOf(userJson.get("newPassword")));
            System.out.println(idUser);
            System.out.println(newPassword);
            Optional<User> user = userRepository.findById(idUser);
            user.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user.get());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> changeAvatar(JsonNode userJson) {
        try{
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(userJson.get("idUser"))));
            String dataAvatar = formatStringByJson(String.valueOf(userJson.get("avatar")));

            Optional<User> user = userRepository.findById(idUser);

            // Xoá đi ảnh trước đó trong cloudinary
            if (user.get().getAvatar().length() > 0) {
                uploadImageService.deleteImage(user.get().getAvatar());
            }

            if (Base64ToMultipartFileConverter.isBase64(dataAvatar)) {
                MultipartFile avatarFile = Base64ToMultipartFileConverter.convert(dataAvatar);
                String avatarUrl = uploadImageService.uploadImage(avatarFile, "User_" + idUser);
                user.get().setAvatar(avatarUrl);
            }

            User newUser =  userRepository.save(user.get());
            final String jwtToken = jwtService.generateToken(newUser.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwtToken));

        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> updateProfile(JsonNode userJson) {
        try{
            User userRequest = objectMapper.treeToValue(userJson, User.class);
            Optional<User> user = userRepository.findById(userRequest.getIdUser());

            user.get().setFirstName(userRequest.getFirstName());
            user.get().setLastName(userRequest.getLastName());
            // Format lại ngày sinh
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Instant instant = Instant.from(formatter.parse(formatStringByJson(String.valueOf(userJson.get("dateOfBirth")))));
            java.sql.Date dateOfBirth = new java.sql.Date(Date.from(instant).getTime());

            user.get().setDateOfBirth(dateOfBirth);
            user.get().setPhoneNumber(userRequest.getPhoneNumber());
            user.get().setDeliveryAddress(userRequest.getDeliveryAddress());
            user.get().setGender(userRequest.getGender());

            userRepository.save(user.get());
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> forgotPassword(JsonNode jsonNode) {
        try{
            User user = userRepository.findByEmail(formatStringByJson(jsonNode.get("email").toString()));

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Đổi mật khẩu cho user
            String passwordTemp = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(passwordTemp));
            userRepository.save(user);

            // Gửi email đê nhận mật khẩu
            sendEmailForgotPassword(user.getEmail(), passwordTemp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    // Thêm method mới để kích hoạt/vô hiệu hóa tài khoản
    @Override
    public ResponseEntity<?> toggleUserStatus(int idUser, JsonNode jsonNode) {
        try {
            Optional<User> userOptional = userRepository.findById(idUser);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Không tìm thấy người dùng"));
            }

            User user = userOptional.get();
            boolean newStatus = Boolean.parseBoolean(formatStringByJson(String.valueOf(jsonNode.get("enabled"))));

            user.setEnabled(newStatus);
            userRepository.save(user);

            String message = newStatus ? "Kích hoạt tài khoản thành công" : "Vô hiệu hóa tài khoản thành công";
            return ResponseEntity.ok(new Notification(message));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi cập nhật trạng thái tài khoản"));
        }
    }

    // Thêm method mới để cập nhật phân quyền
    @Override
    @Transactional
    public ResponseEntity<?> updateUserRoles(JsonNode jsonNode) {
        try {
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idUser"))));
            JsonNode rolesNode = jsonNode.get("roles");

            Optional<User> userOptional = userRepository.findById(idUser);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new Notification("Không tìm thấy người dùng"));
            }

            User user = userOptional.get();
            List<Role> newRoles = new ArrayList<>();

            // Chuyển đổi danh sách role ID thành danh sách Role entity
            if (rolesNode.isArray()) {
                for (JsonNode roleIdNode : rolesNode) {
                    int roleId = roleIdNode.asInt();
                    Optional<Role> roleOptional = roleRepository.findById(roleId);
                    if (roleOptional.isPresent()) {
                        newRoles.add(roleOptional.get());
                    }
                }
            }

            // Cập nhật danh sách role cho user
            user.setListRoles(newRoles);
            userRepository.save(user);

            return ResponseEntity.ok(new Notification("Cập nhật phân quyền thành công"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Notification("Lỗi khi cập nhật phân quyền"));
        }
    }

    private String generateActivationCode() {
        return UUID.randomUUID().toString();
    }

    private void sendEmailActivation(String email, String activationCode) {
        String endpointFE = "http://localhost:3000";
        String url = endpointFE + "/active/" + email + "/" + activationCode;
        String subject = "Kích hoạt tài khoản";
        String message = "Cảm ơn bạn đã là thành viên của chúng tôi. Vui lòng kích hoạt tài khoản!: <br/> Mã kích hoạt: <strong>"+ activationCode +"<strong/>";
        message += "<br/> Click vào đây để <a href="+ url +">kích hoạt</a>";
        try {
            emailService.sendMessage("dongph.0502@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEmailForgotPassword(String email, String password) {
        String subject = "Reset mật khẩu";
        String message = "Mật khẩu tạm thời của bạn là: <strong>" + password + "</strong>";
        message += "<br/> <span>Vui lòng đăng nhập và đổi lại mật khẩu của bạn</span>";
        try {
            emailService.sendMessage("dongph.0502@gmail.com", email, subject, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateTemporaryPassword() {
        return RandomStringUtils.random(10, true, true);
    }

    public ResponseEntity<?> activeAccount(String email, String activationCode) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(new Notification("Người dùng không tồn tại!"));
        }
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(new Notification("Tài khoản đã được kích hoạt"));
        }
        if (user.getActivationCode().equals(activationCode)) {
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            return ResponseEntity.badRequest().body(new Notification("Mã kích hoạt không chính xác!"));
        }
        return ResponseEntity.ok("Kích hoạt thành công");
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}