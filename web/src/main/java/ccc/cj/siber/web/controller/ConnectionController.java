package ccc.cj.siber.web.controller;

import ccc.cj.siber.database.model.DataSourceInfo;
import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.service.IConnectionService;
import ccc.cj.siber.util.Try;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "connection")
@Api(description = "连接相关接口")
public class ConnectionController {


    @Autowired
    private IConnectionService connectionService;

    @ApiOperation(value = "连接测试", httpMethod = "POST", response = Result.class)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public Result testConnection(@RequestBody DataSourceInfo dataSourceInfo) {
//        logger.info("/getDataSource id is " + id);
        return Try.supplier(() -> connectionService.testConnection(dataSourceInfo));
    }

}
