package com.example.studentmanagement;

import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    public void testEntityQueries() {
        System.out.println("\n========== TEST CÁC QUERY TRONG ENTITY ==========");

        // 1. Gọi query lọc theo ngày sinh
        List<Student> list1 = studentRepository.findByDobBetween(
                LocalDate.of(2003, 1, 1),
                LocalDate.of(2003, 12, 31)
        );
        System.out.println("1. Sinh vien sinh trong nam 2003 (" + list1.size() + " ban):");
        list1.forEach(s -> System.out.println("   -> " + s.getName() + " | DOB: " + s.getDob()));

        // 2. Gọi query tìm sinh viên sinh sau một ngày
        List<Student> list2 = studentRepository.findStudentsBornAfter(LocalDate.of(2003, 1, 1));
        System.out.println("\n2. Sinh vien sinh sau 2003-01-01 (" + list2.size() + " ban):");
        list2.forEach(s -> System.out.println("   -> " + s.getName() + " | DOB: " + s.getDob()));

        // 3. Gọi query lấy kèm thông tin khoa có sắp xếp
        List<Student> list3 = studentRepository.findAllWithDepartmentOrdered();
        System.out.println("\n3. Danh sach SV sap xep theo Khoa:");
        list3.forEach(s -> System.out.println("   -> " + s.getName() + " | Khoa: " + s.getDepartment().getName()));

        // 4. Gọi query Native SQL
        List<Student> list4 = studentRepository.findStudentsByDeptNameNative("Information Technology");
        System.out.println("\n4. Sinh vien khoa Information Technology (Native SQL):");
        list4.forEach(s -> System.out.println("   -> " + s.getName()));

        System.out.println("==================================================\n");
    }
}
