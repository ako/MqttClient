# Using the Mendix MQTT connector with The Things Network

## Configuration

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


  [1]: https://staging.thethingsnetwork.org/wiki/Backend/Connect/Application
  [2]: http://thethingsnetwork.org/
  [3]: https://staging.thethingsnetwork.org/wiki/Backend/Security
  [4]: https://staging.thethingsnetwork.org/wiki/Backend/ttnctl/QuickStart
