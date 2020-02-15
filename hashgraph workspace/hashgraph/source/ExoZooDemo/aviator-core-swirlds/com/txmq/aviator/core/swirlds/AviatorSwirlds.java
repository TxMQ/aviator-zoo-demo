package com.txmq.aviator.core.swirlds;

import java.io.IOException;
import java.io.Serializable;

import com.swirlds.platform.Platform;
import com.swirlds.platform.Transaction;
import com.txmq.aviator.core.Aviator;
import com.txmq.aviator.core.AviatorStateBase;
import com.txmq.aviator.core.IAviator;
import com.txmq.aviator.messaging.AviatorMessage;

/**
 * A static locator class for Exo platform constructs. This class allows
 * applications to get access to Swirlds platform, Swirlds state, and the
 * transaction router from anywhere in the application.
 * 
 * I'm not in love with using static methods essentially as global variables.
 * I'd love to hear ideas on a better way to approach this.
 */
public class AviatorSwirlds extends Aviator implements IAviator {
	/**
	 * Reference to the Swirlds platform
	 */
	private static Platform platform;

	/**
	 * Initializes the platform from an aviator-config.json file located in the same
	 * directory as the application runs in.  
	 * 
	 * Developers must call this method from your application's main()
	 * 
	 * @throws ClassNotFoundException
	 */
	
	public static synchronized void init(Platform platform) throws ReflectiveOperationException {
		AviatorSwirlds.platform = platform;
		Aviator.init(AviatorSwirlds.class);
	}

	/**
	 * Submits a transaction to the platform. Applications should use this method
	 * over ExoPlatformLocator.getPlatform().createTransaction() to enable unit
	 * testing via test mode.
	 * 
	 * This signature matches the createTransaction signature of the Swirlds
	 * Platform.
	 */
	@Override
	public void createTransactionImpl(AviatorMessage<? extends Serializable> transaction) throws IOException {
		// Process message received handlers
		getPipelineRouter().routeMessageReceived(transaction, getState());

		// If the transaction was not interrupted, submit it to the platform
		if (transaction.isInterrupted() == false) {
			platform.createTransaction(new Transaction(transaction.serialize()));
			getPipelineRouter().notifySubmitted(transaction);
		}
	}

	/**
	 * Accessor for a reference to the Swirlds platform. Developers must call
	 * PlatformLocator.init() to intialize the locator before calling
	 * getPlatform()
	 */
	public static Platform getPlatform() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException("PlatformLocator has not been initialized.  "
					+ "Please initialize PlatformLocator in your SwirldMain implementation.");
		}

		return platform;
	}

	/**
	 * Swirlds-specific implementation - returns the swirlds state if it exists, otherwise default to super()
	 * 
	 * @return Aviator swirlds state
	 * @throws IllegalStateException
	 */
	@Override
	public AviatorStateBase getStateImpl() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException("AviatorSwirlds has not been initialized.  "
					+ "Please initialize AviatorSwirlds in your SwirldMain implementation.");
		}

		return (AviatorSwirldsState) platform.getState();
	}

	@Override
	public int getBasePortImpl() {
		// TODO Auto-generated method stub
		return platform.getAddress().getPortExternalIpv4();
	}

}
