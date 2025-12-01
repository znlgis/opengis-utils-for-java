package com.znlgis.ogu4j.datasource;

import cn.hutool.core.text.CharSequenceUtil;
import com.znlgis.ogu4j.model.DbConnBaseModel;
import lombok.SneakyThrows;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * PostGIS数据库工具类
 * <p>
 * 提供PostGIS空间数据库的连接、数据读写和管理功能。
 * 支持GeoTools和GDAL两种方式访问PostGIS。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class PostgisUtil {
    private PostgisUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取Postgis数据源参数
     *
     * @param dbConnBaseModel 数据库连接信息
     * @return Postgis数据源参数
     */
    private static Map<String, Object> getPostgisInfo(DbConnBaseModel dbConnBaseModel) {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", dbConnBaseModel.getDbType());
        params.put("host", dbConnBaseModel.getHost());
        params.put("port", dbConnBaseModel.getPort());
        params.put("schema", dbConnBaseModel.getSchema());
        params.put("database", dbConnBaseModel.getDatabase());
        params.put("user", dbConnBaseModel.getUser());
        params.put("passwd", dbConnBaseModel.getPasswd());
        params.put("preparedStatements", true);
        params.put("encode functions", true);

        return params;
    }

    /**
     * 获取GDAL Postgis数据源参数
     *
     * @param dbConnBaseModel 数据库连接信息
     * @return GDAL Postgis数据源参数
     */
    public static String toGdalPostgisConnStr(DbConnBaseModel dbConnBaseModel) {
        return "PG: host=" + dbConnBaseModel.getHost() +
                " port=" + dbConnBaseModel.getPort() +
                " dbname=" + dbConnBaseModel.getDatabase() +
                " user=" + dbConnBaseModel.getUser() +
                " password=" + dbConnBaseModel.getPasswd() +
                " active_schema=" + dbConnBaseModel.getSchema();
    }

    /**
     * 获取Postgis数据源
     *
     * @param dbConnBaseModel 数据库连接信息
     * @return Postgis数据源
     */
    @SneakyThrows
    public static JDBCDataStore getPostgisDataStore(DbConnBaseModel dbConnBaseModel) {
        Map<String, Object> params = getPostgisInfo(dbConnBaseModel);
        return (JDBCDataStore) DataStoreFinder.getDataStore(params);
    }

    /**
     * 获取Postgis数据源
     *
     * @param params 数据源参数
     * @return Postgis数据源
     */
    @SneakyThrows
    public static JDBCDataStore getPostgisDataStore(Map<String, Object> params) {
        return (JDBCDataStore) DataStoreFinder.getDataStore(params);
    }

    /**
     * 删除PostGIS指定图层的指定要素
     *
     * @param dbConnBaseModel 数据源
     * @param layerName       图层名称
     * @param whereClause     过滤条件
     * @return 删除的要素个数
     */
    @SneakyThrows
    public static int deletePostgisFeatures(DbConnBaseModel dbConnBaseModel, String layerName, String whereClause) {
        JDBCDataStore dataStore = PostgisUtil.getPostgisDataStore(dbConnBaseModel);
        Statement statement = dataStore.getConnection(Transaction.AUTO_COMMIT).createStatement();

        String sql = String.format("DELETE FROM %s.%s", dbConnBaseModel.getSchema(), layerName);
        if (CharSequenceUtil.isNotBlank(whereClause)) {
            sql += " WHERE " + whereClause;
        }
        int count = statement.executeUpdate(sql);

        statement.close();
        dataStore.dispose();
        return count;
    }
}
