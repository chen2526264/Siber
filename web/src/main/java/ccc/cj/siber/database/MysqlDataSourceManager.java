package ccc.cj.siber.database;

import ccc.cj.siber.database.model.Column;
import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Row;
import ccc.cj.siber.database.model.Table;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author chenjiong
 * @date 05/02/2018 16:58
 */

public class MysqlDataSourceManager extends AbstractDataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(MysqlDataSourceManager.class);

    public MysqlDataSourceManager(File dataSourceDir, DataSourceInfo dataSourceInfo) {
        super.dataSourceDir = dataSourceDir;
        super.dataSourceInfo = dataSourceInfo;

        BasicDataSource localDataSource = new BasicDataSource();
        localDataSource.setDriverClassName(dataSourceTypes.get(dataSourceInfo.getDataSourceType())); //数据库驱动
        localDataSource.setUsername(dataSourceInfo.getUserName());  //用户名
        localDataSource.setPassword(dataSourceInfo.getPassword());  //密码
        localDataSource.setUrl(dataSourceInfo.getUrl());  //连接url
        localDataSource.setInitialSize(10); // 初始的连接数；
        localDataSource.setMaxTotal(100);  //最大连接数
        localDataSource.setMaxIdle(80);  // 设置最大空闲连接
        localDataSource.setMaxWaitMillis(6000);  // 设置最大等待时间
        localDataSource.setMinIdle(10);  // 设置最小空闲连接

        dataSource = localDataSource;

        jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
    }

    @Override
    public List<Table> getTableInfos() {
        return dataSourceInfo.getTables();
    }

    @Override
    public List<Table> getLatestTableInfos() {
        Map<String, Table> tables = new LinkedHashMap<>();
        Map<String, List<String>> tablePkMapping = new HashMap<>();

        List<TablePk> tablePks = jdbcTemplate.query(tablePksSql(), new TablePkRowMapper());
        List<MysqlTableColumn> mysqlTableColumns = jdbcTemplate.query(tableColumnsSql(), new TableColumnRowMapper());

        tablePks.forEach(pk -> {
            String tableName = pk.getTableName();
            List<String> pks = tablePkMapping.computeIfAbsent(tableName, k -> new ArrayList<>());
            pks.add(pk.getPk());
        });

        mysqlTableColumns.forEach(column -> {
            String tableName = column.getTableName();
            Table table = tables.computeIfAbsent(tableName, k -> {
                Table newTable = new Table();
                newTable.setTableName(tableName);
                //并且如果有pk，则该表示可以对比分析的，如果没有pk，该表示不可对比分析的。
                if (tablePkMapping.get(tableName) != null && tablePkMapping.get(tableName).size() > 0) {
                    newTable.setNeedAnalyse(true);
                } else {
                    newTable.setNeedAnalyse(false);
                }
                newTable.setColumns(new ArrayList<>());
                return newTable;
            });
            table.getColumns().add(column.convertToColumn());

        });

        tables.forEach((key, value) -> {
            List<String> pks = tablePkMapping.get(key);
            List<Column> columns = value.getColumns();
            columns.forEach(column -> {
                if (pks != null && pks.contains(column.getColumnName())) {
                    column.setPk(true);
                } else {
                    column.setPk(false);
                }
            });
        });

        return new ArrayList<>(tables.values());
    }

    private String tablePksSql() {
        return "select " +
                "table_name as tableName," +
                "COLUMN_NAME as pk" +
                " from INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "where TABLE_SCHEMA = '" + getDataSourceInfo().getSchema() + "'" +
                "and CONSTRAINT_NAME = 'PRIMARY'" +
                "order by table_name , column_name;";
    }

    private String tableColumnsSql() {
        return "select table_name as tableName, column_name as columnName,DATA_TYPE as type from information_schema.columns where table_schema='"
                + getDataSourceInfo().getSchema() +
                "'";
    }

    @Override
    protected RowMapper<Row> getRowMapper(Table table) {
        return new DefaultRowMapper(table);
    }

    /**
     * @author chenjiong
     * @date 10/02/2018 20:02
     */
    private static class TablePk {
        private String tableName;
        private String pk;

        public TablePk(String tableName, String pk) {
            this.tableName = tableName;
            this.pk = pk;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getPk() {
            return pk;
        }

        public void setPk(String pk) {
            this.pk = pk;
        }
    }

    /**
     * @author chenjiong
     * @date 10/02/2018 20:02
     */
    private static class MysqlTableColumn {
        private String tableName;
        private String columnName;
        private String type;

        public MysqlTableColumn(String tableName, String columnName, String type) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.type = type;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
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

        public Column convertToColumn() {
            Column result = new Column();
            result.setColumnName(columnName);
            result.setType(type);
            return result;
        }
    }

    private class TablePkRowMapper implements RowMapper<TablePk> {
        @Override
        public TablePk mapRow(ResultSet rs, int rowNum) throws SQLException {
            String tableName = rs.getString("tableName");
            String pk = rs.getString("pk");
            return new TablePk(tableName, pk);
        }
    }

    private class TableColumnRowMapper implements RowMapper<MysqlTableColumn> {
        @Override
        public MysqlTableColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
            String tableName = rs.getString("tableName");
            String columnName = rs.getString("columnName");
            String type = rs.getString("type");
            return new MysqlTableColumn(tableName, columnName, type);
        }
    }
}
