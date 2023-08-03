/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.ui.NlsDialog;
import org.eclipse.wb.internal.core.nls.ui.SourceComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

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
	 * @return the {@link Table} relative location of given item.
	 */
	protected static Point getItemLocation(Table table, int column, int row) {
		// prepare 'x'
		int x;
		{
			x = 0;
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < column; i++) {
				TableColumn tableColumn = columns[i];
				x += tableColumn.getWidth();
			}
			//
			x += columns[column].getWidth() / 2;
		}
		// prepare 'y'
		int y = table.getHeaderHeight() + table.getItemHeight() * row + table.getItemHeight() / 2;
		// return as point
		return new Point(x, y);
	}

	/**
	 * Click on {@link TableItem} with given column/row.
	 */
	protected static void clickItem(Table table, int column, int row, int button) throws Exception {
		Point p = getItemLocation(table, column, row);
		// do click
		EventSender eventSender = new EventSender(table);
		eventSender.postMouseMove(p);
		EventSender.postMouseDown(1);
		EventSender.postMouseUp(1);
		waitEventLoop(10);
	}

	/**
	 * Asserts that {@link TabFolder} has {@link TabItem}'s with given titles.
	 *
	 * @return the array of {@link TabItem}'s.
	 */
	protected static TabItem[] assertItems(TabFolder tabFolder, String[] expectedTitles) {
		TabItem[] items = tabFolder.getItems();
		assertEquals(expectedTitles.length, items.length);
		for (int i = 0; i < items.length; i++) {
			TabItem item = items[i];
			assertEquals(expectedTitles[i], item.getText());
		}
		return items;
	}

	/**
	 * Asserts that {@link List} has items's with given titles.
	 */
	protected static void assertItems(List list, String[] expectedTitles) {
		String[] items = list.getItems();
		assertEquals(expectedTitles.length, items.length);
		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			assertEquals(expectedTitles[i], item);
		}
	}

	/**
	 * Asserts that {@link Table} has {@link TableColumn}'s with given titles.
	 */
	protected static void assertColumns(Table table, String[] expectedTitles) {
		TableColumn[] columns = table.getColumns();
		assertEquals(expectedTitles.length, columns.length);
		for (int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			assertEquals(expectedTitles[i], column.getText());
		}
	}

	/**
	 * Asserts that {@link Table} has {@link TableItems}'s with given titles.
	 */
	protected static void assertItems(Table table, String[][] expectedTitles2) {
		TableItem[] items = table.getItems();
		assertEquals(expectedTitles2.length, items.length);
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String[] expectedTitles = expectedTitles2[i];
			for (int j = 0; j < table.getColumnCount(); j++) {
				assertEquals(expectedTitles[j], item.getText(j));
			}
		}
	}

	/**
	 * @return the {@link SourceComposite} corresponding to given {@link TabItem}.
	 */
	protected static SourceComposite getSourceComposite(UiContext context, TabItem tabItem) {
		return UiContext.findFirstWidget(tabItem, SourceComposite.class);
	}

	/**
	 * @return the {@link Table} from {@link SourceComposite} corresponding to given {@link TabItem}.
	 */
	protected static Table getSourceTable(UiContext context, TabItem tabItem) {
		SourceComposite sourceComposite = getSourceComposite(context, tabItem);
		return UiContext.findFirstWidget(sourceComposite, Table.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Open" support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runnable for executing operations on {@link NlsDialog}.
	 */
	protected static interface NLSDialogRunnable {
		void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception;
	}

	/**
	 * Creates compilation unit, opens Design page, opens NLS dialog and then run given
	 * {@link NLSDialogRunnable}.
	 */
	protected final void openDialogNLS(String initialSource, final NLSDialogRunnable runnable)
			throws Exception {
		openDialogNLS("test", initialSource, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				Shell activeShell = context.useShell("Externalize strings");
				NlsDialog dialog = (NlsDialog) activeShell.getData(ResizableDialog.KEY_DIALOG);
				//
				try {
					TabFolder tabFolder = (TabFolder) ReflectionUtils.getFieldObject(dialog, "m_tabFolder");
					runnable.run(context, dialog, tabFolder);
				} finally {
					// click "OK"
					Button okButton = context.getButtonByText("OK");
					context.click(okButton);
				}
			}
		});
	}
}
