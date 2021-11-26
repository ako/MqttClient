package mqttclient.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mendix.core.Core;

import mqttclient.impl.MqttConnector.MqttConnection;

/**
 * Created by ako on 12-8-2016.
 */
public class MxMqttCallback implements MqttCallbackExtended {
    private MqttConnection mqttConnection = null;
    private String brokerKey;
    private HashMap<String, MqttSubscription> subscriptions = null;

    
    protected MxMqttCallback(String brokerKey, MqttConnection mqttConnection, HashMap<String, MqttSubscription> subscriptions) {
        this.mqttConnection = mqttConnection;
        this.brokerKey=brokerKey;
        this.subscriptions = subscriptions;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        MqttConnector.logger.warn(String.format("Connection Lost for: %s | %s", this.brokerKey, throwable.getMessage()), throwable);
    	this.mqttConnection.reconnect();		
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            MqttConnector.logger.debug(String.format("Message Arrived for: %s | %s | %s", this.brokerKey, topic, new String(mqttMessage.getPayload())));

            MqttSubscription subscription = getSubscriptionForTopic(topic);
            if (subscription != null) {
                String microflow = subscription.getOnMessageMicroflow();
                MqttConnector.logger.trace(String.format("Calling onMessage microflow: %s, %s", microflow, this.brokerKey));
                
                Core.microflowCall(microflow)
                	.withParam("Topic", topic)
                	.withParam("Payload", new String(mqttMessage.getPayload()))
                	.execute(Core.createSystemContext());
            } else {
                MqttConnector.logger.error(String.format("Cannot find microflow for message received on topic %s", topic));
            }
        } catch (Exception e) {
            MqttConnector.logger.error(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        MqttConnector.logger.info(String.format("deliveryComplete: %s", this.brokerKey));
    }

    /**
     * find possibly wildcarded subscription for specific topic
     */
    private MqttSubscription getSubscriptionForTopic(String topic) {

        MqttConnector.logger.trace("getSubscriptionForTopic: " + topic);
        Iterator<String> subscriptionTopics = this.subscriptions.keySet().iterator();
        while (subscriptionTopics.hasNext()) {
            String topicWithWildcards = subscriptionTopics.next();
            String topicWithWildcardsRe = topicWithWildcards.replaceAll("\\+", "[^/]+").replaceAll("/#", "\\(|/.*\\)");
            MqttConnector.logger.trace(String.format("Comparing topic %s with subscription %s as regex %s", topic, topicWithWildcards, topicWithWildcardsRe));
            if (topic.matches(topicWithWildcardsRe)) {
                MqttConnector.logger.trace("Found subscription " + topicWithWildcards);
                return this.subscriptions.get(topicWithWildcards);
            }
        }

        return null;
    }

    @Override
    public void connectComplete(boolean isReconnect, String serverUri) {
        MqttConnector.logger.info(String.format("connectComplete %s, %s", isReconnect, serverUri));
        this.subscriptions.forEach((topic, subs) -> {
            try {
                MqttConnector.logger.info(String.format("Resubscribing microflow %s to topic %s (%s)", subs.getOnMessageMicroflow(), topic, subs.getTopic()));
                this.mqttConnection.subscribe(topic, subs.getOnMessageMicroflow(),subs.getQoS());
            } catch (MqttException e) {
                MqttConnector.logger.error(String.format("Reconnect failed for topic %s: %s", topic, e.getMessage()));
            }
        });
    }
}
