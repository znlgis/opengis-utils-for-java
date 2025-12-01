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
     * 构建PostGIS数据源连接参数（GeoTools格式）
     * <p>
     * 将数据库连接配置转换为GeoTools DataStore所需的参数Map。
     * </p>
     *
     * @param dbConnBaseModel 数据库连接配置
     * @return PostGIS数据源参数Map
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
     * 构建GDAL PostGIS连接字符串
     * <p>
     * 将数据库连接配置转换为GDAL OGR所需的PG连接字符串格式。
     * </p>
     *
     * @param dbConnBaseModel 数据库连接配置
     * @return GDAL PostGIS连接字符串，格式如：PG: host=xxx port=xxx dbname=xxx ...
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
     * 获取GeoTools PostGIS数据源
     * <p>
     * 使用数据库连接配置创建GeoTools JDBCDataStore实例。
     * 使用完毕后应调用dispose()方法释放资源。
     * </p>
     *
     * @param dbConnBaseModel 数据库连接配置
     * @return JDBCDataStore数据源实例
     */
    @SneakyThrows
    public static JDBCDataStore getPostgisDataStore(DbConnBaseModel dbConnBaseModel) {
        Map<String, Object> params = getPostgisInfo(dbConnBaseModel);
        return (JDBCDataStore) DataStoreFinder.getDataStore(params);
    }

    /**
     * 获取GeoTools PostGIS数据源
     * <p>
     * 使用参数Map创建GeoTools JDBCDataStore实例。
     * 使用完毕后应调用dispose()方法释放资源。
     * </p>
     *
     * @param params 数据源连接参数Map
     * @return JDBCDataStore数据源实例
     */
    @SneakyThrows
    public static JDBCDataStore getPostgisDataStore(Map<String, Object> params) {
        return (JDBCDataStore) DataStoreFinder.getDataStore(params);
    }

    /**
     * 解析GDAL PostGIS连接字符串为DbConnBaseModel
     * <p>
     * 将GDAL OGR格式的PG连接字符串解析为数据库连接配置对象。
     * 支持格式：PG: host=xxx port=xxx dbname=xxx user=xxx password=xxx active_schema=xxx
     * </p>
     *
     * @param connStr GDAL PostGIS连接字符串
     * @return 数据库连接配置对象
     */
    public static DbConnBaseModel parseConnectionString(String connStr) {
        DbConnBaseModel model = new DbConnBaseModel();
        model.setDbType("postgis");

        if (connStr == null) {
            return model;
        }

        // Remove "PG:" prefix if present
        String cleanStr = connStr.replaceFirst("^PG:\\s*", "");

        String[] parts = cleanStr.split("\\s+");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].trim().toLowerCase();
            String value = kv[1].trim();

            switch (key) {
                case "host":
                    model.setHost(value);
                    break;
                case "port":
                    model.setPort(value);
                    break;
                case "dbname":
                case "database":
                    model.setDatabase(value);
                    break;
                case "user":
                    model.setUser(value);
                    break;
                case "password":
                case "passwd":
                    model.setPasswd(value);
                    break;
                case "active_schema":
                case "schema":
                    model.setSchema(value);
                    break;
                default:
                    break;
            }
        }

        // Set defaults if not provided
        if (model.getSchema() == null) {
            model.setSchema("public");
        }
        if (model.getPort() == null) {
            model.setPort("5432");
        }

        return model;
    }

    /**
     * 删除PostGIS图层中的要素
     * <p>
     * 根据SQL WHERE条件删除指定图层中的要素。
     * 如果不指定条件，将删除图层中的所有要素。
     * </p>
     *
     * @param dbConnBaseModel 数据库连接配置
     * @param layerName       图层名称（表名）
     * @param whereClause     SQL WHERE子句（不包含WHERE关键字），为null或空时删除所有要素
     * @return 删除的要素数量
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
