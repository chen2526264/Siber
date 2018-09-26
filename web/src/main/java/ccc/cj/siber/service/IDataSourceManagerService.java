package ccc.cj.siber.service;

import ccc.cj.siber.database.DataSourceManager;

import java.util.Optional;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
public interface IDataSourceManagerService {
    Optional<DataSourceManager> get(Integer dataSourceId) throws Exception;

    Boolean remove(Integer dataSourceId);
}
