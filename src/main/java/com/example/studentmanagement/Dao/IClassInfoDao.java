package com.example.studentmanagement.Dao;

import com.example.studentmanagement.entity.ClassInfo;

import java.util.List;

public interface IClassInfoDao {

    ClassInfo save(ClassInfo classInfo);

    ClassInfo update(ClassInfo classInfo);

    void delete(String id);

    ClassInfo getById(String id);

    ClassInfo getByName(String name);

    List<ClassInfo> getAll();

    List<ClassInfo> getByDomain(String domain);

    List<ClassInfo> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType);

    Integer getCount(String query);
}
