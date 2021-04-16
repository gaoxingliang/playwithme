package scugxl.playwithme.conf;

import lombok.*;
import lombok.extern.log4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import scugxl.playwithme.db.*;

import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

@Getter
@Configuration
@Log4j2
public class BaiduConfig {

    @Value("${baidu.appkey}")
    String appkey;

    @Value("${baidu.secretkey}")
    String secretkey;

    @Value("${baidu.basedir:/}")
    String basedir;


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Getter
    Map<String, String> confMap = new ConcurrentHashMap<String, String>();

    @PostConstruct
    public void postCon() {
        refreshConf();
        LOG.info("Conf map loaded {}", confMap);
    }

    public void refreshConf() {
        jdbcTemplate.query("select * from confs ", new BeanPropertyRowMapper<>(Conf.class))
                .stream().forEach(c -> {
            confMap.put(c.getConfkey(), c.getConfvalue());
        });
    }

    public String getConfString(String key) {
        return confMap.get(key);
    }

    public Long getConfLong(String key) {
        if (confMap.get(key) == null) {
            return null;
        }
        return Long.valueOf(confMap.get(key));
    }

    public void saveConf(String key, Object v) {
        synchronized(this) {
            List l = jdbcTemplate.query("select * from confs where confkey=? limit 1 ", new BeanPropertyRowMapper<>(Conf.class), key);
            if (l.isEmpty()) {
                jdbcTemplate.update("insert into confs values(?, ?)", key, v.toString());
            } else {
                jdbcTemplate.update("update confs set confvalue=? where confkey=?", v.toString(), key);
            }
        }
    }

}
