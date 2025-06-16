    package com.example.web_qltv_be.dao;
    
    import com.example.web_qltv_be.entity.BorrowRecord;
    import com.example.web_qltv_be.entity.BorrowRecordDetail;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.data.rest.core.annotation.RepositoryRestResource;
    import org.springframework.data.rest.core.annotation.RestResource;

    import java.util.List;
    
    @RepositoryRestResource(path = "borrow-record-detail")
    public interface BorrowRecordDetailRepository extends JpaRepository<BorrowRecordDetail, Integer> {
        public List<BorrowRecordDetail> findBorrowRecordDetailByBorrowRecord(BorrowRecord borrowRecord);

        @RestResource(path = "findByBookItem_BarcodeAndIsReturnedFalse")
        @Query("SELECT brd FROM BorrowRecordDetail brd " +
                "WHERE brd.bookItem.barcode = :barcode " +
                "AND brd.isReturned = false " +
                "AND brd.borrowRecord.status = 'Đang mượn'")
        BorrowRecordDetail findByBookItem_BarcodeAndIsReturnedFalse(@Param("barcode") String barcode);
    }