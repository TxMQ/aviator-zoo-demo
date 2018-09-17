package com.txmq.exozoodemo;

import com.txmq.exo.messaging.annotations.TransactionType;
import com.txmq.exo.messaging.annotations.TransactionTypes;

@TransactionTypes(namespace=ZooDemoTransactionTypes.NAMESPACE, onlyAnnotatedValues=true)
public class ZooDemoTransactionTypes {
	public static final String NAMESPACE = "ZooDemoTransactionTypes";
	
	@TransactionType
	public static final String GET_ZOO = "GET_ZOO";
	
	@TransactionType
	public static final String ADD_ANIMAL = "ADD_ANIMAL";	
}
