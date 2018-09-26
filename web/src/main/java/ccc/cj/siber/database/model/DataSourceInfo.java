package ccc.cj.siber.database.model;

import java.util.List;

/**
 * @author chenjiong
 * @date 09/02/2018 17:06
 */
public class DataSourceInfo implements Comparable<DataSourceInfo> {
    private Integer id;
    private String dataSourceName;
    private String dataSourceType;
//    private String driverName;
    private String userName;
    private String password;
    private String url;
    private String schema;
    private List<Table> tables;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

//    public String getDriverName() {
//        return driverName;
//    }

//    public void setDriverName(String driverName) {
//        this.driverName = driverName;
//    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    @Override
    public int compareTo(DataSourceInfo o) {
        if (this.getId() < o.getId()) {
            return -1;
        } else if (this.getId().equals(o.getId())) {
            return 0;
        } else {
            return 1;
        }
    }
}
