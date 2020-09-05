package ghost.framework.core.maven.annotation.reader;

import ghost.framework.beans.maven.annotation.MavenDepository;
import ghost.framework.beans.maven.annotation.MavenDepositorys;
import ghost.framework.core.application.reader.IApplicationReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:注释 {@link ghost.framework.beans.maven.annotation.MavenDepository} 读取器接口
 * @Date: 19:42 2020/1/12
 */
public interface IAnnotationMavenDepositoryReader extends IApplicationReader {
    /**
     * 获取注释列表
     * @param c
     * @return
     */
    default List<MavenDepository> getList(Class<?> c) {
        List<MavenDepository> list = new ArrayList<>();
        //获取启动类注释的模块依赖包列表
        if (c.isAnnotationPresent(MavenDepositorys.class)) {
            for (MavenDepository dependency : c.getAnnotation(MavenDepositorys.class).value()) {
                list.add(this.getApp().getProxyAnnotationObject(dependency));
            }
        }
        if (c.isAnnotationPresent(MavenDepository.class)) {
            list.add(c.getAnnotation(MavenDepository.class));
        }
        return list;
    }
}
