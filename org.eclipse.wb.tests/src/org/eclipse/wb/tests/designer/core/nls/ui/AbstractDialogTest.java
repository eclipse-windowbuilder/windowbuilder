/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.ui.NlsDialog;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetOfType.widgetOfType;

import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import java.util.Arrays;

/**
 * Abstract test for {@link NlsDialog}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractDialogTest extends AbstractNlsUiTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that {@link SWTBot} has {@link TabItem}'s with given titles.
	 *
	 * @return the array of {@link TabItem}'s.
	 */
	protected static void assertItems(SWTBot shell, String... expectedTitles) {
		java.util.List<TabItem> items = shell.getFinder().findControls(widgetOfType(TabItem.class));
		assertEquals(expectedTitles.length, items.size());
		for (int i = 0; i < items.size(); i++) {
			SWTBotTabItem item = new SWTBotTabItem(items.get(i));
			assertEquals(expectedTitles[i], item.getText());
		}
	}

	/**
	 * Asserts that {@link List} has items's with given titles.
	 */
	protected static void assertItems(SWTBotList list, String... expectedTitles) {
		String[] items = list.getItems();
		assertEquals(expectedTitles.length, items.length);
		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			assertEquals(expectedTitles[i], item);
		}
	}

	/**
	 * Asserts that {@link SWTBotTable} has {@code TableItems}'s with given items.
	 */
	protected static void assertItems(SWTBotTable table, String[]... expectedItems) {
		assertEquals(expectedItems.length, table.rowCount());
		for (int i = 0; i < table.rowCount(); i++) {
			for (int j = 0; j < table.columnCount(); j++) {
				assertEquals(expectedItems[i][j], table.cell(i, j));
			}
		}
	}

	/**
	 * Asserts that {@link SWTBotTable} has {@code TableColumn}'s with given titles.
	 */
	protected static void assertColumns(SWTBotTable table, String... expectedTitles) {
		assertEquals(table.columns(), Arrays.asList(expectedTitles));
	}
}
