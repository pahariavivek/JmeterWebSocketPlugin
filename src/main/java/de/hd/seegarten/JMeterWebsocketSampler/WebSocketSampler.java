package de.hd.seegarten.JMeterWebsocketSampler;


import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class WebSocketSampler extends AbstractJavaSamplerClient {

	private static String ws_uri;

	private static String ws_message;

	private static String response_message;
	private static String message_rate;
	private static String duration;
	private static String connection_timeout;
	private static String connection_idle_timeout;
	private static String async_timeout;
	private static String proxy_uri;

	private static final Logger log = LoggingManager.getLoggerForClass();

	@Override
	public Arguments getDefaultParameters() {

		Arguments params = new Arguments();

		params.addArgument("URI", "wss://echo.websocket.org");
		params.addArgument("Proxy URI", "");
		params.addArgument("Message", "hadfllo!");
		params.addArgument("Message Rate(per minute)", "1");
		params.addArgument("Duration(seconds)", "60");
		params.addArgument("Connection timeout(miliseconds)", "30000");
		params.addArgument("Connection idle timeout(miliseconds)", "30000");
		params.addArgument("send timeout(miliseconds)", "30000");
		return params;

	}

	@Override
	public void setupTest(JavaSamplerContext context) {

		ws_uri = context.getParameter("URI");
		
		proxy_uri = context.getParameter("Proxy URI");

		ws_message = context.getParameter("Message");
		message_rate = context.getParameter("Message Rate(per minute)");
		duration = context.getParameter("Duration(seconds)");
		connection_timeout = context
				.getParameter("Connection timeout(miliseconds)");
		connection_idle_timeout = context
				.getParameter("Connection idle timeout(miliseconds)");
		async_timeout = context.getParameter("send timeout(miliseconds)");
	}

	public SampleResult runTest(JavaSamplerContext javaSamplerContext) {

		SampleResult rv = new SampleResult();

		rv.sampleStart();

		ClientManager client = ClientManager.createClient();

		try {
			
			if(proxy_uri != null && !proxy_uri.isEmpty())
				client.getProperties().put(ClientManager.PROXY_URI, proxy_uri);
			
			client.getProperties().put(ClientManager.HANDSHAKE_TIMEOUT,
					Integer.parseInt(connection_timeout));
			client.setAsyncSendTimeout(Long.parseLong(async_timeout));
			client.setDefaultMaxSessionIdleTimeout(Long
					.parseLong(connection_idle_timeout));

			Session session = client.connectToServer(WebSocketSampler.class,
					new URI(ws_uri));
			RemoteEndpoint.Basic remote = session.getBasicRemote();

			long start = System.currentTimeMillis();
			long end = start + Long.parseLong(duration) * 1000;
			long interMessageWait = 60000 / Integer.parseInt(message_rate);
			while (System.currentTimeMillis() < end) {
				long msg_start = System.currentTimeMillis();
				remote.sendText(ws_message);
//				log.info("message send");
				long msg_end = System.currentTimeMillis();
				Thread.sleep(interMessageWait + msg_start - msg_end);
			}

			session.close(new CloseReason(
					CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
//			log.info("session  closed");
			rv.setSuccessful(true);

			// rv.setResponseMessage(response_message);

			rv.setResponseCode("200");

			if (response_message != null) {

				rv.setResponseData(response_message.getBytes());

			}

		} catch (DeploymentException e) {
			log.error(e.getMessage(), e);
			rv.setSuccessful(false);
			rv.setResponseCode("400");
			rv.setResponseData(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rv.setSuccessful(false);
			rv.setResponseCode("500");
			rv.setResponseData(e.getMessage());
		}
		rv.sampleEnd();

		return rv;

	}

	@OnOpen
	public void onOpen(Session session) {

		log.info("Connected ... " + session.getId() + "thread "
				+ Thread.currentThread().getId() + " "
				+ Thread.currentThread().getName());

	}

	@OnMessage
	public void onMessage(String message, Session session) {

		// log.info("Received ... " + message + " on session " + session.getId()
		// + "thread " + Thread.currentThread().getId() + " "
		// + Thread.currentThread().getName());

		response_message = message;

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {

		log.info(String.format("%s Session %s", closeReason + "thread "
				+ Thread.currentThread().getId() + " "
				+ Thread.currentThread().getName(), session.getId()));

	}
}
