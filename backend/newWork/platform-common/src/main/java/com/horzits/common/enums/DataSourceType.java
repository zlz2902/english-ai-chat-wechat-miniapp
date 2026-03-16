package com.horzits.common.enums;

/**
 * 数据源
 * 
 * @author ruoyi
 */
public enum DataSourceType
{
    /**
     * 主库
     */
    MASTER,

    /**
     * 从库
     */
    SLAVE,


    /**
     * 农业平台从库
     */
    AMGWSLAVE,

    /**
     * 农业平台业务库
     */
    BUSINESS,

    /**
     * 农业平台业务库蔬菜停车
     */
    PARKSC,
    /**
     * 农业平台业务库水果停车
     */
    PARKSG,
    /**
     * 数据湖中间库
     */
    SEARCHS,
    /**
     * 数据中台业务库
     */
    DATASTATION,
    /**
     * ClickhouseTEST库
     */
    SLAVETEST,

    /**
     * ZSL库
     */
    ZSL

}
