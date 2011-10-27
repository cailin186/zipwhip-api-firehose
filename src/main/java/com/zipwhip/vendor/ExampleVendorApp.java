package com.zipwhip.vendor;

import com.zipwhip.api.dto.Contact;
import com.zipwhip.api.dto.EnrollmentResult;
import com.zipwhip.api.dto.MessageToken;
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

    private AsyncVendorClient client;

    private String customerDeviceAddress;
    private String friendDeviceAddress;
    private String apiKey;
    private String apiSecret;

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

        enrollAndSendMessage();
    }

    public void enrollAndSendMessage() {

        /*
            Here is an example of enrolling a user. The argument is basically the user's mobile number.
            Zipwhip has a concept of device addresses which are formatted like 'deivce:/5555555555/0'.
            This call is pretty forgiving in that it will build the device address for you if you
            pass in a mobile number. Still it is good practice to get used to using the device address.

            Also note that this call is idempotent, meaning that if a user is already enrolled, additional
            calls to enrollUser will have no effect, but neither will they throw an exception.
         */
        NetworkFuture<EnrollmentResult> enrollFuture = client.enrollUser(customerDeviceAddress);

        /*
            If you want to do a blocking call on the result do this.
         */
        enrollFuture.awaitUninterruptibly();

        if (enrollFuture.isSuccess()) {
            EnrollmentResult result = enrollFuture.getResult();

            log("Enrollment", result);

            sendMessage();
        } else {
            LOGGER.error("Unable to enroll successfully", enrollFuture.getCause());
        }

    }

    /**
     * Demonstrate sending a message via the API. The to/from are defined in the spring xml file.
     */
    private void sendMessage() {

        NetworkFuture<List<MessageToken>> messageFuture = client.sendMessage(customerDeviceAddress, Collections.singleton(friendDeviceAddress), "Hello World!");

        messageFuture.addObserver(new Observer<NetworkFuture<List<MessageToken>>>() {
            @Override
            public void notify(Object o, NetworkFuture<List<MessageToken>> future) {
                /**
                 *   Here out result is a list of MessageToken objects. You will get one token per recipient of the message.
                 *   The tokens provide a way for you to track the message.
                 */
                if (future.isSuccess()) {

                    log("Message we sent", CollectionUtil.first(future.getResult()));

                    listContacts();
                } else {
                    LOGGER.error("Unable to read messages for account", future.getCause());
                }
            }
        });

    }

    private void listContacts() {

        /**
         *   Let's get a user's contact list.
         */
        NetworkFuture<List<Contact>> contactListFuture = client.listContacts(customerDeviceAddress);

        contactListFuture.addObserver(new Observer<NetworkFuture<List<Contact>>>() {
            @Override
            public void notify(Object o, NetworkFuture<List<Contact>> future) {
                if (future.isSuccess()) {

                    LOGGER.debug("=== Listing contacts for account ===");
                    int index = 1;
                    for (Contact contact : future.getResult()) {
                        log(String.valueOf(index), contact);
                        index++;
                    }
                } else {
                    LOGGER.error("Problem loading contacts", future.getCause());
                }
            }
        });
    }

    private void log(String prefix, EnrollmentResult result) {
        ToStringCreator c = new ToStringCreator(result);

        c.append("deviceNumber", result.getDeviceNumber());
        c.append("carbonEnabled", result.isCarbonEnabled());
        c.append("carbonInstalled", result.isCarbonInstalled());

        if (StringUtil.exists(prefix)) {
            LOGGER.debug(prefix + ": " + c.toString());
        } else {
            LOGGER.debug(c.toString());
        }
    }

    private void log(String prefix, Contact contact) {

        ToStringCreator c = new ToStringCreator(contact);

        c.append("address", contact.getAddress());
        c.append("deviceId", contact.getDeviceId());

        if (StringUtil.exists(prefix)) {
            LOGGER.debug(prefix + ": " + c.toString());
        } else {
            LOGGER.debug(c.toString());
        }

    }

    private void log(String prefix, MessageToken token) {
        ToStringCreator c = new ToStringCreator(token);

        c.append("contactId", token.getContactId());
        c.append("deviceId", token.getDeviceId());
        c.append("message", token.getMessage());

        if (StringUtil.exists(prefix)) {
            LOGGER.debug(prefix + ": " + c.toString());
        } else {
            LOGGER.debug(c.toString());
        }
    }

    private void verifyKey(String key) {
        if (StringUtil.isNullOrEmpty(key) || StringUtil.equals(key, "{your key here}")) {
            throw new NullPointerException("ApiKey and ApiSecret are both required fields.");
        }
    }

    public String getCustomerDeviceAddress() {
        return customerDeviceAddress;
    }

    public void setCustomerDeviceAddress(String customerDeviceAddress) {
        this.customerDeviceAddress = customerDeviceAddress;
    }

    public String getFriendDeviceAddress() {
        return friendDeviceAddress;
    }

    public void setFriendDeviceAddress(String friendDeviceAddress) {
        this.friendDeviceAddress = friendDeviceAddress;
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

}
