package ghost.framework.core.bean.factory.container;
import ghost.framework.beans.annotation.bean.factory.ClassAnnotationBeanFactory;
import ghost.framework.beans.annotation.container.BeanCollectionContainer;
import ghost.framework.beans.annotation.tags.AnnotationTag;
import ghost.framework.context.base.ICoreInterface;
import ghost.framework.context.bean.factory.AbstractClassAnnotationBeanFactory;
import ghost.framework.context.bean.factory.IClassAnnotationBeanTargetHandle;
import ghost.framework.context.bean.factory.container.ICollectionContainerAnnotationBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
/**
 * package: ghost.framework.core.event.bean.factory
 *
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:操作绑定 {@link java.util.Collection} 列表容器事件工厂类
 * @Date: 10:54 2020/1/25
 * @param <O> 发起方类型
 * @param <T> 绑定定义类型
 * @param <E> 操作绑定事件目标处理类型
 */
@ClassAnnotationBeanFactory(tag = AnnotationTag.AnnotationTags.Container)
public class CollectionContainerAnnotationBeanFactory
        <
                O extends ICoreInterface,
                T extends Class<?>,
                E extends IClassAnnotationBeanTargetHandle<O, T, V, String, Object>,
                V extends Object
                >
        extends AbstractClassAnnotationBeanFactory<O, T, E, V>
        implements ICollectionContainerAnnotationBeanFactory<O, T, E, V> {
    /**
     * 事件工厂注释
     */
    private final Class<? extends Annotation> annotation = BeanCollectionContainer.class;

    /**
     * 获取绑定注释类型
     *
     * @return
     */
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return annotation;
    }

    @Override
    public String toString() {
        return "CollectionContainerAnnotationBeanFactory{" +
                "annotation=" + annotation +
                '}';
    }

    /**
     * 添加绑定后事件
     *
     * @param event 事件对象
     */
    @Override
    public void loader(E event) {
        //获取容器注释
        BeanCollectionContainer container = this.getAnnotation(event);
        //判断是否无效处理引发错误
        if (container.error()) {
            event.getExecuteOwner().getBean(container.value()).add(event.getValue());
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("loader:" + container.value().getName() + " add(" + event.getValue().getClass().getName() + ")");
            }
            return;
        }
        //处理可以忽略找不到容器接口
        Collection list = event.getExecuteOwner().getNullableBean(container.value());
        if (list != null) {
            list.add(event.getValue());
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("loader:" + container.value().getName() + " add(" + event.getValue().getClass().getName() + ")");
            }
            return;
        }
    }

    /**
     * 删除绑定后事件
     *
     * @param event 事件对象
     */
    @Override
    public void unloader(E event) {
        //获取容器注释
        BeanCollectionContainer container = (BeanCollectionContainer) event.getTarget().getAnnotation(this.annotation);
        //判断是否无效处理引发错误
        if (container.error()) {
            event.getExecuteOwner().getBean(container.value()).remove(event.getValue());
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("unloader:" + container.value().getName() + " remove(" + event.getValue().getClass().getName() + ")");
            }
            return;
        }
        //处理可以忽略找不到容器接口
        Collection list = event.getExecuteOwner().getNullableBean(container.value());
        if (list != null) {
            list.remove(event.getValue());
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("unloader:" + container.value().getName() + " remove(" + event.getValue().getClass().getName() + ")");
            }
            return;
        }
    }
}