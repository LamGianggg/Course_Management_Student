package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.Dao.IUserInfoDao;
import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.entity.UserInfo;
import com.example.studentmanagement.exception.ValidationException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.UserRepository;
import com.example.studentmanagement.service.IUserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl implements IUserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    public static final String REJECTED = "REJECTED";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String NO_USER_WITH_SUCH_ID = "No user found with the given ID";
    public static final String ID_FIELD_CANNOT_BE_EMPTY = "ID field cannot be empty";

    private final IUserInfoDao userInfoDao;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoServiceImpl(IUserInfoDao userInfoDao,
                               UserRepository userRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.userInfoDao = userInfoDao;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Map<String, String> update(UserInfoWrapper userInfoWrapper) {
        logger.info("userInfoWrapper is :{}", userInfoWrapper);
        if (userInfoWrapper.getId() != null) {
            UserInfo userInfo = userInfoDao.findByPk(userInfoWrapper.getId());
            if (userInfo != null) {

                if (REJECTED.equals(userInfoWrapper.getStatus())) {
                    userInfo.setStatus(REJECTED);
                    userInfoDao.update(userInfo);
                    return Map.of(STATUS, SUCCESS);
                } else {
                    List<String> orgList = getOrganizationList(userInfoWrapper);
                    if (CollectionUtils.isEmpty(orgList) || !orgList.contains(userInfo.getOrganisation())) {
                        createOrganizationUM(userInfoWrapper);
                    }
                    return createUserUM(userInfoWrapper, userInfo);
                }
            } else
                throw new ValidationException(NO_USER_WITH_SUCH_ID);
        } else {
            throw new ValidationException(ID_FIELD_CANNOT_BE_EMPTY);
        }
    }

    @Override
    public List<String> getOrganizationList(UserInfoWrapper userInfoWrapper) {
        return departmentRepository.findAll().stream()
                .map(Department::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void createOrganizationUM(UserInfoWrapper userInfoWrapper) {
        if (userInfoWrapper.getOrganisation() != null && !userInfoWrapper.getOrganisation().isBlank()) {
            departmentRepository.save(new Department(null, userInfoWrapper.getOrganisation(), "Organisation created via UM flow"));
        }
    }

    @Override
    public Map<String, String> createUserUM(UserInfoWrapper userInfoWrapper, UserInfo userInfo) {
        userInfo.setStatus(userInfoWrapper.getStatus() != null ? userInfoWrapper.getStatus() : "APPROVED");
        userInfoDao.update(userInfo);

        if (!userRepository.existsByUsername(userInfo.getUsername())) {
            User user = new User(userInfo.getUsername(), userInfo.getEmail(), passwordEncoder.encode("Password123@"), userInfo.getUsername());
            user.setRoles(Set.of(RoleType.ROLE_STUDENT));
            userRepository.save(user);
        }

        return Map.of(STATUS, SUCCESS, "message", "User and Organization synchronized successfully");
    }

    @Override
    public UserInfo create(UserInfo userInfo) {
        return userInfoDao.save(userInfo);
    }

    @Override
    public UserInfo getById(Long id) {
        return userInfoDao.findByPk(id);
    }
}
