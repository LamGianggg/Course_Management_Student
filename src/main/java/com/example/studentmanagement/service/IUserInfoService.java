package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.UserInfo;

import java.util.List;
import java.util.Map;

public interface IUserInfoService {

    Map<String, String> update(UserInfoWrapper userInfoWrapper);

    List<String> getOrganizationList(UserInfoWrapper userInfoWrapper);

    void createOrganizationUM(UserInfoWrapper userInfoWrapper);

    Map<String, String> createUserUM(UserInfoWrapper userInfoWrapper, UserInfo userInfo);

    UserInfo create(UserInfo userInfo);

    UserInfo getById(Long id);
}
