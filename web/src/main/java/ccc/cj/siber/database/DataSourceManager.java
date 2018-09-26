package ccc.cj.siber.database;

import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.database.model.Table;
import ccc.cj.siber.database.model.TableData;
import ccc.cj.siber.util.Constant;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjiong
 * @date 03/02/2018 16:37
 */
public interface DataSourceManager {
    Map<String, String> dataSourceTypes = new HashMap<String, String>() {{
        put(Constant.MYSQL, "com.mysql.jdbc.Driver");
        put(Constant.SQLITE, "org.sqlite.JDBC");
        put(Constant.FIRE_BIRD, "org.firebirdsql.jdbc.FBDriver");
    }};

    /**
     * 获得该数据源的最新的表信息，会去从数据库重新获取一次。
     */
    List<Table> getLatestTableInfos();

    /**
     * 获得该数据源以保存的表信息。
     */
    List<Table> getTableInfos();

    DataSourceInfo getDataSourceInfo();

    File getDataSourceItemDir();

    Result testConnect();

    Boolean close();

    Map<String, TableData> queryCurrentData();
}
