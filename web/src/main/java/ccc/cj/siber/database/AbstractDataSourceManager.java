package ccc.cj.siber.database;

import ccc.cj.siber.database.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author chenjiong
 * @date 06/03/2018 11:25
 */
public abstract class AbstractDataSourceManager implements DataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDataSourceManager.class);


    protected JdbcTemplate jdbcTemplate;

    protected DataSourceInfo dataSourceInfo;

    protected DataSource dataSource;

    protected File dataSourceDir;


    @Override
    public DataSourceInfo getDataSourceInfo() {
        return dataSourceInfo;
    }

    public File getDataSourceItemDir() {
        return dataSourceDir;
    }

    @Override
    public Result testConnect() {
        try {
            jdbcTemplate.queryForObject("select 1;", String.class);
            return Result.success();
        } catch (Exception e) {
            logger.error("", e);
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Boolean close() {
        if (dataSource != null && dataSource instanceof BasicDataSource) {
            try {
                ((BasicDataSource) dataSource).close();
                return true;
            } catch (SQLException e) {
                logger.error("", e);

            }
        }
        return false;
    }

    @Override
    public Map<String, TableData> queryCurrentData() {
        Map<String, TableData> newTableData = new LinkedHashMap<>();
        getTableInfos().forEach(table -> {
            if (table.getNeedAnalyse()) {
                logger.info("query table " + table.getTableName() + " data");
                String sql = queryTableInfoSql(table);
                List<Row> data = jdbcTemplate.query(sql, getRowMapper(table));
                TableData tableData = new TableData();
                tableData.setTableName(table.getTableName());
                tableData.setRows(data);
                newTableData.put(table.getTableName(), tableData);
            }
        });
        logger.info("query table finish!");
        return newTableData;
    }


    private String queryTableInfoSql(Table table) {
        StringBuilder orderBy = new StringBuilder(" order by ");
        Iterator<Column> iterator = table.fetchPks().iterator();
        while (iterator.hasNext()) {
            Column column = iterator.next();
            if (column.getPk()) {
                orderBy.append(column.getColumnName());
                if (iterator.hasNext()) {
                    orderBy.append(",");
                }
            }
        }
        return "select * from " + table.getTableName() + orderBy.toString();
    }

    protected abstract RowMapper<Row> getRowMapper(Table table);


    protected class DefaultRowMapper implements RowMapper<Row> {
        private Table table;

        DefaultRowMapper(Table table) {
            this.table = table;
        }

        @Override
        public Row mapRow(ResultSet rs, int rowNum) throws SQLException {
            List<Column> columns = table.getColumns();
            List<Column> columnPks = table.fetchPks();

            String pks = writeCsv(rs, columnPks);
            String data = writeCsv(rs, columns);
            Row row = new Row();
            row.setPks(pks);
            row.setData(data);
            return row;
        }

        protected String writeCsv(ResultSet rs, List<Column> columns) {
            CSVFormat format = CSVFormat.DEFAULT;
            try (Writer out = new StringWriter(); CSVPrinter printer = new CSVPrinter(out, format)) {
                List<String> records = new ArrayList<>();
                for (Column column : columns) {
                    records.add(getValue(rs, column));
                }
                printer.printRecord(records);
                return out.toString();
            } catch (Exception e) {
                logger.error("", e);
                return "";
            }
        }

        protected String getValue(ResultSet rs, Column column) throws SQLException {
            return rs.getString(column.getColumnName());
        }
    }


}
