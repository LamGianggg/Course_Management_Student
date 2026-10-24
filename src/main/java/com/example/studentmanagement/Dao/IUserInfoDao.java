package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IUserInfoDao extends IGenericDao<Long, UserInfo>, JpaSpecificationExecutor<UserInfo> {

    UserInfo findByPk(Long id);

    UserInfo update(UserInfo userInfo);

    UserInfo save(UserInfo userInfo);
}
