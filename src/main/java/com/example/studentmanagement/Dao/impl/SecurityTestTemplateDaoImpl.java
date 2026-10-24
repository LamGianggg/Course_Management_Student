package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.ISecurityTestTemplateDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class SecurityTestTemplateDaoImpl
        extends HibernateGenericDao<Long, SecurityTestTemplate>
        implements ISecurityTestTemplateDao {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTestTemplateDaoImpl.class);
    private static final String USERAPP_ID = "userAppId";
    private static final String APP_ID = "appId";

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "description", "appId", "userAppId", "testType", "status"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "createdTime", "appId", "userAppId", "testType", "status"
    );

    public SecurityTestTemplateDaoImpl(EntityManager entityManager) {
        super(SecurityTestTemplate.class, entityManager);
        this.hibernateDao = new HibernateDao<>(this, ALLOWED_SEARCH_FIELDS, ALLOWED_SORT_FIELDS);
    }

    @Override
    public List<SecurityTestTemplate> getSecurityTestList(String query, Integer lowerLimit, Integer upperLimit,
                                                         String orderBy, String orderType, String appId, String userAppId) {
        logger.info("Query for FIQL:{}", query);
        enableFilter(appId, userAppId);
        return search(query, upperLimit, lowerLimit, orderBy, orderType);
    }

    private void enableFilter(String appId, String userAppId) {
        Session session = (Session) getEntityManager().getDelegate();
        if (userAppId != null) {
            session.enableFilter("filterTestByUserAppId").setParameter(USERAPP_ID, userAppId);
        } else {
            session.enableFilter("filterTestByApplicationId").setParameter(APP_ID, appId);
        }
    }

    public List<SecurityTestTemplate> search(String query, Integer upperLimit, Integer lowerLimit,
                                            String orderBy, String orderType) {
        int page = (lowerLimit != null && upperLimit != null && upperLimit > 0)
                ? lowerLimit / upperLimit
                : 0;
        int size = (upperLimit != null && upperLimit > 0) ? upperLimit : 10;
        List<String> sortParams = (orderBy != null && !orderBy.isBlank())
                ? List.of(orderBy + "," + (orderType != null ? orderType : "asc"))
                : Collections.emptyList();

        Page<SecurityTestTemplate> paged = filter(query, page, size, sortParams);
        return paged.getContent();
    }

    @Override
    public SecurityTestTemplate save(SecurityTestTemplate template) {
        if (template.getId() == null) {
            return create(template);
        }
        return super.save(template);
    }

    @Override
    public SecurityTestTemplate findByPk(Long id) {
        if (id == null) return null;
        return findById(id).orElse(null);
    }
}
