package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ClassInfoService {

    ClassInfo create(ClassInfoWrapper wrapper);

    ClassInfo update(ClassInfoWrapper wrapper);

    void delete(String id);

    ClassInfo getById(String id);

    ClassInfo getByName(String name);

    List<ClassInfo> getAll();

    List<ClassInfo> getByDomain(String domain);

    List<ClassInfo> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType);

    Integer getCount(String query);

    Page<ClassInfo> searchClassInfo(Specification<ClassInfo> spec, Pageable pageable);

    Page<ClassInfo> filterClassInfo(String search, int page, int size, List<String> sort);
}
