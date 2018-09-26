package ccc.cj.siber.service.impl;

import ccc.cj.siber.database.DataSourceManager;
import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.service.IConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
@Service
public class ConnectionService implements IConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    @Override
    public Result testConnection(DataSourceInfo dataSourceInfo) {
        Connection con = null;
        PreparedStatement pre = null;
        ResultSet result = null;
        try {
            Class.forName(DataSourceManager.dataSourceTypes.get(dataSourceInfo.getDataSourceType()));
            logger.info("开始尝试连接数据库！");
            String url = dataSourceInfo.getUrl();
            String user = dataSourceInfo.getUserName();
            String password = dataSourceInfo.getPassword();
            DriverManager.setLoginTimeout(5);//连接超时时间
            con = DriverManager.getConnection(url, user, password);// 获取连接
            System.out.println("连接成功！");
            String sql = "select 1";
            pre = con.prepareStatement(sql);
            result = pre.executeQuery();
            return Result.success();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                // 逐一将上面的几个对象关闭，因为不关闭的话会影响性能、并且占用资源
                // 注意关闭的顺序，最后使用的最先关闭
                if (result != null)
                    result.close();
                if (pre != null)
                    pre.close();
                if (con != null)
                    con.close();
                logger.info("数据库连接已关闭！");
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return Result.fail("连接数据库失败");
    }
}
