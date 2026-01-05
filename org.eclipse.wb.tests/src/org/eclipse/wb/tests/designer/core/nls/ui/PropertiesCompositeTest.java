/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.NewSourceDialog;
import org.eclipse.wb.internal.core.nls.ui.PropertiesComposite;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.gef.UiContext;
import org.eclipse.wb.tests.utils.SWTBotEditableSource;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetOfType.widgetOfType;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PropertiesComposite}.
 *
 * @author scheglov_ke
 */
public class PropertiesCompositeTest extends AbstractDialogTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Sources
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_noSources() throws Exception {
		String initialSource = getTestSource("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "Properties");
				assertEquals(0, shell.list().itemCount());
			}
		});
	}

	/**
	 * Now "real" sources, but two different "possible" sources.
	 */
	@Test
	public void test_possibleSources() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("#Direct ResourceBundle", "frame.title=My JFrame"));
		setFileContentSrc(
				"test/messages2.properties",
				getSourceDQ("#Direct ResourceBundle", "frame.name=My name"));
		String initialSource = getTestSource("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "test.messages2", "Properties");
				shell.tabItem("Properties").activate();
				SWTBotList sourcesList = shell.list();
				//
				assertItems(
						sourcesList,
						"test.messages (Direct ResourceBundle usage)",
						"test.messages2 (Direct ResourceBundle usage)");
			}
		});
	}

	@Test
	public void test_existingSources() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
						setTitle(ResourceBundle.getBundle("test.messages").getString("frame.title")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				shell.tabItem("Properties").activate();
				SWTBotList sourcesList = shell.list();
				//
				assertItems(sourcesList, "test.messages (Direct ResourceBundle usage)");
			}
		});
	}

	@Test
	public void test_properties() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("#Direct ResourceBundle"));
		String initialSource = getTestSource("""
				public class Test extends JFrame {
					public Test() {
						setTitle("My JFrame");
						{
							JButton button = new JButton("New button");
							getContentPane().add(button, BorderLayout.NORTH);
						}
						{
							JTextField textField = new JTextField();
							getContentPane().add(textField, BorderLayout.SOUTH);
						}
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				shell.tabItem("Properties").activate();
				// sources list
				SWTBotList sourcesList = shell.list();
				// properties tree
				SWTBotTree propertiesTree = shell.tree();
				// "Externalize" button
				SWTBotButton externalizeButton = shell.button("Externalize");
				// check content on properties tree
				{
					assertNotNull(
							getItem(propertiesTree, "(javax.swing.JFrame)", "title: My JFrame"));
					assertNotNull(
							getItem(
									propertiesTree,
									"(javax.swing.JFrame)",
									"getContentPane()",
									"button",
									"text: New button"));
					assertNull(
							getItem(
									propertiesTree,
									"(javax.swing.JFrame)", "getContentPane()", "textField"));
				}
				// prepare TreeItem's
				SWTBotTreeItem buttonItem = getItem(
						propertiesTree,
						"(javax.swing.JFrame)", "getContentPane()", "button");
				SWTBotTreeItem buttonTextItem = getItem(buttonItem, "text: New button");
				// set checked "button" item
				{
					// check initial states
					assertFalse(externalizeButton.isEnabled());
					assertTrue(buttonItem.isGrayed());
					assertFalse(buttonItem.isChecked());
					assertFalse(buttonTextItem.isChecked());
					// check "button" item
					buttonItem.check();
					// check state
					assertTrue(buttonItem.isChecked());
					assertTrue(buttonTextItem.isChecked());
					assertTrue(externalizeButton.isEnabled());
				}
				// clear selection in sources - "Externalize" button should be disabled
				{
					assertTrue(externalizeButton.isEnabled());
					UIThreadRunnable.syncExec(() -> {
						sourcesList.widget.deselectAll();
						sourcesList.widget.notifyListeners(SWT.Selection, null);
					});
					assertFalse(externalizeButton.isEnabled());
				}
				// select sole source - "Externalize" button should be enabled
				{
					sourcesList.select(0);
					assertTrue(externalizeButton.isEnabled());
				}
				// check "&Enable all"
				{
					shell.button("&Enable all").click();
					assertTrue(buttonTextItem.isChecked());
				}
				// check "D&isable all"
				{
					shell.button("D&isable all").click();
					assertFalse(buttonTextItem.isChecked());
				}
				// do externalize
				{
					buttonItem.check();
					shell.button("E&xternalize").click();
					// items for "button" and its "text" property should be removed
					assertNull(
							getItem(
									propertiesTree,
									"(javax.swing.JFrame)",
									"getContentPane()",
									"button", 
									"text: New button"));
					assertNull(
							getItem(
									propertiesTree,
									"(javax.swing.JFrame)", "getContentPane()", "button"));
					assertNull(
							getItem(propertiesTree, "(javax.swing.JFrame)", "getContentPane()"));
					// check IEditableSource
					SWTBotEditableSource editableSource = getEditableSource(shell);
					assertEquals("New button", editableSource.getValue(LocaleInfo.DEFAULT, "Test.button.text"));
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// NewSourceDialog
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Open {@link NewSourceDialog} from {@link PropertiesComposite}.
	 */
	@Test
	public void test_open_NewSourceDialog() throws Exception {
		String initialSource = getTestSource("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "Properties");
				SWTBotList sourcesList = shell.list();
				assertEquals(0, sourcesList.itemCount());
				//
				shell.button("&New...").click();
				bot.shell("New source").bot().button("Cancel").click();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link SWTBotTreeItem} of given {@link SWTBotTree} on given path.
	 */
	private static SWTBotTreeItem getItem(SWTBotTree tree, String... pathElements) {
		try {
			return tree.expandNode(pathElements);
		} catch (WidgetNotFoundException e) {
			return null;
		}
	}

	/**
	 * @return child {@link SWTBotTreeItem} of given {@link SWTBotTreeItem} on given
	 *         path.
	 */
	private static SWTBotTreeItem getItem(SWTBotTreeItem item, String... pathElements) {
		try {
			return item.expandNode(pathElements);
		} catch (WidgetNotFoundException e) {
			return null;
		}
	}

	/**
	 * @return {@link SWTBotEditableSource} of this dialog.
	 */
	private static SWTBotEditableSource getEditableSource(SWTBot shell) throws Exception {
		PropertiesComposite composite = shell.getFinder().findControls(widgetOfType(PropertiesComposite.class)).get(0);
		IEditableSource editableSource = (IEditableSource) UIThreadRunnable.syncExec(
				() -> ExecutionUtils.runObject(() -> ReflectionUtils.invokeMethod(composite, "getSelectedSource()")));
		return new SWTBotEditableSource(editableSource);
	}
}
