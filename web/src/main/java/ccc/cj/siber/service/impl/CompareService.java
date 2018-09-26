package ccc.cj.siber.service.impl;

import ccc.cj.siber.database.DataSourceManager;
import ccc.cj.siber.database.model.*;
import ccc.cj.siber.service.ICompareService;
import ccc.cj.siber.service.IDataSourceManagerService;
import ccc.cj.siber.service.IDataSourceService;
import ccc.cj.siber.util.Constant;
import ccc.cj.siber.util.MetaDataUtil;
import ccc.cj.siber.util.SiberFileUtils;
import ccc.cj.siber.util.Try;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
@Service
public class CompareService implements ICompareService {
    private static final Logger logger = LoggerFactory.getLogger(CompareService.class);
    private static final String STATUS_FILE_NAME = "status.json";
    private static final Integer MAX_DIR_AMOUNT = 10;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Integer NOT_CHANGED = 0;
    private static final Integer CHANGED = 1;
    private static final Integer NEW = 2;
    private static final Integer DELETE = 3;

    @Autowired
    private IDataSourceManagerService datasourceManagerService;
    @Autowired
    private IDataSourceService dataSourceService;

    //最新的一次保存的状态
    private OldStatus latestStatus;

    @Override
    public Result saveCurrentStatus(Integer datasourceId) throws Exception {
        Optional<DataSourceManager> dataSourceManager = datasourceManagerService.get(datasourceId);
        return dataSourceManager
                .map(Try.function(this::doSaveCurrentStatus))
                .orElse(Result.fail("该数据源连接不成功,请检查数据源配置信息!"));
    }

    private Result doSaveCurrentStatus(DataSourceManager manager) throws Exception {
//        List<Table> tableInfos = manager.getTableInfos();
//        JdbcTemplate jdbcTemplate = manager.getJdbcTemplate();
        File dataSourceItemDir = manager.getDataSourceItemDir();

        //获得该数据源最新的表数据
        Map<String, TableData> latestData = manager.queryCurrentData();

        //获得该数据源的metadata文件，如果不存在则创建
        Metadata metadata;
        File metadataFile = MetaDataUtil.getMetadataFile(dataSourceItemDir);
        if (metadataFile.exists()) {
            metadata = MetaDataUtil.parseMetadataFile(metadataFile);
            MetaDataUtil.compareAndFresh(metadataFile, metadata, dataSourceItemDir);
        } else {
            metadata = MetaDataUtil.initMetadata(metadataFile);
        }

        //获取当前时间作为数据状态的名字
        String statusDirName = dtf.format(LocalDateTime.now());

        String statusDirStr = MetaDataUtil.newDir(metadata, statusDirName);
        if (statusDirStr == null) {
            return new Result(Constant.FAILED_CODE, "状态名 " + statusDirName + " 已经存在!");
        }
        File statusDir = new File(dataSourceItemDir, statusDirStr);
        SiberFileUtils.mkdirIfNotExist(statusDir);
        //创建该状态的数据文件,并写入内容
        File statusFile = new File(statusDir, STATUS_FILE_NAME);
        String statusInfo = objectMapper.writeValueAsString(latestData);
        FileUtils.writeStringToFile(statusFile, statusInfo, Constant.CHARSET);

        //刷新metadata文件
        MetaDataUtil.saveNewDir(metadataFile, metadata, statusDirName);

        while (metadata.getDirNames().size() > MAX_DIR_AMOUNT) {
            //如果当前状态目录的个数大于最大个数，需要删除最早的一个。
            String dirToBeDelete = MetaDataUtil.deleteFirst(metadataFile, metadata, dataSourceItemDir);
            if (dirToBeDelete != null) {
                FileUtils.deleteDirectory(new File(dataSourceItemDir, dirToBeDelete));
            }
        }
        OldStatus latestStatus = new OldStatus();
        latestStatus.setDatasourceId(manager.getDataSourceInfo().getId());
        latestStatus.setTableData(latestData);
        latestStatus.setStatusId(metadata.getMaxId());
        this.latestStatus = latestStatus;

        return Result.success();
    }

    @Override
    public Result compare(Integer datasourceId, Integer oldStatusId, Integer newStatusId) throws Exception {
        Optional<Pair<File, DataSourceInfo>> fileDataSourceInfoPair = dataSourceService.doGetDataSource(datasourceId);
        return fileDataSourceInfoPair.map(Try.function((Pair<File, DataSourceInfo> pair) -> {
            File datasourceItemDir = pair.getFirst();
            Map<String, TableData> oldStatus = getStatus(datasourceId, oldStatusId, datasourceItemDir);
            Map<String, TableData> newStatus = getStatus(datasourceId, newStatusId, datasourceItemDir);
            if (oldStatus == null || newStatus == null) {
                return Result.fail(String.format("ID为%s的状态信息不存在!", oldStatusId == null ? null : newStatusId));
            }
            List<Table> tables = pair.getSecond().getTables();
            return doCompare(tables, oldStatus, newStatus);
        })).orElse(Result.fail("Datasource does not exist which id is " + datasourceId + " !"));
    }

    private Map<String, TableData> getStatus(Integer datasourceId, Integer statusId, File datasourceItemDir) throws IOException {
        if (latestStatus != null) {
            if (datasourceId.equals(latestStatus.getDatasourceId())
                    && statusId.equals(latestStatus.getStatusId())) {
                return latestStatus.getTableData();
            }
        }
        File[] allStatus = datasourceItemDir.listFiles();
        if (allStatus != null) {
            for (File status : allStatus) {
                if (status.isDirectory() && status.getName().startsWith(statusId + "_")) {
                    File statusFile = new File(status, STATUS_FILE_NAME);
                    if (statusFile.exists()) {
                        String statusInfo = FileUtils.readFileToString(statusFile, Constant.CHARSET);
                        JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(LinkedHashMap.class, Map.class, String.class, TableData.class);
                        return objectMapper.readValue(statusInfo, javaType);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Result saveAndCompare(Integer datasourceId) throws Exception {
        Map<String, TableData> oldStatus;
        if (latestStatus != null && datasourceId.equals(latestStatus.getDatasourceId())) {
            oldStatus = latestStatus.getTableData();
        } else {
            Result<List<Status>> status = listStatus(datasourceId);
            List<Status> result = status.getResult();
            Integer maxId = -1;
            String name = "";
            for (Status s : result) {
                if (s.getId() > maxId) {
                    maxId = s.getId();
                    name = s.getName();
                }
            }
            if (maxId == -1) {
                return Result.fail("该数据源没有以保存的状态！");
            }
            oldStatus = getStatus(datasourceId, maxId + "_" + name);
        }
        if (oldStatus == null) {
            return Result.fail("该数据源没有以保存的状态！");
        }
        Result result = saveCurrentStatus(datasourceId);
        if (!result.getCode().equals(Constant.SUCCESS_CODE)) {
            return Result.fail("获取数据源最新状态失败！");
        }
        Map<String, TableData> newStatus = latestStatus.getTableData();

        return doCompare(dataSourceService.doGetDataSource(datasourceId).get().getSecond().getTables()
                , oldStatus, newStatus);
    }

    private Map<String, TableData> getStatus(Integer datasourceId, String statusDirName) throws Exception {
        Optional<Pair<File, DataSourceInfo>> datasource = dataSourceService.doGetDataSource(datasourceId);
        if (!datasource.isPresent()) {
            return null;
        }
        File datasourceDir = datasource.get().getFirst();
        File statusDirFile = new File(datasourceDir, statusDirName);
        File statusFile = new File(statusDirFile, STATUS_FILE_NAME);
        String statusInfo = FileUtils.readFileToString(statusFile, Constant.CHARSET);
        JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(LinkedHashMap.class, Map.class, String.class, TableData.class);
        return objectMapper.readValue(statusInfo, javaType);
    }

    @Override
    public Result<List<Status>> listStatus(Integer datasourceId) throws Exception {
        List<Status> statuses = new ArrayList<>();

        Optional<Pair<File, DataSourceInfo>> datasource = dataSourceService.doGetDataSource(datasourceId);
        if (!datasource.isPresent()) {
            return Result.fail("Datasource does not exist which id is " + datasourceId + " !");
        }

        File datasourceDir = datasource.get().getFirst();
        File[] files = datasourceDir.listFiles();
        if (files != null) {
            for (File itemDir : files) {
                if (itemDir.isDirectory()) {
                    File statusFile = new File(itemDir, STATUS_FILE_NAME);
                    if (statusFile.exists()) {
                        String statusDirName = itemDir.getName();
                        int firstUnderlineIndex = statusDirName.indexOf("_");
                        String id = statusDirName.substring(0, firstUnderlineIndex);
                        if (!StringUtils.isNumeric(id)) {
                            continue;
                        }
                        String name = statusDirName.substring(firstUnderlineIndex + 1);
                        Status status = new Status(datasourceId, Integer.valueOf(id), name);
                        statuses.add(status);
                    }
                }
            }
        }
        Collections.sort(statuses);
        return new Result<>(statuses);
    }

    private Result<List<TableCompare>> doCompare(List<Table> tables,
                                                 Map<String, TableData> oldTableData,
                                                 Map<String, TableData> newTableData) throws JsonProcessingException {
        List<TableCompare> tableCompares = new ArrayList<>();
//        tables.parallelStream().forEach(table -> {
        tables.stream()
                .filter(Table::getNeedAnalyse)//过滤掉不需要对比分析的表
                .forEach((Table table) -> {
                    logger.info("begin compare, table name is " + table.getTableName());
                    TableCompare tableCompare = new TableCompare();
                    tableCompare.setTable(table);
                    List<TableCompare.CompareRow> compareRows = new ArrayList<>();
                    List<String> sqls = new ArrayList<>();

                    List<Row> oldRows = oldTableData.get(table.getTableName()).getRows();
                    List<Row> newRows = newTableData.get(table.getTableName()).getRows();
                    String[] csvHeader = generateCsvHeader(table);
                    int i = 0;
                    int j = 0;
                    while (i < oldRows.size() && j < newRows.size()) {
                        String oldPks = oldRows.get(i).getPks();
                        String oldData = oldRows.get(i).getData();

                        String newPks = newRows.get(j).getPks();
                        String newData = newRows.get(j).getData();

                        if (oldPks.equals(newPks)) {
                            //第一种情况，内容有变更
                            if (!oldData.equals(newData)) {
                                List<Integer> changedIndex = new ArrayList<>();
                                String compareStr = oldData + System.getProperty("line.separator") + newData;
//                                logger.info("compareStr is " + compareStr);
                                try (CSVParser parser = CSVParser.parse(compareStr, CSVFormat.DEFAULT.withHeader(csvHeader))) {
                                    List<TableCompare.CompareColumn> compareColumns = new ArrayList<>();
                                    List<CSVRecord> records = parser.getRecords();
                                    CSVRecord oldRecord = records.get(0);
                                    CSVRecord newRecord = records.get(1);
                                    for (int k = 0; k < oldRecord.size(); k++) {
                                        TableCompare.CompareColumn compareColumn = new TableCompare.CompareColumn();
                                        compareColumn.setOldValue(oldRecord.get(k));
                                        compareColumn.setNewValue(newRecord.get(k));
                                        if (oldRecord.get(k).equals(newRecord.get(k))) {
                                            compareColumn.setStatus(NOT_CHANGED);
                                        } else {
                                            compareColumn.setStatus(CHANGED);
                                            changedIndex.add(k);
                                        }
                                        compareColumns.add(compareColumn);
                                    }
                                    TableCompare.CompareRow compareRow = new TableCompare.CompareRow();
                                    compareRow.setColumns(compareColumns);
                                    compareRows.add(compareRow);
                                    String updateSql = generateUpdateSql(table, newRecord, changedIndex);
                                    sqls.add(updateSql);
                                } catch (Exception e) {
                                    logger.error("", e);
                                }
                            }
                            //第四种情况，数据没有变化,什么都不做。
                            i++;
                            j++;
                        } else {
                            //第二种情况，内容被删除
                            if (oldPks.compareTo(newPks) < 0) {
                                Pair<TableCompare.CompareRow, String> comparePair = getCompareRow(table, oldData, csvHeader, DELETE);
                                compareRows.add(comparePair.getFirst());
                                sqls.add(comparePair.getSecond());
                                i++;
                            } else {//第三种情况，新增内容
                                Pair<TableCompare.CompareRow, String> comparePair = getCompareRow(table, newData, csvHeader, NEW);
                                compareRows.add(comparePair.getFirst());
                                sqls.add(comparePair.getSecond());
                                j++;
                            }
                        }
                    }

                    if (i < oldRows.size()) {
                        while (i < oldRows.size()) {
                            Pair<TableCompare.CompareRow, String> comparePair = getCompareRow(table, oldRows.get(i).getData(), csvHeader, DELETE);
                            compareRows.add(comparePair.getFirst());
                            sqls.add(comparePair.getSecond());
                            i++;
                        }
                    } else if (j < newRows.size()) {
                        while (j < newRows.size()) {
                            Pair<TableCompare.CompareRow, String> comparePair = getCompareRow(table, newRows.get(j).getData(), csvHeader, NEW);
                            compareRows.add(comparePair.getFirst());
                            sqls.add(comparePair.getSecond());
                            j++;
                        }
                    }
                    tableCompare.setCompareRows(compareRows);
                    tableCompare.setSqls(sqls);

                    if (!compareRows.isEmpty()) {
                        tableCompares.add(tableCompare);
                    }
                    logger.info("finish compare, table name is " + table.getTableName());
                });
        Result<List<TableCompare>> result = new Result<>(tableCompares);
        logger.info("compare result is " + new ObjectMapper().writeValueAsString(result));
        return result;
    }

    private String[] generateCsvHeader(Table table) {
        List<String> headers = new ArrayList<>();
        table.getColumns().forEach(column -> {
            headers.add(column.getColumnName());
        });
        return headers.toArray(new String[headers.size()]);
    }

    private String generateUpdateSql(Table table, CSVRecord newRecord, List<Integer> changedIndex) {
        List<Column> columns = table.getColumns();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(table.getTableName());
        sb.append(" SET ");
        changedIndex.forEach(index -> {
            sb.append(" ");
            sb.append(columns.get(index).getColumnName());
            sb.append("=");
            sb.append("\"").append(newRecord.get(index)).append("\"");
            sb.append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE");

        Iterator<Column> iterator = table.fetchPks().iterator();
        while (iterator.hasNext()) {
            Column column = iterator.next();
            sb.append(" ");
            sb.append(column.getColumnName());
            sb.append("=");
            sb.append("\"").append(newRecord.get(column.getColumnName())).append("\"");
            if (iterator.hasNext()) {
                sb.append(" AND");
            }
        }

        sb.append(";");
        return sb.toString();
    }

    private Pair<TableCompare.CompareRow, String> getCompareRow(Table table, String data, String[] csvHeader, Integer status) {
        TableCompare.CompareRow compareRow = new TableCompare.CompareRow();
        String sql = "";
        try (CSVParser parser = CSVParser.parse(data, CSVFormat.RFC4180.withHeader(csvHeader))) {
            List<TableCompare.CompareColumn> compareColumns = new ArrayList<>();
            List<CSVRecord> records = parser.getRecords();
            CSVRecord record = records.get(0);
            for (int k = 0; k < record.size(); k++) {
                TableCompare.CompareColumn compareColumn = new TableCompare.CompareColumn();
                if (status.equals(NEW)) {
                    compareColumn.setOldValue("");
                    compareColumn.setNewValue(record.get(k));
                } else if (status.equals(DELETE)) {
                    compareColumn.setOldValue(record.get(k));
                    compareColumn.setNewValue("");
                }
                compareColumn.setStatus(status);
                compareColumns.add(compareColumn);
            }

            if (status.equals(NEW)) {
                sql = generateInsertSql(table, record);
            } else if (status.equals(DELETE)) {
                sql = generateDeleteSql(table, record);
            }

            compareRow.setColumns(compareColumns);
        } catch (Exception e) {
            logger.error("", e);
        }
        return new Pair<>(compareRow, sql);
    }

    private String generateDeleteSql(Table table, CSVRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(table.getTableName());
        sb.append(" WHERE ");

        Iterator<Column> iterator = table.fetchPks().iterator();
        while (iterator.hasNext()) {
            Column pk = iterator.next();
            sb.append(pk.getColumnName());
            sb.append("=");
            sb.append("\"").append(record.get(pk.getColumnName())).append("\"");
            if (iterator.hasNext()) {
                sb.append(" AND ");
            }
        }
        sb.append(";");
        return sb.toString();
    }

    private String generateInsertSql(Table table, CSVRecord record) {
        List<Column> columns = table.getColumns();

        StringBuilder sb = new StringBuilder();
        StringBuilder columnNames = new StringBuilder();
        StringBuilder values = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(table.getTableName());
        columnNames.append(" (");
        values.append(" (");

        columns.forEach(column -> {
            columnNames.append(column.getColumnName());
            columnNames.append(",");
            values.append("\"").append(record.get(column.getColumnName())).append("\"");
            values.append(",");
        });

        columnNames.deleteCharAt(columnNames.length() - 1);
        values.deleteCharAt(values.length() - 1);
        columnNames.append(")");
        values.append(")");
        sb.append(columnNames);
        sb.append(" VALUES ");
        sb.append(values);
        sb.append(";");
        return sb.toString();
    }

    private class OldStatus {
        private Integer datasourceId;
        private Integer statusId;
        private Map<String, TableData> tableData;

        public Integer getDatasourceId() {
            return datasourceId;
        }

        public void setDatasourceId(Integer datasourceId) {
            this.datasourceId = datasourceId;
        }

        Integer getStatusId() {
            return statusId;
        }

        void setStatusId(Integer statusId) {
            this.statusId = statusId;
        }

        Map<String, TableData> getTableData() {
            return tableData;
        }

        void setTableData(Map<String, TableData> tableData) {
            this.tableData = tableData;
        }
    }
}
