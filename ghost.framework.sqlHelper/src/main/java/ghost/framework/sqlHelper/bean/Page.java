package ghost.framework.sqlHelper.bean;

import java.util.Collections;
import java.util.List;

/**
 * 分页类
 * 
 */
public class Page {
	Long count = 0l;// 总记录数
	Integer current = 1; // 起始页
	Integer limit = 10;// 每页记录数

	List records = Collections.emptyList();

	public <T> List<T> getRecords(Class<T> clazz) {
		return (List<T>) records;
	}

	public List getRecords() {
		return records;
	}

	public void setRecords(List records) {
		this.records = records;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}


	public Integer getCurrent() {
		return current;
	}

	public void setCurrent(Integer current) {
		this.current = current;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}
