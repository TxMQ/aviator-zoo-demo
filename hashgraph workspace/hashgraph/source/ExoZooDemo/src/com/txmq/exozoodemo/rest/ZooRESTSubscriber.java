package com.txmq.exozoodemo.rest;

import javax.ws.rs.container.AsyncResponse;

import com.txmq.aviator.messaging.AviatorNotification;
import com.txmq.aviator.pipeline.ReportingEvents;
import com.txmq.aviator.pipeline.metadata.AviatorSubscriber;
import com.txmq.aviator.pipeline.subscribers.AviatorSubscriberBase;
import com.txmq.exozoodemo.ZooDemoTransactionTypes;

//TODO:  Add a method for just stuffing the payload into the pipe, eliminate boilerplate yo! 
public class ZooRESTSubscriber extends AviatorSubscriberBase<AsyncResponse> {

	@AviatorSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
					events={ReportingEvents.transactionComplete})
	public void addAnimalTransactionCompleted(AviatorNotification<?> notification) {
		AsyncResponse responder = this.getResponder(notification);
		if (responder != null) {
			responder.resume(notification);
		}
	}
	
	@AviatorSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.GET_ZOO, 
					events={ReportingEvents.transactionComplete})
	public void getZooTransactionCompleted(AviatorNotification<?> notification) {
		AsyncResponse responder = this.getResponder(notification);
		if (responder != null) {
			responder.resume(notification);
		}
	}
}
