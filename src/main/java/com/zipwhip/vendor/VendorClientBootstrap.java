package com.zipwhip.vendor;

import org.apache.log4j.BasicConfigurator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 10/10/11
 * Time: 10:55 AM
 *
 *
 */
public class VendorClientBootstrap {

    /**
     * Runs the application which is defined as the XML in the first parameter.
     *
     * @param args String example: vendor-client.xml
     */
    public static void main(String[] args) {

        // Configure basic console logging
//        BasicConfigurator.configure();

        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("vendor-client.xml");

        ctx.registerShutdownHook();
    }

}
