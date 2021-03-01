package prosayj.execises.projects.user.repository;

import prosayj.execises.projects.user.domain.User;
import prosayj.execises.projects.user.sql.DBConnectionManager;
import prosayj.execises.support.function.ThrowableFunction;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;

/**
 * 内嵌型 {@link UserRepository} 实现
 *
 * @author yangjian
 * @date 2021-02-28 上午 12:10
 * @since 1.0.0
 */
public class DatabaseUserRepository implements UserRepository {
    private static final Logger logger = Logger.getLogger(DatabaseUserRepository.class.getName());
    /**
     * 通用处理方式
     */
    private static final Consumer<Throwable> COMMON_EXCEPTION_HANDLER = e -> logger.log(Level.SEVERE, e.getMessage());

    private final DBConnectionManager dbConnectionManager;

    /**
     * 构造注入
     *
     * @param dbConnectionManager dbConnectionManager
     */
    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    private Connection getConnection() {
        return dbConnectionManager.getConnection();
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class<?>, String> resultSetMethodMappings = new HashMap<Class<?>, String>() {{
        put(Long.class, "getLong");
        put(String.class, "getString");
    }};

    /**
     *
     */
    static Map<Class<?>, String> preparedStatementMethodMappings = new HashMap<Class<?>, String>() {{
        // long
        put(Long.class, "setLong");
        put(String.class, "setString");
    }};


    public static final String INSERT_USER_DML_SQL = "INSERT INTO users(name,password,email,phoneNumber) VALUES " +
            "(?,?,?,?)";

    public static final String QUERY_ALL_USERS_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users";


    @Override
    public Collection<User> getAll() {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users", resultSet -> {
            // BeanInfo -> IntrospectionException
            BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
            List<User> users = new ArrayList<>();
            while (resultSet.next()) { // 如果存在并且游标滚动 // SQLException
                User user = new User();
                for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                    String fieldName = propertyDescriptor.getName();
                    Class<?> fieldType = propertyDescriptor.getPropertyType();
                    String methodName = resultSetMethodMappings.get(fieldType);
                    // 可能存在映射关系（不过此处是相等的）
                    String columnLabel = mapColumnLabel(fieldName);
                    Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                    // 通过放射调用 getXXX(String) 方法
                    Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                    // 获取 User 类 Setter方法
                    // PropertyDescriptor ReadMethod 等于 Getter 方法
                    // PropertyDescriptor WriteMethod 等于 Setter 方法
                    Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                    // 以 id 为例，  user.setId(resultSet.getLong("id"));
                    setterMethodFromUser.invoke(user, resultValue);
                }
            }
            return users;
        }, e -> {
            // 异常处理
        });
    }


    @Override
    public boolean save(User user) {
        return false;
    }

    @Override
    public boolean deleteById(Long userId) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery("SELECT id,name,password,email,phoneNumber FROM users WHERE name=? and password=?",
                resultSet -> {
                    // TODO
                    return new User();
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }


    /**
     * 通用结果查询方法
     *
     * @param sql      预执行sql
     * @param function 处理执行结果单元
     * @param <T>      结果集映射对象类
     * @return 返回结果集
     */
    protected <T> T executeQuery(String sql,
                                 ThrowableFunction<ResultSet, T> function,
                                 Consumer<Throwable> exceptionHandler,
                                 Object... args) {
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Class<?> argType = arg.getClass();

                Class<?> wrapperType = wrapperToPrimitive(argType);

                if (wrapperType == null) {
                    wrapperType = argType;
                }

                // Boolean -> boolean
                String methodName = preparedStatementMethodMappings.get(argType);
                Method method = PreparedStatement.class.getMethod(methodName, wrapperType);
                method.invoke(preparedStatement, i + 1, args);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            return function.apply(resultSet);
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return null;
    }


    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

}