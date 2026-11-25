package com.tl_connect.dev.enroll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.tl_connect.dev.modules.course_class.service.interfaces.CourseClassService;
import com.tl_connect.dev.modules.enroll.dto.CourseClassForEnrollDTO;
import com.tl_connect.dev.modules.enroll.dto.EnrollmentHeaderDTO;
import com.tl_connect.dev.modules.enroll.dto.EnrollmentValidationResult;
import com.tl_connect.dev.modules.enroll.dto.EnrollViewDTO;
import com.tl_connect.dev.modules.enroll.dto.ScheduleForEnrollDTO;
import com.tl_connect.dev.modules.enroll.entity.EnrollmentPeriod;
import com.tl_connect.dev.modules.enroll.projection.CourseClassForEnrollRow;
import com.tl_connect.dev.modules.enroll.service.EnrollServiceImpl;
import com.tl_connect.dev.modules.enroll.service.EnrollTransactionService;
import com.tl_connect.dev.modules.enroll.service.EnrollmentPeriodServiceImpl;
import com.tl_connect.dev.modules.enroll.service.EnrollmentPreCheckService;
import com.tl_connect.dev.modules.enroll.service.StudentEnrollmentProfileService;
import com.tl_connect.dev.modules.enroll.service.interfaces.StudentCourseClassService;
import com.tl_connect.dev.modules.schedule.ScheduleRepository;
import com.tl_connect.dev.modules.semester.Semester;
import com.tl_connect.dev.modules.semester.service.interfaces.SemesterService;
import com.tl_connect.dev.modules.study_program.projection.StudyProgramHeaderView;
import com.tl_connect.dev.modules.study_program.service.interfaces.StudyProgramService;
import com.tl_connect.dev.shared.common.enums.ResponseStatus;
import com.tl_connect.dev.shared.common.exception.DuplicateRegistrationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollServiceImpl Tests")
class EnrollServiceImplTest {

    @Mock
    private StudentCourseClassService studentCourseClassService;

    @Mock
    private CourseClassService courseClassService;

    @Mock
    private StudyProgramService studyProgramService;

    @Mock
    private StudentEnrollmentProfileService profileService;

    @Mock
    private EnrollmentPeriodServiceImpl enrollmentPeriodService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private EnrollmentPreCheckService preCheckService;

    @Mock
    private EnrollTransactionService enrollTransactionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SemesterService semesterService;

    @InjectMocks
    private EnrollServiceImpl enrollService;

    private EnrollmentPeriod testEnrollmentPeriod;
    private Semester testSemester;
    private StudyProgramHeaderView testStudyProgramHeader;

    @BeforeEach
    void setUp() {
        testEnrollmentPeriod = new EnrollmentPeriod();
        testEnrollmentPeriod.setId(1L);
        testEnrollmentPeriod.setSemesterId(5L);
        testEnrollmentPeriod.setStartTime(LocalDateTime.now().minusDays(1));
        testEnrollmentPeriod.setEndTime(LocalDateTime.now().plusDays(7));

        testSemester = new Semester();
        testSemester.setId(5L);
        testSemester.setSemesterName("Semester 1");
        testSemester.setSemesterCode("HK1");

        testStudyProgramHeader = new StudyProgramHeaderView() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getStudyProgramCode() {
                return "CS001";
            }

            @Override
            public String getStudyProgramName() {
                return "Computer Science";
            }

            @Override
            public Integer getStartYear() {
                return 2022;
            }

            @Override
            public Integer getTotalCredits() {
                return 150;
            }

            @Override
            public String getMajorCode() {
                return "CS001";
            }

            @Override
            public String getMajorName() {
                return "Computer Science";
            }

            @Override
            public String getFaculty() {
                return "Faculty of Information Technology";
            }
        };
    }

    @Nested
    @DisplayName("getEnrollmentPeriods Tests")
    class GetEnrollmentPeriodsTests {

        @Test
        @DisplayName("Should return enrollment periods successfully")
        void testGetEnrollmentPeriodsSuccess() {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";

            when(enrollmentPeriodService.getPeriod()).thenReturn(testEnrollmentPeriod);
            when(semesterService.findByIdAndIsActive(testEnrollmentPeriod.getSemesterId()))
                    .thenReturn(testSemester);

            // Act
            EnrollmentHeaderDTO result = enrollService.getEnrollmentPeriods(studentId, studyProgramCode);

            // Assert
            assertNotNull(result);
            assertEquals(testEnrollmentPeriod.getSemesterId(), result.getSemesterId());
            assertEquals("Semester 1", result.getSemesterName());
            assertEquals("HK1", result.getSemesterCode());
            verify(enrollmentPeriodService, times(1)).getPeriod();
            verify(semesterService, times(1)).findByIdAndIsActive(testEnrollmentPeriod.getSemesterId());
        }
    }

    @Nested
    @DisplayName("getAvailableSubjects Tests")
    class GetAvailableSubjectsTests {

        @Test
        @DisplayName("Should return available subjects during enrollment period")
        void testGetAvailableSubjetsSuccess() {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";

            when(studyProgramService.findByStudyProgramCodeAndStudentId(studyProgramCode, studentId))
                    .thenReturn(testStudyProgramHeader);
            when(enrollmentPeriodService.getPeriod()).thenReturn(testEnrollmentPeriod);

            // Act
            EnrollViewDTO result = enrollService.getAvailableSubjects(studentId, studyProgramCode);

            // Assert
            assertNotNull(result);
            assertEquals(studyProgramCode, result.getStudyProgramCode());
            assertEquals(testEnrollmentPeriod.getSemesterId(), result.getSemesterId());
            verify(studyProgramService, times(1))
                    .findByStudyProgramCodeAndStudentId(studyProgramCode, studentId);
            verify(enrollmentPeriodService, times(1)).getPeriod();
        }

        @Test
        @DisplayName("Should return empty subjects when outside enrollment period")
        void testGetAvailableSubjectsOutsidePeriod() {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";
            
            EnrollmentPeriod closedPeriod = new EnrollmentPeriod();
            closedPeriod.setStartTime(LocalDateTime.now().minusDays(10));
            closedPeriod.setEndTime(LocalDateTime.now().minusDays(5));

            when(studyProgramService.findByStudyProgramCodeAndStudentId(studyProgramCode, studentId))
                    .thenReturn(testStudyProgramHeader);
            when(enrollmentPeriodService.getPeriod()).thenReturn(closedPeriod);

            // Act
            EnrollViewDTO result = enrollService.getAvailableSubjects(studentId, studyProgramCode);

            // Assert
            assertNotNull(result);
            assertTrue(result.getSubjects().isEmpty());
        }
    }

    @Nested
    @DisplayName("getAvailableCourseClasses Tests")
    class GetAvailableCourseClassesTests {

        @Test
        @DisplayName("Should return course classes for a subject successfully")
        void testGetAvailableCourseClassesSuccess() {
            // Arrange
            Long subjectId = 10L;
            Long semesterId = 5L;
            
            CourseClassForEnrollRow row = createCourseClassForEnrollRow(1L, "CS001", "CS001 - Morning");

            when(courseClassService.findCourseClassForEnrollment(subjectId, semesterId))
                    .thenReturn(List.of(row));

            // Act
            List<CourseClassForEnrollDTO> result = enrollService.getAvailableCourseClasses(subjectId, semesterId);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(courseClassService, times(1))
                    .findCourseClassForEnrollment(subjectId, semesterId);
        }

        @Test
        @DisplayName("Should return empty list when no course classes available")
        void testGetAvailableCourseClassesEmpty() {
            // Arrange
            Long subjectId = 999L;
            Long semesterId = 5L;

            when(courseClassService.findCourseClassForEnrollment(subjectId, semesterId))
                    .thenReturn(new ArrayList<>());

            // Act
            List<CourseClassForEnrollDTO> result = enrollService.getAvailableCourseClasses(subjectId, semesterId);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should group schedules by course class")
        void testGetAvailableCourseClassesGroupsSchedules() {
            // Arrange
            Long subjectId = 10L;
            Long semesterId = 5L;
            
            CourseClassForEnrollRow row1 = createCourseClassForEnrollRow(1L, "CS001", "CS001 - Morning");
            CourseClassForEnrollRow row2 = createCourseClassForEnrollRow(1L, "CS001", "CS001 - Morning");

            when(courseClassService.findCourseClassForEnrollment(subjectId, semesterId))
                    .thenReturn(List.of(row1, row2));

            // Act
            List<CourseClassForEnrollDTO> result = enrollService.getAvailableCourseClasses(subjectId, semesterId);

            // Assert
            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getSchedules().size());
        }
    }

    @Nested
    @DisplayName("enroll Tests")
    class EnrollTests {

        @Test
        @DisplayName("Should throw exception when enrollment period not started")
        void testEnrollOutsidePeriod() {
            // Arrange
            Long studentId = 1L;
            Long courseClassId = 10L;
            Long studyProgramId = 1L;

            EnrollmentPeriod closedPeriod = new EnrollmentPeriod();
            closedPeriod.setStartTime(LocalDateTime.now().plusDays(10));
            closedPeriod.setEndTime(LocalDateTime.now().plusDays(20));

            when(enrollmentPeriodService.getPeriod()).thenReturn(closedPeriod);

            // Act & Assert
            assertThrows(DuplicateRegistrationException.class,
                    () -> enrollService.enroll(studentId, courseClassId, studyProgramId));
        }

        @Test
        @DisplayName("Should throw exception when enrollment period ended")
        void testEnrollAfterPeriodEnd() {
            // Arrange
            Long studentId = 1L;
            Long courseClassId = 10L;
            Long studyProgramId = 1L;

            EnrollmentPeriod closedPeriod = new EnrollmentPeriod();
            closedPeriod.setStartTime(LocalDateTime.now().minusDays(20));
            closedPeriod.setEndTime(LocalDateTime.now().minusDays(5));

            when(enrollmentPeriodService.getPeriod()).thenReturn(closedPeriod);

            // Act & Assert
            assertThrows(DuplicateRegistrationException.class,
                    () -> enrollService.enroll(studentId, courseClassId, studyProgramId));
        }
    }

    @Nested
    @DisplayName("drop Tests")
    class DropTests {

        @Test
        @DisplayName("Should drop enrollment successfully")
        void testDropEnrollmentSuccess() {
            // Arrange

            Long studentId = 1L;

            Long courseClassId = 1L;

            when(enrollmentPeriodService.getPeriod()).thenReturn(testEnrollmentPeriod);

            // Act
            enrollService.drop(studentId, courseClassId);

            // Assert
            verify(enrollmentPeriodService, times(1)).getPeriod();
        }

        @Test
        @DisplayName("Should throw exception when trying to drop outside period")
        void testDropEnrollmentOutsidePeriod() {
            // Arrange

            Long studentId = 1L;

            Long courseClassId = 1L;

            EnrollmentPeriod closedPeriod = new EnrollmentPeriod();
            closedPeriod.setStartTime(LocalDateTime.now().minusDays(10));
            closedPeriod.setEndTime(LocalDateTime.now().minusDays(5));

            when(enrollmentPeriodService.getPeriod()).thenReturn(closedPeriod);

            // Act & Assert
            assertThrows(Exception.class, () -> enrollService.drop(studentId,courseClassId));
        }
    }

    // ==================== Helper Methods ====================

    private CourseClassForEnrollRow createCourseClassForEnrollRow(Long id, String classCode, String className) {
        return new CourseClassForEnrollRow() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getLecturerCode() {
                return "LEC001";
            }

            @Override
            public String getLecturerName() {
                return "Dr. Smith";
            }

            @Override
            public String getClassCode() {
                return classCode;
            }

            @Override
            public String getClassName() {
                return className;
            }

            @Override
            public Integer getCapacity() {
                return 30;
            }

            @Override
            public Integer getEnrolledCount() {
                return 25;
            }

            @Override
            public Integer getDayOfWeek() {
                return 2;
            }

            @Override
            public Integer getStartPeriod() {
                return 1;
            }

            @Override
            public Integer getEndPeriod() {
                return 3;
            }

            @Override
            public LocalTime getStartTime() {
                return LocalTime.of(8, 0);
            }

            @Override
            public LocalTime getEndTime() {
                return LocalTime.of(10, 0);
            }

            @Override
            public String getRoom() {
                return "A101";
            }
        };
    }
}
