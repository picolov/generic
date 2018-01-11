package com.baswara.generic.client;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AuthorizedFeignClient(name = "uaa")
public interface AccountFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/account")
    public Map<String, Object> getAccount();

    @RequestMapping(method = RequestMethod.POST, value = "/api/account")
    public Map<String, Object> updateUser(Map<String, Object> userMap);

    @RequestMapping(method = RequestMethod.POST, value = "/api/account/change-password")
    public Map<String, Object> changePassword(String password);

    @RequestMapping(method = RequestMethod.POST, value = "/api/users")
    public Map<String, Object> createUser(Map<String, Object> userMap);

    @RequestMapping(method = RequestMethod.DELETE, value = "/api/users/{login}")
    public void createUser(@PathVariable("login") String login);

    @RequestMapping(method = RequestMethod.POST, value = "/api/register")
    public String registerUser(Map<String, Object> userMap);

}
