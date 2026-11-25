// package com.tl_connect.dev.payment;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.data.redis.core.StringRedisTemplate;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.tl_connect.dev.modules.notification.dto.CreateNotificationReqDTO;
// import com.tl_connect.dev.modules.notification.service.interfaces.NotificationModifyService;
// import com.tl_connect.dev.modules.payment.PaymentRepository;
// import com.tl_connect.dev.modules.payment.dto.CreateTuitionPaymentReqDTO;
// import com.tl_connect.dev.modules.payment.dto.CreateTuitionPaymentResDTO;
// import com.tl_connect.dev.modules.payment.entity.Payment;
// import com.tl_connect.dev.modules.payment.provider.PaymentFactory;
// import com.tl_connect.dev.modules.payment.service.PaymentServiceImpl;
// import com.tl_connect.dev.modules.tuition.entity.TuitionInvoice;
// import com.tl_connect.dev.modules.tuition.entity.TuitionInvoiceItem;
// import com.tl_connect.dev.modules.tuition.projection.TuitionInvoiceView;
// import com.tl_connect.dev.modules.tuition.service.interfaces.TuitionModifyService;
// import com.tl_connect.dev.modules.tuition.service.interfaces.TuitionService;
// import com.tl_connect.dev.shared.common.enums.PaymentStatus;
// import com.tl_connect.dev.shared.common.enums.TuitionStatus;
// import com.tl_connect.dev.shared.common.exception.ConflictException;
// import com.tl_connect.dev.shared.common.exception.ErrorException;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("PaymentServiceImpl Tests")
// class PaymentServiceImplTest {

//     @Mock
//     private TuitionService tuitionService;

//     @Mock
//     private PaymentRepository paymentRepository;

//     @Mock
//     private TuitionModifyService tuitionModifyService;

//     @Mock
//     private ObjectMapper objectMapper;

//     @Mock
//     private StringRedisTemplate redisTemplate;

//     @Mock
//     private PaymentFactory paymentFactory;

//     @Mock
//     private NotificationModifyService notificationModifyService;

//     @Mock
//     private ApplicationEventPublisher publisher;

//     @InjectMocks
//     private PaymentServiceImpl paymentService;

//     private Payment testPayment;
//     private TuitionInvoiceItem testInvoiceItem;
//     private CreateTuitionPaymentReqDTO testPaymentReq;

//     @BeforeEach
//     void setUp() {
//         testPayment = Payment.builder()
//                 .id(1L)
//                 .invoiceId(100L)
//                 .amount(new BigDecimal("500000"))
//                 .provider("ZALO_PAY")
//                 .transactionCode("TXN001")
//                 .providerTransId("PROVIDER_001")
//                 .status(PaymentStatus.SUCCESS)
//                 .createdAt(LocalDateTime.now())
//                 .updatedAt(LocalDateTime.now())
//                 .build();

//         testInvoiceItem = new TuitionInvoiceItem();
//         testInvoiceItem.setId(1L);
//         testInvoiceItem.setInvoiceId(100L);
//         testInvoiceItem.setCourseClassId(10L);
//         testInvoiceItem.setAmount(new BigDecimal("500000"));

//         testPaymentReq = new CreateTuitionPaymentReqDTO();
//         testPaymentReq.setInvoiceId(100L);
//     }

//     @Nested
//     @DisplayName("createPayment Tests")
//     class CreatePaymentTests {

//         @Test
//         @DisplayName("Should create payment successfully for unpaid invoice")
//         void testCreatePaymentSuccess() throws Exception {
//             // Arrange
//             Long studentId = 1L;
//             TuitionInvoiceView invoiceView = createTuitionInvoiceView(100L, TuitionStatus.UNPAID);

//             when(tuitionService.findByIdAndStudentId(testPaymentReq.getInvoiceId(), studentId))
//                     .thenReturn(invoiceView);
//             when(tuitionService.findAllItemsByInvoiceId(testPaymentReq.getInvoiceId()))
//                     .thenReturn(List.of(testInvoiceItem));
//             when(objectMapper.writeValueAsString(anyList())).thenReturn("[]");
//             when(paymentFactory.getProvider(anyString())).thenReturn(mock());

//             // Act & Assert
//             assertDoesNotThrow(() -> paymentService.createPayment(studentId, testPaymentReq));
//             verify(tuitionService, times(1)).findByIdAndStudentId(testPaymentReq.getInvoiceId(), studentId);
//         }

//         @Test
//         @DisplayName("Should throw exception when invoice already paid")
//         void testCreatePaymentAlreadyPaid() {
//             // Arrange
//             Long studentId = 1L;
//             TuitionInvoiceView invoiceView = createTuitionInvoiceView(100L, TuitionStatus.PAID);

//             when(tuitionService.findByIdAndStudentId(testPaymentReq.getInvoiceId(), studentId))
//                     .thenReturn(invoiceView);

//             // Act & Assert
//             assertThrows(ConflictException.class,
//                     () -> paymentService.createPayment(studentId, testPaymentReq));
//         }

//         @Test
//         @DisplayName("Should throw exception when invoice is cancelled")
//         void testCreatePaymentCancelledInvoice() {
//             // Arrange
//             Long studentId = 1L;
//             TuitionInvoiceView invoiceView = createTuitionInvoiceView(100L, TuitionStatus.CANCELLED);

//             when(tuitionService.findByIdAndStudentId(testPaymentReq.getInvoiceId(), studentId))
//                     .thenReturn(invoiceView);

//             // Act & Assert
//             assertThrows(ConflictException.class,
//                     () -> paymentService.createPayment(studentId, testPaymentReq));
//         }

//         @Test
//         @DisplayName("Should throw exception when invoice has no items")
//         void testCreatePaymentNoItems() {
//             // Arrange
//             Long studentId = 1L;
//             TuitionInvoiceView invoiceView = createTuitionInvoiceView(100L, TuitionStatus.UNPAID);

//             when(tuitionService.findByIdAndStudentId(testPaymentReq.getInvoiceId(), studentId))
//                     .thenReturn(invoiceView);
//             when(tuitionService.findAllItemsByInvoiceId(testPaymentReq.getInvoiceId()))
//                     .thenReturn(new ArrayList<>());

//             // Act & Assert
//             assertThrows(ErrorException.class,
//                     () -> paymentService.createPayment(studentId, testPaymentReq));
//         }
//     }

//     @Nested
//     @DisplayName("getPaymentByTransactionCode Tests")
//     class GetPaymentByTransactionCodeTests {

//         @Test
//         @DisplayName("Should return payment by transaction code successfully")
//         void testGetPaymentByTransactionCodeSuccess() {
//             // Arrange
//             String transactionCode = "TXN001";
//             when(paymentRepository.findByTransactionCode(transactionCode))
//                     .thenReturn(Optional.of(testPayment));

//             // Act
//             Payment result = paymentService.findByTransactionCode(transactionCode);

//             // Assert
//             assertNotNull(result);
//             assertEquals(transactionCode, result.getTransactionCode());
//             verify(paymentRepository, times(1)).findByTransactionCode(transactionCode);
//         }

//         @Test
//         @DisplayName("Should throw exception when transaction code not found")
//         void testGetPaymentByTransactionCodeNotFound() {
//             // Arrange
//             String transactionCode = "INVALID";
//             when(paymentRepository.findByTransactionCode(transactionCode))
//                     .thenReturn(Optional.empty());

//             // Act & Assert
//             assertThrows(Exception.class,
//                     () -> paymentService.getPaymentByTransactionCode(transactionCode));
//         }
//     }

//     @Nested
//     @DisplayName("getPaymentByInvoiceId Tests")
//     class GetPaymentByInvoiceIdTests {

//         @Test
//         @DisplayName("Should return payment by invoice ID successfully")
//         void testGetPaymentByInvoiceIdSuccess() {
//             // Arrange
//             Long invoiceId = 100L;
//             when(paymentRepository.findByInvoiceId(invoiceId))
//                     .thenReturn(Optional.of(testPayment));

//             // Act
//             Payment result = paymentService.getPaymentByInvoiceId(invoiceId);

//             // Assert
//             assertNotNull(result);
//             assertEquals(invoiceId, result.getInvoiceId());
//             verify(paymentRepository, times(1)).findByInvoiceId(invoiceId);
//         }

//         @Test
//         @DisplayName("Should throw exception when invoice ID not found")
//         void testGetPaymentByInvoiceIdNotFound() {
//             // Arrange
//             Long invoiceId = 999L;
//             when(paymentRepository.findByInvoiceId(invoiceId))
//                     .thenReturn(Optional.empty());

//             // Act & Assert
//             assertThrows(Exception.class,
//                     () -> paymentService.getPaymentByInvoiceId(invoiceId));
//         }
//     }

//     @Nested
//     @DisplayName("queryPayment Tests")
//     class QueryPaymentTests {

//         @Test
//         @DisplayName("Should query payment status successfully")
//         void testQueryPaymentSuccess() throws Exception {
//             // Arrange
//             Long studentId = 1L;
//             String transactionCode = "TXN001";
//             Payment existingPayment = new Payment();
//             existingPayment.setStatus(PaymentStatus.PENDING);

//             when(paymentRepository.findByTransactionCode(transactionCode))
//                     .thenReturn(Optional.of(existingPayment));

//             // Act & Assert
//             assertDoesNotThrow(() -> paymentService.queryPayment(studentId, transactionCode));
//         }
//     }

//     @Nested
//     @DisplayName("refundPayment Tests")
//     class RefundPaymentTests {

//         @Test
//         @DisplayName("Should process refund successfully")
//         void testRefundPaymentSuccess() throws Exception {
//             // Arrange
//             Long paymentId = 1L;
//             when(paymentRepository.findById(paymentId))
//                     .thenReturn(Optional.of(testPayment));

//             // Act & Assert
//             assertDoesNotThrow(() -> paymentService.refundPayment(paymentId));
//             verify(paymentRepository, times(1)).findById(paymentId);
//         }

//         @Test
//         @DisplayName("Should throw exception when payment not found for refund")
//         void testRefundPaymentNotFound() {
//             // Arrange
//             Long paymentId = 999L;
//             when(paymentRepository.findById(paymentId))
//                     .thenReturn(Optional.empty());

//             // Act & Assert
//             assertThrows(Exception.class, () -> paymentService.refundPayment(paymentId));
//         }
//     }

//     // ==================== Helper Methods ====================

//     private TuitionInvoiceView createTuitionInvoiceView(Long invoiceId, TuitionStatus status) {
//         return new TuitionInvoiceView() {
//             @Override
//             public Long getId() {
//                 return invoiceId;
//             }

//             @Override
//             public Long getStudentId() {
//                 return 1L;
//             }

//             @Override
//             public BigDecimal getTotalAmount() {
//                 return new BigDecimal("500000");
//             }

//             @Override
//             public TuitionStatus getStatus() {
//                 return status;
//             }

//             @Override
//             public LocalDateTime getCreatedAt() {
//                 return LocalDateTime.now();
//             }
//         };
//     }
// }
