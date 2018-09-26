package ccc.cj.siber.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "test")
@Api(value = "test", description = "test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @ApiOperation(value = "Search a product with an ID", response = Object.class)
    @RequestMapping(method = RequestMethod.GET)
    public Object test() {
        logger.info("/test [GET]");
        try {
            return "success";
        } catch (Throwable t) {
            logger.error("", t);
        }
        return null;
    }
}
