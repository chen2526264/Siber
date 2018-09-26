package ccc.cj.siber.database.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjiong
 * @date 08/02/2018 19:46
 * 一张数据库表的基本信息
 */
public class Table {
    private String tableName;

    private List<Column> columns;

    private Boolean needAnalyse = true;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Boolean getNeedAnalyse() {
        return needAnalyse;
    }

    public void setNeedAnalyse(Boolean needAnalyse) {
        this.needAnalyse = needAnalyse;
    }

    public List<Column> fetchPks() {
        List<Column> result = new ArrayList<>();
        if (columns != null) {
            columns.forEach(column -> {
                if (column.getPk()) {
                    result.add(column);
                }
            });
        }
        return result;
    }

    public TableWeb convertToWeb(Integer datasourceId) {
        TableWeb result = new TableWeb();
        result.setDatasourceId(datasourceId);
        result.setTableName(tableName);
        result.setNeedAnalyse(needAnalyse);
        List<TableWeb.ColumnWeb> columnWebs = new ArrayList<>();
        columns.forEach(column -> columnWebs.add(column.convertToColumnWeb()));
        result.setColumns(columnWebs);
        return result;
    }
}
