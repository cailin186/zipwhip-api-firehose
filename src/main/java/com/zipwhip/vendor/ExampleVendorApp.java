package com.zipwhip.vendor;

import com.zipwhip.api.dto.*;
import com.zipwhip.api.signals.LoggingSignalTokenProcessor;
import com.zipwhip.api.signals.Signal;
import com.zipwhip.api.signals.SignalTokenProcessor;
import com.zipwhip.concurrent.NetworkFuture;
import com.zipwhip.events.Observer;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.style.ToStringCreator;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 10/17/11
 * Time: 10:28 AM
 */
public class ExampleVendorApp implements InitializingBean {

    private static Logger LOGGER = Logger.getLogger(ExampleVendorApp.class);

    private static AsyncVendorClient client;

    private LoggingSignalTokenProcessor signalTokenProcessor;

    private String apiKey;
    private String apiSecret;

    /**
     * Spring will call this method automatically.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        verifyKey(apiKey);
        verifyKey(apiSecret);

        try {
            /**
             *   Here is an example of creating a AsyncVendorClient using a factory.
             *   This fa
             */
            client = AsyncVendorClientFactory.createViaApiKey(apiKey, apiSecret);

        } catch (Exception e) {
            LOGGER.fatal("Error creating client", e);
        }


        signalTokenProcessor.addObserver(new Observer<SignalToken>() {
            @Override
            public void notify(Object sender, SignalToken signalToken) {
                if (signalToken == null || CollectionUtil.isNullOrEmpty(signalToken.getSignals())){
                    return;
                }
                for (Signal signal : signalToken.getSignals()) {
                    if (StringUtil.equals(signal.getType(), "message")){

                        LOGGER.debug(StringUtil.join("Got a ", signal.getUri(), " signal."));

                    }
                }
            }
        });

    }

    private void verifyKey(String key) {
        if (StringUtil.isNullOrEmpty(key) || StringUtil.equals(key, "{your key here}")) {
            throw new NullPointerException("ApiKey and ApiSecret are both required fields.");
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public LoggingSignalTokenProcessor getSignalTokenProcessor() {
        return signalTokenProcessor;
    }

    public void setSignalTokenProcessor(LoggingSignalTokenProcessor signalTokenProcessor) {
        this.signalTokenProcessor = signalTokenProcessor;
    }
}
