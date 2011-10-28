package com.zipwhip.vendor;

import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.events.Observer;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 3:45 PM
 *
 *
 */
public class SpringBrokerSignalTokenProcessorAdapter implements InitializingBean {

    BrokerSignalTokenProcessor brokerSignalTokenProcessor;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (brokerSignalTokenProcessor == null){
            throw new NullPointerException("You need to set the brokerSignalTokenProcessor property");
        }

        if (CollectionUtil.isNullOrEmpty(observers)){
            return;
        }

        // convert a getter/setter into registrations
        for (String uri : observers.keySet()) {
            if (StringUtil.isNullOrEmpty(uri)){
                continue;
            }

            brokerSignalTokenProcessor.register(uri, observers.get(uri));
        }
    }

    Map<String, List<Observer<SignalToken>>> observers;

    public Map<String, List<Observer<SignalToken>>> getObservers() {
        return observers;
    }

    public void setObservers(Map<String, List<Observer<SignalToken>>> observers) {
        this.observers = observers;
    }

    public BrokerSignalTokenProcessor getBrokerSignalTokenProcessor() {
        return brokerSignalTokenProcessor;
    }

    public void setBrokerSignalTokenProcessor(BrokerSignalTokenProcessor brokerSignalTokenProcessor) {
        this.brokerSignalTokenProcessor = brokerSignalTokenProcessor;
    }
}
