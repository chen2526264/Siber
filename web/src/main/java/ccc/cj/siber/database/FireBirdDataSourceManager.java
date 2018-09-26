package ccc.cj.siber.database;

import ccc.cj.siber.database.model.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenjiong
 * @date 05/02/2018 16:58
 */

public class FireBirdDataSourceManager extends AbstractDataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(FireBirdDataSourceManager.class);

    public FireBirdDataSourceManager(File dataSourceDir, DataSourceInfo dataSourceInfo) {
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
        List<Table> result = new ArrayList<>();
        List<String> tableNames = getTableNames();
        if (tableNames != null) {
            for (String tableName : tableNames) {
                List<String> pks = getFireBirdPks(tableName);
                List<FireBirdColumnInfo> fireBirdColumns = getFireBirdColumns(tableName);
                List<Column> columns = new ArrayList<>();

                fireBirdColumns.forEach(fireBirdColumn -> {
                    Column column = fireBirdColumn.convertToColumn();
                    String columnName = column.getColumnName();
                    if (pks.contains(columnName)) {
                        column.setPk(true);
                    } else {
                        column.setPk(false);
                    }
                    columns.add(column);
                });
                Table table = new Table();
                table.setTableName(tableName);
                table.setColumns(columns);
                if (pks != null && pks.size() != 0) {
                    table.setNeedAnalyse(true);
                } else {
                    table.setNeedAnalyse(false);
                }
                result.add(table);
            }
        }
        return result;
    }

    private List<String> getFireBirdPks(String tableName) {
        String sql = "SELECT S.RDB$FIELD_NAME AS COLUMN_NAME " +
                "FROM RDB$RELATION_CONSTRAINTS RC " +
                "LEFT JOIN RDB$INDICES I ON " +
                "   (I.RDB$INDEX_NAME = RC.RDB$INDEX_NAME) " +
                "LEFT JOIN RDB$INDEX_SEGMENTS S ON " +
                "   (S.RDB$INDEX_NAME = I.RDB$INDEX_NAME) " +
                "WHERE (RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY') " +
                "AND (I.RDB$RELATION_NAME = '" + tableName + "');";

        List<String> pks = jdbcTemplate.queryForList(sql, String.class);
        List<String> result = new ArrayList<>();
        if (pks != null) {
            pks.forEach(e -> {
                result.add(e.trim());
            });
        }
        return result;
    }

    private List<FireBirdColumnInfo> getFireBirdColumns(String tableName) {
        logger.info("getFireBirdColumns tableName is " + tableName);
        String sql = "SELECT RF.RDB$FIELD_NAME AS name ,T.RDB$TYPE_NAME AS TYPE " +
                "FROM RDB$RELATION_FIELDS RF " +
                "LEFT JOIN RDB$FIELDS F ON (F.RDB$FIELD_NAME = RF.RDB$FIELD_SOURCE) " +
                "LEFT JOIN RDB$TYPES T ON (T.RDB$TYPE = F.RDB$FIELD_TYPE) " +
                "LEFT JOIN RDB$CHARACTER_SETS CS ON (CS.RDB$CHARACTER_SET_ID = F.RDB$CHARACTER_SET_ID) " +
                "WHERE T.RDB$FIELD_NAME = 'RDB$FIELD_TYPE' AND " +
                "RF.RDB$RELATION_NAME = '" + tableName + "'" +
                "ORDER BY RF.RDB$FIELD_POSITION;";

        return jdbcTemplate.query(sql, new ColumnInfoMapper());
    }

    private List<String> getTableNames() {
        String sql = "SELECT RDB$RELATION_NAME AS TABLE_NAME " +
                "FROM RDB$RELATIONS " +
                "WHERE RDB$SYSTEM_FLAG = 0 AND  " +
                "RDB$VIEW_SOURCE IS NULL  " +
                "ORDER BY TABLE_NAME;";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);
        List<String> result = new ArrayList<>();
        if (tableNames != null) {
            tableNames.forEach(e -> {
                result.add(e.trim());
            });
        }
        return result;
    }

    @Override
    public Result testConnect() {
        try {
            String sql = "select MON$DATABASE_NAME from mon$database;";
            String databaseName = jdbcTemplate.queryForObject(sql, String.class);
            if (StringUtils.isNotBlank(databaseName)) {
                return Result.success();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return Result.fail("连接数据源失败，请检查数据源配置信息！");
    }

    @Override
    protected RowMapper<Row> getRowMapper(Table table) {
        return new FireBirdRowMapper(table);
    }


    private class ColumnInfoMapper implements RowMapper<FireBirdColumnInfo> {
        @Override
        public FireBirdColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            String columnName = rs.getString("name").trim();
            String type = rs.getString("type").trim();

            return new FireBirdColumnInfo(columnName, null, type);
        }
    }

    private class FireBirdColumnInfo {
        private String name;
        private String type;
        private Boolean pk;

        public FireBirdColumnInfo(String name, Boolean pk, String type) {
            this.name = name;
            this.pk = pk;
            this.type = type;
        }

        public Column convertToColumn() {
            Column result = new Column();
            result.setColumnName(name);
            result.setType(type);
            result.setPk(pk);
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

        public Boolean getPk() {
            return pk;
        }

        public void setPk(Boolean pk) {
            this.pk = pk;
        }
    }

    protected class FireBirdRowMapper extends DefaultRowMapper {
        FireBirdRowMapper(Table table) {
            super(table);
        }

        @Override
        public String getValue(ResultSet rs, Column column) throws SQLException {
            String value = "";
            if (column.getType().equals("VARYING") || column.getType().equals("BLOB")) {
                try {
                    byte[] bytes = rs.getBytes(column.getColumnName());
                    if (bytes != null) {
                        value = new String(bytes, "GBK");
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("", e);
                }
            } else {
                value = rs.getString(column.getColumnName());
            }
            return value;
        }

//        @Override
//        public String combine(ResultSet rs, List<Column> columns) throws SQLException {
//            StringBuilder result = new StringBuilder();
//            Iterator<Column> iterator = columns.iterator();
//            while (iterator.hasNext()) {
//                Column next = iterator.next();
////                logger.info("column name is " + next);
//
//                String value = "";
//                if (next.getType().equals("VARYING")
//                        || next.getType().equals("BLOB")) {
//                    try {
//                        byte[] bytes = rs.getBytes(next.getColumnName());
//                        if (bytes != null) {
//                            value = new String(bytes, "GBK");
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        logger.error("", e);
//                    }
//                } else {
//                    value = rs.getString(next.getColumnName());
//                }
//
//                if (value != null) {
//                    if (value.contains("\"")) {
//                        value = value.replace("\"", "\"\"");
//                    }
//                    if (value.contains(",")) {
//                        value = "\"" + value + "\"";
//                    }
//                }
//                result.append(value);
//                if (iterator.hasNext()) {
//                    result.append(",");
//                }
//            }
//            return result.toString();
//        }
    }

}
