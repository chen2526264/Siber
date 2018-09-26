package ccc.cj.siber.database.model;

import java.util.List;

/**
 * @author chenjiong
 * @date 10/02/2018 22:19
 */
public class TableData {
//    private Table table;
    private String TableName;
    private List<Row> rows;

//    public Table getTable() {
//        return table;
//    }
//
//    public void setTable(Table table) {
//        this.table = table;
//    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}
