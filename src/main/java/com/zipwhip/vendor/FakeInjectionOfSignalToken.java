package com.zipwhip.vendor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 10/12/11
 * Time: 3:58 PM
 */
public class FakeInjectionOfSignalToken implements InitializingBean {

    ConnectionFactory connectionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        JmsTemplate template = new JmsTemplate(connectionFactory);

        template.convertAndSend("/vendor/aol", "{mobileNumber:'', deviceAddress:'', signals:[{}], subscriptionEntry: {}}");
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
