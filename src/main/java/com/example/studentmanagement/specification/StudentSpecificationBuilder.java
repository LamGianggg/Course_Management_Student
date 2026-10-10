package com.example.studentmanagement.specification;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.entity.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

/**
 * Parses a search query string into a combined JPA Specification for Student.
 * Delegates to reusable HibernateDao.
 */
public class StudentSpecificationBuilder {

    private StudentSpecificationBuilder() {
        // utility class
    }

    /**
     * Parse a search query string and build a combined Specification.
     *
     * @param search the search query string (e.g. "name:john,dob>2003-01-01")
     * @return combined Specification, or null if search is empty
     * @throws IllegalArgumentException if search syntax is invalid or field is not allowed
     */
    public static Specification<Student> parse(String search) {
        return HibernateDao.parse(search, getAllowedSearchFields());
    }

    /**
     * Returns the set of allowed search fields (for documentation/error messages).
     */
    public static Set<String> getAllowedSearchFields() {
        return HibernateDao.getAllowedSearchFields();
    }
}

