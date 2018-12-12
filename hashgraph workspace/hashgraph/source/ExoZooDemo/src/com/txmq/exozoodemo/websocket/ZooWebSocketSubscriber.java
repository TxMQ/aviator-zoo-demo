package com.txmq.exozoodemo.websocket;

import com.txmq.aviator.core.PlatformLocator;
import com.txmq.aviator.messaging.AviatorNotification;
import com.txmq.aviator.pipeline.ReportingEvents;
import com.txmq.aviator.pipeline.metadata.AviatorSubscriber;
import com.txmq.aviator.pipeline.subscribers.AviatorWebSocketSubscriber;
import com.txmq.exozoodemo.ZooDemoTransactionTypes;

/**
 * This class listens for events on zoo demo transactions and relays them back to clients through the web socket.
 * 
 * TODO:  Either through annotations or a simple JSON mapping file, there should be a way to configure this without having to write code.  
 * Code generation is a possibility, like I want to do with REST, but a config file or a section of exo-config.json might be a better approach.  
 * @author craigdrabik
 *
 */
public class ZooWebSocketSubscriber extends AviatorWebSocketSubscriber {

	@AviatorSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
					events= {	ReportingEvents.submitted, 
								ReportingEvents.preConsensusResult, 
								ReportingEvents.consensusResult, 
								ReportingEvents.transactionComplete	})
	public void addAnimalTransactionProgress(AviatorNotification<?> notification) {
		this.sendNotification(notification);
	}
	
	@AviatorSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.GET_ZOO, 
					events={ReportingEvents.transactionComplete})
	public void getZooTransactionProgress(AviatorNotification<?> notification) {
		this.sendNotification(notification);
	}


}
