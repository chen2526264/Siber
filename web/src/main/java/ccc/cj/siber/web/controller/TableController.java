package ccc.cj.siber.web.controller;

import ccc.cj.siber.database.model.Result;
import ccc.cj.siber.database.model.TableWeb;
import ccc.cj.siber.service.ITableService;
import ccc.cj.siber.util.Try;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "table")
@Api(description = "数据表相关接口")
public class TableController {
    private static final Logger logger = LoggerFactory.getLogger(TableController.class);

    @Autowired
    private ITableService tableService;

    @ApiOperation(value = "根据id获取数据源", httpMethod = "GET", response = Result.class)
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Result getDatasource(@RequestParam(value = "datasourceId") Integer datasourceId,
                                @RequestParam(value = "tableName") String tableName) {
        return Try.supplier(() -> tableService.getTable(datasourceId, tableName));
    }

    @ApiOperation(value = "刷新数据源表信息", httpMethod = "POST", response = Result.class)
    @RequestMapping(value = "/refresh", method = RequestMethod.POST, produces = "application/json")
    public Result refresh(@RequestParam(value = "id") Integer id) {
        return Try.supplier(() -> tableService.refresh(id));
    }

    @ApiOperation(value = "修改数据源表信息", httpMethod = "POST", response = Result.class)
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
    public Result update(@RequestBody TableWeb tableWeb) {
        logger.info("/TableController update[post]");
        return Try.supplier(() -> tableService.update(tableWeb));
    }

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = "{\n" +
                "  \"columns\": [\n" +
                "    {\n" +
                "      \"columnName\": \"string\",\n" +
                "      \"pk\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"datasourceId\": 0,\n" +
                "  \"needAnalyse\": true,\n" +
                "  \"tableName\": \"string\"\n" +
                "}";
        try {
            TableWeb tableWeb = objectMapper.readValue(str, TableWeb.class);
            System.out.println(tableWeb);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
