package com.txmq.exo.messaging.websocket.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.subscribers.ExoSubscriberManager;

public class ExoWebSocketApplication extends WebSocketApplication {
	
	private ExoSubscriberManager subscriberManager = new ExoSubscriberManager();

	public ExoWebSocketApplication() {
		super();
	}
	
	@Override
	public void onConnect(WebSocket socket) {
		System.out.println("Connected");
		super.onConnect(socket);
	}
	
	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		System.out.println("Closed");
		super.onClose(socket, frame);
	}
	
	@SuppressWarnings("finally")
	@Override
    public void onMessage(WebSocket socket, String frame) {
		
		//Parse the incoming message
        ExoMessageJsonParser parser = new ExoMessageJsonParser();
        ExoMessage<?> message = null;
        try {
			message = parser.readValue(frame, ExoMessage.class);
		} catch (Exception e) {
			//Uh-oh..  Try to report the failure back to the caller
			e.printStackTrace();
			ExoMessage<String> errorResponse = new ExoMessage<String>();
			errorResponse.payload = "Could not deserialize message: " + frame;
			try {
				socket.send(parser.writeValueAsString(errorResponse));
			} catch (JsonProcessingException e1) {
				// OK, we're screwed..  Bail out.
				System.out.println("Websocket message deserialization and error reporting failed!");
				System.out.println(frame);
				e1.printStackTrace();
			} finally {
				return;
			}
		}
        
        /*
         * We're still here, so we must have been able to deserialize the message we received.  
         * Register responders, and pass the transaction on to the platform.
         */
        try {
        	subscriberManager.registerAllAvailableResponders(message, socket);
        	ExoPlatformLocator.createTransaction(message);
        } catch (IOException e) {
        	e.printStackTrace();
        }        
    }
}

