package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.entity.AppDetail;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete DAO extending HibernateDao for AppDetail entity.
 * Inherits all query parsing, pagination bounds validation, and sorting capabilities.
 */
@Component
public class AppDetailHibernateDao extends HibernateDao<AppDetail> {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "description", "createdTime"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "createdTime"
    );

    public AppDetailHibernateDao() {
        super(null, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "createdTime", Sort.Direction.DESC);
    }

    public AppDetailHibernateDao(JpaSpecificationExecutor<AppDetail> repository) {
        super(repository, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "createdTime", Sort.Direction.DESC);
    }
}
