package prosayj.execises.projects.user.repository;

import prosayj.execises.projects.user.domain.User;
import prosayj.execises.projects.user.sql.DBConnectionManager;
import prosayj.execises.support.function.ThrowableFunction;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.ClassUtils.wrapperToPrimitive;
import static prosayj.execises.projects.user.sql.DBConnectionManager.CREATE_USERS_TABLE_DDL_SQL;
import static prosayj.execises.projects.user.sql.DBConnectionManager.DROP_USERS_TABLE_DDL_SQL;

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
    private static final Consumer<Throwable> COMMON_EXCEPTION_HANDLER =
            e -> logger.log(Level.SEVERE, e.getMessage());

    private final DBConnectionManager dbConnectionManager;
    /**
     * 数据类型与 ResultSet 方法名映射
     */
    private final Map<Class<?>, String> resultSetMethodMappings;

    /**
     *
     */
    private final Map<Class<?>, String> preparedStatementMethodMappings;

    /**
     * 构造注入
     *
     * @param dbConnectionManager dbConnectionManager
     */
    public DatabaseUserRepository(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;

        resultSetMethodMappings = new HashMap<>();
        resultSetMethodMappings.put(Long.class, "getLong");
        resultSetMethodMappings.put(String.class, "getString");

        preparedStatementMethodMappings = new HashMap<>();
        preparedStatementMethodMappings.put(Long.class, "setLong");
        preparedStatementMethodMappings.put(String.class, "setString");
    }

    public static final String INSERT_USER_DML_SQL = "INSERT INTO users(name,password,email,phoneNumber) VALUES (?,?,?,?)";
    public static final String QUERY_ALL_USERS_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users";
    public static final String QUERY_USERS_BY_ID_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users WHERE id=?";
    public static final String QUERY_USERS_BY_ID_AND_PASSWORD_DML_SQL = "SELECT id,name,password,email,phoneNumber FROM users WHERE name=? and password=?";


    @Override
    public boolean save(User user) {
        boolean success;
        // init();
        success = executeUpdate(INSERT_USER_DML_SQL,
                COMMON_EXCEPTION_HANDLER,
                user.getName(),
                user.getPassword(),
                user.getEmail(),
                user.getPhoneNumber()) == 1;

        //测试查询方法：getAll
        getAll().forEach(user1 -> System.out.println("getAll=========>" + user1));

        //测试查询方法：getByNameAndPassword
        System.out.println("getByNameAndPassword=========>" + getByNameAndPassword(user.getName(), user.getPassword()));

        //测试查询方法：getById
        System.out.println("getById=========>" + getById(1L));
        return success;
    }

    @Override
    public Collection<User> getAll() {
        return executeQuery(QUERY_ALL_USERS_DML_SQL, resultSet -> {
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
                users.add(user);
            }
            return users;
        }, e -> {
            // 异常处理
        });
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
        return executeQuery(QUERY_USERS_BY_ID_DML_SQL,
                resultSet -> {
                    User user = new User();
                    resultSet.next();
                    user.setId(resultSet.getLong("id"));
                    user.setName(resultSet.getString("name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhoneNumber(resultSet.getString("phoneNumber"));
                    return user;
                }, COMMON_EXCEPTION_HANDLER, userId);
    }

    @Override
    public User getByNameAndPassword(String userName, String password) {
        return executeQuery(QUERY_USERS_BY_ID_AND_PASSWORD_DML_SQL,
                resultSet -> {
                    List<User> result = new ArrayList<>();
                    while (resultSet.next()) {
                        User user = new User();
                        user.setId(resultSet.getLong("id"));
                        user.setName(resultSet.getString("name"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPhoneNumber(resultSet.getString("phoneNumber"));
                        result.add(user);
                    }
                    return result.get(0);
                }, COMMON_EXCEPTION_HANDLER, userName, password);
    }

    /**
     * 组件 PreparedStatement
     *
     * @param sql  sql模板
     * @param args 动态参数
     * @return PreparedStatement
     * @throws SQLException              SQLException
     * @throws NoSuchMethodException     NoSuchMethodException
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException    IllegalAccessException
     */
    private PreparedStatement doCreatePreparedStatement(String sql, Object... args) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Connection connection = getConnection();
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
            Method method = PreparedStatement.class.getMethod(methodName, int.class, wrapperType);
            method.invoke(preparedStatement, i + 1, arg);
        }
        return preparedStatement;
    }


    /**
     * 通用结果查询方法
     * {@link PreparedStatement#executeQuery()}
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
        try {
            PreparedStatement preparedStatement = doCreatePreparedStatement(sql, args);
            ResultSet resultSet = preparedStatement.executeQuery();
            // 返回一个 POJO List -> ResultSet -> POJO List
            // ResultSet -> T
            return function.apply(resultSet);
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return null;
    }

    /**
     * 通用更新方法
     * {@link PreparedStatement#executeUpdate()}
     *
     * @param sql 预执行sql
     * @return 返回结果集
     */
    protected int executeUpdate(String sql,
                                Consumer<Throwable> exceptionHandler,
                                Object... args) {
        try {
            PreparedStatement preparedStatement = doCreatePreparedStatement(sql, args);
            return preparedStatement.executeUpdate();
        } catch (Throwable e) {
            exceptionHandler.accept(e);
        }
        return -1;
    }

    /**
     * 为了方便测试。初始化数据库
     */
    private void init() {
        Connection connection = getConnection();
        try {
            //初始化
            Statement statement = connection.createStatement();
            // result： false
            System.out.println(statement.execute(DROP_USERS_TABLE_DDL_SQL));
            // result：false
            System.out.println(statement.execute(CREATE_USERS_TABLE_DDL_SQL));
//            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_DML_SQL);
//            preparedStatement.setString(1, user.getName());
//            preparedStatement.setString(2, user.getPassword());
//            preparedStatement.setString(3, user.getEmail());
//            preparedStatement.setString(4, user.getPhoneNumber());
//            preparedStatement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    private Connection getConnection() {
        return dbConnectionManager.getConnection();
    }


}