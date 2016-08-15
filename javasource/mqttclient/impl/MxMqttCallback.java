package mqttclient.impl;

import com.google.common.collect.ImmutableMap;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.ISession;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;

/**
 * Created by ako on 12-8-2016.
 */
public class MxMqttCallback implements MqttCallback {
    private ILogNode logger = null;
    private MqttClient client = null;
    private HashMap<String, Subscription> subscriptions = null;

    public MxMqttCallback(ILogNode logger, MqttClient client, HashMap<String, Subscription> subscriptions) {
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
            if(subscriptions.containsKey(topic)) {
                String microflow = subscriptions.get(topic).getOnMessageMicroflow();
                logger.info(String.format("Calling onMessage microflow: %s, %s", microflow, client.getClientId()));
                //Core.executeAsync(ctx, microflow, true, ImmutableMap.of("Topic", s, "Payload", new String(mqttMessage.getPayload())));
                final ImmutableMap map = ImmutableMap.of("Topic", topic, "Payload", new String(mqttMessage.getPayload()));
                logger.info("Parameter map: " + map);
                Core.execute(ctx, microflow, true, map);
            } else {
                logger.error(String.format("Cannot find microflow for message received on topic %s",topic));
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        logger.info(String.format("deliveryComplete: %s", client.getClientId()));
    }
}
