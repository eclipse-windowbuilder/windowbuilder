/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler
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
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.core.nls.ExternalizeStringsContributionItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Wrapped for the {@code Externalize} {@link ToolBar} button to allow access
 * when not in the UI thread.
 */
public class SWTBotExternalizeDropDownButton extends SWTBotToolbarDropDownButton {
	public SWTBotExternalizeDropDownButton(ToolItem w) {
		super(w);
	}

	/**
	 * Gets the "{@code Externalize} menu of this widget.
	 */
	public SWTBotRootMenu externalizeMenu() throws WidgetNotFoundException {
		// Set-up
		Menu[] menu = new Menu[1];

		Listener l = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.widget instanceof Menu m) {
					menu[0] = m;
				}
			}
		};
		// Open menu
		try {
			syncExec(() -> display.addFilter(SWT.Show, l));

			asyncExec(() -> widget.notifyListeners(SWT.Selection, createArrowEvent()));

			new SWTBot().waitUntil(new WaitForMenu(menu));

			return new SWTBotRootMenu(menu[0]);
		} finally {
			syncExec(() -> display.removeFilter(SWT.Show, l));
		}
	}

	private static class WaitForMenu extends DefaultCondition {
		private final Menu[] menu;

		public WaitForMenu(Menu[] menu) {
			assertEquals(menu.length, 1, "Menu array must be of length 1: " + menu.length);
			this.menu = menu;
		}

		@Override
		public boolean test() throws Exception {
			return menu[0] != null;
		}

		@Override
		public String getFailureMessage() {
			return "Unable to find \"Externalize\" context menu";
		}
	}

	/**
	 * Used to notify
	 * {@link ExternalizeStringsContributionItem#handleClick(ToolBar,Event)}
	 */
	private static Event createArrowEvent() {
		Event event = new Event();
		event.detail = SWT.ARROW;
		return event;
	}
}

