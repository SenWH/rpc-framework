package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class HelloObject implements Serializable {
    private String name;
}
