package com.example.studentmanagement.rest.Impl;

import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import com.example.studentmanagement.rest.ClassInfoRest;
import com.example.studentmanagement.service.ClassInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/class-info")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClassInfoRestImpl implements ClassInfoRest {

    private static final Logger logger = LoggerFactory.getLogger(ClassInfoRestImpl.class);

    @Autowired
    private ClassInfoService classInfoService;

    @Override
    public ResponseEntity<ClassInfo> createClassInfo(ClassInfoWrapper wrapper) {
        try {
            logger.info("Creating new Class Info: {}", wrapper.getName());
            ClassInfo classInfo = classInfoService.create(wrapper);
            return ResponseEntity.ok(classInfo);
        } catch (Exception e) {
            logger.error("Error creating Class Info: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<ClassInfo> updateClassInfo(ClassInfoWrapper wrapper) {
        try {
            logger.info("Updating Class Info: {}", wrapper.getId());
            ClassInfo classInfo = classInfoService.update(wrapper);
            return ResponseEntity.ok(classInfo);
        } catch (Exception e) {
            logger.error("Error updating Class Info: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<String> deleteClassInfo(String id) {
        try {
            logger.info("Deleting Class Info: {}", id);
            classInfoService.delete(id);
            return ResponseEntity.ok("Class Info deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting Class Info: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<ClassInfo> getClassInfoById(String id) {
        try {
            logger.info("Getting Class Info by id: {}", id);
            ClassInfo classInfo = classInfoService.getById(id);
            if (classInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(classInfo);
        } catch (Exception e) {
            logger.error("Error getting Class Info by id: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<ClassInfo> getClassInfoByName(String name) {
        try {
            logger.info("Getting Class Info by name: {}", name);
            ClassInfo classInfo = classInfoService.getByName(name);
            if (classInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(classInfo);
        } catch (Exception e) {
            logger.error("Error getting Class Info by name: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<ClassInfo>> getAllClassInfo() {
        try {
            logger.info("Getting all Class Info");
            List<ClassInfo> classInfoList = classInfoService.getAll();
            return ResponseEntity.ok(classInfoList);
        } catch (Exception e) {
            logger.error("Error getting all Class Info: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<ClassInfo>> getClassInfoByDomain(String domain) {
        try {
            logger.info("Getting Class Info by domain: {}", domain);
            List<ClassInfo> classInfoList = classInfoService.getByDomain(domain);
            return ResponseEntity.ok(classInfoList);
        } catch (Exception e) {
            logger.error("Error getting Class Info by domain: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<ClassInfo>> searchClassInfo(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType) {
        try {
            logger.info("Searching Class Info with query: {}", query);
            List<ClassInfo> classInfoList = classInfoService.search(query, lowerLimit, upperLimit, orderBy, orderType);
            return ResponseEntity.ok(classInfoList);
        } catch (Exception e) {
            logger.error("Error searching Class Info: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Integer> getClassInfoCount(String query) {
        try {
            logger.info("Getting Class Info count");
            Integer count = classInfoService.getCount(query);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Error getting Class Info count: ", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<com.example.studentmanagement.dto.ApiResponse<com.example.studentmanagement.dto.PagedResponse<ClassInfo>>> filterClassInfo(
            String search, int page, int size, List<String> sort) {
        try {
            logger.info("Filtering Class Info: search={}, page={}, size={}, sort={}", search, page, size, sort);
            Page<ClassInfo> resultPage = classInfoService.filterClassInfo(search, page, size, sort);
            com.example.studentmanagement.dto.PagedResponse<ClassInfo> pagedResponse =
                    new com.example.studentmanagement.dto.PagedResponse<>(resultPage);
            return ResponseEntity.ok(com.example.studentmanagement.dto.ApiResponse.ok("Class Info filtered successfully", pagedResponse));
        } catch (Exception e) {
            logger.error("Error filtering Class Info: ", e);
            throw e;
        }
    }
}
