package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.entity.Department;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete DAO extending HibernateDao for Department entity.
 * Inherits all query parsing, pagination bounds validation, and sorting capabilities.
 */
@Component
public class DepartmentHibernateDao extends HibernateDao<Department> {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "description"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name"
    );

    public DepartmentHibernateDao() {
        super(null, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC);
    }

    public DepartmentHibernateDao(JpaSpecificationExecutor<Department> repository) {
        super(repository, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC);
    }
}
