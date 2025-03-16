package org.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class RpcRequest implements Serializable {
    private String serviceName ;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
}
