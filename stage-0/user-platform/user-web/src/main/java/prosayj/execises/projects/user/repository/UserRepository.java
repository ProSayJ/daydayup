package prosayj.execises.projects.user.repository;

import prosayj.execises.projects.user.domain.User;

import java.util.Collection;

/**
 * 用户存储仓库
 *
 * @author yangjian
 * @since 1.0
 */
public interface UserRepository {

    boolean save(User user);

    boolean deleteById(Long userId);

    boolean update(User user);

    User getById(Long userId);

    User getByNameAndPassword(String userName, String password);

    Collection<User> getAll();
}
