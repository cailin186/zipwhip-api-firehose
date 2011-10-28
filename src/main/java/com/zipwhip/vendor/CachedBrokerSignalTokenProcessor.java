package com.zipwhip.vendor;

import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.events.ObservableHelper;
import com.zipwhip.events.Observer;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 8:52 PM
 *
 * Caches the observable list, so you dont have to recaculate every time.
 */
public class CachedBrokerSignalTokenProcessor extends BrokerSignalTokenProcessor {

    final Map<String, Collection<ObservableHelper<SignalToken>>> observable_cache = new HashMap<String, Collection<ObservableHelper<SignalToken>>>();

    @Override
    public void unregister(String uri, Observer<SignalToken> observer) {
        synchronized (observable_cache) {
            super.unregister(uri, observer);

            observable_cache.clear();
        }
    }

    @Override
    public void register(String uri, Observer<SignalToken> observer) {
        synchronized (observable_cache) {
            super.register(uri, observer);

            observable_cache.clear();
        }
    }

    @Override
    protected Collection<ObservableHelper<SignalToken>> getObservableForExecution(String uri) {
        // check the cache first, that's the most efficient.
        // this is only possible because we have a FINITE amount of URI's that can be thrown.
        // If the URI had a user content (for example /signal/message/receive/FRIEND_MOBILE, then it
        // would crash the server, the hashmap would be too big!)
        Collection<ObservableHelper<SignalToken>> result = observable_cache.get(uri);

        // if it's null, we need to populate the cache
        if (CollectionUtil.isNullOrEmpty(result)) {
            synchronized (observable_cache) {
                result = observable_cache.get(uri);
                // check again, in case some jerk already did the work.
                if (!CollectionUtil.isNullOrEmpty(result)){
                    return result;
                }

                // split out the parts (also pre-cached)
                Collection<String> _uri = uriHelper.getParts(uri);
                if (CollectionUtil.isNullOrEmpty(_uri)){
                    // very weird... why would this be null??
                    return null;
                }
                // go thru each one and
                result = new LinkedList<ObservableHelper<SignalToken>>();
                for (String part : _uri) {
                    if (StringUtil.isNullOrEmpty(part)){
                        continue;
                    }

                    ObservableHelper<SignalToken> helper = observables.get(part);
                    if (helper == null){
                        continue;
                    }

                    result.add(helper);
                }

                // cache it, so we dont have to recaculate every time.
                observable_cache.put(uri, result);
            }
        }

        return result;
    }
}
