package prosayj.execises.projects.user.service.impl;

import prosayj.execises.projects.user.domain.User;
import prosayj.execises.projects.user.repository.UserRepository;
import prosayj.execises.projects.user.service.UserService;

import java.util.ServiceLoader;

/**
 * UserServiceImpl
 *
 * @author yangjian201127@credithc.com
 * @date 2021-03-02 下午 11:58
 * @since 1.0.0
 */
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl() {
        ServiceLoader<UserRepository> load = ServiceLoader.load(UserRepository.class);
        userRepository = load.iterator().next();
    }


    @Override
    public boolean register(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}
