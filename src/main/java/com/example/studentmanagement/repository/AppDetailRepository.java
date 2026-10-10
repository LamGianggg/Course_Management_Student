package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.AppDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDetailRepository extends JpaRepository<AppDetail, String> {
}
