package ccc.cj.siber.web.controller;

import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.service.ICompareService;
import ccc.cj.siber.util.Try;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "compare")
@Api(description = "数据对比相关接口")
public class CompareController {
    private static final Logger logger = LoggerFactory.getLogger(CompareController.class);


    @Autowired
    private ICompareService compareService;

    @ApiOperation(value = "获得所有状态", httpMethod = "GET", response = Result.class)
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json")
    public Result status(@RequestParam(value = "id") Integer id) {
        return Try.supplier(() -> compareService.listStatus(id));
    }

    @ApiOperation(value = "保存当前状态", httpMethod = "POST", response = Result.class)
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json")
    public Result saveCurrentStatus(@RequestParam(value = "id") Integer id) {
        return Try.supplier(() -> compareService.saveCurrentStatus(id));
    }

    @ApiOperation(value = "对比", httpMethod = "POST", response = Result.class)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public Result compare(@RequestParam(value = "datasourceId") Integer datasourceId,
                          @RequestParam(value = "oldStatusId") Integer oldStatusId,
                          @RequestParam(value = "newStatusId") Integer newStatusId) {
        return Try.supplier(() -> compareService.compare(datasourceId, oldStatusId, newStatusId));
    }

    @ApiOperation(value = "保存当前状态，并与上一次保存的状态对比", httpMethod = "POST", response = Result.class)
    @RequestMapping(value = "/saveAndCompare", method = RequestMethod.POST, produces = "application/json")
    public Result saveAndCompare(@RequestParam(value = "datasourceId") Integer datasourceId) {
        return Try.supplier(() -> compareService.saveAndCompare(datasourceId));
    }

}
