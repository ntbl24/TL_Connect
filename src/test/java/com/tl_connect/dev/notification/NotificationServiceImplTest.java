package com.tl_connect.dev.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tl_connect.dev.modules.course_class.service.interfaces.CourseClassService;
import com.tl_connect.dev.modules.notification.dto.DetailNotifyDTO;
import com.tl_connect.dev.modules.notification.dto.NotificationReqDTO;
import com.tl_connect.dev.modules.notification.dto.PrepareNotificationDTO;
import com.tl_connect.dev.modules.notification.dto.SummaryNotifyDTO;
import com.tl_connect.dev.modules.notification.dto.UnreadNotificationDTO;
import com.tl_connect.dev.modules.notification.entity.Notification;
import com.tl_connect.dev.modules.notification.projection.NotificationRow;
import com.tl_connect.dev.modules.notification.projection.PrepareNotificationView;
import com.tl_connect.dev.modules.notification.repository.NotificationReadRepository;
import com.tl_connect.dev.modules.notification.repository.NotificationRepository;
import com.tl_connect.dev.modules.notification.service.NotificationServiceImpl;
import com.tl_connect.dev.modules.student.service.interfaces.StudentService;
import com.tl_connect.dev.shared.common.dto.PagedResponse;
import com.tl_connect.dev.shared.common.enums.NotificationType;
import com.tl_connect.dev.shared.ultility.NotificationHelper;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CourseClassService courseClassService;

    @Mock
    private NotificationReadRepository notificationReadRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private PrepareNotificationView testPrepareNotificationView;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("Test Notification");
        testNotification.setContent("Test notification content");
        testNotification.setCreatedAt(LocalDateTime.now());

        testPrepareNotificationView = new PrepareNotificationView() {
            @Override
            public Long getOauthUserId() {
                return 100L;
            }

            @Override
            public Long getStudentClassId() {
                return 10L;
            }

            @Override
            public Long getFacultyId() {
                return 5L;
            }
        };
    }

    @Nested
    @DisplayName("prepareNotification Tests")
    class PrepareNotificationTests {

        @Test
        @DisplayName("Should prepare notification with topics successfully")
        void testPrepareNotificationSuccess() {
            // Arrange
            Long studentId = 1L;
            List<Long> courseClassIds = List.of(20L, 21L);

            when(notificationRepository.getStudentInfo(studentId))
                    .thenReturn(Optional.of(testPrepareNotificationView));
            when(courseClassService.findIdsByStudentIdAndSemesterId(studentId, LocalDate.now()))
                    .thenReturn(courseClassIds);
            when(notificationHelper.buildTopic(any(), any()))
                    .thenReturn("topic");

            // Act
            PrepareNotificationDTO result = notificationService.prepareNotification(studentId);

            // Assert
            assertNotNull(result);
            assertEquals(testPrepareNotificationView.getOauthUserId(), result.getOauthUserId());
            assertEquals(testPrepareNotificationView.getStudentClassId(), result.getStudentClassId());
            assertEquals(testPrepareNotificationView.getFacultyId(), result.getFacultyId());
            assertNotNull(result.getTopics());
            verify(notificationRepository, times(1)).getStudentInfo(studentId);
        }

        @Test
        @DisplayName("Should handle null course class IDs")
        void testPrepareNotificationNullCourseClassIds() {
            // Arrange
            Long studentId = 1L;

            when(notificationRepository.getStudentInfo(studentId))
                    .thenReturn(Optional.of(testPrepareNotificationView));
            when(courseClassService.findIdsByStudentIdAndSemesterId(studentId, LocalDate.now()))
                    .thenReturn(null);
            when(notificationHelper.buildTopic(any(), any()))
                    .thenReturn("topic");

            // Act
            PrepareNotificationDTO result = notificationService.prepareNotification(studentId);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getCourseClassIds());
            assertTrue(result.getCourseClassIds().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when student not found")
        void testPrepareNotificationStudentNotFound() {
            // Arrange
            Long studentId = 999L;

            when(notificationRepository.getStudentInfo(studentId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(Exception.class, () -> notificationService.prepareNotification(studentId));
        }
    }

    @Nested
    @DisplayName("getAllNotification Tests")
    class GetAllNotificationTests {

        @Test
        @DisplayName("Should return all notifications for student successfully")
        void testGetAllNotificationSuccess() {
            // Arrange
            Long studentId = 1L;
            NotificationReqDTO reqDTO = new NotificationReqDTO();
            reqDTO.setOauthUserId(100L);
            reqDTO.setStudentClassId(10L);
            reqDTO.setFacultyId(5L);
            reqDTO.setCourseClassIds(List.of(20L));

            Pageable pageable = PageRequest.of(0, 10);
            List<NotificationRow> rows = new ArrayList<>();
            Page<NotificationRow> notificationPage = new PageImpl<>(rows, pageable, 0);

            when(notificationRepository.findAllNotificationByStudent(
                    studentId,
                    reqDTO.getOauthUserId(),
                    reqDTO.getStudentClassId(),
                    reqDTO.getFacultyId(),
                    reqDTO.getCourseClassIds(),
                    pageable,
                    null))
                    .thenReturn(notificationPage);

            // Act
            PagedResponse<SummaryNotifyDTO> result = notificationService
                    .getAllNotification(studentId, reqDTO, null, pageable);

            // Assert
            assertNotNull(result);
            verify(notificationRepository, times(1))
                    .findAllNotificationByStudent(
                            studentId,
                            reqDTO.getOauthUserId(),
                            reqDTO.getStudentClassId(),
                            reqDTO.getFacultyId(),
                            reqDTO.getCourseClassIds(),
                            pageable,
                            null);
        }

        @Test
        @DisplayName("Should handle empty course class IDs")
        void testGetAllNotificationEmptyCourseClassIds() {
            // Arrange
            Long studentId = 1L;
            NotificationReqDTO reqDTO = new NotificationReqDTO();
            reqDTO.setOauthUserId(100L);
            reqDTO.setStudentClassId(10L);
            reqDTO.setFacultyId(5L);
            reqDTO.setCourseClassIds(new ArrayList<>());

            Pageable pageable = PageRequest.of(0, 10);
            List<NotificationRow> rows = new ArrayList<>();
            Page<NotificationRow> notificationPage = new PageImpl<>(rows, pageable, 0);

            when(notificationRepository.findAllNotificationByStudent(
                    eq(studentId),
                    eq(100L),
                    eq(10L),
                    eq(5L),
                    eq(List.of(-1L)),
                    eq(pageable),
                    isNull()))
                    .thenReturn(notificationPage);

            // Act
            PagedResponse<SummaryNotifyDTO> result = notificationService
                    .getAllNotification(studentId, reqDTO, null, pageable);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should return paginated results")
        void testGetAllNotificationPaginated() {
            // Arrange
            Long studentId = 1L;
            NotificationReqDTO reqDTO = new NotificationReqDTO();
            reqDTO.setOauthUserId(100L);
            reqDTO.setStudentClassId(10L);
            reqDTO.setFacultyId(5L);
            reqDTO.setCourseClassIds(List.of(20L));

            Pageable pageable = PageRequest.of(1, 5);
            List<NotificationRow> rows = new ArrayList<>();
            Page<NotificationRow> notificationPage = new PageImpl<>(rows, pageable, 50);

            when(notificationRepository.findAllNotificationByStudent(
                    anyLong(), anyLong(), anyLong(), anyLong(), anyList(), eq(pageable), anyString()))
                    .thenReturn(notificationPage);

            // Act
            PagedResponse<SummaryNotifyDTO> result = notificationService
                    .getAllNotification(studentId, reqDTO, "search", pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getPage());
        }
    }

    @Nested
    @DisplayName("getDetailNotification Tests")
    class GetDetailNotificationTests {

        @Test
        @DisplayName("Should return notification details successfully")
        void testGetDetailNotificationSuccess() {
            // Arrange
            Long notificationId = 1L;
            Notification notification = new Notification();
            notification.setId(notificationId);
            notification.setTitle("Test Notification");
            notification.setContent("Test content");
            notification.setCreatedAt(LocalDateTime.now());

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.of(notification));

            // Act
            DetailNotifyDTO result = notificationService.getDetailNotification(notificationId);

            // Assert
            assertNotNull(result);
            assertEquals("Test Notification", result.getTitle());
            assertEquals("Test content", result.getContent());
            verify(notificationRepository, times(1)).findById(notificationId);
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void testGetDetailNotificationNotFound() {
            // Arrange
            Long notificationId = 999L;

            when(notificationRepository.findById(notificationId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(Exception.class, () -> notificationService.getDetailNotification(notificationId));
        }
    }

    @Nested
    @DisplayName("countUnreadNotification Tests")
    class CountUnreadNotificationTests {

        @Test
        @DisplayName("Should count unread notifications successfully")
        void testCountUnreadNotificationSuccess() {
            // Arrange
            Long studentId = 1L;
            NotificationReqDTO reqDTO = new NotificationReqDTO();
            reqDTO.setOauthUserId(100L);
            reqDTO.setStudentClassId(10L);
            reqDTO.setFacultyId(5L);
            reqDTO.setCourseClassIds(List.of(20L));

            UnreadNotificationDTO expectedDTO = new UnreadNotificationDTO();
            expectedDTO.setCount(5L);

            when(notificationRepository.countUnreadNotification(
                    studentId,
                    reqDTO.getOauthUserId(),
                    reqDTO.getStudentClassId(),
                    reqDTO.getFacultyId(),
                    reqDTO.getCourseClassIds()))
                    .thenReturn(5L);

            // Act
            UnreadNotificationDTO result = notificationService.countUnreadNotification(studentId, reqDTO);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getCount());
        }
    }

    @Nested
    @DisplayName("markNotificationAsRead Tests")
    class MarkNotificationAsReadTests {

        @Test
        @DisplayName("Should mark notifications as read successfully")
        void testMarkNotificationAsReadSuccess() {
            // Arrange
            Long studentId = 1L;
            List<Long> notificationIds = List.of(1L, 2L, 3L);

            doNothing().when(notificationReadRepository)
                    .markAsRead(studentId, notificationIds.toArray(new Long[0]));

            // Act
            notificationService.markNotificationAsRead(studentId, notificationIds);

            // Assert
            verify(notificationReadRepository, times(1)).markAsRead(studentId, notificationIds.toArray(new Long[0]));
        }

        @Test
        @DisplayName("Should handle empty notification list")
        void testMarkNotificationAsReadEmpty() {
            // Arrange
            Long studentId = 1L;
            List<Long> notificationIds = new ArrayList<>();

            // Act & Assert
            assertDoesNotThrow(() -> notificationService.markNotificationAsRead(studentId, notificationIds));
        }
    }
}
