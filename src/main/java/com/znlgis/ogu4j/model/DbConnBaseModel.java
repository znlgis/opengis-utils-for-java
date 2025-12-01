package com.znlgis.ogu4j.model;

import lombok.Data;

/**
 * 数据库连接配置模型
 * <p>
 * 用于存储数据库连接的基本配置信息，包括数据库类型、主机地址、端口、数据库名称、模式和认证信息。
 * 主要用于PostGIS等空间数据库的连接配置。
 * </p>
 *
 * @see com.znlgis.ogu4j.datasource.PostgisUtil
 * @see com.znlgis.ogu4j.datasource.OguLayerConverter#fromPostGIS
 * @see com.znlgis.ogu4j.datasource.OguLayerConverter#toPostGIS
 */
@Data
public class DbConnBaseModel {
    /**
     * 数据库类型
     */
    private String dbType;
    /**
     * 数据库地址
     */
    private String host;
    /**
     * 数据库端口
     */
    private String port;
    /**
     * 数据库schema
     */
    private String schema;
    /**
     * 数据库名称
     */
    private String database;
    /**
     * 数据库用户名
     */
    private String user;
    /**
     * 数据库密码
     */
    private String passwd;

    /**
     * 默认构造函数。
     * <p>
     * 供序列化框架和反射创建空的数据库连接信息实例使用。
     * </p>
     */
    public DbConnBaseModel(){
        // no-op default constructor
    }
}
