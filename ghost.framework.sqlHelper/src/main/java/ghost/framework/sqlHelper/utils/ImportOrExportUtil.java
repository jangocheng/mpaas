package ghost.framework.sqlHelper.utils;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.config.Table;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 数据库导入导出工具
 *
 */
@Service
public class ImportOrExportUtil {
	// 写链接(写到主库,可使用事务)
	@Autowired
	private SqlHelper sqlHelper;

	@Value("${spring.database.package}")
	private String packageName;

	public void exportDb(String path) {
		path = path.replace(".zip", "");
		FileUtil.del(path);
		FileUtil.del(path + ".zip");
		try {
			Set<Class<?>> set = ClassUtil.scanPackage(packageName);
			Page page = new Page();
			page.setLimit(1000);

			for (Class<?> clazz : set) {
				Table table = clazz.getAnnotation(Table.class);
				if (table != null) {
					page.setCurrent(1);
					while (true) {
						page = sqlHelper.findPage(page, clazz);
						if (page.getRecords().size() == 0) {
							break;
						}

						List<String> lines = new ArrayList<String>();
						for (Object object : page.getRecords()) {
							lines.add(JSONUtil.toJsonStr(object));
						}
						FileUtil.appendLines(lines, path + File.separator + clazz.getSimpleName() + ".json", "UTF-8");
						System.out.println(clazz.getSimpleName() + "表导出了" + page.getRecords().size() + "条数据");
						page.setCurrent(page.getCurrent() + 1);
					}
				}
			}
			ZipUtil.zip(path);

		} catch (Exception e) {
			e.printStackTrace();
			FileUtil.del(path + ".zip");
		}

		FileUtil.del(path);
	}

	public void importDb(String path) {
		if (!FileUtil.exist(path)) {
			System.out.println(path + "文件不存在");
			return;
		}
		BufferedReader reader = null;

		path = path.replace(".zip", "");
		FileUtil.del(path);
		ZipUtil.unzip(path + ".zip");
		try {
			Set<Class<?>> set = ClassUtil.scanPackage(packageName);
			for (Class<?> clazz : set) {
				Table table = clazz.getAnnotation(Table.class);
				if (table != null) {
					File file = new File(path + File.separator + clazz.getSimpleName() + ".json");
					if (file.exists()) {
						sqlHelper.deleteByQuery(new ConditionAndWrapper(), clazz);

						reader = FileUtil.getReader(file, "UTF-8");
						List<Object> list = new ArrayList<Object>();
						while (true) {
							String json = reader.readLine();
							if (StrUtil.isEmpty(json)) {
								sqlHelper.insertAll(list);
								System.out.println(clazz.getSimpleName() + "表导入了" + list.size() + "条数据");
								list.clear();
								break;
							}
							list.add(JSONUtil.toBean(json, clazz));
							if (list.size() == 1000) {
								sqlHelper.insertAll(list);
								System.out.println(clazz.getSimpleName() + "表导入了" + list.size() + "条数据");
								list.clear();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IoUtil.close(reader);
		}
		FileUtil.del(path);
	}
}
