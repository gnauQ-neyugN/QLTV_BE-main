    package com.example.web_bookstore_be.service.librarycard;

    import com.example.web_bookstore_be.dao.LibraryCardRepository;
    import com.example.web_bookstore_be.dao.LibraryViolationTypeRepository;
    import com.example.web_bookstore_be.dao.UserRepository;
    import com.example.web_bookstore_be.entity.LibraryCard;
    import com.example.web_bookstore_be.entity.LibraryViolationType;
    import com.example.web_bookstore_be.entity.User;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;

    import java.sql.Date;
    import java.time.LocalDate;
    import java.util.Optional;

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
                String indentifierCode = formatStringByJson(String.valueOf(jsonNode.get("identifierCode")));
                if(!userRepository.existsByIdentifierCode(indentifierCode)) {
                    return ResponseEntity.badRequest().body("Người dùng chưa có mã định danh");
                }
                User user = userRepository.findByIdentifierCode(indentifierCode);
                LibraryCard libraryCard = new LibraryCard();
                libraryCard.setCardNumber(indentifierCode);
                libraryCard.setIssuedDate(Date.valueOf(LocalDate.now()));
                libraryCard.setActivated(true);
                libraryCard.setUser(user);

                libraryCardRepository.save(libraryCard);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.ok().body("Tạo thẻ thư viện thành công");
        }

        @Override
        public ResponseEntity<?> update(JsonNode jsonNode) {
            try {
                int idLibraryCard = Integer.valueOf(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
                Optional<LibraryCard> libraryCard = libraryCardRepository.findById(idLibraryCard);

                if(libraryCard.isEmpty()){
                    return ResponseEntity.notFound().build();
                }
                Date issuedNewDate = Date.valueOf(formatStringByJson(String.valueOf(jsonNode.get("issuedNewDate"))));

                libraryCard.get().setIssuedDate(issuedNewDate);
                libraryCardRepository.save(libraryCard.get());
            }catch (Exception e){
                e.printStackTrace();
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.ok().build();
        }

        @Override
        public ResponseEntity<?> delete(JsonNode jsonNode) {
            try {
                int idLibraryCard = Integer.valueOf(formatStringByJson(String.valueOf(jsonNode.get("idLibraryCard"))));
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
            return ResponseEntity.ok().build();
        }

        private String formatStringByJson(String json) {
            return json.replaceAll("\"", "");
        }

    }
