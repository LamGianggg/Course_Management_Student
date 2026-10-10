package com.example.studentmanagement;

import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }

    @Bean
    public CommandLineRunner runTestQueries(StudentRepository studentRepository) {
        return args -> {

            // 1. Gọi query lọc theo ngày sinh (findByDobBetween)
            List<Student> list1 = studentRepository.findByDobBetween(
                    LocalDate.of(2003, 1, 1),
                    LocalDate.of(2003, 12, 31)
            );
            System.out.println("\n[Query 1] Sinh viên sinh trong năm 2003 (tổng: " + list1.size() + " bạn):");
            list1.forEach(s -> System.out.println("   -> ID: " + s.getId() + " | Tên: " + s.getName() + " | Ngày sinh: " + s.getDob()));

            // 2. Gọi query tìm sinh viên sinh sau một ngày (findStudentsBornAfter)
            List<Student> list2 = studentRepository.findStudentsBornAfter(LocalDate.of(2003, 1, 1));
            System.out.println("\n[Query 2] Sinh viên sinh sau ngày 2003-01-01 (tổng: " + list2.size() + " bạn):");
            list2.forEach(s -> System.out.println("   -> ID: " + s.getId() + " | Tên: " + s.getName() + " | Ngày sinh: " + s.getDob()));

            // 3. Gọi query lấy kèm thông tin khoa có sắp xếp (findAllWithDepartmentOrdered)
            List<Student> list3 = studentRepository.findAllWithDepartmentOrdered();
            System.out.println("\n[Query 3] Danh sách SV sắp xếp theo Khoa:");
            list3.forEach(s -> System.out.println("   -> Khoa: [" + s.getDepartment().getName() + "] - SV: " + s.getName()));

            // 4. Gọi query Native SQL (findStudentsByDeptNameNative)
            List<Student> list4 = studentRepository.findStudentsByDeptNameNative("Information Technology");
            System.out.println("\n[Query 4] Sinh viên thuộc khoa 'Information Technology' (Native SQL):");
            list4.forEach(s -> System.out.println("   -> Tên: " + s.getName()));
        };
    }
}
