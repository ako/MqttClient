# MQTT client for Mendix

Mendix module to send and receive [MQTT][1] messages. This module uses the [Eclipse Paho][3] library. 
Tested with [AWS IoT][5], [The Things network][4] and [mosquitto][2] brokers.

## Usage

Main java actions:

 * MqttPublish - publish a message to specified topic
 * MqttSubscribe - subscribe to a topic. Required you to specify a microflow which will be called upon receiving 
   a message. This microflow should have two string parameters: Topic and Payload.
 * MqttUnsubscribe
 
## Usage with Amazon AWS IoT
 
You need to register your app as a Thing on AWS IoT. Download the generated certificates and store them in your resources 
folder of your app. When subscribing or publishing a message specify their location reletive to the resources folder.

## Version history

 * 0.0.1 - initial release

 [1]: http://mqtt.org/
 [2]: http://mosquitto.org/
 [3]: http://www.eclipse.org/paho/
 [4]: http://thethingsnetwork.org/
 [5]: https://aws.amazon.com/iot/