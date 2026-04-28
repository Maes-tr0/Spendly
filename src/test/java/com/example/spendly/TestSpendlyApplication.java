package com.example.spendly;

import org.springframework.boot.SpringApplication;

public class TestSpendlyApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpendlyApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
