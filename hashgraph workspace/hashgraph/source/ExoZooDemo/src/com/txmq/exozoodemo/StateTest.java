package com.txmq.exozoodemo;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;

public class StateTest {

	public static Platform staticPlatform;
	
	private Platform platform;
	public StateTest(Platform platform) {
		this.platform = platform;
	}
	
	public SwirldState getState() {
		return this.platform.getState();
	}
	
	public SwirldState getStaticState() {
		return staticPlatform.getState();
	}
	
	public static SwirldState staticGetState() {
		return staticPlatform.getState();
	}
}
