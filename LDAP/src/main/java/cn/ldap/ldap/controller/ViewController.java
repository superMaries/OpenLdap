package cn.ldap.ldap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @title: ViewController
 * @Author Wy
 * @Date: 2023/4/21 13:30
 * @Version 1.0
 */
@Controller
@RequestMapping("")
public class ViewController {
    @RequestMapping({"/index","/login"})
    public String index() {
        return "index";
    }

    @RequestMapping("")
    public String defaultIndex() {
        return "index";
    }
}
