package ccc.cj.siber.service;

import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.database.model.Status;
import ccc.cj.siber.database.model.TableCompare;

import java.util.List;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
public interface ICompareService {
    Result saveCurrentStatus(Integer datasourceId) throws Exception;

    /**
     * 对比指定数据源下的两次状态
     * @param datasourceId 数据源id
     * @param oldStatusId 旧状态id
     * @param newStatusId 新状态id
     * @return
     * @throws Exception
     */
    Result compare(Integer datasourceId, Integer oldStatusId, Integer newStatusId) throws Exception;

    /**
     * 获取最新的数据库表状态，并与先前一次保存的状态对比，并保存当前状态信息
     * @param datasourceId
     * @return
     */
    Result<List<TableCompare>> saveAndCompare(Integer datasourceId) throws Exception;

    Result<List<Status>> listStatus(Integer datasourceId) throws Exception;
}
