package org.example;

public class HelloServiceImpl implements HelloService {
     public String sayHello(HelloObject object){
            // 返回一个包含 HelloObject 对象名称的问候语字符串
            return "Hello " + object.getName();
    }
}
