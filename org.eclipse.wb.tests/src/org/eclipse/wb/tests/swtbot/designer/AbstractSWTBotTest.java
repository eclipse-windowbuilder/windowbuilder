/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer;

import org.eclipse.wb.tests.swtbot.designer.AbstractSWTBotTest.LoggerExtension;
import org.eclipse.wb.tests.swtbot.designer.bot.WindowBuilderWorkbenchBot;

import org.eclipse.swt.widgets.Display;

import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Abstract base class for all JUnit tests using the SWTBot. This class
 * initializes the workspace with the {@code Resource} perspective and an empty
 * test project. After each test run, all created Java files are removed from
 * the project.
 */
@ExtendWith(LoggerExtension.class)
public abstract class AbstractSWTBotTest {
	protected static final Logger LOGGER = Logger.getLogger(AbstractSWTBotTest.class.getSimpleName());
	protected WindowBuilderWorkbenchBot bot;

	@BeforeEach
	public void setUp() throws Exception {
		if (Display.getCurrent() != null) {
			fail("""
					SWTBot test needs to run in a non-UI thread.
					Make sure that "Run in UI thread" is unchecked in your launch configuration or that useUIThread is set to false in the pom.xml
					""");
		}

		bot = new WindowBuilderWorkbenchBot();
	}

	static {
		try (InputStream is = AbstractSWTBotTest.class.getResourceAsStream("WindowBuilder SWTBot Logging.properties")) {
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static class LoggerExtension implements BeforeTestExecutionCallback {
		@Override
		public void beforeTestExecution(ExtensionContext context) throws Exception {
			LOGGER.info(context.getRequiredTestClass().getSimpleName() + ':' + context.getDisplayName());
		}
	}
}
