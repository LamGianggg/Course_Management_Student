package com.example.studentmanagement;

import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.specification.StudentSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StudentSpecificationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testStudentSpecification() {
        SearchCriteria criteria = new SearchCriteria("name", ":", "Nguyen");
        StudentSpecification spec = new StudentSpecification(criteria);
        assertNotNull(spec);

        // Execute toPredicate through repository
        assertNotNull(studentRepository.findAll(spec));
    }
}
