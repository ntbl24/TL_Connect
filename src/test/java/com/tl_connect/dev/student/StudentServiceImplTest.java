// package com.tl_connect.dev.student;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Optional;
// import java.util.Set;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;

// import com.tl_connect.dev.modules.student.dto.StudentFullInfo;
// import com.tl_connect.dev.modules.student.entity.Student;
// import com.tl_connect.dev.modules.student.projection.StudentRow;
// import com.tl_connect.dev.modules.student.repository.StudentRepository;
// import com.tl_connect.dev.modules.student.service.StudentServiceImpl;
// import com.tl_connect.dev.modules.student_class.service.interfaces.StudentClassService;
// import com.tl_connect.dev.shared.common.dto.PagedResponse;
// import com.tl_connect.dev.shared.common.enums.Gender;
// import com.tl_connect.dev.shared.common.enums.StudentStatus;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("StudentServiceImpl Tests")
// class StudentServiceImplTest {

//     @Mock
//     private StudentRepository studentRepository;

//     @Mock
//     private StudentClassService studentClassService;

//     @InjectMocks
//     private StudentServiceImpl studentService;

//     private Student testStudent;
//     private Student testStudent2;

//     @BeforeEach
//     void setUp() {
//         testStudent = Student.builder()
//                 .id(1L)
//                 .oauthUserId(100L)
//                 .studentClassId(10L)
//                 .fullName("Nguyen Van A")
//                 .studentCode("SV001")
//                 .gender(Gender.NAM)
//                 .dateOfBirth(LocalDate.of(2003, 1, 15))
//                 .avatarUrl("https://example.com/avatar.jpg")
//                 .status(StudentStatus.ACTIVE)
//                 .createdAt(LocalDateTime.now())
//                 .updatedAt(LocalDateTime.now())
//                 .build();

//         testStudent2 = Student.builder()
//                 .id(2L)
//                 .oauthUserId(101L)
//                 .studentClassId(10L)
//                 .fullName("Tran Thi B")
//                 .studentCode("SV002")
//                 .gender(Gender.NU)
//                 .dateOfBirth(LocalDate.of(2003, 5, 20))
//                 .status(StudentStatus.ACTIVE)
//                 .build();
//     }


//     @Nested
//     @DisplayName("getAllStudents Tests")
//     class GetAllStudentsTests {

//         @Test
//         @DisplayName("Should return all students successfully without faculty filter")
//         void testGetAllStudentsSuccess() {
//             // Arrange
//             Pageable pageable = PageRequest.of(0, 10);
//             List<StudentRow> studentRows = new ArrayList<>();
//             Page<StudentRow> studentPage = new PageImpl<>(studentRows, pageable, 0);

//             when(studentRepository.findAllStudent(pageable, null))
//                     .thenReturn(studentPage);

//             // Act
//             PagedResponse<StudentFullInfo> result = studentService.getAllStudents(pageable, null);

//             // Assert
//             assertNotNull(result);
//             assertEquals(0, result.getContent().size());
//             assertEquals(0, result.getTotalElements());
//             verify(studentRepository, times(1)).findAllStudent(pageable, null);
//         }

//         @Test
//         @DisplayName("Should return students filtered by faculty code")
//         void testGetAllStudentsByFacultyCode() {
//             // Arrange
//             String facultyCode = "CNTT";
//             Pageable pageable = PageRequest.of(0, 10);
//             List<StudentRow> studentRows = new ArrayList<>();
//             Page<StudentRow> studentPage = new PageImpl<>(studentRows, pageable, 0);

//             when(studentRepository.findAllStudent(pageable, facultyCode))
//                     .thenReturn(studentPage);

//             // Act
//             PagedResponse<StudentFullInfo> result = studentService.getAllStudents(pageable, facultyCode);

//             // Assert
//             assertNotNull(result);
//             verify(studentRepository, times(1)).findAllStudent(pageable, facultyCode);
//         }

//         @Test
//         @DisplayName("Should convert blank faculty code to null")
//         void testGetAllStudentsWithBlankFacultyCode() {
//             // Arrange
//             Pageable pageable = PageRequest.of(0, 10);
//             List<StudentRow> studentRows = new ArrayList<>();
//             Page<StudentRow> studentPage = new PageImpl<>(studentRows, pageable, 0);

//             when(studentRepository.findAllStudent(pageable, null))
//                     .thenReturn(studentPage);

//             // Act
//             studentService.getAllStudents(pageable, "   ");

//             // Assert
//             verify(studentRepository, times(1)).findAllStudent(pageable, null);
//         }

//         @Test
//         @DisplayName("Should handle pagination correctly")
//         void testGetAllStudentsWithPagination() {
//             // Arrange
//             Pageable pageable = PageRequest.of(2, 20);
//             List<StudentRow> studentRows = new ArrayList<>();
//             Page<StudentRow> studentPage = new PageImpl<>(studentRows, pageable, 100);

//             when(studentRepository.findAllStudent(pageable, null))
//                     .thenReturn(studentPage);

//             // Act
//             PagedResponse<StudentFullInfo> result = studentService.getAllStudents(pageable, null);

//             // Assert
//             assertEquals(pageable.getPageNumber(), result.getCurrentPage());
//             verify(studentRepository, times(1)).findAllStudent(pageable, null);
//         }
//     }

//     @Nested
//     @DisplayName("findById Tests")
//     class FindByIdTests {

//         @Test
//         @DisplayName("Should return student by ID successfully")
//         void testFindByIdSuccess() {
//             // Arrange
//             Long studentId = 1L;
//             when(studentRepository.findById(studentId))
//                     .thenReturn(Optional.of(testStudent));

//             // Act
//             Student result = studentService.findById(studentId);

//             // Assert
//             assertNotNull(result);
//             assertEquals(studentId, result.getId());
//             assertEquals("SV001", result.getStudentCode());
//             verify(studentRepository, times(1)).findById(studentId);
//         }

//         @Test
//         @DisplayName("Should throw NotFoundException when student not found")
//         void testFindByIdNotFound() {
//             // Arrange
//             Long studentId = 999L;
//             when(studentRepository.findById(studentId))
//                     .thenReturn(Optional.empty());

//             // Act & Assert
//             assertThrows(Exception.class, () -> studentService.findById(studentId));
//             verify(studentRepository, times(1)).findById(studentId);
//         }
//     }

//     @Nested
//     @DisplayName("findByStudentCode Tests")
//     class FindByStudentCodeTests {

//         @Test
//         @DisplayName("Should return student by student code successfully")
//         void testFindByStudentCodeSuccess() {
//             // Arrange
//             String studentCode = "SV001";
//             when(studentRepository.findByStudentCode(studentCode))
//                     .thenReturn(Optional.of(testStudent));

//             // Act
//             Student result = studentService.findByStudentCode(studentCode);

//             // Assert
//             assertNotNull(result);
//             assertEquals(studentCode, result.getStudentCode());
//             verify(studentRepository, times(1)).findByStudentCode(studentCode);
//         }

//         @Test
//         @DisplayName("Should throw NotFoundException when student code not found")
//         void testFindByStudentCodeNotFound() {
//             // Arrange
//             String studentCode = "INVALID";
//             when(studentRepository.findByStudentCode(studentCode))
//                     .thenReturn(Optional.empty());

//             // Act & Assert
//             assertThrows(Exception.class, () -> studentService.findByStudentCode(studentCode));
//             verify(studentRepository, times(1)).findByStudentCode(studentCode);
//         }
//     }

//     @Nested
//     @DisplayName("existsStudent Tests")
//     class ExistsStudentTests {

//         @Test
//         @DisplayName("Should return true when student exists")
//         void testExistsStudentTrue() {
//             // Arrange
//             Long studentId = 1L;
//             when(studentRepository.existsByStudentCode(testStudent.getStudentCode())).thenReturn(true);

//             // Act
//             boolean result = studentService.existsStudent(studentId);

//             // Assert
//             assertTrue(result);
//             verify(studentRepository, times(1)).existsByStudentCode(testStudent.getStudentCode());
//         }

//         @Test
//         @DisplayName("Should return false when student does not exist")
//         void testExistsStudentFalse() {
//             // Arrange
//             Long studentId = 999L;
//             when(studentRepository.existsById(studentId)).thenReturn(false);

//             // Act
//             boolean result = studentService.existsStudent(studentId);

//             // Assert
//             assertFalse(result);
//             verify(studentRepository, times(1)).existsById(studentId);
//         }
//     }

//     @Nested
//     @DisplayName("findByStudentCodeIn Tests")
//     class FindByStudentCodeInTests {

//         @Test
//         @DisplayName("Should return students by student codes successfully")
//         void testFindByStudentCodeInSuccess() {
//             // Arrange
//             Set<String> studentCodes = new HashSet<>();
//             studentCodes.add("SV001");
//             studentCodes.add("SV002");

//             List<Student> students = List.of(testStudent, testStudent2);

//             when(studentRepository.findByStudentCodeIn(studentCodes))
//                     .thenReturn(students);

//             // Act
//             List<Student> result = studentService.findByStudentCodeIn(studentCodes);

//             // Assert
//             assertNotNull(result);
//             assertEquals(2, result.size());
//             verify(studentRepository, times(1)).findByStudentCodeIn(studentCodes);
//         }

//         @Test
//         @DisplayName("Should return empty list when no students found")
//         void testFindByStudentCodeInEmpty() {
//             // Arrange
//             Set<String> studentCodes = new HashSet<>();
//             studentCodes.add("INVALID1");
//             studentCodes.add("INVALID2");

//             when(studentRepository.findByStudentCodeIn(studentCodes))
//                     .thenReturn(new ArrayList<>());

//             // Act
//             List<Student> result = studentService.findByStudentCodeIn(studentCodes);

//             // Assert
//             assertNotNull(result);
//             assertTrue(result.isEmpty());
//         }
//     }

//     @Nested
//     @DisplayName("getAllStudentsBySearch Tests")
//     class GetAllStudentsBySearchTests {

//         @Test
//         @DisplayName("Should search students by name")
//         void testSearchByName() {
//             // Arrange
//             String search = "Nguyen";
//             Pageable pageable = PageRequest.of(0, 10);
//             List<Student> students = List.of(testStudent);
//             Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

//             when(studentRepository.findAll(any(), eq(pageable)))
//                     .thenReturn(studentPage);

//             // Act
//             Page<Student> result = studentService.getAllStudentsBySearch(search, pageable);

//             // Assert
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//             verify(studentRepository, times(1)).findAll(any(), eq(pageable));
//         }

//         @Test
//         @DisplayName("Should search students by code")
//         void testSearchByCode() {
//             // Arrange
//             String search = "SV001";
//             Pageable pageable = PageRequest.of(0, 10);
//             List<Student> students = List.of(testStudent);
//             Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

//             when(studentRepository.findAll(any(), eq(pageable)))
//                     .thenReturn(studentPage);

//             // Act
//             Page<Student> result = studentService.getAllStudentsBySearch(search, pageable);

//             // Assert
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//         }

//         @Test
//         @DisplayName("Should return empty when no match found")
//         void testSearchNoMatch() {
//             // Arrange
//             String search = "NONEXISTENT";
//             Pageable pageable = PageRequest.of(0, 10);
//             Page<Student> studentPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

//             when(studentRepository.findAll(any(), eq(pageable)))
//                     .thenReturn(studentPage);

//             // Act
//             Page<Student> result = studentService.getAllStudentsBySearch(search, pageable);

//             // Assert
//             assertNotNull(result);
//             assertTrue(result.isEmpty());
//         }
//     }

//     @Nested
//     @DisplayName("findStudentByFacultyCode Tests")
//     class FindStudentByFacultyCodeTests {

//         @Test
//         @DisplayName("Should return students by faculty code")
//         void testFindStudentByFacultyCodeSuccess() {
//             // Arrange
//             String facultyCode = "CNTT";
//             Pageable pageable = PageRequest.of(0, 10);
//             List<Student> students = List.of(testStudent);
//             Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

//             when(studentRepository.findByFacultyCode(pageable, facultyCode))
//                     .thenReturn(studentPage);

//             // Act
//             Page<Student> result = studentService.findStudentByFacultyCode(pageable, facultyCode);

//             // Assert
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//             verify(studentRepository, times(1)).findByFacultyCode(pageable, facultyCode);
//         }

//         @Test
//         @DisplayName("Should return empty when no students in faculty")
//         void testFindStudentByFacultyCodeEmpty() {
//             // Arrange
//             String facultyCode = "UNKNOWN";
//             Pageable pageable = PageRequest.of(0, 10);
//             Page<Student> studentPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

//             when(studentRepository.findByFacultyCode(pageable, facultyCode))
//                     .thenReturn(studentPage);

//             // Act
//             Page<Student> result = studentService.findStudentByFacultyCode(pageable, facultyCode);

//             // Assert
//             assertNotNull(result);
//             assertTrue(result.isEmpty());
//         }
//     }
// }
