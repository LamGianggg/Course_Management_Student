package com.example.studentmanagement.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class EntityLifecycleTest {

    @Test
    void testDocumentLifecycleHooks() {
        // Document: onCreate when fields are populated
        Document doc1 = new Document("custom-id", "app-1", "name", "path", "owner", 2.0, true, new Date(), new Date(), false);
        doc1.onCreate();
        assertEquals("custom-id", doc1.getId());
        assertEquals(2.0, doc1.getVersion());
        assertTrue(doc1.getDeleted());
        assertFalse(doc1.getActive());
        doc1.onPersist();
        assertNotNull(doc1.getModificationTime());

        // Document: onCreate when id is blank
        Document doc2 = new Document();
        doc2.setId("   ");
        doc2.onCreate();
        assertNotNull(doc2.getId());
        assertFalse(doc2.getId().trim().isEmpty());

        // Document: onCreate when id is null
        Document doc3 = new Document();
        doc3.setId(null);
        doc3.onCreate();
        assertNotNull(doc3.getId());
    }

    @Test
    void testClassInfoLifecycleHooks() {
        // ClassInfo: onCreate when createdTime is already set
        ClassInfo ci = new ClassInfo();
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);
        ci.setCreatedTime(time);
        ci.onCreate();
        assertEquals(time, ci.getCreatedTime());

        // ClassInfo: onCreate when createdTime is null
        ClassInfo ci2 = new ClassInfo();
        ci2.onCreate();
        assertNotNull(ci2.getCreatedTime());
    }

    @Test
    void testAppDetailLifecycleHooks() {
        // AppDetail: onCreate when createdTime is already set
        AppDetail ad = new AppDetail();
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);
        ad.setCreatedTime(time);
        ad.onCreate();
        assertEquals(time, ad.getCreatedTime());

        // AppDetail: onCreate when createdTime is null
        AppDetail ad2 = new AppDetail();
        ad2.onCreate();
        assertNotNull(ad2.getCreatedTime());
    }
}
