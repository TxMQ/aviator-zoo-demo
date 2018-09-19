package com.txmq.exozoodemo.transactions;

import com.txmq.aviator.messaging.AviatorMessage;
import com.txmq.aviator.pipeline.PlatformEvents;
import com.txmq.aviator.pipeline.metadata.AviatorHandler;
import com.txmq.exozoodemo.SocketDemoState;
import com.txmq.exozoodemo.ZooDemoTransactionTypes;

import io.swagger.model.Animal;
import io.swagger.model.Zoo;

public class ZooTransactions {

	@AviatorHandler(namespace=ZooDemoTransactionTypes.NAMESPACE, 
				transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
				events={PlatformEvents.executePreConsensus, PlatformEvents.executeConsensus},
				payloadClass=Animal.class)
	public void addAnimal(AviatorMessage<Animal> message, SocketDemoState state) {
		//todo:  improve this so that we're testing if an animal of the same name exists, and failing if so 
		Animal animal = message.payload;
		switch (animal.getSpecies()) {
			case "lion":
				state.addLion(animal.getName());
				break;
			case "tiger":
				state.addTiger(animal.getName());
				break;
			case "bear":
				state.addBear(animal.getName());
				break;
		}						
	}
	
	@AviatorHandler(namespace=ZooDemoTransactionTypes.NAMESPACE,
				transactionType=ZooDemoTransactionTypes.GET_ZOO, 
				events={PlatformEvents.messageReceived})
	public Zoo getZoo(AviatorMessage<?> message, SocketDemoState state) {
		Zoo zoo = new Zoo();
		zoo.setLions(state.getLions());
		zoo.setTigers(state.getTigers());
		zoo.setBears(state.getBears());
		
		message.interrupt();
		return zoo;
	}
}
