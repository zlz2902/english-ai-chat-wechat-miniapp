package com.horzits.framework.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import com.horzits.common.enums.DataSourceType;
import com.horzits.common.utils.spring.SpringUtils;
import com.horzits.framework.config.properties.DruidProperties;
import com.horzits.framework.datasource.DynamicDataSource;

/**
 * druid 配置多数据源
 * 
 * @author ruoyi
 */
@Configuration
public class DruidConfig
{
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource masterDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //农业平台业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.business")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.business", name = "enabled", havingValue = "true")
    public DataSource businessDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //蔬菜停车业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.parksc")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.parksc", name = "enabled", havingValue = "true")
    public DataSource parkscDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //水果停车业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.parksg")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.parksg", name = "enabled", havingValue = "true")
    public DataSource parksgDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //蔬菜停车业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.searchs")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.searchs", name = "enabled", havingValue = "true")
    public DataSource searchsDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //数据中台业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.datastation")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.datastation", name = "enabled", havingValue = "true")
    public DataSource datastationDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    //蔬菜停车业务数据库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.slavetest")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slavetest", name = "enabled", havingValue = "true")
    public DataSource slavetestDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }
    //农业平台从库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.amgwslave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.amgwslave", name = "enabled", havingValue = "true")
    public DataSource amgwslaveDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }
    //zsl从库
    @Bean
    @ConfigurationProperties("spring.datasource.druid.zsl")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.zsl", name = "enabled", havingValue = "true")
    public DataSource zslDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource masterDataSource)
    {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER.name(), masterDataSource);
        setDataSource(targetDataSources, DataSourceType.SLAVE.name(), "slaveDataSource");
        setDataSource(targetDataSources, DataSourceType.AMGWSLAVE.name(), "amgwslaveDataSource");
        //农业平台业务数据库
        setDataSource(targetDataSources, DataSourceType.BUSINESS.name(), "businessDataSource");
        setDataSource(targetDataSources, DataSourceType.PARKSC.name(), "parkscDataSource");
        setDataSource(targetDataSources, DataSourceType.PARKSG.name(), "parksgDataSource");
        setDataSource(targetDataSources, DataSourceType.SEARCHS.name(),"searchsDataSource");
        setDataSource(targetDataSources, DataSourceType.DATASTATION.name(),"datastationDataSource");
        setDataSource(targetDataSources, DataSourceType.SLAVETEST.name(),"slavetestDataSource");
        setDataSource(targetDataSources, DataSourceType.ZSL.name(),"zslDataSource");
        return new DynamicDataSource(masterDataSource, targetDataSources);
    }
    
    /**
     * 设置数据源
     * 
     * @param targetDataSources 备选数据源集合
     * @param sourceName 数据源名称
     * @param beanName bean名称
     */
    public void setDataSource(Map<Object, Object> targetDataSources, String sourceName, String beanName)
    {
        try
        {
            DataSource dataSource = SpringUtils.getBean(beanName);
            targetDataSources.put(sourceName, dataSource);
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 去除监控页面底部的广告
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties)
    {
        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 提取common.js的配置路径
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        final String filePath = "support/http/resources/js/common.js";
        // 创建filter进行过滤
        Filter filter = new Filter()
        {
            @Override
            public void init(javax.servlet.FilterConfig filterConfig) throws ServletException
            {
            }
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException
            {
                chain.doFilter(request, response);
                // 重置缓冲区，响应头不会被重置
                response.resetBuffer();
                // 获取common.js
                String text = Utils.readFromResource(filePath);
                // 正则替换banner, 除去底部的广告信息
                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
                text = text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }
            @Override
            public void destroy()
            {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }
}
