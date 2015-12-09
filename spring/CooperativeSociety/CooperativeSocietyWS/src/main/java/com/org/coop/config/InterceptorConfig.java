/**
 * This class registers any pre/post interceptor into MVC life-cycle 
 * so any incoming request will be passed through these registered interceptor 
 */
package com.org.coop.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.org.coop.authentication.RestEndpointInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages="com.org.coop")
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RestEndpointInterceptor());
    }

}
