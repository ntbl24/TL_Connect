package com.tl_connect.dev.academic_result;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tl_connect.dev.modules.academic_result.dto.AcademicResultAdmDTO;
import com.tl_connect.dev.modules.academic_result.dto.AcademicResultDTO;
import com.tl_connect.dev.modules.academic_result.entity.StudentSubjectResult;
import com.tl_connect.dev.modules.academic_result.projection.SemesterSummaryRow;
import com.tl_connect.dev.modules.academic_result.projection.SemesterSummaryView;
import com.tl_connect.dev.modules.academic_result.projection.SubjectResultAdmRow;
import com.tl_connect.dev.modules.academic_result.projection.SubjectResultRow;
import com.tl_connect.dev.modules.academic_result.repository.StudentSemesterSummaryRepository;
import com.tl_connect.dev.modules.academic_result.repository.StudentSubjectResultRepository;
import com.tl_connect.dev.modules.academic_result.service.AcademicResultServiceImpl;
import com.tl_connect.dev.modules.academic_result.service.interfaces.AcademicResultExporter;
import com.tl_connect.dev.modules.student.entity.Student;
import com.tl_connect.dev.modules.student.service.interfaces.StudentService;
import com.tl_connect.dev.shared.common.dto.PagedResponse;
import com.tl_connect.dev.shared.common.enums.Gender;
import com.tl_connect.dev.shared.common.enums.StudentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicResultServiceImpl Tests")
class AcademicResultServiceImplTest {

    @Mock
    private StudentSubjectResultRepository resultRepository;

    @Mock
    private StudentSemesterSummaryRepository semesterSummaryRepository;

    @Mock
    private AcademicResultExporter excelExporter;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private AcademicResultServiceImpl academicResultService;

    private Student testStudent;
    private StudentSubjectResult testSubjectResult;

    @BeforeEach
    void setUp() {
        testStudent = Student.builder()
                .id(1L)
                .oauthUserId(100L)
                .studentClassId(10L)
                .fullName("Nguyen Van A")
                .studentCode("SV001")
                .gender(Gender.NAM)
                .status(StudentStatus.ACTIVE)
                .build();

        testSubjectResult = new StudentSubjectResult();
        testSubjectResult.setId(1L);
        testSubjectResult.setStudentId(1L);
        testSubjectResult.setSubjectId(10L);
        testSubjectResult.setSemesterId(5L);
        testSubjectResult.setFinalScore(new BigDecimal("8.5"));
        testSubjectResult.setScore10(new BigDecimal("8.5"));
        testSubjectResult.setScore4(new BigDecimal("4.0"));
        testSubjectResult.setCredits(3);
        testSubjectResult.setLetterGrade("A");
        testSubjectResult.setIsPass(true);
    }

    @Nested
    @DisplayName("getAllAcademicResult Tests")
    class GetAllAcademicResultTests {

        @Test
        @DisplayName("Should return all academic results successfully without faculty filter")
        void testGetAllAcademicResultsSuccess() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Student> students = new ArrayList<>();
            students.add(testStudent);
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

            List<SubjectResultAdmRow> subjectResults = new ArrayList<>();
            List<SemesterSummaryRow> semesterSummaries = new ArrayList<>();

            when(studentService.findStudentByFacultyCode(pageable, null))
                    .thenReturn(studentPage);
            when(resultRepository.findSubjectResultByStudentIds(anyList()))
                    .thenReturn(subjectResults);
            when(semesterSummaryRepository.findSemesterSummaryByStudentIds(anyList()))
                    .thenReturn(semesterSummaries);

            // Act
            PagedResponse<AcademicResultAdmDTO> result = academicResultService
                    .getAllAcademicResult(pageable, null);

            // Assert
            assertNotNull(result);
            verify(studentService, times(1)).findStudentByFacultyCode(pageable, null);
            verify(resultRepository, times(1)).findSubjectResultByStudentIds(anyList());
        }

        @Test
        @DisplayName("Should return results filtered by faculty code")
        void testGetAllAcademicResultsByFaculty() {
            // Arrange
            String facultyCode = "CNTT";
            Pageable pageable = PageRequest.of(0, 10);
            List<Student> students = new ArrayList<>();
            students.add(testStudent);
            Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

            List<SubjectResultAdmRow> subjectResults = new ArrayList<>();
            List<SemesterSummaryRow> semesterSummaries = new ArrayList<>();

            when(studentService.findStudentByFacultyCode(pageable, facultyCode))
                    .thenReturn(studentPage);
            when(resultRepository.findSubjectResultByStudentIds(anyList()))
                    .thenReturn(subjectResults);
            when(semesterSummaryRepository.findSemesterSummaryByStudentIds(anyList()))
                    .thenReturn(semesterSummaries);

            // Act
            PagedResponse<AcademicResultAdmDTO> result = academicResultService
                    .getAllAcademicResult(pageable, facultyCode);

            // Assert
            assertNotNull(result);
            verify(studentService, times(1)).findStudentByFacultyCode(pageable, facultyCode);
        }

        @Test
        @DisplayName("Should handle empty result list")
        void testGetAllAcademicResultsEmpty() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Student> studentPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

            when(studentService.findStudentByFacultyCode(pageable, null))
                    .thenReturn(studentPage);
            when(resultRepository.findSubjectResultByStudentIds(anyList()))
                    .thenReturn(new ArrayList<>());
            when(semesterSummaryRepository.findSemesterSummaryByStudentIds(anyList()))
                    .thenReturn(new ArrayList<>());

            // Act
            PagedResponse<AcademicResultAdmDTO> result = academicResultService
                    .getAllAcademicResult(pageable, null);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("getSubjectResult Tests")
    class GetSubjectResultTests {

        @Test
        @DisplayName("Should return subject results for student successfully")
        void testGetSubjectResultSuccess() {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";

            List<SubjectResultRow> subjectResults = new ArrayList<>();
            List<SemesterSummaryView> semesterSummaries = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(subjectResults);
            when(semesterSummaryRepository.findSemesterSummaryByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(semesterSummaries);

            // Act & Assert
            assertDoesNotThrow(() -> academicResultService.getSubjectResult(studentId, studyProgramCode));
        }

        @Test
        @DisplayName("Should return empty result when no subjects found")
        void testGetSubjectResultEmpty() {
            // Arrange
            Long studentId = 999L;
            String studyProgramCode = "CS001";

            when(resultRepository.findSubjectResultByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(new ArrayList<>());
            when(semesterSummaryRepository.findSemesterSummaryByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(new ArrayList<>());

            // Act & Assert
            assertDoesNotThrow(() -> academicResultService.getSubjectResult(studentId, studyProgramCode));
        }
    }

    @Nested
    @DisplayName("findSubjectResultByStudentId Tests")
    class FindSubjectResultByStudentIdTests {

        @Test
        @DisplayName("Should return all subject results for a student using study program code")
        void testFindSubjectResultByStudentIdSuccess() {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";
            List<SubjectResultRow> results = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(results);

            // Act
            List<StudentSubjectResult> result = academicResultService.findSubjectResultByStudentId(studentId);

            // Assert
            assertNotNull(result);
            verify(resultRepository, times(1)).findSubjectResultByStudentIdAndStudyProgramCode(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should return empty list when no results found")
        void testFindSubjectResultByStudentIdEmpty() {
            // Arrange
            Long studentId = 999L;

            when(resultRepository.findSubjectResultByStudentIds(anyList()))
                    .thenReturn(new ArrayList<>());

            // Act
            List<StudentSubjectResult> result = academicResultService.findSubjectResultByStudentId(studentId);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("calculateCumulativeGpa Tests")
    class CalculateCumulativeGpaTests {

        @Test
        @DisplayName("Should calculate cumulative GPA successfully")
        void testCalculateCumulativeGpaSuccess() {
            // Arrange
            Long studentId = 1L;
            Long studyProgramId = 1L;

            List<SubjectResultAdmRow> results = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIds(List.of(studentId)))
                    .thenReturn(results);

            // Act
            BigDecimal result = academicResultService.calculateCumulativeGpa(studentId, studyProgramId);

            // Assert
            assertNotNull(result);
            verify(resultRepository, times(1)).findSubjectResultByStudentIds(anyList());
        }

        @Test
        @DisplayName("Should return zero when student has no results")
        void testCalculateCumulativeGpaNoResults() {
            // Arrange
            Long studentId = 999L;
            Long studyProgramId = 1L;

            when(resultRepository.findSubjectResultByStudentIds(List.of(studentId)))
                    .thenReturn(new ArrayList<>());

            // Act
            BigDecimal result = academicResultService.calculateCumulativeGpa(studentId, studyProgramId);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result);
        }
    }

    @Nested
    @DisplayName("sumTotalCredits Tests")
    class SumTotalCreditsTests {

        @Test
        @DisplayName("Should sum total credits successfully")
        void testSumTotalCreditsSuccess() {
            // Arrange
            Long studentId = 1L;
            Long studyProgramId = 1L;

            List<SubjectResultAdmRow> results = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIds(List.of(studentId)))
                    .thenReturn(results);

            // Act
            Integer result = academicResultService.sumTotalCredits(studentId, studyProgramId);

            // Assert
            assertNotNull(result);
            verify(resultRepository, times(1)).findSubjectResultByStudentIds(anyList());
        }

        @Test
        @DisplayName("Should return zero when student has no credits")
        void testSumTotalCreditsZero() {
            // Arrange
            Long studentId = 999L;
            Long studyProgramId = 1L;

            when(resultRepository.findSubjectResultByStudentIds(List.of(studentId)))
                    .thenReturn(new ArrayList<>());

            // Act
            Integer result = academicResultService.sumTotalCredits(studentId, studyProgramId);

            // Assert
            assertEquals(0, result);
        }

        @Test
        @DisplayName("Should sum multiple credits correctly")
        void testSumTotalCreditsMultiple() {
            // Arrange
            Long studentId = 1L;
            Long studyProgramId = 1L;

            List<SubjectResultAdmRow> results = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIds(List.of(studentId)))
                    .thenReturn(results);

            // Act
            Integer result = academicResultService.sumTotalCredits(studentId, studyProgramId);

            // Assert
            assertNotNull(result);
            verify(resultRepository, times(1)).findSubjectResultByStudentIds(anyList());
        }
    }

    @Nested
    @DisplayName("exportExcel Tests")
    class ExportExcelTests {

        @Test
        @DisplayName("Should export academic results to Excel successfully")
        void testExportExcelSuccess() throws IOException {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";
            var response = mock(jakarta.servlet.http.HttpServletResponse.class);

            List<SubjectResultRow> subjectResults = new ArrayList<>();
            List<SemesterSummaryView> semesterSummaries = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(subjectResults);
            when(semesterSummaryRepository.findSemesterSummaryByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(semesterSummaries);

            // Act & Assert
            assertDoesNotThrow(() -> academicResultService.exportExcel(studentId, studyProgramCode, response));
        }

        @Test
        @DisplayName("Should handle export IO exception")
        void testExportExcelIOException() throws IOException {
            // Arrange
            Long studentId = 1L;
            String studyProgramCode = "CS001";
            var response = mock(jakarta.servlet.http.HttpServletResponse.class);

            List<SubjectResultRow> subjectResults = new ArrayList<>();
            List<SemesterSummaryView> semesterSummaries = new ArrayList<>();

            when(resultRepository.findSubjectResultByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(subjectResults);
            when(semesterSummaryRepository.findSemesterSummaryByStudentIdAndStudyProgramCode(studentId, studyProgramCode))
                    .thenReturn(semesterSummaries);
            doThrow(new IOException("Export error"))
                    .when(excelExporter).exportToExcel(any(), any());

            // Act & Assert
            assertThrows(IOException.class, () -> academicResultService.exportExcel(studentId, studyProgramCode, response));
        }
    }
}
