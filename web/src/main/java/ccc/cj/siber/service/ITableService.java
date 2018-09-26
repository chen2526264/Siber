package ccc.cj.siber.service;

import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.database.model.Table;
import ccc.cj.siber.database.model.TableWeb;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
public interface ITableService {
    Result refresh(Integer id) throws Exception;

    Result update(TableWeb tableWeb) throws Exception;

    Result getTable(Integer datasourceId, String tableName) throws Exception;
}
