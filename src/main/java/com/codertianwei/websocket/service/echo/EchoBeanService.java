package com.codertianwei.websocket.service.echo;

import com.codertianwei.websocket.service.RequestBean;
import com.codertianwei.websocket.service.WebsocketCommand;
import com.codertianwei.websocket.service.WebsocketService;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebsocketService("echo")
@RestController
public class EchoBeanService {
    private static final Logger logger = LogManager.getLogger(EchoBeanService.class);

    @Bean("echoBean")
    public EchoBean getEchoBean() {
        return new EchoBean();
    }

    @ApiOperation(value = "echo", notes = "")
    @PostMapping("/echo.echo")
    @RequestBean("echoBean")
    @WebsocketCommand("echo")
    public EchoBean doService(@RequestBody EchoBean content) {
        return content;
    }
}
