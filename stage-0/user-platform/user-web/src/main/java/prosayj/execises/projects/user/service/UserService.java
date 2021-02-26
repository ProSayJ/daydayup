package prosayj.execises.projects.user.service;

import prosayj.execises.projects.user.domain.User;

/**
 * 用户服务
 *
 * @author yangjian
 * @since 1.0
 */
public interface UserService {

    /**
     * 注册用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    boolean register(User user);

    /**
     * 注销用户
     *
     * @param user 用户对象
     * @return 成功返回<code>true</code>
     */
    boolean deregister(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return true 更新成功，false 更新失败
     */
    boolean update(User user);

    User queryUserById(Long id);

    User queryUserByNameAndPassword(String name, String password);
}
