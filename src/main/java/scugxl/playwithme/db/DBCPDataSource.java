package scugxl.playwithme.db;

import lombok.*;
import lombok.extern.log4j.*;
import org.apache.commons.dbcp2.*;
import org.apache.commons.io.*;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.*;
import org.springframework.core.io.support.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;

import javax.annotation.*;
import java.io.*;
import java.sql.*;

@Log4j2
@Component
public class DBCPDataSource {

    @Getter
    private BasicDataSource ds = new BasicDataSource();


    @Value("${jdbc.url}")
    String url;

    @Value("${jdbc.minIdle:3}")
    int minIdle;


    @Value("${jdbc.maxTotal:20}")
    int maxTotal;

    @Value("${jdbc.driver:org.sqlite.JDBC}")
    String driver;

    @Value("${jdbc.user:}")
    String user;

    @Value("${jdbc.pass:}")
    String pass;

    @Value("${jdbc.validateQuery:select 1}")
    String validateQuery;

    @Getter
    PlatformTransactionManager txMgr;

    @Autowired
    ResourceLoader resourceLoader;

    @PostConstruct
    public void postConstruct() throws SQLException, IOException {
        ds.setUrl(url);
        ds.setMinIdle(minIdle);
        ds.setMaxTotal(maxTotal);
        ds.setDriverClassName(driver);
        ds.setAbandonedUsageTracking(true);
        ds.setLogAbandoned(true);
        ds.setValidationQuery(validateQuery);
        if (StringUtils.isNotEmpty(user)) {
            ds.setUsername(user);
            ds.setPassword(pass);
        }
        try {
            validateDatasource();
        } catch (Exception e) {
            throw new SQLException("Sql is not available - " + ExceptionUtils.getRootCauseMessage(e));
        }
        txMgr = new DataSourceTransactionManager(ds);
        LOG.info("!!!Database is ready");

        org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:./init_sql/*.sql");
        for (Resource r : resources) {
            try (Connection conn = ds.getConnection()) {
                String sql = IOUtils.toString(r.getInputStream());
                sql = sql.trim();
                conn.prepareStatement(sql).execute();
                LOG.info("Sql {} executed", r.getFilename());
            } catch (Exception e) {
                LOG.error("Fail to execute the sql file {}", r.getFilename(), e);
                throw new SQLException(e);
            }
        }

    }

    private void validateDatasource() throws SQLException {
        try (Connection conn = ds.getConnection()) {
            conn.prepareStatement(validateQuery).execute();
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate j = new JdbcTemplate(ds);
        return j;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(ds);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }


}