# JMeterWebsocketSampler
load test your websocket server. This is minimal jmeter plugin. Modify it to generate load according to your needs

## What is does
Each thread opens single websocket connection to the server. Once connection is open, it periodically sends messages for given duration. Aggregate Report's error% shows number of unsuccessful connections. 

It also works inside proxy.

It accepts

1. URI: server uri under test (ws://localhost:8080)
2. Proxy URI: proxy uri if running jmeter behind proxy. Leave empty if no proxy	
3. Message: message that is send periodically
4. Message Rate(per minute): rate at which message is send for given duration
5. Duration(seconds): if Duration is 90 seconds and Message Rate is 10 then it will send 9 messages in 90 seconds 
6. Connection timeout(miliseconds): client would wait for timeout untill it mark connection as unsuccessful
7. Connection idle timeout(miliseconds): number of milliseconds after which an idle session will be closed(<=0 for no timeout)
8. send timeout(miliseconds): number of milliseconds till timeout while attempting to send a websocket message for all RemoteEndpoints(non-positive for no timeout)


##How to install

1. Run ```mvn install``` to generate jar file. 
2. copy generated jar file and tyrus-standalone-client-1.11.jar into path-to-jmeter-installation/lib/ext
3. run jmeter
4. open websoket.jmx
5. start test




