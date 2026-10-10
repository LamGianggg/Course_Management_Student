package com.example.studentmanagement.specification;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.entity.Student;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification implementation for Student entity.
 * Delegates to reusable HibernateDao.
 */
public class StudentSpecification implements Specification<Student> {

    private final SearchCriteria criteria;

    public StudentSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return HibernateDao.<Student>createSpecification(criteria).toPredicate(root, query, builder);
    }
}

