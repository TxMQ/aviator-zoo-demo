package com.txmq.aviator.test;

import java.util.List;

import org.junit.Test;

import com.txmq.exo.config.AviatorConfig;
import com.txmq.exo.config.model.BlockLoggerConfig;
import com.txmq.exo.config.model.MessagingConfig;

public class AviatorConfigTest {

	@Test
	public void test() {
		AviatorConfig.loadConfiguration("../../aviator-config.json");
		MessagingConfig messagingConfig = (MessagingConfig) AviatorConfig.get("rest");
		List<BlockLoggerConfig> loggers = (List<BlockLoggerConfig>) AviatorConfig.get("blockLoggers");
		BlockLoggerConfig logger = (BlockLoggerConfig) AviatorConfig.get("blockLogger");
		System.out.println("!");
	}
}
