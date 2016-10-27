# start mosquitto with unlimited inflight and queued messages to avoid dropping messages

import paho.mqtt.publish as publish
import json

for i in range(1,1001):
    publish.single("topic1import",payload=json.dumps({"test":True,"topic":"topic1import","index":i}),hostname="localhost",qos=1)
    publish.single("topic2import",payload=json.dumps({"test":False,"topic":"topic2import","index":i}),hostname="localhost",qos=1)
