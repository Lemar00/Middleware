package ept.dic2.git.bdr.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = "middlewareDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.middleware")
    public DataSource middlewareDataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "fragment1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.fragment1")
    public DataSource fragment1DataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "fragment2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.fragment2")
    public DataSource fragment2DataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "fragment1CopyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.fragment1.copy")
    public DataSource fragment1CopyDataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "fragment2CopyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.fragment2.copy")
    public DataSource fragment2CopyDataSource() {
        return new DriverManagerDataSource();
    }

    @Bean(name = "middlewareJdbcTemplate")
    public JdbcTemplate middlewareJdbcTemplate(@Qualifier("middlewareDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "fragment1JdbcTemplate")
    public JdbcTemplate fragment1JdbcTemplate(@Qualifier("fragment1DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "fragment2JdbcTemplate")
    public JdbcTemplate fragment2JdbcTemplate(@Qualifier("fragment2DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "fragment1CopyJdbcTemplate")
    public JdbcTemplate fragment1CopyJdbcTemplate(@Qualifier("fragment1CopyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "fragment2CopyJdbcTemplate")
    public JdbcTemplate fragment2CopyJdbcTemplate(@Qualifier("fragment2CopyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}