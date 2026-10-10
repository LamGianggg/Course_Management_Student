package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IAppDetailDao;
import com.example.studentmanagement.Dao.IDocumentDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.entity.AppDetail;
import com.example.studentmanagement.entity.Document;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Document Search (Specification), Pagination, and Sorting
 * using the unified HibernateDao.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocumentFilterPaginationSortingTest {

    @Autowired
    private IDocumentDao documentDao;

    @Autowired
    private IAppDetailDao appDetailDao;

    @Autowired
    private MockMvc mockMvc;

    private static final String APP_ID_1 = "APP_DOC_01";
    private static final String APP_ID_2 = "APP_DOC_02";

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private Document doc4;
    private Document doc5;

    @BeforeEach
    void setUp() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!appDetailDao.existsById(APP_ID_1)) {
            appDetailDao.save(new AppDetail(APP_ID_1, "Application 1", "For Document Testing"));
        }
        if (!appDetailDao.existsById(APP_ID_2)) {
            appDetailDao.save(new AppDetail(APP_ID_2, "Application 2", "For Document Testing"));
        }

        if (documentDao.findByPk("doc-test-1") != null) documentDao.delete("doc-test-1");
        if (documentDao.findByPk("doc-test-2") != null) documentDao.delete("doc-test-2");
        if (documentDao.findByPk("doc-test-3") != null) documentDao.delete("doc-test-3");
        if (documentDao.findByPk("doc-test-4") != null) documentDao.delete("doc-test-4");
        if (documentDao.findByPk("doc-test-5") != null) documentDao.delete("doc-test-5");

        doc1 = new Document("doc-test-1", APP_ID_1, "Annual_Financial_Report.pdf",
                APP_ID_1 + "/doc-test-1/Annual_Financial_Report.pdf", "alice", 1.0,
                false, sdf.parse("2024-01-10 09:00:00"), sdf.parse("2024-01-10 09:00:00"), true);

        doc2 = new Document("doc-test-2", APP_ID_1, "Quarterly_Summary.docx",
                APP_ID_1 + "/doc-test-2/Quarterly_Summary.docx", "bob", 1.5,
                false, sdf.parse("2024-03-15 10:30:00"), sdf.parse("2024-03-15 10:30:00"), true);

        doc3 = new Document("doc-test-3", APP_ID_1, "System_Architecture_Plan.pdf",
                APP_ID_1 + "/doc-test-3/System_Architecture_Plan.pdf", "charlie", 2.0,
                false, sdf.parse("2024-06-20 14:00:00"), sdf.parse("2024-06-20 14:00:00"), true);

        doc4 = new Document("doc-test-4", APP_ID_2, "Student_Thesis_Draft.docx",
                APP_ID_2 + "/doc-test-4/Student_Thesis_Draft.docx", "alice", 2.5,
                false, sdf.parse("2024-09-05 16:45:00"), sdf.parse("2024-09-05 16:45:00"), true);

        doc5 = new Document("doc-test-5", APP_ID_2, "Deprecated_Guidelines.pdf",
                APP_ID_2 + "/doc-test-5/Deprecated_Guidelines.pdf", "david", 3.0,
                true, sdf.parse("2024-11-11 11:11:00"), sdf.parse("2024-11-11 11:11:00"), false);

        documentDao.save(doc1);
        documentDao.save(doc2);
        documentDao.save(doc3);
        documentDao.save(doc4);
        documentDao.save(doc5);
    }

    // =========================================================================
    // 1. SPECIFICATION / SEARCH TESTS USING HIBERNATEDAO
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("Search: Tìm tài liệu theo tên chứa từ khóa (: operator LIKE)")
    void testSearchByNameContains() {
        Specification<Document> spec = HibernateDao.parse("name:Report",
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Annual_Financial_Report.pdf", result.get(0).getName());
    }

    @Test
    @Order(2)
    @DisplayName("Search: Tìm tài liệu theo appId")
    void testSearchByAppId() {
        Specification<Document> spec = HibernateDao.parse("appId:" + APP_ID_2,
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> APP_ID_2.equals(d.getAppId())));
    }

    @Test
    @Order(3)
    @DisplayName("Search: Tìm tài liệu có version > 1.8")
    void testSearchByVersionGreaterThan() {
        Specification<Document> spec = HibernateDao.parse("version>1.8",
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(3, result.size()); // doc3 (2.0), doc4 (2.5), doc5 (3.0)
    }

    @Test
    @Order(4)
    @DisplayName("Search: Tìm tài liệu theo deleted:false và active:true")
    void testSearchByDeletedAndActive() {
        Specification<Document> spec = HibernateDao.parse("deleted:false,active:true",
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(4, result.size());
        assertTrue(result.stream().noneMatch(Document::getDeleted));
    }

    @Test
    @Order(5)
    @DisplayName("Search: Tìm tài liệu theo creationTime > 2024-05-01")
    void testSearchByCreationTimeGreaterThan() {
        Specification<Document> spec = HibernateDao.parse("creationTime>2024-05-01",
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(3, result.size()); // doc3 (June), doc4 (Sept), doc5 (Nov)
    }

    @Test
    @Order(6)
    @DisplayName("Search: Tìm tài liệu kết hợp nhiều điều kiện AND")
    void testSearchCombinedConditions() {
        Specification<Document> spec = HibernateDao.parse("appId:" + APP_ID_1 + ",owner:alice,version<2.0",
                Set.of("id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"));
        assertNotNull(spec);
        List<Document> result = documentDao.findAll(spec);
        assertEquals(1, result.size());
        assertEquals("Annual_Financial_Report.pdf", result.get(0).getName());
    }

    // =========================================================================
    // 2. PAGINATION TESTS USING HIBERNATEDAO
    // =========================================================================

    @Test
    @Order(7)
    @DisplayName("Pagination: Phân trang hợp lệ")
    void testPaginationValid() {
        Pageable pageable = HibernateDao.createPageable(0, 2);
        Page<Document> page = documentDao.findAll(pageable);
        assertEquals(2, page.getContent().size());
        assertEquals(0, page.getNumber());
        assertEquals(2, page.getSize());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @Order(8)
    @DisplayName("Pagination: Lỗi khi size ngoài khoảng 1..100")
    void testPaginationInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(0, 0));
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(0, 101));
    }

    @Test
    @Order(9)
    @DisplayName("Pagination: Lỗi khi page < 0")
    void testPaginationNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(-1, 10));
    }

    // =========================================================================
    // 3. SORTING TESTS USING HIBERNATEDAO
    // =========================================================================

    @Test
    @Order(10)
    @DisplayName("Sorting: Sắp xếp theo tên tăng dần ASC")
    void testSortByNameAsc() {
        Sort sort = HibernateDao.createSort(List.of("name,asc"),
                Set.of("id", "name", "appId", "owner", "version", "creationTime", "active", "deleted"),
                "creationTime", Sort.Direction.DESC);
        Pageable pageable = HibernateDao.createPageable(0, 10, sort);
        Page<Document> page = documentDao.findAll(pageable);

        List<Document> content = page.getContent();
        assertEquals("Annual_Financial_Report.pdf", content.get(0).getName());
        assertEquals("Deprecated_Guidelines.pdf", content.get(1).getName());
    }

    @Test
    @Order(11)
    @DisplayName("Sorting: Sắp xếp theo version giảm dần DESC")
    void testSortByVersionDesc() {
        Sort sort = HibernateDao.createSort(List.of("version,desc"),
                Set.of("id", "name", "appId", "owner", "version", "creationTime", "active", "deleted"),
                "creationTime", Sort.Direction.DESC);
        Pageable pageable = HibernateDao.createPageable(0, 10, sort);
        Page<Document> page = documentDao.findAll(pageable);

        List<Document> content = page.getContent();
        assertEquals(3.0, content.get(0).getVersion());
        assertEquals(2.5, content.get(1).getVersion());
    }

    @Test
    @Order(12)
    @DisplayName("Sorting: Lỗi khi sắp xếp trường không thuộc Whitelist")
    void testSortInvalidField() {
        assertThrows(IllegalArgumentException.class, () ->
                HibernateDao.createSort(List.of("password,asc"),
                        Set.of("id", "name", "appId", "owner", "version", "creationTime")));
    }

    // =========================================================================
    // 4. INSTANCE & COMBINED EXECUTION TESTS
    // =========================================================================

    @Test
    @Order(13)
    @DisplayName("HibernateDao: Sử dụng instance getHibernateDao().find(...)")
    void testInstanceHibernateDao() {
        HibernateDao<Document> helper = documentDao.getHibernateDao();
        assertNotNull(helper);

        Page<Document> result = helper.find("owner:alice", 0, 10, List.of("name,asc"));
        assertEquals(2, result.getTotalElements()); // doc1 and doc4
        assertEquals("Annual_Financial_Report.pdf", result.getContent().get(0).getName());
        assertEquals("Student_Thesis_Draft.docx", result.getContent().get(1).getName());
    }

    @Test
    @Order(14)
    @DisplayName("HibernateDao: Sử dụng documentDao.filter(search, page, size, sort)")
    void testDocumentDaoFilterDirect() {
        Page<Document> page = documentDao.filter("appId:" + APP_ID_1, 0, 5, List.of("version,desc"));
        assertEquals(3, page.getTotalElements());
        assertEquals(2.0, page.getContent().get(0).getVersion());
    }

    // =========================================================================
    // 5. REST API INTEGRATION TESTS (MOCKMVC)
    // =========================================================================

    @Test
    @Order(15)
    @DisplayName("REST API: GET /api/documents/filter thành công với phân trang, lọc và sắp xếp")
    void testRestFilterSuccess() throws Exception {
        mockMvc.perform(get("/api/documents/filter")
                        .param("search", "appId:" + APP_ID_1 + ",version>1.0")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "version,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].version").value(2.0))
                .andExpect(jsonPath("$.data.content[1].version").value(1.5));
    }

    @Test
    @Order(16)
    @DisplayName("REST API: GET /api/documents/filter không truyền search -> trả về tất cả kèm phân trang")
    void testRestFilterWithoutSearch() throws Exception {
        mockMvc.perform(get("/api/documents/filter")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "creationTime,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.size").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    @Order(17)
    @DisplayName("REST API: GET /api/documents/filter lỗi khi trường sắp xếp không hợp lệ (400)")
    void testRestFilterInvalidSort() throws Exception {
        mockMvc.perform(get("/api/documents/filter")
                        .param("sort", "secretKey,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(18)
    @DisplayName("REST API: GET /api/documents/filter lỗi khi trường tìm kiếm không hợp lệ (400)")
    void testRestFilterInvalidSearchField() throws Exception {
        mockMvc.perform(get("/api/documents/filter")
                        .param("search", "forbiddenField:test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(19)
    @DisplayName("REST API: GET /api/documents/filter lỗi khi page size vượt quá 100 (400)")
    void testRestFilterPageSizeExceeded() throws Exception {
        mockMvc.perform(get("/api/documents/filter")
                        .param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(20)
    @DisplayName("REST API: GET /api/documents/search alias hoạt động chuẩn xác")
    void testRestSearchAlias() throws Exception {
        mockMvc.perform(get("/api/documents/search")
                        .param("search", "name:Thesis")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Student_Thesis_Draft.docx"));
    }
}
