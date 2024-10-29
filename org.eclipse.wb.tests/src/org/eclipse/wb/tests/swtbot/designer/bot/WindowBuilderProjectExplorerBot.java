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
package org.eclipse.wb.tests.swtbot.designer.bot;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IViewReference;

import java.util.logging.Logger;

/**
 * The SWTBot instance over the {@code Project Explorer}.
 */
public class WindowBuilderProjectExplorerBot extends SWTBotView {
	private static final Logger LOGGER = Logger.getLogger(WindowBuilderProjectExplorerBot.class.getSimpleName());

	public WindowBuilderProjectExplorerBot(IViewReference partReference, SWTWorkbenchBot bot) {
		super(partReference, bot);
	}

	/**
	 * Opens the "New" wizard via the context menu of the test project.
	 */
	public SWTBotShell openNewWizard() {
		LOGGER.fine("Open New wizard");
		bot().tree().getTreeItem("TestProject").contextMenu().menu("New", "Other...").click();
		LOGGER.fine("Opened New wizard");
		return bot.shell("Select a wizard");
	}
}
