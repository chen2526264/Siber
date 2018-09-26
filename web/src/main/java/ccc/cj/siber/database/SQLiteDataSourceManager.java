package ccc.cj.siber.database;

import ccc.cj.siber.database.model.Column;
import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Row;
import ccc.cj.siber.database.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjiong
 * @date 05/02/2018 16:58
 */

public class SQLiteDataSourceManager extends AbstractDataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteDataSourceManager.class);

    public SQLiteDataSourceManager(File dataSourceDir, DataSourceInfo dataSourceInfo) {
        super.dataSourceDir = dataSourceDir;
        super.dataSourceInfo = dataSourceInfo;


        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl(dataSourceInfo.getUrl());

        dataSource = sqLiteDataSource;
        jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
    }


    @Override
    public List<Table> getTableInfos() {
        return dataSourceInfo.getTables();
    }

    @Override
    public List<Table> getLatestTableInfos() {
        List<Table> result = new ArrayList<>();
        List<String> tableNames = getTableNames();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                List<SQLiteColumnInfo> tableInfos = getSQLiteTableInfos(tableName);
                List<Column> columns = new ArrayList<>();
                Table table = new Table();
                table.setTableName(tableName);
                table.setNeedAnalyse(false);
                tableInfos.forEach(info -> {
                    Column column = info.convertToColumn();
                    columns.add(column);
                    if (column.getPk()) {
                        table.setNeedAnalyse(true);
                    }
                });
                table.setColumns(columns);
                result.add(table);
            }
        }
        return result;
    }

    private List<String> getTableNames() {
        String sql = "select name from sqlite_master where type='table' order by name;";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private List<SQLiteColumnInfo> getSQLiteTableInfos(String tableName) {
        String sql = "pragma table_info ('" + tableName + "')";
        return jdbcTemplate.query(sql, new ColumnInfoMapper());
    }

    @Override
    protected RowMapper<Row> getRowMapper(Table table) {
        return new DefaultRowMapper(table);
    }

    private class SQLiteColumnInfo {
        private String name;
        private String type;
        private Integer pk;

        public SQLiteColumnInfo(String name, Integer pk, String type) {
            this.name = name;
            this.pk = pk;
            this.type = type;
        }

        public Column convertToColumn() {
            Column result = new Column();
            result.setColumnName(name);
            result.setType(type);
            if (1 == pk) {
                result.setPk(true);
            } else {
                result.setPk(false);
            }
            return result;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getPk() {

            return pk;
        }

        public void setPk(Integer pk) {
            this.pk = pk;
        }
    }

    private class ColumnInfoMapper implements RowMapper<SQLiteColumnInfo> {
        @Override
        public SQLiteColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            String columnName = rs.getString("name");
            Integer pk = rs.getInt("pk");
            String type = rs.getString("type");

            return new SQLiteColumnInfo(columnName, pk, type);
        }
    }
}
