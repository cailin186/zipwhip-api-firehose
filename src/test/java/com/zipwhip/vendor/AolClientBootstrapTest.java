package com.zipwhip.vendor;

import com.zipwhip.api.Address;
import com.zipwhip.api.dto.Message;
import com.zipwhip.api.dto.SignalToken;
import com.zipwhip.api.signals.LoggingSignalTokenProcessor;
import com.zipwhip.api.signals.Signal;
import com.zipwhip.concurrent.NetworkFuture;
import com.zipwhip.events.Observer;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.StringUtil;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 10/10/11
 * Time: 10:59 AM
 */
public class AolClientBootstrapTest {

    private static final Logger LOGGER = Logger.getLogger(AolClientBootstrapTest.class);

    private static ApplicationContext context;
    private static AsyncVendorClient client;

    private static final String friend = "3134147502";
    private static final String admin = "9139802972";

    private static final String apiKey = "";
    private static final String secret = "";

    @BeforeClass
    public static void beforeClass() throws Exception {
        context = new ClassPathXmlApplicationContext("vendor-client.xml");

        client = AsyncVendorClientFactory.createViaApiKey(apiKey, secret);
    }

    @Test
    public void testEnroll() throws Exception {

        NetworkFuture<?> future = client.enrollUser(admin);

        future.await(10, TimeUnit.SECONDS);

        assertTrue(future.isSuccess());



    }

    @Test
    public void testListMessages() throws Exception {
        NetworkFuture<List<Message>> future = client.listMessages(admin);

        future.awaitUninterruptibly();

        assertTrue(future.isSuccess());
        assertTrue(!CollectionUtil.isNullOrEmpty(future.getResult()));

    }

    @Test
    public void testSendMessage() throws Exception {

        NetworkFuture<?> future = client.sendMessage(admin, friend, "This is a test!");

        future.await(10, TimeUnit.SECONDS);

        assertTrue(future.isSuccess());

    }

    @Test
    public void testReceiveMessage() throws Exception {

        // this processor allows for observing traffic.
        LoggingSignalTokenProcessor processor = context.getBean(LoggingSignalTokenProcessor.class);

        final Message[] message = {null};
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<SignalToken> observer = new Observer<SignalToken>() {
            @Override
            public void notify(Object o, SignalToken signalToken) {

                if (!StringUtil.equals(signalToken.getMobileNumber(), admin)) {
                    return;
                }

                if (CollectionUtil.isNullOrEmpty(signalToken.getSignals())) {
                    return;
                }

                for (Signal signal : signalToken.getSignals()) {
                    if (!StringUtil.equalsIgnoreCase(signal.getType(), "message") || !StringUtil.equalsIgnoreCase(signal.getEvent(), "receive")) {
                        continue;
                    }
                    // this is a firehose, so all accounts would hit this code.
                    // let's filter out messages that were not to our admin account

                    message[0] = (Message) signal.getContent();

                    LOGGER.debug("received message! " + message[0].getBody());

                    latch.countDown();
                }
            }
        };

        processor.addObserver(observer);

        latch.await(3, TimeUnit.MINUTES);

        assertNotNull(message[0]);

        processor.removeObserver(observer);


    }
}
