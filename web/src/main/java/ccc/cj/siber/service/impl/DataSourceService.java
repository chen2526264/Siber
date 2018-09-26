package ccc.cj.siber.service.impl;

import ccc.cj.siber.database.model.*;
import ccc.cj.siber.service.IDataSourceService;
import ccc.cj.siber.util.Constant;
import ccc.cj.siber.util.MetaDataUtil;
import ccc.cj.siber.util.SiberFileUtils;
import ccc.cj.siber.util.Try;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author chenjiong
 * @date 10/02/2018 22:24
 */
@Service
public class DataSourceService implements IDataSourceService {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceService.class);

    private static final String DATASOURCE_INFO_FILE_NAME = "datasource.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    private String datasourcePath;

    private File metadataFile;

    private Metadata metadata;

    //当electron和jetty一起使用时，不会执行@PostConstruct注解的方法，所以初始化放在构造函数中。
    public DataSourceService() {
        logger.info("enter datasourceService");
        try {
            datasourcePath = Constant.DATA_PATH + File.separator + "datasource";
            //如果data/datasource目录不存在，则创建
            SiberFileUtils.mkdirIfNotExist(datasourcePath);

            //读取metadata.json
            metadataFile = MetaDataUtil.getMetadataFile(datasourcePath);
            if (metadataFile.exists()) {
                metadata = MetaDataUtil.parseMetadataFile(metadataFile);

                //对比datasource目录下的所有数据源的名字和metadata里的是否一致，如果不一致，需要更新metadata文件里的数据，
                //以防止文件夹被手动删除的情况
                MetaDataUtil.compareAndFresh(metadataFile, metadata, datasourcePath);
            } else {
                metadata = MetaDataUtil.initMetadata(metadataFile);
            }
            logger.info("metadata is " + objectMapper.writeValueAsString(metadata));
        } catch (Throwable t) {
            logger.error("", t);
        }
    }

    @Override
    public Result getDataSource(Integer id) throws Exception {
        Optional<Pair<File, DataSourceInfo>> datasource = doGetDataSource(id);
        return datasource
                .map(Pair::getSecond)
                .map(info -> {
                    if (info.getTables() != null) {
                        info.getTables().forEach(this::filterTable);
                    }
                    return info;
                })
                .map(Result::new)
                .orElse(new Result<>(Constant.FAILED_CODE, "Datasource does not exist which id is " + id + " !"));
    }


    public Optional<Pair<File, DataSourceInfo>> doGetDataSource(Integer id) throws Exception {
        if (id == null) {
            return Optional.empty();
        }
        File parent = new File(datasourcePath);
        File[] files = parent.listFiles();
        if (files != null) {
            for (File item : files) {
                if (item.isDirectory() && item.getName().startsWith(id + "_")) {
                    File baseInfoFile = new File(item, DATASOURCE_INFO_FILE_NAME);
                    if (baseInfoFile.exists()) {
                        String baseInfo = FileUtils.readFileToString(baseInfoFile, Constant.CHARSET);
                        DataSourceInfo dataSourceInfo = objectMapper.readValue(baseInfo, DataSourceInfo.class);
                        if (id.equals(dataSourceInfo.getId())) {
                            return Optional.of(new Pair<>(item, dataSourceInfo));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void filterTable(Table table) {
        table.setColumns(null);
        table.setNeedAnalyse(null);
    }

    @Override
    public Result getAllDataSource() throws IOException {
        File datasourceDir = new File(datasourcePath);
        File[] files = datasourceDir.listFiles();
        if (files == null) {
            return new Result(Constant.FAILED_CODE, "Datasource directory does not exist!");
        }
        List<DataSourceInfo> dataSourceInfos = new ArrayList<>();
        for (File itemDir : files) {
            if (itemDir.isDirectory()) {
                File baseInfoFile = new File(itemDir, DATASOURCE_INFO_FILE_NAME);
                if (baseInfoFile.exists()) {
                    String baseInfo = FileUtils.readFileToString(baseInfoFile, Constant.CHARSET);
                    DataSourceInfo dataSourceInfo = objectMapper.readValue(baseInfo, DataSourceInfo.class);
                    if (dataSourceInfo.getTables() != null) {
                        dataSourceInfo.getTables().forEach(this::filterTable);
                    }
                    dataSourceInfos.add(dataSourceInfo);
                }
            }
        }
        Collections.sort(dataSourceInfos);
        return new Result<>(dataSourceInfos);
    }

    @Override
    public Result addDataSource(DataSourceInfo dataSourceInfo) throws Exception {
        String datasourceName = dataSourceInfo.getDataSourceName();

        //如果该datasourceName已经存在，返回失败，否则创建该目录
        String itemName = MetaDataUtil.newDir(metadata, datasourceName);
        if (itemName == null) {
            return new Result(Constant.FAILED_CODE, "Datasource " + datasourceName + " already exist!");
        }
        Integer id = metadata.getMaxId();

        //创建该目录
        String itemPath = datasourcePath + File.separator + itemName;
        SiberFileUtils.mkdirIfNotExist(itemPath);

        //创建该datasource的基础信息文件datasource.json,并写入内容
        File baseInfoFile = new File(itemPath + File.separator + DATASOURCE_INFO_FILE_NAME);
        dataSourceInfo.setId(id);
        String baseInfo = objectMapper.writeValueAsString(dataSourceInfo);
        FileUtils.writeStringToFile(baseInfoFile, baseInfo, Constant.CHARSET);

        //刷新metadata文件
        MetaDataUtil.saveNewDir(metadataFile, metadata, datasourceName);

        return Result.success();
    }

    @Override
    public Result deleteDataSource(Integer id) throws Exception {
        Optional<Pair<File, DataSourceInfo>> datasource = doGetDataSource(id);
        return datasource
                .map(Try.function(pair -> {
                    //删除该数据源的目录
                    FileUtils.deleteDirectory(pair.getFirst());
                    //从metadata中删除该数据源名字
                    MetaDataUtil.removeDir(metadataFile, metadata, pair.getSecond().getDataSourceName());
                    return Result.success();
                }))
                .orElse(new Result<>(Constant.FAILED_CODE, "Datasource does not exist which id is " + id + " !"));
    }

    @Override
    public Result updateDataSource(DataSourceInfo dataSourceInfo) throws Exception {
        if (dataSourceInfo == null) {
            return new Result(Constant.FAILED_CODE, "DataSourceInfo is null!");
        }
        Optional<Pair<File, DataSourceInfo>> datasource = doGetDataSource(dataSourceInfo.getId());
        return datasource
                .map(Try.function(pair -> {
                    File baseInfoFile = new File(pair.getFirst(), DATASOURCE_INFO_FILE_NAME);
                    if (dataSourceInfo.getTables() == null) {
                        dataSourceInfo.setTables(pair.getSecond().getTables());
                    }
                    String baseInfo = objectMapper.writeValueAsString(dataSourceInfo);
                    FileUtils.writeStringToFile(baseInfoFile, baseInfo, Constant.CHARSET);
                    if (!dataSourceInfo.getDataSourceName().equals(pair.getSecond().getDataSourceName())) {
                        MetaDataUtil.updateDir(metadataFile, metadata, pair.getSecond().getDataSourceName(), dataSourceInfo.getDataSourceName());
                    }
                    return Result.success();
                }))
                .orElse(new Result<>(Constant.FAILED_CODE, "Datasource does not exist which id is " + dataSourceInfo.getId() + " !"));
    }


}
