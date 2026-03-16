package com.horzits.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;

/**
 * @author yanglei
 * @date 2022/12/9 - 15:56
 */
public class JdbcTest {
    public static void main(String[] args) throws Exception {
        //JSONArray resultSet = (JSONArray) jd("com.mysql.cj.jdbc.Driver", "jdbc:mysql://192.168.10.194:3306/amgw", "root", "111111", "SHOW TABLES;","2");
        JSONArray resultSet = (JSONArray) jd("ru.yandex.clickhouse.ClickHouseDriver", "jdbc:clickhouse://192.168.10.95:8123/amgw", "default", "123123", "USE ;SHOW TABLES","2");
        System.out.println(resultSet);
    }

    public static  Object  jd(String className,String database,String username,String password,String sql,String type)throws Exception, SQLException, InstantiationException, IllegalAccessException
    {
        //返回数值判断是否连接成功
        int ret1=0;
        JSONArray ret2=new JSONArray();
        JSONObject ret3=new JSONObject();
        //加载数据库驱动包
        Class clas= Class.forName(className);
        Driver driver= (Driver) clas.newInstance();
        //注册驱动
        DriverManager.registerDriver(driver);
        System.out.println("数据库驱动加载成功");
        //建立数据库链接
        String  url;
        if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(className)){
             url=database;

        }else{
             url=database+"?serverTimezone=Asia/Shanghai";

        }
        Connection connection= DriverManager.getConnection(url,username,password);
        //Connection connection= DriverManager.getConnection("jdbc:mysql://192.168.10.194:3306/amgw?user=root&password=111111&useUnicode=true&characterEncoding=UTF-8");
        //创建操作命令
        Statement statement=connection.createStatement();
        //执行SQL语句
        ResultSet resultSet=statement.executeQuery(sql);
        //处理结果集
        if(type.equals("3")) {
            while (resultSet.next()) {
                ret3=(JSONObject)JSONObject.toJSON(resultSet.getMetaData());
            }
        }
        //处理结果集
        if(type.equals("2")) {
            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            while (resultSet.next()) {
                JSONObject mapOfColValues = new JSONObject();
                for (int i = 1; i <= num; i++) {
                    mapOfColValues.put(md.getColumnName(i).replace("SCHEMA_NAME","name")
                            .replace("TABLE_NAME","tableName")
                            .replace("TABLE_SCHEMA","databaseName")
                            .replace("COLUMN_NAME","columnName")
                            .replace("DATA_TYPE","type")
                            .replace("COLUMN_COMMENT","comment")
                            .replace("TABLE_ROWS","rows")
                            , resultSet.getObject(i));
                }
                ret2.add(mapOfColValues);
            }
        }
        //关闭结果集
        if (resultSet !=null)
        {
            try {
                ret1=1;
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //关闭命令
        if (statement!=null)
        {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //关闭连接命令
        if (connection!=null)
        {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(type.equals("2")) {
            return ret2;
        }else if(type.equals("3")) {
            return ret3;
        }else{
            return ret1;
        }
    }
}
