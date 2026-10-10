package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.AppDetail;

public interface IAppDetailDao extends IGenericDao<String, AppDetail> {

    AppDetail findByPk(String id);

    AppDetail save(AppDetail appDetail);

    boolean existsById(String id);
}
