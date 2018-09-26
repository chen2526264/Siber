package ccc.cj.siber.service;

import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Pair;
import ccc.cj.siber.database.model.Result;

import java.io.File;
import java.util.Optional;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
public interface IDataSourceService {
    Result getDataSource(Integer id) throws Exception;

//    Result getDatasourceTypes() throws Exception;

    Result getAllDataSource() throws Exception;

    Result addDataSource(DataSourceInfo dataSourceInfo) throws Exception;

    Result deleteDataSource(Integer id) throws Exception;

    Result updateDataSource(DataSourceInfo dataSourceInfo) throws Exception;
    /**
     * 根据id，获取dataSource路径下的数据源信息。
     * @param id
     * @return File是该数据源目录。
     */
    Optional<Pair<File, DataSourceInfo>> doGetDataSource(Integer id) throws Exception;
}
