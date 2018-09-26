package ccc.cj.siber.database.model;

import java.util.List;

/**
 * @author chenjiong
 * @date 10/02/2018 22:19
 */
public class TableCompare {
    private Table table;
    private List<CompareRow> compareRows;
    private List<String> sqls;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }


    public List<CompareRow> getCompareRows() {
        return compareRows;
    }

    public void setCompareRows(List<CompareRow> compareRows) {
        this.compareRows = compareRows;
    }

    public List<String> getSqls() {
        return sqls;
    }

    public void setSqls(List<String> sqls) {
        this.sqls = sqls;
    }

    public static class CompareRow {
        private List<CompareColumn> columns;

        public List<CompareColumn> getColumns() {
            return columns;
        }

        public void setColumns(List<CompareColumn> columns) {
            this.columns = columns;
        }
    }

    public static class CompareColumn {
        private String oldValue;
        private String newValue;
        private Integer status;

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
