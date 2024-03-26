package org.example.backendhttpserver.controller;

import com.xiaobao.gateway.client.core.ApiInvoker;
import com.xiaobao.gateway.client.core.ApiProtocol;
import com.xiaobao.gateway.client.core.ApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiService(serviceId = "backend-http-server",protocol = ApiProtocol.HTTP,patternPath ="/http-demo/**" )
public class HelloController {
    @ApiInvoker(path = "")
    @GetMapping("/http-demo/ping")
    public String ping(){
        return "pong";
    }
}
