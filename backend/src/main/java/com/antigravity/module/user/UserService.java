package com.antigravity.module.user;

import com.antigravity.common.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 用户 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface UserService {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    PageResult<User> pageQuery(int pageNumber, int pageSize, String username, String role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User createUser(User user);

    boolean updateUser(User user);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean updateRole(Long userId, String role);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

}
