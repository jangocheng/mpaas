package ghost.framework.beans.data.annotation;
import ghost.framework.beans.maven.annotation.MavenDependency;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:自定义数据源
 * @Date: 21:36 2019/11/30
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomDataSource {
    /**
     * 数据源类型
     * @return
     */
    String source() default "com.zaxxer.hikari.HikariDataSource";
    /**
     * 数据源包信息
     *
     * @return
     */
    MavenDependency dependency();
    /**
     * 依赖类型
     * @return
     */
    Class<?>[] type() default {};
}