package mqttclient.impl;

import com.google.common.collect.ImmutableMap;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.ISession;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ako on 12-8-2016.
 */
public class MxMqttCallback implements MqttCallbackExtended {
    private ILogNode logger = null;
    private MqttClient client = null;
    private HashMap<String, MqttSubscription> subscriptions = null;

    public MxMqttCallback(ILogNode logger, MqttClient client, HashMap<String, MqttSubscription> subscriptions) {
        this.logger = logger;
        this.client = client;
        this.subscriptions = subscriptions;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.info(String.format("connectionLost: %s, %s", throwable.getMessage(), client.getClientId()));
        logger.warn(throwable);
        try {
            client.connect();
        } catch (MqttException e) {
            logger.error(e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            logger.info(String.format("messageArrived: %s, %s, %s", topic, new String(mqttMessage.getPayload()), client.getClientId()));
            IContext ctx = Core.createSystemContext();
            ISession session = ctx.getSession();
            MqttSubscription subscription = getSubscriptionForTopic(topic);
            if (subscription != null) {
                String microflow = subscription.getOnMessageMicroflow();
                logger.info(String.format("Calling onMessage microflow: %s, %s", microflow, client.getClientId()));
                final ImmutableMap map = ImmutableMap.of("Topic", topic, "Payload", new String(mqttMessage.getPayload()));
                logger.info("Parameter map: " + map);
                //Core.execute(ctx, microflow, true, map);
                Core.executeAsync(ctx, microflow, true, map);
            } else {
                logger.error(String.format("Cannot find microflow for message received on topic %s", topic));
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info(String.format("deliveryComplete: %s", client.getClientId()));
    }

    /**
     * find possibly wildcarded subscription for specific topic
     */
    private MqttSubscription getSubscriptionForTopic(String topic) {

        logger.info("getSubscriptionForTopic: " + topic);
        Iterator<String> subscriptionTopics = subscriptions.keySet().iterator();
        while (subscriptionTopics.hasNext()) {
            String topicWithWildcards = subscriptionTopics.next();
            String topicWithWildcardsRe = topicWithWildcards.replaceAll("\\+", "[^/]+").replaceAll("/#", "\\(|/.*\\)");
            logger.info(String.format("Comparing topic %s with subscription %s as regex %s", topic, topicWithWildcards, topicWithWildcardsRe));
            if (topic.matches(topicWithWildcardsRe)) {
                logger.info("Found subscription " + topicWithWildcards);
                return subscriptions.get(topicWithWildcards);
            }
        }
        logger.info("No subscription found for topic " + topic);
        return null;
    }

    @Override
    public void connectComplete(boolean isReconnect, String serverUri) {
        logger.info(String.format("connectComplete %s, %s", isReconnect, serverUri));
        this.subscriptions.forEach((topic, subs) -> {
            try {
                logger.info(String.format("Resubscribing microflow %s to topic %s (%s)", subs.getOnMessageMicroflow(), topic, subs.getTopic()));
                client.subscribe(topic, 1);
            } catch (MqttException e) {
                logger.error(String.format("Reconnect failed for topic %s: %s", topic, e.getMessage()));
            }
        });
    }
}
