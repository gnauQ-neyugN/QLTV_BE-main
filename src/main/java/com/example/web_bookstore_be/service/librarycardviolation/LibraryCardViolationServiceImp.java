package com.example.web_bookstore_be.service.librarycardviolation;

import com.example.web_bookstore_be.dao.LibraryViolationTypeRepository;
import com.example.web_bookstore_be.entity.LibraryViolationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LibraryCardViolationServiceImp implements LibraryCardViolationService {
    @Autowired
    private LibraryViolationTypeRepository libraryViolationTypeRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public ResponseEntity<?> save(JsonNode jsonNode) {
        try{
            LibraryViolationType libraryViolationType = objectMapper.treeToValue(jsonNode, LibraryViolationType.class);
            String code = jsonNode.get("code").asText();
            JsonNode fine = jsonNode.get("fine");
            Optional<LibraryViolationType> libraryViolationTypeOptional = libraryViolationTypeRepository.findByCode(code);
            if(libraryViolationTypeOptional.isPresent()){
                return ResponseEntity.badRequest().body("Đã có lỗi này");
            }
            if(fine.isNull() ){
                return ResponseEntity.badRequest().body("Trường tiền phạt là bắt buộc");
            }
            libraryViolationTypeRepository.save(libraryViolationType);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> update(JsonNode jsonNode) {
        try{
            int idLibraryViolationType = jsonNode.get("id").asInt();
            double fine = jsonNode.get("fine").asDouble();
            String code = jsonNode.get("code").asText();
            String description = jsonNode.get("description").asText();
            Optional<LibraryViolationType> libraryViolationTypeOptional = libraryViolationTypeRepository.findById(idLibraryViolationType);
            if(libraryViolationTypeOptional.isEmpty()){
                return ResponseEntity.badRequest().build();
            }
            libraryViolationTypeOptional.get().setFine(fine);
            libraryViolationTypeOptional.get().setCode(code);
            libraryViolationTypeOptional.get().setDescription(description);
            libraryViolationTypeRepository.save(libraryViolationTypeOptional.get());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
