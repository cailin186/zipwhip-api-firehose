package com.zipwhip.vendor.observers;

import com.zipwhip.api.dto.Message;
import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.api.signals.Signal;
import com.zipwhip.events.Observer;
import com.zipwhip.util.StringUtil;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 3:39 PM
 *
 * A special class setup to handle inbound messages. This is a class that the Vendor would want to modify.
 *
 * Our strategy of handling traffic is to do so horizontally. We're going to have a new class per signal type
 * that way our code can be small/targeted rather than a huge cumbersome IF-statement.
 */
public class MessageReceivedSignalTokenObserver implements Observer<SignalToken> {

    private static final Logger LOGGER = Logger.getLogger(MessageReceivedSignalTokenObserver.class);

    /**
     * This is the main entry point. We implement this interface to listen for /signal/message/receive.
     *
     * NOTE: it's important to have your Spring config setup correctly, we can't verify that we're wired
     * up correctly via code, need to do so yourself.
     *
     * @param sender The object that is telling us about this event
     * @param signalToken The payload of the event
     */
    @Override
    public void notify(Object sender, SignalToken signalToken) {

        // step 1. access the database for user
        String mobileNumber = signalToken.getMobileNumber();

        // step 2. hit the database
        /// AolSubscriber subscriber = getDataLayerFor(mobileNumber);

        // step 3. do some dirty work
        for (Signal signal : signalToken.getSignals()) {
            // Do the work here that you want to do when a message signal comes in. (the casting was for expressiveness)
            Message message = (Message) signal.getContent();


            if (StringUtil.equals(message.getMessageType(), "SY")) {
                LOGGER.debug("Received a system message, going to ignore it?");
                return;
            } else {
                LOGGER.debug("Received a new message! " + message.toString());
            }

            String body = message.getBody();
            String friend = message.getMobileNumber();

        }
    }

}
