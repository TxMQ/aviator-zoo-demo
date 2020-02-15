package com.txmq.aviator.rest;

import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.txmq.aviator.config.AviatorConfig;
import com.txmq.aviator.config.model.MessagingConfig;
import com.txmq.aviator.core.Aviator;
import com.txmq.aviator.core.annotations.AviatorShutdown;
import com.txmq.aviator.core.annotations.AviatorStartup;
import com.txmq.aviator.messaging.AviatorCoreTransactionTypes;
import com.txmq.aviator.messaging.AviatorMessage;
import com.txmq.aviator.messaging.rest.CORSFilter;
import com.txmq.aviator.messaging.websocket.grizzly.AviatorWebSocketApplication;

public class AviatorRESTBootstrapper {
	
	/**
	 * Tracks running Grizzly instances so they can be shut down later
	 */
	private static List<HttpServer> servers = new ArrayList<HttpServer>();
	

	@AviatorStartup
	public static void startup() {
		MessagingConfig restConfig = null;
		try {
			restConfig = (MessagingConfig) AviatorConfig.get("rest");
		} catch (Exception e) {
			throw new IllegalStateException("aviator-config.json does not contain a rest configuration, or it is invalid.");
		}
		
		restConfig = parseMessagingConfig(restConfig);
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(restConfig.port).build();
		ResourceConfig config = new ResourceConfig().packages("com.txmq.aviator.messaging.rest.api")
				.register(new CORSFilter()).register(JacksonFeature.class).register(MultiPartFeature.class);

		for (String pkg : restConfig.handlers) {
			config.packages(pkg);
		}

		System.out.println("Attempting to start Grizzly on " + baseUri);
		HttpServer grizzly = null;
		if (restConfig.secured == true) {
			SSLContextConfigurator sslContext = new SSLContextConfigurator();
			sslContext.setKeyStoreFile(restConfig.serverKeystore.path);
			sslContext.setKeyStorePass(restConfig.serverKeystore.password);
			sslContext.setTrustStoreFile(restConfig.serverTruststore.path);
			sslContext.setTrustStorePass(restConfig.serverTruststore.password);

			grizzly = GrizzlyHttpServerFactory.createHttpServer(baseUri, config, true,
					new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false));
		} else {
			grizzly = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
		}

		System.out.println("Starting Grizzly");
		try {
			grizzly.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		HttpServer wsServer = HttpServer.createSimpleServer(".", restConfig.port + 1000);
		final WebSocketAddOn addon = new WebSocketAddOn();
		for (NetworkListener listener : wsServer.getListeners()) {
			listener.registerAddOn(addon);
		}
		WebSocketEngine.getEngine().register("", "/wstest", new AviatorWebSocketApplication());

		try {
			wsServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Track Grizzly instances so they can be shut down later
		if (grizzly != null) {
			servers.add(grizzly);
		}

		// Publish available Exo API endpoints to the HG.
		try {
			String externalUrl = "http://" + Inet4Address.getLocalHost().getHostAddress() + ":" + restConfig.port;
			System.out.println("Reporting available REST API at " + externalUrl);
			new AviatorMessage<Serializable>(
					AviatorCoreTransactionTypes.NAMESPACE,
					AviatorCoreTransactionTypes.ANNOUNCE_NODE, externalUrl
				).submit();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@AviatorShutdown
	public static void shutdown() {
		for (HttpServer server : servers) {
			server.shutdown(30, TimeUnit.SECONDS);
		}
	}
	
	private static MessagingConfig parseMessagingConfig(MessagingConfig config) {
		MessagingConfig result = new MessagingConfig();

		// If a port has been defined in the config, use it over the derived port.
		if (config.port > 0) {
			result.port = config.port;
		} else {
			// Test if there's a derived port value. If not, we have an invalid messaging
			// config
			if (config.derivedPort != null) {
				// Calculate the port for socket connections based on the hashgraph's port
				// If we're in test mode, mock this up to be a typical value, e.g. 5220X
				if (!Aviator.isTestMode()) {
					result.port = Aviator.getBasePort() + config.derivedPort;
				} else {
					result.port = 50204 + config.derivedPort;
				}
			} else {
				throw new IllegalArgumentException("One of \"port\" or \"derivedPort\" must be defined.");
			}
		}

		if (config.handlers != null && config.handlers.length > 0) {
			result.handlers = config.handlers;
		} else {
			throw new IllegalArgumentException("No handlers were defined in configuration");
		}

		result.secured = config.secured;
		result.clientKeystore = config.clientKeystore;
		result.clientTruststore = config.clientTruststore;
		result.serverKeystore = config.serverKeystore;
		result.serverTruststore = config.serverTruststore;
		return result;
	}
}

