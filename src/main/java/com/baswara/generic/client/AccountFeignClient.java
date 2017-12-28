package com.baswara.generic.client;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AuthorizedFeignClient(name = "uaa")
public interface AccountFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/account")
    public Map<String, Object> getAccount();

//    @RequestMapping(method = RequestMethod.PUT, value = "/api/demo/string/{pathArg}")
//    public String setString(@PathVariable("pathArg") String pathArg);
}
