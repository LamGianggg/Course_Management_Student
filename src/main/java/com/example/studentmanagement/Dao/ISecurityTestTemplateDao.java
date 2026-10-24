package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ISecurityTestTemplateDao
        extends IGenericDao<Long, SecurityTestTemplate>, JpaSpecificationExecutor<SecurityTestTemplate> {

    List<SecurityTestTemplate> getSecurityTestList(String query, Integer lowerLimit, Integer upperLimit,
                                                  String orderBy, String orderType, String appId, String userAppId);

    SecurityTestTemplate save(SecurityTestTemplate template);

    SecurityTestTemplate findByPk(Long id);
}
