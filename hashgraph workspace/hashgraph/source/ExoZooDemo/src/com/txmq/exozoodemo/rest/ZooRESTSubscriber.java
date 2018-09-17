package com.txmq.exozoodemo.rest;

import javax.ws.rs.container.AsyncResponse;

import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.ReportingEvents;
import com.txmq.exo.pipeline.metadata.ExoSubscriber;
import com.txmq.exo.pipeline.subscribers.ExoSubscriberBase;
import com.txmq.exozoodemo.ZooDemoTransactionTypes;

//TODO:  Add a method for just stuffing the payload into the pipe, eliminate boilerplate yo! 
public class ZooRESTSubscriber extends ExoSubscriberBase<AsyncResponse> {

	@ExoSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
					events={ReportingEvents.transactionComplete})
	public void addAnimalTransactionCompleted(ExoNotification<?> notification) {
		AsyncResponse responder = this.getResponder(notification);
		if (responder != null) {
			responder.resume(notification);
		}
	}
	
	@ExoSubscriber(	namespace=ZooDemoTransactionTypes.NAMESPACE,
					transactionType=ZooDemoTransactionTypes.GET_ZOO, 
					events={ReportingEvents.transactionComplete})
	public void getZooTransactionCompleted(ExoNotification<?> notification) {
		AsyncResponse responder = this.getResponder(notification);
		if (responder != null) {
			responder.resume(notification);
		}
	}
}
