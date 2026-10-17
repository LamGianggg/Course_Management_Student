package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.entity.ClassInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete DAO extending HibernateDao for ClassInfo entity.
 * Inherits all query parsing, pagination bounds validation, and sorting capabilities.
 */
@Component
public class ClassInfoHibernateDao extends HibernateDao<ClassInfo> {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "domain", "description", "createdTime"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "domain", "createdTime"
    );

    public ClassInfoHibernateDao() {
        super(null, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "createdTime", Sort.Direction.DESC);
    }

    public ClassInfoHibernateDao(JpaSpecificationExecutor<ClassInfo> repository) {
        super(repository, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "createdTime", Sort.Direction.DESC);
    }
}
