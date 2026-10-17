package com.example.studentmanagement;

import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.specification.StudentSpecification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentSpecificationTest {

    @Test
    void testStudentSpecification() {
        SearchCriteria criteria = new SearchCriteria("name", ":", "Nguyen");
        StudentSpecification spec = new StudentSpecification(criteria);
        assertNotNull(spec);
    }
}
