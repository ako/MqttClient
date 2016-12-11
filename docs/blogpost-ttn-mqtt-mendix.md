# Using the Mendix MQTT connector with The Things Network

You can use the MQTT connector to receive messages from IoT devices connected to The Thing Network.

Basic approach:
* Create a TTN user
* Login
* Create a TTN application
* Authorize your user for your TTN application
* Register your devices
* Subscribe to the required topics in your Mendix application

## Configuration

You should [Download ttnctl][6] or use the [TTN dashboard][7].

Create a TTN user:

    λ .\ttnctl-windows-amd64.exe user create andrej.koelewijn@mendix.com
    Password:
    Confirm password:
      INFO User created

Login:

    λ ttnctl-windows-amd64.exe user login andrej.koelewijn@mendix.com
    Password:
      INFO Logged in as andrej.koelewijn@mendix.com and persisted token in C:\Users\ako/.ttnctl/auths.json

Create an TTN app:

    λ ttnctl-windows-amd64.exe applications create "MxMqttClient"
      INFO Application created successfully

List apps:

    λ ttnctl-windows-amd64.exe applications
      INFO Found 1 application(s)
    EUI                     Name            Owner                           Access Keys                                     Valid
    eui-xxxx                MxMqttClient    andrej.koelewijn@mendix.com     xxxxxx                                          true

Authorize user for app:

    λ ttnctl-windows-amd64.exe applications authorize eui-xxx andrej.koelewijn@mendix.com
      INFO User authorized successfully

Use app:

    λ ttnctl-windows-amd64.exe applications use eui-xxx
      WARN Could not read configuration file, will just create a new one
      INFO You are now using application eui-xxx.

Register personalized device:

    λ  ttnctl-windows-amd64.exe devices register personalized 02020701
      INFO Generating random NwkSKey and AppSKey...
      INFO Registered personalized device           AppSKey=XXX DevAddr=02020701 Flags=0 NwkSKey=XXXX

    λ ttnctl-windows-amd64.exe devices register personalized 02020601
      INFO Generating random NwkSKey and AppSKey...
      INFO Registered personalized device           AppSKey=XXX DevAddr=02020601 Flags=0 NwkSKey=XXX

List devices:

    λ ttnctl-windows-amd64.exe devices
      INFO Application does not activate new devices with default AppKey
      INFO Found 1 personalized devices (ABP)

    DevAddr         FCntUp  FCntDown        Flags
    02020701        0       0               -

      INFO Found 0 dynamic devices (OTAA)

    DevEUI  DevAddr FCntUp  FCntDown

      INFO Run 'ttnctl devices info [DevAddr|DevEUI]' for more information about a specific device

Device details:

    λ ttnctl-windows-amd64.exe devices info 02020701
    Personalized device:

      DevAddr: 02020701
               {0x02, 0x02, 0x07, 0x01}

      NwkSKey: XXX

      AppSKey:  XXX

      FCntUp:  0
      FCntDn:  0

      Flags:   -

Create a Microflow to receive messages from your devices:

 ![Callback microflow][8]
 
Subscribe to receive all messages for your application:

 ![Subscribe to TTN message][9]
 
Details for subcribe action:

 ![Subscribe to TTN messages details][10]
 
You can test this setup by creating some dummy message with uplink:

    λ .\ttnctl-windows-amd64 uplink false 02020601 XXXX XXXX --plain "HelloWorld2" 5
      WARN Sending data as plain text is bad practice. We recommend to transmit data in a binary format.
      INFO Sending packet: Packet{Version:1,Token:[50 153],Identifier:0,GatewayId:[1 2 3 4 5 6 7 8],Payload:{,RXPK:[RXPK{Codr:4/5,Data:QAEGAgIABQABFA0mF7yU0kxPzJbAA42J,Datr:SF8BW125,Freq:869.9751,Lsnr:4.9,Modu:LoRa,Rssi:-4,Stat:1,Time:2016-12-11 11:51:41.7044895 +0000 UTC,Tmst:1,}]}}
      INFO Received PullAck: Packet{Version:1,Token:[50 153],Identifier:1,GatewayId:[]}
      INFO Received Ack: Packet{Version:1,Token:[1 2],Identifier:4,GatewayId:[]}

You should see the following in the Mendix console indicating that the message has been received:

 ![Message received in console][11]

  [1]: https://staging.thethingsnetwork.org/wiki/Backend/Connect/Application
  [2]: http://thethingsnetwork.org/
  [3]: https://staging.thethingsnetwork.org/wiki/Backend/Security
  [4]: https://staging.thethingsnetwork.org/wiki/Backend/ttnctl/QuickStart
  [5]: http://forum.thethingsnetwork.org/t/ttn-uno-beta-release-documentation/290
  [6]: https://www.thethingsnetwork.org/docs/current/cli/#installation
  [7]: https://staging.thethingsnetwork.org/applications
  [8]: images/ttn-callback-microflow.png
  [9]: images/ttn-subscribe.png
  [10]: images/ttn-subscribe-details.png
  [11]: images/ttn-message-received-console.png