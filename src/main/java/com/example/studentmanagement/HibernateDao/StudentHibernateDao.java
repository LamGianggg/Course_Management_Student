package com.example.studentmanagement.HibernateDao;

import com.example.studentmanagement.entity.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Concrete DAO extending HibernateDao for Student entity.
 * Inherits all query parsing, pagination bounds validation, and sorting capabilities.
 */
@Component
public class StudentHibernateDao extends HibernateDao<Student> {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "dob", "department.name"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "dob"
    );

    public StudentHibernateDao() {
        super(null, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC);
    }

    public StudentHibernateDao(JpaSpecificationExecutor<Student> repository) {
        super(repository, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC);
    }
}
