package ccc.cj.siber.web.controller;

import ccc.cj.siber.database.DataSourceManager;
import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.service.IDataSourceService;
import ccc.cj.siber.service.IDataSourceManagerService;
import ccc.cj.siber.util.Try;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping(value = "datasource")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(description = "数据源相关接口")
public class DataSourceController {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    private IDataSourceService datasourceService;
    @Autowired
    private IDataSourceManagerService datasourceManagerService;

    @ApiOperation(value = "根据id获取数据源", httpMethod = "GET", response = Result.class)
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Result getDatasource(@RequestParam(value = "id") Integer id) {
        return Try.supplier(() -> datasourceService.getDataSource(id));
    }

    @ApiOperation(value = "获取所有数据库类型", httpMethod = "GET", response = Result.class)
    @RequestMapping(value = "/types", method = RequestMethod.GET, produces = "application/json")
    public Result getDatasourceTypes() {
        return new Result<>(new ArrayList<>(DataSourceManager.dataSourceTypes.keySet()));
    }

    @ApiOperation(value = "获取所有数据源", httpMethod = "GET", response = Result.class)
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "application/json")
    public Result getAllDatasource() {
        return Try.supplier(() -> datasourceService.getAllDataSource());
    }

    @ApiOperation(value = "添加数据源", httpMethod = "POST", response = Result.class)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public Result addDatasource(@RequestBody DataSourceInfo input) {
        logger.info("/addDataSource [post]");
        return Try.supplier(() -> datasourceService.addDataSource(input));
    }

    @ApiOperation(value = "修改数据源", httpMethod = "PUT", response = Result.class)
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    public Result updateDatasource(@RequestBody DataSourceInfo input) {
//        logger.info("/updateDataSource [put]");
        return Try.supplier(() -> {
            datasourceManagerService.remove(input.getId());
            return datasourceService.updateDataSource(input);
        });
    }

    @ApiOperation(value = "根据id删除数据源", httpMethod = "DELETE", response = Result.class)
    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
    public Result deleteDatasource(@RequestParam(value = "id") Integer id) {
//        logger.info("/deleteDataSource id is " + id);
        return Try.supplier(() -> datasourceService.deleteDataSource(id));
    }


}
