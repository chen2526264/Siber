package ccc.cj.siber;

import ccc.cj.siber.util.SiberFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.io.File;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class SiberApplication {
    private static final Logger logger = LoggerFactory.getLogger(SiberApplication.class);


    public static void main(String[] args) {
        beforeStartup();
        SpringApplication.run(SiberApplication.class, args);
    }

    /**
     * 在项目启动前检查工具目录是否存在。
     */
    public static void beforeStartup() {
        String tempPath = System.getProperty("user.home");

        String dataPath = tempPath + File.separator + "siber_data";
        logger.info("dataPath is " + dataPath);
        //如果dataPath不存在，则创建
        SiberFileUtils.mkdirIfNotExist(dataPath);
        System.setProperty("dataPath", dataPath);
    }
}
