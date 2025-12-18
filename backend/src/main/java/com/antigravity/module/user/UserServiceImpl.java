package com.antigravity.module.user;

import com.antigravity.common.PageResult;
import com.antigravity.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 用户 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private static final String SALT = "Antigravity@2024";

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectByUsername(username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.selectByEmail(email));
    }

    @Override
    public List<User> findByRole(String role) {
        return userMapper.selectByRole(role);
    }

    @Override
    public PageResult<User> pageQuery(int pageNumber, int pageSize, String username, String role) {
        int offset = (pageNumber - 1) * pageSize;
        List<User> records = userMapper.selectPage(username, role, offset, pageSize);
        long total = userMapper.selectCount(username, role);
        return PageResult.of(records, pageNumber, pageSize, total);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        user.setPassword(encryptPassword(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        user.setIsDeleted(false);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
            return false;
        }

        String encryptedOldPassword = encryptPassword(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            return false;
        }

        return userMapper.updatePassword(userId, encryptPassword(newPassword)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Long userId, String role) {
        return userMapper.updateRole(userId, role) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        return userMapper.deleteByIds(ids) > 0;
    }

    private String encryptPassword(String password) {
        String saltedPassword = password + SALT;
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes(StandardCharsets.UTF_8));
    }

}
