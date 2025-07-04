package com.example.web_qltv_be.service.book;

import com.example.web_qltv_be.dao.*;
import com.example.web_qltv_be.entity.*;
import com.example.web_qltv_be.service.UploadImage.UploadImageService;
import com.example.web_qltv_be.service.util.Base64ToMultipartFileConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImp implements BookService {

    private final ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private DdcCategoryRepository ddcCategoryRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private BookItemRepository bookItemRepository;


    public BookServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode bookJson) {
        try {
            Book book = objectMapper.treeToValue(bookJson, Book.class);
            // Lưu thể loại của sách
            List<Integer> idGenreList = objectMapper.readValue(bookJson.get("idGenres").traverse(), new TypeReference<List<Integer>>() {});
            List<Genre> genreList = new ArrayList<>();
            for (int idGenre : idGenreList) {
                Optional<Genre> genre = genreRepository.findById(idGenre);
                genreList.add(genre.get());
            }
            book.setListGenres(genreList);
            // Lưu mã ddc
            List<Integer> idDdcCategory = objectMapper.readValue(bookJson.get("idDdcCategory").traverse(), new TypeReference<List<Integer>>() {});
            List<DdcCategory> dcdCategoryList = new ArrayList<>();
            for (int idDcdCategory : idDdcCategory) {
                Optional<DdcCategory> ddcCategory = ddcCategoryRepository.findById(idDcdCategory);
                dcdCategoryList.add(ddcCategory.get());
            }
            book.setListDdcCategory(dcdCategoryList);
            // Lưu mã isbn
            String isbn = bookJson.get("isbn").asText();
            String cleanedIsbn = isbn.replaceAll("-", ""); // Bỏ dấu gạch ngang
            Optional<Book> existingBook = bookRepository.findByIsbn(cleanedIsbn);
            if (existingBook.isPresent()) {
                return ResponseEntity.badRequest().body("ISBN đã tồn tại!");
            }
            // Lưu trước để lấy id sách đặt tên cho ảnh
            Book newBook = bookRepository.save(book);
            String location = bookJson.get("location").asText();
            // Tạo danh sách BookItem tương ứng với quantityForBorrow
            List<BookItem> bookItemList = new ArrayList<>();
            for (int i = 0; i < newBook.getQuantityForBorrow(); i++) {
                BookItem bookItem = new BookItem();
                bookItem.setBook(newBook);
                bookItem.setStatus("Có sẵn");
                bookItem.setLocation(location);
                bookItem.setCondition(100);
                bookItem.setBarcode(cleanedIsbn + "-" + (i + 1));

                bookItemList.add(bookItem);
            }
            newBook.setListBookItems(bookItemList);


            // Lưu thumbnail cho ảnh
            String dataThumbnail = formatStringByJson(String.valueOf((bookJson.get("thumbnail"))));

            Image thumbnail = new Image();
            thumbnail.setBook(newBook);
//            thumbnail.setDataImage(dataThumbnail);
            thumbnail.setThumbnail(true);
            MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(dataThumbnail);
            String thumbnailUrl = uploadImageService.uploadImage(multipartFile, "Book_" + newBook.getIdBook());
            thumbnail.setUrlImage(thumbnailUrl);

            List<Image> imagesList = new ArrayList<>();
            imagesList.add(thumbnail);


            // Lưu những ảnh có liên quan
            String dataRelatedImg = formatStringByJson(String.valueOf((bookJson.get("relatedImg"))));
            List<String> arrDataRelatedImg = objectMapper.readValue(bookJson.get("relatedImg").traverse(), new TypeReference<List<String>>() {
            });

            for (int i = 0; i < arrDataRelatedImg.size(); i++) {
                String img = arrDataRelatedImg.get(i);
                Image image = new Image();
                image.setBook(newBook);
//                image.setDataImage(img);
                image.setThumbnail(false);
                MultipartFile relatedImgFile = Base64ToMultipartFileConverter.convert(img);
                String imgURL = uploadImageService.uploadImage(relatedImgFile, "Book_" + newBook.getIdBook() + "." + i);
                image.setUrlImage(imgURL);
                imagesList.add(image);
            }

            newBook.setListImages(imagesList);
            // Cập nhật lại ảnh
            bookRepository.save(newBook);

            return ResponseEntity.ok("Success!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(JsonNode bookJson) {
        try {
            Book book = objectMapper.treeToValue(bookJson, Book.class);
            List<Image> imagesList = imageRepository.findImagesByBook(book);

            // Lưu thể loại của sách
            List<Integer> idGenreList = objectMapper.readValue(bookJson.get("idGenres").traverse(), new TypeReference<List<Integer>>() {
            });
            List<Genre> genreList = new ArrayList<>();
            for (int idGenre : idGenreList) {
                Optional<Genre> genre = genreRepository.findById(idGenre);
                genreList.add(genre.get());
            }
            book.setListGenres(genreList);

            List<Integer> idDdcCategory = objectMapper.readValue(bookJson.get("idDdcCategory").traverse(), new TypeReference<List<Integer>>() {});
            List<DdcCategory> dcdCategoryList = new ArrayList<>();
            for (int idDcdCategory : idDdcCategory) {
                Optional<DdcCategory> ddcCategory = ddcCategoryRepository.findById(idDcdCategory);
                dcdCategoryList.add(ddcCategory.get());
            }
            book.setListDdcCategory(dcdCategoryList);


            // Kiểm tra xem thumbnail có thay đổi không
            String dataThumbnail = formatStringByJson(String.valueOf((bookJson.get("thumbnail"))));
            if (Base64ToMultipartFileConverter.isBase64(dataThumbnail)) {
                for (Image image : imagesList) {
                    if (image.isThumbnail()) {
//                        image.setDataImage(dataThumbnail);
                        MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(dataThumbnail);
                        String thumbnailUrl = uploadImageService.uploadImage(multipartFile, "Book_" + book.getIdBook());
                        image.setUrlImage(thumbnailUrl);
                        imageRepository.save(image);
                        break;
                    }
                }
            }

            Book newBook = bookRepository.save(book);
            int currentBookItemCount = bookItemRepository.countByBook(newBook);
            int updatedQuantity = newBook.getQuantityForBorrow();
            String cleanedIsbn = book.getIsbn().replaceAll("-", "");
            String location = bookJson.get("location").asText();
            List<BookItem> currentBookItems = bookItemRepository.findByBook(newBook);
            for(BookItem bookItem : currentBookItems) {
                bookItem.setLocation(location);
                bookItemRepository.save(bookItem);
            }
            if (updatedQuantity > currentBookItemCount) {
                List<BookItem> newItems = new ArrayList<>();
                for (int i = currentBookItemCount + 1; i <= updatedQuantity; i++) {
                    BookItem bookItem = new BookItem();
                    bookItem.setBook(newBook);
                    bookItem.setStatus("Có sẵn");
                    bookItem.setLocation(location);
                    bookItem.setCondition(100);
                    bookItem.setBarcode(cleanedIsbn + "-" + i);
                    newItems.add(bookItem);
                }
                bookItemRepository.saveAll(newItems);
            }

            // Kiểm tra ảnh có liên quan
            List<String> arrDataRelatedImg = objectMapper.readValue(bookJson.get("relatedImg").traverse(), new TypeReference<List<String>>() {});

            // Xem có xoá tất ở bên FE không
            boolean isCheckDelete = true;

            for (String img : arrDataRelatedImg) {
                if (!Base64ToMultipartFileConverter.isBase64(img)) {
                    isCheckDelete = false;
                }
            }

            // Nếu xoá hết tất cả
            if (isCheckDelete) {
                imageRepository.deleteImagesWithFalseThumbnailByBookId(newBook.getIdBook());
                Image thumbnailTemp = imagesList.get(0);
                imagesList.clear();
                imagesList.add(thumbnailTemp);
                for (int i = 0; i < arrDataRelatedImg.size(); i++) {
                    String img = arrDataRelatedImg.get(i);
                    Image image = new Image();
                    image.setBook(newBook);
//                    image.setDataImage(img);
                    image.setThumbnail(false);
                    MultipartFile relatedImgFile = Base64ToMultipartFileConverter.convert(img);
                    String imgURL = uploadImageService.uploadImage(relatedImgFile, "Book_" + newBook.getIdBook() + "." + i);
                    image.setUrlImage(imgURL);
                    imagesList.add(image);
                }
            } else {
                // Nếu không xoá hết tất cả (Giữ nguyên ảnh hoặc thêm ảnh vào)
                for (int i = 0; i < arrDataRelatedImg.size(); i++) {
                    String img = arrDataRelatedImg.get(i);
                    if (Base64ToMultipartFileConverter.isBase64(img)) {
                        Image image = new Image();
                        image.setBook(newBook);
//                        image.setDataImage(img);
                        image.setThumbnail(false);
                        MultipartFile relatedImgFile = Base64ToMultipartFileConverter.convert(img);
                        String imgURL = uploadImageService.uploadImage(relatedImgFile, "Book_" + newBook.getIdBook() + "." + i);
                        image.setUrlImage(imgURL);
                        imageRepository.save(image);
                    }
                }
            }

            newBook.setListImages(imagesList);
            // Cập nhật lại ảnh
            bookRepository.save(newBook);

            return ResponseEntity.ok("Success!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public long getTotalBook() {
        return bookRepository.count();
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }

}

