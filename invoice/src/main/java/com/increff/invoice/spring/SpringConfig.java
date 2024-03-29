package com.increff.invoice.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan("com.increff.invoice")
@PropertySources({@PropertySource(value = "classpath:./invoice.properties", ignoreResourceNotFound = true)})
public class SpringConfig {
}
