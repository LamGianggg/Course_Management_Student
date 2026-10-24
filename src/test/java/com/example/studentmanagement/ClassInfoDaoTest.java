package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IClassInfoDao;
import com.example.studentmanagement.entity.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ClassInfoDaoTest {

    @Autowired
    private IClassInfoDao classInfoDao;

    @Test
    void testCrudOperations() {
        // 1. Create / Save
        ClassInfo classInfo = new ClassInfo(null, "Biology 101", "Science", "Introductory Biology", LocalDateTime.now());
        ClassInfo saved = classInfoDao.save(classInfo);
        assertNotNull(saved.getId());
        assertEquals("Biology 101", saved.getName());

        // 2. getById
        ClassInfo foundById = classInfoDao.getById(saved.getId());
        assertNotNull(foundById);
        assertEquals("Science", foundById.getDomain());
        assertNull(classInfoDao.getById(null));

        // 3. getByName
        ClassInfo foundByName = classInfoDao.getByName("Biology 101");
        assertNotNull(foundByName);
        assertEquals(saved.getId(), foundByName.getId());
        assertNull(classInfoDao.getByName("Non Existent Class Name 9999"));

        // 4. Update
        saved.setDescription("Updated Biology Description");
        ClassInfo updated = classInfoDao.update(saved);
        assertEquals("Updated Biology Description", updated.getDescription());

        // 5. getAll & getByDomain
        List<ClassInfo> all = classInfoDao.getAll();
        assertFalse(all.isEmpty());
        List<ClassInfo> domainList = classInfoDao.getByDomain("Science");
        assertFalse(domainList.isEmpty());
        List<ClassInfo> emptyDomainList = classInfoDao.getByDomain("UnknownNonExistentDomain");
        assertTrue(emptyDomainList.isEmpty());

        // 6. Search
        List<ClassInfo> searchResults = classInfoDao.search("Biology", 0, 10, "name", "ASC");
        assertFalse(searchResults.isEmpty());

        List<ClassInfo> searchResultsDesc = classInfoDao.search("Biology", 0, 5, "domain", "DESC");
        assertFalse(searchResultsDesc.isEmpty());

        List<ClassInfo> searchAllNoQuery = classInfoDao.search(null, 0, 10, "createdTime", "DESC");
        assertFalse(searchAllNoQuery.isEmpty());

        // fallback safeOrderBy & safeOrderType, null limits
        List<ClassInfo> searchFallback = classInfoDao.search("Biology", null, null, "invalidOrderBy", "invalidOrderType");
        assertFalse(searchFallback.isEmpty());

        // upperLimit <= lowerLimit branch
        List<ClassInfo> searchUpperLess = classInfoDao.search("Biology", 5, 2, "id", "ASC");
        assertNotNull(searchUpperLess);

        // negative limits and blank query
        List<ClassInfo> searchNegative = classInfoDao.search("   ", -1, -1, "id", "ASC");
        assertNotNull(searchNegative);

        // 7. getCount
        Integer countWithQuery = classInfoDao.getCount("Biology");
        assertTrue(countWithQuery > 0);
        Integer totalCount = classInfoDao.getCount(null);
        assertTrue(totalCount > 0);

        // 8. Pagination & Specification
        Page<ClassInfo> page = classInfoDao.findAll(PageRequest.of(0, 10));
        assertNotNull(page);

        Specification<ClassInfo> spec = (root, query, cb) -> cb.equal(root.get("name"), "Biology 101");
        Page<ClassInfo> filtered = classInfoDao.filter(spec, PageRequest.of(0, 10));
        assertEquals(1, filtered.getTotalElements());

        Page<ClassInfo> nullFiltered = classInfoDao.filter((Specification<ClassInfo>) null, PageRequest.of(0, 10));
        assertNotNull(nullFiltered);

        // 9. Delete
        classInfoDao.delete(saved.getId());
        assertNull(classInfoDao.getById(saved.getId()));
    }
}
