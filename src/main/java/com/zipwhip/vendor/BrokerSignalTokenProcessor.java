package com.zipwhip.vendor;

import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.api.signals.Signal;
import com.zipwhip.api.signals.SignalTokenProcessor;
import com.zipwhip.events.ObservableHelper;
import com.zipwhip.events.Observer;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 3:09 PM
 * <p/>
 * Processes signals in a parallel way.
 */
public class BrokerSignalTokenProcessor implements SignalTokenProcessor {

    private static final Logger LOGGER = Logger.getLogger(BrokerSignalTokenProcessor.class);

    final Map<String, ObservableHelper<SignalToken>> observables = new HashMap<String, ObservableHelper<SignalToken>>();
    final UriHelper uriHelper = new UriHelper();

    /**
     * These signalTokens come in from JMS consumers. The thread they come in on are the JMS Consumer threads
     * out of the DefaultMessageListenerContainer. If we block, it will slow down JMS consumption. If we throw
     * work over to another thread, it will consume another one from JMS. But that might consume too
     * fast and we'll run out of memory in the JVM.
     * <p/>
     * So let's process these SYNCHRONOUSLY. If you don't, you'll get backlog in this JVM and not
     * in the JMS Broker. You actually want it to backlog in the broker, so if you have to reboot your
     * JVM or reboot the broker, they wont be lost. The broker is persistent and stores them on disk.
     * Our JVM has finite resources, let's not overwhelm it.
     *
     * @param signalToken This came through the JMS system
     */
    @Override
    public void process(SignalToken signalToken) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received via JMS " + signalToken.toString());
        }

        // grab out the ObservableHelper object. This is a helper that does the work of
        // notifying all the observers when the event happens.
        Collection<ObservableHelper<SignalToken>> observables = getObservableForExecution(signalToken);

        if (CollectionUtil.isNullOrEmpty(observables)) {
            LOGGER.warn(StringUtil.join("The observable for ", signalToken.toString(), " was null. Did you intend to register this?"));
            return;
        }

        for (ObservableHelper<SignalToken> observable : observables) {
            // notify all the observers synchronously.
            // we dont want to consume from JMS faster than we can process the work!
            observable.notifyObservers(this, signalToken);
        }
    }


    public void unregister(String uri, Collection<Observer<SignalToken>> observers) {
        if (CollectionUtil.isNullOrEmpty(observers)) {
            return;
        }

        for (Observer<SignalToken> observer : observers) {
            unregister(uri, observer);
        }
    }

    public void unregister(String uri, Observer<SignalToken> observer) {
        if (StringUtil.isNullOrEmpty(uri)) {
            throw new NullPointerException("The URI cannot be null/empty");
        }
        if (observer == null) {
            throw new NullPointerException("The Observer cannot be null");
        }

        ObservableHelper<SignalToken> observable = getObservable(uri);

        if (observable == null) {
            // this is ok.
            return;
        }

        observable.removeObserver(observer);
    }

    /**
     * When a specific URI is processed, call these observers. NOTE: This is additive,
     * so if you call it repeatedly they will merge together.
     *
     * @param uri
     * @param observers
     */
    public void register(String uri, Collection<Observer<SignalToken>> observers) {
        if (CollectionUtil.isNullOrEmpty(observers)) {
            throw new NullPointerException("Observers collection cannot be null");
        }

        for (Observer<SignalToken> observer : observers) {
            register(uri, observer);
        }
    }

    public void register(String uri, Observer<SignalToken> observer) {
        ObservableHelper<SignalToken> observable = getObservable(uri);

        if (observable == null) {
            synchronized (observables) {
                observable = getObservable(uri);
                if (observable == null) {
                    observable = new ObservableHelper<SignalToken>();
                    observables.put(uri, observable);
                }
            }
        }

        observable.addObserver(observer);

    }

    /**
     * Get an observable that is scoped /signal, /signal/message, /signal/message/receive
     *
     * @param signalToken
     * @return
     */
    private Collection<ObservableHelper<SignalToken>> getObservableForExecution(SignalToken signalToken) {
        String uri = getSignalUri(signalToken);

        if (StringUtil.isNullOrEmpty(uri)) {
            throw new NullPointerException("The uri was null for " + signalToken.toString());
        }

        return getObservableForExecution(uri);
    }

    protected Collection<ObservableHelper<SignalToken>> getObservableForExecution(String uri) {
        if (StringUtil.isNullOrEmpty(uri)) {
            throw new NullPointerException("The uri must be defined");
        }

        Collection<String> uris = uriHelper.getParts(uri);
        Collection<ObservableHelper<SignalToken>> result = new LinkedList<ObservableHelper<SignalToken>>();
        for (String _uri : uris) {
            ObservableHelper<SignalToken> ob = getObservable(_uri);
            if (ob == null) {
                continue;
            }
            result.add(ob);
        }

        return result;
    }


    private ObservableHelper<SignalToken> getObservable(String uri) {
        if (StringUtil.isNullOrEmpty(uri)) {
            throw new NullPointerException("The uri must be defined");
        }

        return observables.get(uri);
    }

    private String getSignalUri(SignalToken signalToken) {
        if (signalToken == null) {
            throw new NullPointerException("The signalToken cannot be processed");
        }

        // It's the use case that all signals that are "bulked" are guaranteed to be the same URI.
        Signal signal = CollectionUtil.first(signalToken.getSignals());
        if (signal == null) {
            throw new NullPointerException("The signals cannot be null");
        }

        // let's pull out the signal.uri
        return signal.getUri();
    }
}
