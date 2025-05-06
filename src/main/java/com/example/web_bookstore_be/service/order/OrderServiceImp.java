package com.example.web_bookstore_be.service.order;

import com.example.web_bookstore_be.dao.*;
import com.example.web_bookstore_be.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImp implements OrderService{
    private final ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    public OrderServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode jsonData) {
        try{
            Order orderData = objectMapper.treeToValue(jsonData, Order.class);
            orderData.setTotalPrice(orderData.getTotalPriceProduct());
            orderData.setDateCreated(Date.valueOf(LocalDate.now()));
            orderData.setStatus("Đang xử lý");

            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idUser"))));
            Optional<User> userOptional = userRepository.findById(idUser);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy người dùng với ID: " + idUser);
            }
            orderData.setUser(userOptional.get());

            int idPayment = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idPayment"))));
            Optional<Payment> paymentOptional = paymentRepository.findById(idPayment);
            if (paymentOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy phương thức thanh toán với ID: " + idPayment);
            }
            orderData.setPayment(paymentOptional.get());

            Order newOrder = orderRepository.save(orderData);

            JsonNode jsonNode = jsonData.get("book");
            for (JsonNode node : jsonNode) {
                int quantity = Integer.parseInt(formatStringByJson(String.valueOf(node.get("quantity"))));
                Book bookResponse = objectMapper.treeToValue(node.get("book"), Book.class);
                Optional<Book> bookOptional = bookRepository.findById(bookResponse.getIdBook());
                if (bookOptional.isEmpty()) {
                    return ResponseEntity.badRequest().body("Không tìm thấy sách với ID: " + bookResponse.getIdBook());
                }
                Book book = bookOptional.get();
                book.setQuantity(book.getQuantity() - quantity);
                book.setSoldQuantity(book.getSoldQuantity() + quantity);

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setBook(book);
                orderDetail.setQuantity(quantity);
                orderDetail.setOrder(newOrder);
                orderDetail.setPrice(quantity * book.getSellPrice());
                orderDetail.setReview(false);
                orderDetailRepository.save(orderDetail);
                bookRepository.save(book);
            }

            cartItemRepository.deleteCartItemsByIdUser(userOptional.get().getIdUser());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(JsonNode jsonData) {
        try{
            int idOrder =  Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idOrder"))));
            String status = formatStringByJson(String.valueOf(jsonData.get("status")));
            Optional<Order> orderOptional = orderRepository.findById(idOrder);
            if (orderOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng với ID: " + idOrder);
            }
            Order order = orderOptional.get();
            order.setStatus(status);

            if (status.equals("Bị huỷ")) {
                List<OrderDetail> orderDetailList = orderDetailRepository.findOrderDetailsByOrder(order);
                for (OrderDetail orderDetail : orderDetailList) {
                    Book bookOrderDetail = orderDetail.getBook();
                    bookOrderDetail.setSoldQuantity(bookOrderDetail.getSoldQuantity() - orderDetail.getQuantity());
                    bookOrderDetail.setQuantity(bookOrderDetail.getQuantity() + orderDetail.getQuantity());
                    bookRepository.save(bookOrderDetail);
                }
            }

            orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> cancel(JsonNode jsonData) {
        try{
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idUser"))));
            Optional<User> userOptional = userRepository.findById(idUser);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy người dùng với ID: " + idUser);
            }
            User user = userOptional.get();

            Order order = orderRepository.findFirstByUserOrderByIdOrderDesc(user);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            order.setStatus("Bị huỷ");

            List<OrderDetail> orderDetailList = orderDetailRepository.findOrderDetailsByOrder(order);
            for (OrderDetail orderDetail : orderDetailList) {
                Book bookOrderDetail = orderDetail.getBook();
                bookOrderDetail.setSoldQuantity(bookOrderDetail.getSoldQuantity() - orderDetail.getQuantity());
                bookOrderDetail.setQuantity(bookOrderDetail.getQuantity() + orderDetail.getQuantity());
                bookRepository.save(bookOrderDetail);
            }

            orderRepository.save(order);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}
