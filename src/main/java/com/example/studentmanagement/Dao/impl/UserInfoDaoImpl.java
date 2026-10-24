package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IUserInfoDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.UserInfo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class UserInfoDaoImpl extends HibernateGenericDao<Long, UserInfo> implements IUserInfoDao {

    public UserInfoDaoImpl(EntityManager entityManager) {
        super(UserInfo.class, entityManager);
    }

    @Override
    public UserInfo findByPk(Long id) {
        if (id == null) return null;
        return findById(id).orElse(null);
    }

    @Override
    public UserInfo update(UserInfo userInfo) {
        return super.save(userInfo);
    }

    @Override
    public UserInfo save(UserInfo userInfo) {
        if (userInfo.getId() == null) {
            return create(userInfo);
        }
        return super.save(userInfo);
    }
}
