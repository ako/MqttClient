import paho.mqtt.publish as publish

for i in range(1,1000):
    publish.single("topic1","{{'test':'true','topic':'topic1','index':{}}}".format(i),hostname="localhost")
    publish.single("topic2","{{'test':'true','topic':'topic2','index':{}}}".format(i),hostname="localhost")
