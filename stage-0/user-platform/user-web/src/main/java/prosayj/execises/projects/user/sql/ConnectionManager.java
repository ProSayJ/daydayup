package prosayj.execises.projects.user.sql;

import java.sql.Connection;

/**
 * 标记接口
 *
 * @author yangjian201127
 * @date 2021-03-03 下午 03:01
 * @since 1.0.0
 */
public interface ConnectionManager {
    Connection getConnection();
}
