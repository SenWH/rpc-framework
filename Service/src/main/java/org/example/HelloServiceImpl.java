package org.example;

public class HelloServiceImpl implements HelloService {
     public String sayHello(HelloObject object){
         System.out.println(object);
         return "Hello " + object.getName();
    }
}
