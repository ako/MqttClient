import paho.mqtt.publish as publish

publish.single("topic1","{'test':'true'}",hostname="localhost")
publish.single("topic2","{'test':'false'}",hostname="localhost")

