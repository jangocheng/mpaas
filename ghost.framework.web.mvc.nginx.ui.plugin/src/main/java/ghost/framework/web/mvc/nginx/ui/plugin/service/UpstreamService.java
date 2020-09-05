package ghost.framework.web.mvc.nginx.ui.plugin.service;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import ghost.framework.beans.annotation.application.Application;
import ghost.framework.beans.annotation.injection.Autowired;
import ghost.framework.beans.annotation.stereotype.Service;
import ghost.framework.data.hibernate.ISessionFactory;
import ghost.framework.web.mvc.nginx.ui.plugin.entity.Param;
import ghost.framework.web.mvc.nginx.ui.plugin.entity.Upstream;
import ghost.framework.web.mvc.nginx.ui.plugin.entity.UpstreamServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UpstreamService {
	@Autowired
	@Application
	private ISessionFactory sessionFactory;

	public Page search(Page page, String word) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		if (StrUtil.isNotEmpty(word)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like("name", word));
		}

		page = sqlHelper.findPage(conditionAndWrapper, new Sort("id", Direction.DESC), page, Upstream.class);

		return page;
	}

	//	@Transactional
	public synchronized void deleteById(String id) throws SQLException {
		sessionFactory.deleteById(Upstream.class, id);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);
	}

	//	@Transactional
	public synchronized void addOver(Upstream upstream, List<UpstreamServer> upstreamServers, String upstreamParamJson) throws SQLException {
		if (upstream.getProxyType() == 1 || upstream.getTactics() == null) {
			upstream.setTactics("");
		}

		sessionFactory.insert(upstream);

		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(upstreamParamJson) && JSONUtil.isJson(upstreamParamJson)) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(upstreamParamJson), Param.class);
		}
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", upstream.getId()), Param.class);
		// 反向插入,保证列表与输入框对应
		Collections.reverse(paramList);
		for (Param param : paramList) {
			param.setUpstreamId(upstream.getId());
			sessionFactory.insert(param);
		}


		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", upstream.getId()), UpstreamServer.class);
		if (upstreamServers != null) {
			// 反向插入,保证列表与输入框对应
			Collections.reverse(upstreamServers);

			for (UpstreamServer upstreamServer : upstreamServers) {
				upstreamServer.setUpstreamId(upstream.getId());
				sessionFactory.insert(upstreamServer);
			}
		}

	}

	public List<UpstreamServer> getUpstreamServers(String id) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);
	}

	//	@Transactional
	public synchronized void del(String id) throws SQLException {
		sessionFactory.deleteById(Upstream.class, id);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);

	}

	public List<Upstream> getListByProxyType(Integer proxyType) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("proxyType", proxyType), Upstream.class);
	}

	public Long getCountByName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Upstream.class);
	}

	public Long getCountByNameWithOutId(String name, String id) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name).ne("id", id), Upstream.class);
	}

	public List<UpstreamServer> getServerListByMonitor(int monitor) {
		List<String> upstreamIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("monitor", monitor), Upstream.class);

		return sqlHelper.findListByQuery(new ConditionAndWrapper().in("upstreamId", upstreamIds), UpstreamServer.class);
	}

	public List<UpstreamServer> getAllServer() {
		return sessionFactory.findAll(UpstreamServer.class);
	}
}