package ccc.cj.siber.database.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjiong
 * @date 06/03/2018 17:56
 */
public class TableWeb {
    //datasource的id
    private Integer datasourceId;

    private String tableName;
    private List<ColumnWeb> columns;
    private Boolean needAnalyse = true;

    public Integer getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Integer datasourceId) {
        this.datasourceId = datasourceId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnWeb> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnWeb> columns) {
        this.columns = columns;
    }

    public Boolean getNeedAnalyse() {
        return needAnalyse;
    }

    public void setNeedAnalyse(Boolean needAnalyse) {
        this.needAnalyse = needAnalyse;
    }

    public Table convertToTable() {
        Table result = new Table();
        result.setTableName(tableName);
        result.setNeedAnalyse(needAnalyse);
        List<Column> columnsResults = new ArrayList<>();
        columns.forEach(column -> {
            columnsResults.add(column.convertToColumn());
        });

        result.setColumns(columnsResults);
        return result;
    }

    public static class ColumnWeb {
        private String columnName;
        private String type;
        private Boolean isPk;

        public ColumnWeb() {
        }

        public ColumnWeb(String columnName, Boolean isPk) {
            this.columnName = columnName;
            this.isPk = isPk;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public Boolean getPk() {
            return isPk;
        }

        public void setPk(Boolean pk) {
            isPk = pk;
        }

        public Column convertToColumn() {
            Column result = new Column();
            result.setColumnName(columnName);
            result.setPk(isPk);
            result.setType(type);
            return result;
        }
    }


}
