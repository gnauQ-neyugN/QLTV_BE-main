package com.example.web_qltv_be.service.librarycard;

import com.example.web_qltv_be.dao.LibraryCardRepository;
import com.example.web_qltv_be.dao.LibraryViolationTypeRepository;
import com.example.web_qltv_be.dao.UserRepository;
import com.example.web_qltv_be.entity.LibraryCard;
import com.example.web_qltv_be.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryCardServiceImp implements LibraryCardService {
    @Autowired
    private LibraryCardRepository libraryCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LibraryViolationTypeRepository libraryViolationTypeRepository;
    private final ObjectMapper objectMapper;

    public LibraryCardServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<?> save(JsonNode jsonNode) {
        try {
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idUser"))));
            Optional<User> userOptional = userRepository.findById(idUser);
            if(userOptional.isEmpty()){
                return ResponseEntity.badRequest().build();
            }

            int idLibraryCard = userOptional.get().getLibraryCard().getIdLibraryCard();
            String cardNumber = formatStringByJson(String.valueOf(jsonNode.get("cardNumber")));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);
            libraryCard.get().setCardNumber(cardNumber);
            libraryCard.get().setIssuedDate(Date.valueOf(LocalDate.now()));
            libraryCard.get().setExpiryDate(Date.valueOf(LocalDate.now().plusDays(365)));
            libraryCard.get().setActivated(false);
            libraryCard.get().setStatus("Chưa kích hoạt");

            libraryCardRepository.save(libraryCard.get());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Kích hoạt thẻ thư viện thành công");
    }

    @Override
    public ResponseEntity<?> update(JsonNode jsonNode) {
        try {
            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);

            if(libraryCard.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            Date expiryNewDate = Date.valueOf(formatStringByJson(String.valueOf(jsonNode.get("expiryNewDate"))));
            libraryCard.get().setActivated(true);
            libraryCard.get().setStatus("Đang hoạt động");
            libraryCard.get().setExpiryDate(expiryNewDate);  // Fixed: was setting issuedDate instead of expiryDate
            libraryCardRepository.save(libraryCard.get());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Cập nhật thẻ thư viện thành công");
    }

    @Override
    public ResponseEntity<?> delete(JsonNode jsonNode) {
        try {
            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);

            if(libraryCard.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            libraryCard.get().setActivated(false);
            libraryCardRepository.save(libraryCard.get());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Huỷ kích hoạt thẻ thư viện thành công");
    }

    @Override
    public ResponseEntity<?> getByUserId(int userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);

            if (!userOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOptional.get();
            Optional<LibraryCard> libraryCardOptional = libraryCardRepository.findByUser(user);

            if (!libraryCardOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            LibraryCard libraryCard = libraryCardOptional.get();

            // Kiểm tra thẻ có hết hạn không
            LocalDate now = LocalDate.now();
            LocalDate expiryDate = libraryCard.getExpiryDate().toLocalDate();
            boolean isExpired = now.isAfter(expiryDate);

            // Nếu thẻ hết hạn, cập nhật trạng thái
            if (isExpired && libraryCard.isActivated()) {
                libraryCard.setActivated(false);
                libraryCardRepository.save(libraryCard);
            }

            // Trả về thông tin thẻ thư viện
            Map<String, Object> response = new HashMap<>();
            response.put("idLibraryCard", libraryCard.getIdLibraryCard());
            response.put("cardNumber", libraryCard.getCardNumber());
            response.put("issueDate", libraryCard.getIssuedDate().toString());
            response.put("expiryDate", libraryCard.getExpiryDate().toString());
            response.put("status", libraryCard.isActivated() ? "ACTIVE" : "INACTIVE");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy thông tin thẻ thư viện: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> sendRequestRenewCard(JsonNode jsonNode) {
        try{
            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> LibraryCardOptional = libraryCardRepository.findById(idLibraryCard);
            if(LibraryCardOptional.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            LibraryCard libraryCard = LibraryCardOptional.get();
            libraryCard.setStatus("Yêu cầu gia hạn thẻ thư viện");
            libraryCardRepository.save(libraryCard);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Gửi yêu cầu gia hạn thẻ thư viện không thành công");
        }
        return ResponseEntity.ok().body("Gửi yêu cầu gia hạn thẻ thành công");
    }

    @Override
    public ResponseEntity<?> renewCard(JsonNode jsonNode) {
        try {
            int idLibraryCard = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
            Optional<LibraryCard> libraryCardOptional = libraryCardRepository.findById(idLibraryCard);

            if (libraryCardOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LibraryCard libraryCard = libraryCardOptional.get();
            int renewalPeriodDays = 365; // Default renewal period

            // Check if renewal period is provided in the request
            if (jsonNode.has("renewalPeriodDays")) {
                renewalPeriodDays = Integer.parseInt(formatStringByJson(String.valueOf(jsonNode.get("renewalPeriodDays"))));
            }

            // Calculate new expiry date - either from current date or from existing expiry date if not expired
            LocalDate currentDate = LocalDate.now();
            LocalDate existingExpiryDate = libraryCard.getExpiryDate().toLocalDate();
            LocalDate newExpiryDate;

            if (currentDate.isAfter(existingExpiryDate)) {
                // If card already expired, calculate from today
                newExpiryDate = currentDate.plusDays(renewalPeriodDays);
            } else {
                // If card still valid, extend from current expiry date
                newExpiryDate = existingExpiryDate.plusDays(renewalPeriodDays);
            }

            libraryCard.setExpiryDate(Date.valueOf(newExpiryDate));
            libraryCard.setActivated(true);
            libraryCard.setStatus("Gia hạn thành công");
            libraryCardRepository.save(libraryCard);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gia hạn thẻ thư viện thành công");
            response.put("newExpiryDate", newExpiryDate.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi gia hạn thẻ thư viện: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getCardStatus(String cardNumber) {
        try {
            Optional<LibraryCard> libraryCardOptional = libraryCardRepository.findByCardNumber(cardNumber);

            if (libraryCardOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LibraryCard libraryCard = libraryCardOptional.get();
            LocalDate now = LocalDate.now();
            LocalDate expiryDate = libraryCard.getExpiryDate().toLocalDate();
            boolean isExpired = now.isAfter(expiryDate);

            // Calculate days until expiry
            long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);

            // If expired and still marked as active, update the status
            if (isExpired && libraryCard.isActivated()) {
                libraryCard.setActivated(false);
                libraryCardRepository.save(libraryCard);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("cardNumber", libraryCard.getCardNumber());
            response.put("active", libraryCard.isActivated());
            response.put("expired", isExpired);
            response.put("daysUntilExpiry", isExpired ? -daysUntilExpiry : daysUntilExpiry);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi kiểm tra trạng thái thẻ: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllCards(Pageable pageable, String status) {
        try {
            Page<LibraryCard> libraryCardPage;

            if (status != null && !status.isEmpty()) {
                boolean isActive = "ACTIVE".equalsIgnoreCase(status);
                libraryCardPage = libraryCardRepository.findByActivated(isActive, pageable);
            } else {
                libraryCardPage = libraryCardRepository.findAll(pageable);
            }

            // Check and update expired cards
            LocalDate now = LocalDate.now();
            List<Map<String, Object>> cardDataList = libraryCardPage.getContent().stream()
                    .map(card -> {
                        boolean isExpired = now.isAfter(card.getExpiryDate().toLocalDate());
                        if (isExpired && card.isActivated()) {
                            card.setActivated(false);
                            libraryCardRepository.save(card);
                        }

                        Map<String, Object> cardData = new HashMap<>();
                        cardData.put("idLibraryCard", card.getIdLibraryCard());
                        cardData.put("cardNumber", card.getCardNumber());
                        cardData.put("userName", card.getUser().getFirstName() + " " + card.getUser().getLastName());
                        cardData.put("issueDate", card.getIssuedDate().toString());
                        cardData.put("expiryDate", card.getExpiryDate().toString());
                        cardData.put("status", card.isActivated() ? "ACTIVE" : "INACTIVE");

                        return cardData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", cardDataList);
            response.put("totalElements", libraryCardPage.getTotalElements());
            response.put("totalPages", libraryCardPage.getTotalPages());
            response.put("currentPage", pageable.getPageNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách thẻ thư viện: " + e.getMessage());
        }
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}