package com.rrd.coresystem.testasist.service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.rrd.coresystem.testasist.requestVo.SqlResponseVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lihao
 *
 */
@Slf4j
@Service
public class SqlServiceImpl {

	// 测试辅助需要链接非常多的数据库（约7个)，这里每次请求一个链接不采用连接池。 测试辅助使用，暂时不考虑性能。
	// private MysqlDataSource dataSource = new MysqlDataSource();

	private MysqlDataSource setupDataSource(String env, String service) {

		MysqlDataSource dataSource = new MysqlDataSource();

		dataSource.setServerName(EnvConfig.envMap.get(env).serviceDbConf.get(service)[0]);
		dataSource.setPort(3306);// 端口默认写死3306
		dataSource.setDatabaseName(EnvConfig.envMap.get(env).serviceDbConf.get(service)[1]);
		dataSource.setUser(EnvConfig.envMap.get(env).serviceDbConf.get(service)[2]);
		dataSource.setPassword(EnvConfig.envMap.get(env).serviceDbConf.get(service)[3]);
		dataSource.setCharacterEncoding("UTF-8");

		return dataSource;

	}

	public Statement getMysqlConnectionSatement(MysqlDataSource setupDataSourc) throws SQLException {

		Connection conn = (Connection) setupDataSourc.getConnection();
		return (Statement) conn.createStatement();

	}

	public SqlResponseVo runSql(String env, String service, String sql) {

		sql = sql.trim();

		if (isQuery(sql)) {

			return runSelectSql(env, service, sql);

		}

		else if (checkWhere(sql) && (isDelete(sql) || isUpdate(sql))) {

			int updateCount = checkSqlCount(env, service, sql);
			if (updateCount == 1) {
				return runUpdateSql(env, service, sql);
			} else if (updateCount > 1) {
				return new SqlResponseVo("-1", "will update " + String.valueOf(updateCount)
						+ " record, only 1 record update will be permitted!", null);
			} else {

				return new SqlResponseVo("-1", "will update 0 record, not executed this time!", null);

			}
		}

		else if (isInsert(sql)) {
			return runUpdateSql(env, service, sql);
		}

		else if (isProcedure(sql)) {
			return runProcedureSql(env, service, sql);
		} else {

			return new SqlResponseVo("-1",
					"sql is not recognized as select or update or delete statement and update or delete need 'where' !",
					null);

		}

	}

	private boolean isInsert(String sql) {
		// TODO Auto-generated method stub

		return sql.startsWith("insert");

	}

	private boolean isProcedure(String sql) {
		return sql.contains("call");
	}

	private SqlResponseVo runSelectSql(String env, String service, String sql) {

		MysqlDataSource dataSource = setupDataSource(env, service);
		SqlResponseVo sqlResponseVo = new SqlResponseVo();
		// @check env ,service is exist
		log.info("select sql : {}", sql);
		sqlResponseVo.setStatus("0");
		Connection conn = null;
		int count = 0;

		try {
			conn = (Connection) dataSource.getConnection();
			Statement stmt = (Statement) conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			JSONArray json = new JSONArray();
			ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
			while (rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JSONObject obj = new JSONObject();
				for (int i = 1; i <= numColumns; i++) {
					String column_name = rsmd.getColumnName(i);
					obj.put(column_name, rs.getObject(column_name));
				}
				json.add(obj);
				sqlResponseVo.setData(json);
				count++;
			}
			sqlResponseVo.setCount(count);

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			sqlResponseVo.setStatus(String.valueOf(e.getErrorCode()));
			sqlResponseVo.setMessage(e.getMessage());
		}

		finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return sqlResponseVo;
	}

	private SqlResponseVo runUpdateSql(String env, String service, String sql) {

		log.info("update sql : {}", sql);

		MysqlDataSource dataSource = setupDataSource(env, service);
		SqlResponseVo sqlResponseVo = new SqlResponseVo();
		// @check env ,service is exist

		Connection conn = null;
		try {
			conn = (Connection) dataSource.getConnection();
			Statement stmt = (Statement) conn.createStatement();
			int count = stmt.executeUpdate(sql);
			sqlResponseVo.setCount(count);
			sqlResponseVo.setMessage("success");
			sqlResponseVo.setStatus("0");
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			sqlResponseVo.setStatus(String.valueOf(e.getErrorCode()));
			sqlResponseVo.setMessage(e.getMessage());
		}

		finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sqlResponseVo;
	}

	private SqlResponseVo runProcedureSql(String env, String service, String sql) {

		log.info("procedure sql : {}", sql);

		MysqlDataSource dataSource = setupDataSource(env, service);
		SqlResponseVo sqlResponseVo = new SqlResponseVo();
		// @check env ,service is exist

		Connection conn = null;
		try {
			conn = (Connection) dataSource.getConnection();
			// 调用存储过程
			CallableStatement cs = conn.prepareCall(sql);
			ResultSet rs = cs.executeQuery();
			sqlResponseVo.setMessage("success");
			sqlResponseVo.setStatus("0");
			rs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			sqlResponseVo.setStatus(String.valueOf(e.getErrorCode()));
			sqlResponseVo.setMessage(e.getMessage());
		}

		finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sqlResponseVo;
	}

	// 这里针对del 和 update 语句，检查待跟新条数。
	private int checkSqlCount(String env, String service, String sql) {

		if (isDelete(sql))
			sql = sql.replaceAll("delete.*from", "select count(*) from");

		if (isUpdate(sql))
			sql = sql.replaceAll("update\\s*(.*)\\s*set.*where", "select count(*) from $1 where");

		log.info("check count sql : {}", sql);

		MysqlDataSource dataSource = setupDataSource(env, service);
		// SqlResponseVo sqlResponseVo = new SqlResponseVo();

		int count = 999;
		// @check env ,service is exist
		Connection conn = null;
		try {
			conn = (Connection) dataSource.getConnection();
			Statement stmt = (Statement) conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				log.info("{}", rs.getInt(1));
				count = rs.getInt(1);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		finally {

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return count;
	}

	private Boolean isQuery(String sql) {

		return sql.startsWith("select");
	}

	private Boolean isDelete(String sql) {
		return sql.startsWith("delete");
	}

	private Boolean isUpdate(String sql) {
		return sql.startsWith("update");
	}

	private Boolean checkWhere(String sql) {

		return sql.contains("where");

	}

	public static void main(String[] args) {
		String sql = "update hello set a=b where dcc=ssjjs;";
		sql = sql.replaceAll("update\\s*(.*)\\s*set.*where", "select count(*) from $1 where");
		System.out.println(sql);
	}

}
