package prosayj.execises.web.mvc.header.annotation;

import java.lang.annotation.*;

/**
 * Controller 标记接口-控制器
 *
 * @author yangjian
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheControl {

    String[] value() default {};
}
