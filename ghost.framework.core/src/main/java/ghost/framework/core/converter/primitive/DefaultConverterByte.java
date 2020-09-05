package ghost.framework.core.converter.primitive;

import ghost.framework.beans.annotation.constructor.Constructor;
import ghost.framework.beans.annotation.converter.ConverterFactory;
import ghost.framework.beans.annotation.injection.Autowired;
import ghost.framework.context.base.ICoreInterface;
import ghost.framework.context.converter.AbstractConverter;
import ghost.framework.context.converter.ConverterException;
import ghost.framework.context.converter.primitive.ConverterByte;
import ghost.framework.util.StringUtils;

import java.util.Arrays;

/**
 * package: ghost.framework.core.converter.primitive
 *
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 2020/2/27:10:19
 */
@ConverterFactory(defaultValue = true)
public class DefaultConverterByte<S extends Object, T extends Byte>
        extends AbstractConverter<S, T>
        implements ConverterByte<S, T> {
    @Constructor
    public DefaultConverterByte(@Autowired(required = false) ICoreInterface domain) {
        super(domain);
    }

    public DefaultConverterByte() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultConverterByte<?, ?> that = (DefaultConverterByte<?, ?>) o;
        return Arrays.equals(targetType, that.targetType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(targetType);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultConverterByte{" +
                "targetType=" + Arrays.toString(targetType) +
                '}';
    }

    private final Class<?>[] targetType = {Byte.class, byte.class};

    @Override
    public Class<?>[] getTargetType() {
        return targetType;
    }

    @Override
    public boolean canConvert(S source) {
        try {
            //判断是否可以转换
            if (StringUtils.isEmpty(source)) {
                return false;
            }
            Byte.valueOf(source.toString());
            return true;
        } catch (IllegalArgumentException e) {
        }
        return false;
    }

    @Override
    public T convert(S source) throws ConverterException {
        try {
            if (source instanceof String && StringUtils.isEmpty(source)) {
                return (T) Byte.valueOf("0");
            }
            return (T) Byte.valueOf(source.toString());
        } catch (NumberFormatException e) {
            throw new ConverterException(e.getMessage(), e);
        }
    }
}