package ghost.framework.datasource.entity.nosql;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * package: ghost.framework.datasource.entity.nosql
 *
 * @Author: 郭树灿{gsc-e590}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 2020/6/12:18:11
 */
@Entity
@Table(name="a3404a922a454d0cbfd554f7ab9a5aa5",
        indexes = {
                @Index(name = "uk", columnList = "data_source_id", unique = true),
                @Index(name = "pk", columnList = "data_source_id")
        })
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RedisEntity implements Serializable {
    private static final long serialVersionUID = -3364698727071438943L;
    /**
     * 主键id
     */
    @Id
    @Column(name = "data_source_id", updatable = false, nullable = false, unique = true, length = 36, columnDefinition = "char(36)")
    @NotNull(message = "null error")
    @Length(max = 36, min = 36, message = "length error")
    private String dataSourceId;

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
