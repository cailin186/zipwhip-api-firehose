package com.zipwhip.vendor;

import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.api.response.JsonSignalTokenParser;
import com.zipwhip.api.signals.SignalTokenProcessor;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.Parser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 10/12/11
 * Time: 2:47 PM
 *
 * Shows how to parse values from JMS.
 */
public class ExampleJmsListener implements InitializingBean {

    private static final Logger LOGGER = Logger.getLogger(ExampleJmsListener.class);

    private Parser<String, SignalToken> parser = new JsonSignalTokenParser();
    private SignalTokenProcessor signalTokenProcessor;

    /**
     * This is the JSON from Zipwhip servers. It is of the form "Signal Token":
     *
     * {
     *      deviceAddress: String,
     *      subscriptionEntry: {
     *          encodedSubscriptionSettings: String,
     *          signalFilters: String,
     *          subscriptionId: long,
     *          subscriptionKey: String,
     *          deviceId: long
     *      },
     *      signals: [{
     *          event: String,
     *          id: String,
     *          scope: String,
     *          content: Object
     *          type: String,
     *          reason: String,
     *          uuid: String
     *      }]
     * }
     *
     * @param json A string to be parsed into json content.
     * @exception Exception If an error occurs processing the token.
     */
    public void onMessage(String json) throws Exception {

        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("Inbound token: " + json);
        }

        SignalToken signalToken = parser.parse(json);

        if (!isValidToken(signalToken)){
            LOGGER.warn("The token was not valid, so we are aborting");
        } else {
            LOGGER.info("The token was valid, continuing parsing");
        }


        signalTokenProcessor.process(signalToken);

    }

    private boolean isValidToken(SignalToken signalToken) {
        if (signalToken == null){
            return false;
        }
        if (signalToken.getSubscriptionEntry() == null){
            return false;
        }
        if (CollectionUtil.isNullOrEmpty(signalToken.getSignals())) {
            return false;
        }
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (signalTokenProcessor == null){
            throw new NullPointerException("The signalTokenProcessor is a required field");
        }
    }

    public SignalTokenProcessor getSignalTokenProcessor() {
        return signalTokenProcessor;
    }

    public void setSignalTokenProcessor(SignalTokenProcessor signalTokenProcessor) {
        this.signalTokenProcessor = signalTokenProcessor;
    }

}
