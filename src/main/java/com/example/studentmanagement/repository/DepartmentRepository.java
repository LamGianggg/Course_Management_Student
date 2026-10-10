package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    Optional<Department> findByName(String name);

    // Liên kết với @NamedQuery "Department.findAllOrderByNameAsc" trong Department Entity
    List<Department> findAllOrderByNameAsc();

    // Liên kết với @NamedQuery "Department.findByNameExact" trong Department Entity
    Optional<Department> findByNameExact(@Param("name") String name);
}
