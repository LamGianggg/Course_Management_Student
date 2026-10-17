package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.Dao.IClassInfoDao;
import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.service.ClassInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassInfoServiceImpl implements ClassInfoService {

    private final IClassInfoDao classInfoDao;

    @Autowired
    public ClassInfoServiceImpl(IClassInfoDao classInfoDao) {
        this.classInfoDao = classInfoDao;
    }

    @Override
    public ClassInfo create(ClassInfoWrapper wrapper) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(wrapper.getName());
        classInfo.setDomain(wrapper.getDomain());
        classInfo.setDescription(wrapper.getDescription());
        return classInfoDao.save(classInfo);
    }

    @Override
    public ClassInfo update(ClassInfoWrapper wrapper) {
        if (wrapper.getId() == null) {
            throw new IllegalArgumentException("ClassInfo id is required for update");
        }
        ClassInfo classInfo = classInfoDao.getById(wrapper.getId());
        if (classInfo == null) {
            throw new ResourceNotFoundException("ClassInfo not found with id: " + wrapper.getId());
        }
        classInfo.setName(wrapper.getName());
        classInfo.setDomain(wrapper.getDomain());
        classInfo.setDescription(wrapper.getDescription());
        return classInfoDao.update(classInfo);
    }

    @Override
    public void delete(String id) {
        ClassInfo classInfo = classInfoDao.getById(id);
        if (classInfo == null) {
            throw new ResourceNotFoundException("ClassInfo not found with id: " + id);
        }
        classInfoDao.delete(id);
    }

    @Override
    public ClassInfo getById(String id) {
        ClassInfo classInfo = classInfoDao.getById(id);
        if (classInfo == null) {
            throw new ResourceNotFoundException("ClassInfo not found with id: " + id);
        }
        return classInfo;
    }

    @Override
    public ClassInfo getByName(String name) {
        ClassInfo classInfo = classInfoDao.getByName(name);
        if (classInfo == null) {
            throw new ResourceNotFoundException("ClassInfo not found with name: " + name);
        }
        return classInfo;
    }

    @Override
    public List<ClassInfo> getAll() {
        return classInfoDao.getAll();
    }

    @Override
    public List<ClassInfo> getByDomain(String domain) {
        return classInfoDao.getByDomain(domain);
    }

    @Override
    public List<ClassInfo> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType) {
        return classInfoDao.search(query, lowerLimit, upperLimit, orderBy, orderType);
    }

    @Override
    public Integer getCount(String query) {
        return classInfoDao.getCount(query);
    }

    @Override
    public Page<ClassInfo> searchClassInfo(Specification<ClassInfo> spec, Pageable pageable) {
        return classInfoDao.filter(spec, pageable);
    }

    @Override
    public Page<ClassInfo> filterClassInfo(String search, int page, int size, List<String> sort) {
        return classInfoDao.filter(search, page, size, sort);
    }
}
