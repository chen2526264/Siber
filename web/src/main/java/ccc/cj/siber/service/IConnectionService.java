package ccc.cj.siber.service;

import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Result;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
public interface IConnectionService {
    Result testConnection(DataSourceInfo dataSourceInfo) throws Exception;


}
