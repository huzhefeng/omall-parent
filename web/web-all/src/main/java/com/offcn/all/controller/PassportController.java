package com.offcn.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PassportController {

    //跳转到登录页面
    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        //从request对象获取原始访问地址
        String originUrl = request.getParameter("originUrl");
        //把原始访问地址封装到request
        request.setAttribute("originUrl",originUrl);
        //跳转到登录页面
        return "login";
    }
}
