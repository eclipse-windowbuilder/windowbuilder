/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer;

import org.eclipse.wb.tests.swtbot.designer.bot.WindowBuilderWorkbenchBot;

import org.eclipse.swt.widgets.Display;

import static org.assertj.core.api.Assertions.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;

import java.util.logging.Logger;

/**
 * Abstract base class for all JUnit tests using the SWTBot. This class
 * initializes the workspace with the {@code Resource} perspective and an empty
 * test project. After each test run, all created Java files are removed from
 * the project.
 */
public abstract class AbstractSWTBotTest {
	protected final Logger logger = Logger.getLogger(getClass().getSimpleName());
	protected WindowBuilderWorkbenchBot bot;

	@Before
	public void setUp() throws Exception {
		if (Display.getCurrent() != null) {
			fail("""
					SWTBot test needs to run in a non-UI thread.
					Make sure that "Run in UI thread" is unchecked in your launch configuration or that useUIThread is set to false in the pom.xml
					""");
		}

		bot = new WindowBuilderWorkbenchBot();
	}

	@Rule
	public TestRule loggerRule = (base, description) -> {
		logger.info(description.getClassName() + ':' + description.getMethodName());
		return base;
	};
}
