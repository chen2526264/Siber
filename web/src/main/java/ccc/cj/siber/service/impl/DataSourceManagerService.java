package ccc.cj.siber.service.impl;

import ccc.cj.siber.database.DataSourceManager;
import ccc.cj.siber.database.FireBirdDataSourceManager;
import ccc.cj.siber.database.MysqlDataSourceManager;
import ccc.cj.siber.database.SQLiteDataSourceManager;
import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Pair;
import ccc.cj.siber.service.IDataSourceManagerService;
import ccc.cj.siber.service.IDataSourceService;
import ccc.cj.siber.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DataSourceManagerService implements IDataSourceManagerService {

    @Autowired
    private IDataSourceService datasourceService;

    private Map<Integer, DataSourceManager> managers = new HashMap<>();

    public Optional<DataSourceManager> get(Integer dataSourceId) throws Exception {
        DataSourceManager result = managers.get(dataSourceId);
        if (result == null) {
            result = generateDatasourceManager(dataSourceId);
            if (result != null) {
                managers.put(dataSourceId, result);
            }
        }
        return Optional.ofNullable(result);
    }

    private DataSourceManager generateDatasourceManager(Integer datasourceId) throws Exception {
        Optional<Pair<File, DataSourceInfo>> datasource = datasourceService.doGetDataSource(datasourceId);
        return datasource.map(pair -> {
            DataSourceManager result = null;
            DataSourceInfo dataSourceInfo = pair.getSecond();
            String datasourceType = dataSourceInfo.getDataSourceType();
            if (Constant.MYSQL.equals(datasourceType)) {
                result = new MysqlDataSourceManager(pair.getFirst(), dataSourceInfo);
            } else if (Constant.SQLITE.equals(datasourceType)) {
                result = new SQLiteDataSourceManager(pair.getFirst(), dataSourceInfo);
            } else if (Constant.FIRE_BIRD.equals(datasourceType)) {
                result = new FireBirdDataSourceManager(pair.getFirst(), dataSourceInfo);
            }
            if (result != null && result.testConnect().getCode().equals(Constant.SUCCESS_CODE)) {
                return result;
            } else {
                return null;
            }
        }).orElse(null);
    }

    public Boolean remove(Integer dataSourceId) {
        DataSourceManager dataSourceManager = managers.get(dataSourceId);
        if (dataSourceManager != null) {
            dataSourceManager.close();
        }
        return true;
    }

}
