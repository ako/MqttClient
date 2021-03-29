package mqttclient.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;

/**
 * Created by ako on 12-8-2016.
 */
public class MxMqttCallback implements MqttCallbackExtended {
    private MqttClient client = null;
    private HashMap<String, MqttSubscription> subscriptions = null;

    protected MxMqttCallback(MqttClient client, HashMap<String, MqttSubscription> subscriptions) {
        this.client = client;
        this.subscriptions = subscriptions;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        MqttConnector._logger.info(String.format("connectionLost: %s, %s", throwable.getMessage(), this.client.getClientId()));
        MqttConnector._logger.warn(throwable);
        try {
            this.client.connect();
        } catch (MqttException e) {
            MqttConnector._logger.error(e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            MqttConnector._logger.info(String.format("messageArrived: %s, %s, %s", topic, new String(mqttMessage.getPayload()), this.client.getClientId()));
            IContext ctx = Core.createSystemContext();
//            ISession session = ctx.getSession();
            MqttSubscription subscription = getSubscriptionForTopic(topic);
            if (subscription != null) {
                String microflow = subscription.getOnMessageMicroflow();
                MqttConnector._logger.info(String.format("Calling onMessage microflow: %s, %s", microflow, this.client.getClientId()));
                final Map map = Map.of("Topic", topic, "Payload", new String(mqttMessage.getPayload()));
                MqttConnector._logger.info("Parameter map: " + map);
                Core.executeAsync(ctx, microflow, true, map);
            } else {
                MqttConnector._logger.error(String.format("Cannot find microflow for message received on topic %s", topic));
            }
        } catch (Exception e) {
            MqttConnector._logger.error(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        MqttConnector._logger.info(String.format("deliveryComplete: %s", this.client.getClientId()));
    }

    /**
     * find possibly wildcarded subscription for specific topic
     */
    private MqttSubscription getSubscriptionForTopic(String topic) {

        MqttConnector._logger.info("getSubscriptionForTopic: " + topic);
        Iterator<String> subscriptionTopics = this.subscriptions.keySet().iterator();
        while (subscriptionTopics.hasNext()) {
            String topicWithWildcards = subscriptionTopics.next();
            String topicWithWildcardsRe = topicWithWildcards.replaceAll("\\+", "[^/]+").replaceAll("/#", "\\(|/.*\\)");
            MqttConnector._logger.info(String.format("Comparing topic %s with subscription %s as regex %s", topic, topicWithWildcards, topicWithWildcardsRe));
            if (topic.matches(topicWithWildcardsRe)) {
                MqttConnector._logger.info("Found subscription " + topicWithWildcards);
                return this.subscriptions.get(topicWithWildcards);
            }
        }
        MqttConnector._logger.info("No subscription found for topic " + topic);
        return null;
    }

    @Override
    public void connectComplete(boolean isReconnect, String serverUri) {
        MqttConnector._logger.info(String.format("connectComplete %s, %s", isReconnect, serverUri));
        this.subscriptions.forEach((topic, subs) -> {
            try {
                MqttConnector._logger.info(String.format("Resubscribing microflow %s to topic %s (%s)", subs.getOnMessageMicroflow(), topic, subs.getTopic()));
                this.client.subscribe(topic, 1);
            } catch (MqttException e) {
                MqttConnector._logger.error(String.format("Reconnect failed for topic %s: %s", topic, e.getMessage()));
            }
        });
    }
}
