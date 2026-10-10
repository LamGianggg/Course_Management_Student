package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;

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
}
