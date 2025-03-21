package org.example.Service.impl;

import org.example.POJO.HelloObject;
import org.example.Service.HelloService;

public class HelloServiceImpl implements HelloService {
     public String sayHello(HelloObject object){
         System.out.println(object);
         return "Hello " + object.getName();
    }
}
