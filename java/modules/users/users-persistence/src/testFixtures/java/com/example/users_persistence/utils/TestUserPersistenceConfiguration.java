package com.example.users_persistence.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.example.users_persistence.repository")
@Import(TestUserPersistence.class)
public class TestUserPersistenceConfiguration {
}
