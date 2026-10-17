package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.entity.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete DAO extending HibernateDao for Document entity.
 * Inherits all query parsing, pagination bounds validation, and sorting capabilities.
 */
@Component
public class DocumentHibernateDao extends HibernateDao<Document> {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "appId", "owner", "version", "creationTime", "modificationTime", "active", "deleted"
    );

    public DocumentHibernateDao() {
        super(null, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "creationTime", Sort.Direction.DESC);
    }

    public DocumentHibernateDao(JpaSpecificationExecutor<Document> repository) {
        super(repository, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "creationTime", Sort.Direction.DESC);
    }
}
