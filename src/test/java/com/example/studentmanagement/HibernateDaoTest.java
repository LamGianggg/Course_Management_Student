package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IDepartmentDao;
import com.example.studentmanagement.Dao.IStudentDao;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
public class HibernateDaoTest {

    @Autowired
    private IStudentDao studentDao;

    @Autowired
    private IDepartmentDao departmentDao;

    @Test
    public void testGetType() {
        Assertions.assertEquals(Student.class, studentDao.getType());
        Assertions.assertEquals(Department.class, departmentDao.getType());
    }

    @Test
    @Transactional
    public void testCreateValidEntity() {
        Department dept = new Department(null, "Test Department", "Testing DAO");
        Department savedDept = departmentDao.create(dept);
        Assertions.assertNotNull(savedDept.getId());
        Assertions.assertEquals("Test Department", savedDept.getName());

        Student student = new Student(null, "Test Student", LocalDate.of(2003, 5, 20), savedDept);
        Student savedStudent = studentDao.create(student);
        Assertions.assertNotNull(savedStudent.getId());
        Assertions.assertEquals("Test Student", savedStudent.getName());
    }

    @Test
    public void testCreateNullEntityThrowsException() {
        Exception studentEx = Assertions.assertThrows(
                Exception.class,
                () -> studentDao.create(null)
        );
        Assertions.assertTrue(studentEx.getMessage().contains("Cannot create null entity[" + Student.class.getName() + "]"));

        Exception deptEx = Assertions.assertThrows(
                Exception.class,
                () -> departmentDao.create(null)
        );
        Assertions.assertTrue(deptEx.getMessage().contains("Cannot create null entity[" + Department.class.getName() + "]"));
    }
}
