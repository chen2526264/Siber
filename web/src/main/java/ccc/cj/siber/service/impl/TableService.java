package ccc.cj.siber.service.impl;

import ccc.cj.siber.database.DataSourceManager;
import ccc.cj.siber.database.model.*;
import ccc.cj.siber.service.IDataSourceManagerService;
import ccc.cj.siber.service.IDataSourceService;
import ccc.cj.siber.service.ITableService;
import ccc.cj.siber.util.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * @author chenjiong
 * @date 06/03/2018 10:18
 */
@Service
public class TableService implements ITableService {

    @Autowired
    private IDataSourceManagerService datasourceManagerService;
    @Autowired
    private IDataSourceService datasourceService;

    @Override
    public Result getTable(Integer datasourceId, String tableName) throws Exception {
        Optional<Pair<File, DataSourceInfo>> dataSource = datasourceService.doGetDataSource(datasourceId);
        if (!dataSource.isPresent()) {
            return Result.fail("Datasource does not exist which id is " + datasourceId + " !");
        }
        DataSourceInfo dataSourceInfo = dataSource.get().getSecond();
        List<Table> tables = dataSourceInfo.getTables();
        for (Table table : tables) {
            if (tableName.equals(table.getTableName())) {
                return new Result(table.convertToWeb(datasourceId));
            }
        }
        return Result.fail("table does not exist which table name is " + tableName + " !");

    }

    @Override
    public Result refresh(Integer id) throws Exception {
        Optional<DataSourceManager> datasourceManager = datasourceManagerService.get(id);
        return datasourceManager.map(Try.function(manager -> {
            List<Table> tableInfos = manager.getLatestTableInfos();
            DataSourceInfo dataSourceInfo = manager.getDataSourceInfo();
            dataSourceInfo.setTables(tableInfos);
            datasourceService.updateDataSource(dataSourceInfo);
            return Result.success();
        })).orElse(Result.fail("该数据源连接不成功,请检查数据源配置信息!"));
    }

    @Override
    public Result update(TableWeb tableWeb) throws Exception {
        Optional<DataSourceManager> datasourceManager = datasourceManagerService.get(tableWeb.getDatasourceId());
        Table inputTable = tableWeb.convertToTable();
        return datasourceManager
                .map(Try.function(manager -> {
                    DataSourceInfo dataSourceInfo = manager.getDataSourceInfo();
                    List<Table> tables = dataSourceInfo.getTables();
                    for (Table table : tables) {
                        if (table.getTableName().equals(inputTable.getTableName())) {
                            table.setNeedAnalyse(inputTable.getNeedAnalyse());
                            table.setColumns(inputTable.getColumns());

                            datasourceService.updateDataSource(dataSourceInfo);
                            return Result.success();
                        }
                    }
                    return null;
                }))
                .orElse(Result.fail(String.format("ID为%s的数据源没有表%s！", tableWeb.getDatasourceId(), tableWeb.getTableName())));
    }


}
