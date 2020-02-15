package com.txmq.aviator.core.swirlds;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.swirlds.platform.Transaction;
import com.txmq.aviator.blocklogger.AviatorBlockLoggerBootstrapper;
import com.txmq.aviator.core.AviatorStateBase;
import com.txmq.aviator.messaging.AviatorMessage;

/**
 * ExoState is a base class for developers to extend when implementing Swirlds states.
 * ExoState encapsulates persisting the address book, enables the collection of data
 * about available endpoints (exposed by the endpoints API), and contains the hooks
 * for routing transactions using annotations.
 * 
 * Developers should be sure to call super-methods when extending/implementing init, 
 * copyFrom, and handleTransaction when subclassing ExoState.
 * 
 * @see com.txmq.aviator.messaging.rest.EndpointsApi
 */
public class AviatorSwirldsState extends AviatorStateBase {
	
	/** names and addresses of all members */
	protected AddressBook addressBook;
	
	/**
	 * Base implementation of copyFrom.  Copies endpoints, addressBook, 
	 * and node naming information stored in the state.
	 */
	public synchronized void copyFrom(SwirldState old) {
		this.copyFrom((AviatorStateBase) old);
		if (addressBook != null) {
			addressBook = ((AviatorSwirldsState) old).addressBook.copy();
		}		
	}
	
	/**
	 * Base implementation of transaction handler for Swirlds states.  The 
	 * platform will invoke this method once per transaction as transactions 
	 * are received, and again once consensus has been reached.
	 * 
	 * The base implementation routes events using Exo's annotation model.
	 * When transactions are received, they are routed to methods that 
	 * have been annotated with @ExoTransaction(<transaction type>).  
	 * 
	 * Transactions that have reached consensus are logged to a BlockLogger automatically.
	 * 
	 * TODO:  Make blockchain logging configurable
	 */
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timeCreated, Instant timestamp, Transaction transaction, Address address) {
		
		try {
			AviatorMessage<?> message = AviatorMessage.deserialize(transaction.getContents());
			if (consensus == false) {
				//Route the transaction through the pre-consensus part of the pipeline
				AviatorSwirlds.getPipelineRouter(this.myName).routeExecutePreConsensus(message, this);				
			} else {
				AviatorSwirlds.getPipelineRouter(this.myName).routeExecuteConsensus(message, this);
				if (message.isInterrupted() == false) {
					//TODO:  We need a better way to cut this in, maybe some kind of interceptor model?
					//Can't make block logging optional at the build level with this dependency in place.
					AviatorBlockLoggerBootstrapper.getBlockLogger().addTransaction(message, this.myName);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initializer method.  This gets called by the platform when it creates a
	 * copy of the state.  When extending ExoState, be sure to call super.init()
	 * when overriding/implementing the init method.
	 */
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.myName = platform.getAddress().getSelfName();
		this.addressBook = addressBook;
	}

}
