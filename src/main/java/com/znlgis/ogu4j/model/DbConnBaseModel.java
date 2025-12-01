package com.znlgis.ogu4j.model;

import lombok.Data;

/**
 * 数据库连接信息基类
 *
 * @author znlgis
 */
@Data
public class DbConnBaseModel {
    /**
     * 数据库类型
     */
    private String dbtype;
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
