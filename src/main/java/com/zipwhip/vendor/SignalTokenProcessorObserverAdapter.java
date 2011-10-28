package com.zipwhip.vendor;

import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.api.signals.SignalTokenProcessor;
import com.zipwhip.events.Observable;
import com.zipwhip.events.Observer;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 9:04 PM
 *
 * Adapts between the two interfaces
 */
public class SignalTokenProcessorObserverAdapter implements SignalTokenProcessor, Observer<SignalToken> {

    SignalTokenProcessor signalTokenProcessor;

    @Override
    public void notify(Object sender, SignalToken item) {
        signalTokenProcessor.process(item);
    }

    @Override
    public void process(SignalToken signalToken) {
        signalTokenProcessor.process(signalToken);
    }

    public SignalTokenProcessor getSignalTokenProcessor() {
        return signalTokenProcessor;
    }

    public void setSignalTokenProcessor(SignalTokenProcessor signalTokenProcessor) {
        this.signalTokenProcessor = signalTokenProcessor;
    }
}
