package com.zipwhip.vendor;

import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: msmyers
 * Date: 10/27/11
 * Time: 8:43 PM
 *
 * This class makes it more efficient to work with a fixed set of scoped URI's. It's important to note
 * that it's a fixed number of possible options, because we use a HashMap internally. If you had an infinite
 * number of different URI's, then the HashMap would get SOO BIG that your server will absolutely crash.
 */
public class UriHelper {

    private final Map<String, Collection<String>> uri_cache = new HashMap<String, Collection<String>>();

    public void clear() {
        uri_cache.clear();
    }

    public Collection<String> getParts(String uri) {
        Collection<String> _uri = uri_cache.get(uri);

        if (CollectionUtil.isNullOrEmpty(_uri)){
            synchronized (uri_cache) {
                _uri = uri_cache.get(uri);
                if (CollectionUtil.isNullOrEmpty(_uri)){
                    // scope it
                    // TODO: test this
                    String[] parts = uri.split("/");
                    if (CollectionUtil.isNullOrEmpty(parts)) {
                        return null;
                    }

                    _uri = new ArrayList<String>(4);
                    StringBuilder sb = new StringBuilder("/");
                    for (String part : parts) {
                        if(StringUtil.isNullOrEmpty(part)) {
                            continue;
                        }
                        sb.append(part);
                        _uri.add(sb.toString());
                        sb.append("/");
                    }
                }
            }
        }

        return _uri;
    }

}
