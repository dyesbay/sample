package app.expert.configs;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
public class DatabaseConfig {
    @Value("${info.db.driver}")
    private String driver;

    @Value("${info.db.password}")
    private String password;

    @Value("${info.db.url}")
    private String url;

    @Value("${info.db.username}")
    private String username;

    @Value("${info.hibernate.dialect}")
    private String hibernateDialect;

    @Value("${info.hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${info.hibernate.hbm2ddl.auto}")
    private String hibernateHbm2DdlAuto;

    @Value("${info.hibernate.c3p0.max_size:64}")
    private int connPoolMaxSize;

    @Value("${info.hibernate.c3p0.min_size:5}")
    private int connPoolMinSize;

    @Value("${info.hibernate.c3p0.idle_test_period:900}")
    private int connPoolIdlePeriod;

    @Bean
    public ComboPooledDataSource dataSource() {
        // a named datasource is best practice for later jmx monitoring
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        try {
            dataSource.setDriverClass(driver);
        } catch (PropertyVetoException pve) {
//            logger.error("Cannot load datasource driver ({}) : {}", driver, pve.getMessage());
            return null;
        }
        dataSource.setJdbcUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setMinPoolSize(connPoolMinSize);
        dataSource.setMaxPoolSize(connPoolMaxSize);
        dataSource.setInitialPoolSize(connPoolMinSize);
        dataSource.setMaxIdleTime(connPoolIdlePeriod);
        dataSource.setMaxStatements(1000);
        dataSource.setMaxStatementsPerConnection(100);
        dataSource.setMaxAdministrativeTaskTime(60);
        dataSource.setStatementCacheNumDeferredCloseThreads(1);
        dataSource.setTestConnectionOnCheckout(true);
        dataSource.setPreferredTestQuery("SELECT 1");
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.dialect", hibernateDialect);
        hibernateProperties.put("hibernate.show_sql", hibernateShowSql);
        hibernateProperties.put("hibernate.hbm2ddl.auto", hibernateHbm2DdlAuto);
        sessionFactoryBean.setHibernateProperties(hibernateProperties);

        return sessionFactoryBean;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

}
