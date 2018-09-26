package ccc.cj.siber.database.model;

/**
 * @author chenjiong
 * @date 20/03/2018 18:59
 */
public class Column {
    private String columnName;
    private String type;
    //是否是PK
    private Boolean pk;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getPk() {
        return pk;
    }

    public void setPk(Boolean pk) {
        this.pk = pk;
    }

    public TableWeb.ColumnWeb convertToColumnWeb() {
        TableWeb.ColumnWeb result = new TableWeb.ColumnWeb();
        result.setColumnName(columnName);
        result.setType(type);
        result.setPk(pk);
        return result;
    }

}
