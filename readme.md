# Mendix MQTT connector

Mendix module to send and receive [MQTT][1] messages. This module uses the [Eclipse Paho][3] library. 
Tested with [AWS IoT][5], [The Things network][4] and [mosquitto][2] brokers.

## Usage

Main java actions:

 * MqttPublish - publish a message to specified topic
 * MqttSubscribe - subscribe to a topic. Required you to specify a microflow which will be called upon receiving
   a message. This microflow should have two string parameters: Topic and Payload.
 * MqttUnsubscribe - unsubscribe from topic

  ![MQTT Microflow actions toolbox][9]


### MQTT Publish
This activity allows you to publish a message to a specific MQTT topic. When you execute this activit the module will setup a connection to the topic and publish the message, it will retain the connection in memory for faster publishing in the future.  

Parameters (if parameters are optional you can pass empty or ''):

* **Broker host** *(required)*: The url of your MQTT broker, needs to be the URL without protocol and without port. (e.g. 'test.mosquitto.org')
* **Broker port** *(required)*: The port of your broker 
* **Broker organisation** *(optional)*: Some brokers use an organization as part of the URL, pass that here.
* **Timeout** *(required)*: Maximum timeout between broker and this application in Seconds (default=60)
* **Broker Username** *(optional)*: If your broker has username/password authentication pass the username here. If you set a username you must set a password. 
* **Broker Password** *(optional)*: If your broker has username/password authentication pass the username here. If you set a password you must set a username.
* **Topic name** *(required)*: The full topic name you want to publish the message to. This needs to be the topic name starting at the root, but **not** including a leading slash. Example: 'iot-2/type/exampledevice/status'
* **Payload** *(required)*: The message that is passed to the broker, this can be any format. The message is passed as-is to the broker. 
* **CA** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Certificate Authority used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/ca.pem' if your certificate is located in 'resources/cert/'.  
* **Client certificate** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Client Certificate used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/client.pem' if your certificate is located in 'resources/cert/'.
* **Client key** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Client Key used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/client.key' if your certificate is located in 'resources/cert/'.
* **Certificate Password** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is password that is needed to use the client key. 
* **QOS** *(required)*: Passes the Quality of Service parameter straight to the broker (At_Most_Once_0 / At_Least_Once_1 / Exactly_Once_2). *See MQTT service definitions for more info, i.e.: [https://mosquitto.org/man/mqtt-7.html#idm72][17]* (In short, Higher levels of QoS are more reliable, but involve higher latency and have higher bandwidth requirements.)


### MQTT Subscribe
This activity sets up the Subscription to an MQTT topic. When you execute this activit the module will setup a connection to the topic and wait for any message that comes in on the topic.  

Parameters (if parameters are optional you can pass empty or ''):

* **Broker host** *(required)*: The url of your MQTT broker, needs to be the URL without protocol and without port. (e.g. 'test.mosquitto.org')
* **Broker port** *(required)*: The port of your broker 
* **Broker organisation** *(optional)*: Some brokers use an organization as part of the URL, pass that here.
* **Timeout** *(required)*: Maximum timeout between broker and this application in Seconds (default=60)
* **Broker Username** *(optional)*: If your broker has username/password authentication pass the username here. If you set a username you must set a password. 
* **Broker Password** *(optional)*: If your broker has username/password authentication pass the username here. If you set a password you must set a username.
* **Topic name** *(required)*: The full topic name you want to publish the message to. This needs to be the topic name starting at the root, but **not** including a leading slash. Example: 'iot-2/type/exampledevice/status'
* **On message Microflow** *(required)*: The Microflow that will be executed for each message on the Topic. This microflow must have 2 parameters of type String: Topic & Payload.  
* **CA** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Certificate Authority used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/ca.pem' if your certificate is located in 'resources/cert/'.  
* **Client certificate** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Client Certificate used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/client.pem' if your certificate is located in 'resources/cert/'.
* **Client key** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is the Client Key used for the connection. Place the certificates inside of the resource folders. This value must be a relative path inside the resource folder. E.g.:   'cert/client.key' if your certificate is located in 'resources/cert/'.
* **Certificate Password** *(optional)*: If the broker requires a (custom) SSL authentication pass the certificates here. This is password that is needed to use the client key. 
* **QOS** *(required)*: Passes the Quality of Service parameter straight to the broker (At_Most_Once_0 / At_Least_Once_1 / Exactly_Once_2). *See MQTT service definitions for more info, i.e.: [https://mosquitto.org/man/mqtt-7.html#idm72][17]* (In short, Higher levels of QoS are more reliable, but involve higher latency and have higher bandwidth requirements.)



### MQTT Unsubscribe
This activity will unsubscribe your application from listening to an MQTT topic. All values passed into this acitivity need to be identical to the subscribe action. These values are used to identify unique MQTT connections.

Parameters (if parameters are optional you can pass empty or ''):


* **Broker host** *(required)*: The url of your MQTT broker, needs to be the URL without protocol and without port. (e.g. 'test.mosquitto.org')
* **Broker port** *(required)*: The port of your broker 
* **Broker organisation** *(optional)*: Some brokers use an organization as part of the URL, pass that here.
* **Broker Username** *(optional)*: If your broker has username/password authentication pass the username here. If you set a username you must set a password. 
* **Topic name** *(required)*: The full topic name you want to publish the message to. This needs to be the topic name starting at the root, but **not** including a leading slash. Example: 'iot-2/type/exampledevice/status'



## Examples

### Usage with Amazon AWS IoT
 
You need to register your app as a Thing on AWS IoT. Download the generated certificates and store them in your resources 
folder of your app. When subscribing or publishing a message specify their location reletive to the resources folder.

### Usage with TTN

For TTN you can leave the certificate info empty, instead provide username and password.

Microflow to subscribe to an MQTT topic:

 ![MQTT subscribe to topic][10]

Configuration of subscribe for TTN:

 ![][11]

Microflow to handled messages received:

 ![][12]

More info can be found here: [Using Mendix with TTN][14]

### Usage with IBM IoT
To use IBM IOT you need to fill in your organisation ID with your organisation ID. For the Username & Password you need to use the API Key for the username and Authentication Token for password.

 ![IBM IOT][15]

#### Setting up your IBM Application
To get your API Key and Authentication token login to your IBM IOT Launchpad and click on apps. Then click new Generate New API Key.
 ![IBM Apps][16]

### Development

Java dependencies are managed using Apache Ivy. There are two configuration:
* export - this is used to make sure only the required jars for the connector module are in userlib
* default - this downloads all dependencies required to run the project

Before you export the connector module run runivy-export.cmd to ensure you have the correct set of libraries to be
included in the connector mpk.

## License

 [Apache License V2.0][13]
 
 [Eclipse Public License][18]
   
 [1]: http://mqtt.org/
 [2]: http://mosquitto.org/
 [3]: http://www.eclipse.org/paho/
 [4]: http://thethingsnetwork.org/
 [5]: https://aws.amazon.com/iot/
 [6]: https://staging.thethingsnetwork.org/wiki/Backend/Connect/Application
 [7]: https://staging.thethingsnetwork.org/wiki/Backend/Security
 [8]: https://staging.thethingsnetwork.org/wiki/Backend/ttnctl/QuickStart
 [9]: docs/images/mqtt-toolbox.png
 [10]: docs/images/ttn-subscribe.png
 [11]: docs/images/ttn-subscribe-details.png
 [12]: docs/images/ttn-callback-microflow.png
 [13]: license.txt
 [14]: docs/blogpost-ttn-mqtt-mendix.md
 [15]: docs/images/IBM.png
 [16]: docs/images/IBMApps.png
 [17]: https://mosquitto.org/man/mqtt-7.html#idm72
 [18]: https://www.eclipse.org/legal/epl-2.0/