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

import org.eclipse.wb.core.controls.CTableCombo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.SourceComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils.ITableTooltipProvider;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UiContext;
import org.eclipse.wb.tests.utils.SWTBotCTableCombo;
import org.eclipse.wb.tests.utils.SWTBotEditableSource;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetOfType.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WithText.withText;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForShell;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.Traverse;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;

/**
 * Tests for {@link SourceComposite}.
 *
 * @author scheglov_ke
 */
public class SourceCompositeTest extends AbstractDialogTest {

	////////////////////////////////////////////////////////////////////////////
	//
	// ITableTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ITableTooltipProvider() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				// check items
				SWTBotTable table = shell.table();
				assertItems(
						table,
						new String[] { "frame.name", "My name" },
						new String[] { "frame.title", "My JFrame" });
				//
				try (SWTBotTableTooltipProvider provider = getTableToolTipProvider(shell)) {
					// not first column
					{
						Control control = provider.createTooltipControl(table.getTableItem(1), 1);
						assertNull(control);
					}
					// no components
					{
						Control control = provider.createTooltipControl(table.getTableItem(0), 0);
						assertNull(control);
					}
					// one component
					{
						Control control = provider.createTooltipControl(table.getTableItem(1), 0);
						assertNotNull(control);
						assertTrue(provider.getChildren().length > 0);
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contextMenu_removeLocale() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		setFileContentSrc(
				"test/messages_it.properties",
				getSourceDQ("frame.title=My JFrame IT", "frame.name=My name IT"));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				SWTBotTable table = shell.table();
				table.getTableItem(0).click(2);
				// Workaround to make sure the context menu for the 2nd column is opened
				SWTBotRootMenu tableMenu = new SWTBotRootMenu(UIThreadRunnable.syncExec(() -> {
					Menu menu = table.widget.getMenu();
					menu.setVisible(true);
					return menu;
				}));
				final SWTBotMenu removeLocaleItem = tableMenu.menu("Remove locale...");
				// don't confirm, no changes expected
				{
					context.execute(new FailableRunnable<Exception>() {
						@Override
						public void run() {
							removeLocaleItem.click();
						}
					});
					bot.waitUntil(waitForShell(withText("Confirm")));
					bot.shell("Confirm").bot().button("Cancel").click();
					// check items
					assertItems(
							table,
							new String[] { "frame.name", "My name", "My name IT" },
							new String[] { "frame.title", "My JFrame", "My JFrame IT" });
				}
				// confirm
				{
					context.execute(new FailableRunnable<Exception>() {
						@Override
						public void run() {
							removeLocaleItem.click();
						}
					});
					bot.waitUntil(waitForShell(withText("Confirm")));
					bot.shell("Confirm").bot().button("OK").click();
					// check items
					assertItems(
							table,
							new String[] { "frame.name", "My name" },
							new String[] { "frame.title", "My JFrame" });
				}
				shell.button("OK").click();
			}
		});
		// 'it' properties should be deleted
		assertFalse(getFileSrc("test/messages_it.properties").exists());
	}

	@Test
	public void test_contextMenu_internalizeKey() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				SWTBotTable table = shell.table();
				SWTBotRootMenu tableMenu = table.getTableItem(0).contextMenu();
				final SWTBotMenu internalizeItem = tableMenu.contextMenu("Internalize key...");
				// don't confirm, no changes expected
				{
					context.execute(new FailableRunnable<Exception>() {
						@Override
						public void run() {
							internalizeItem.click();
						}
					});
					bot.shell("Confirm").bot().button("Cancel").click();
					// check items
					assertItems(table, new String[] { "frame.title", "My JFrame" });
				}
				// confirm
				{
					context.execute(new FailableRunnable<Exception>() {
						@Override
						public void run() {
							internalizeItem.click();
						}
					});
					bot.shell("Confirm").bot().button("OK").click();
					// check items
					assertItems(table /* , <no elements> */);
					shell.button("OK").click();
				}
			}
		});
		// check source
		assertEditor("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
						setTitle("My JFrame");
					}
				}""");
		assertFalse(getFileContentSrc("test/messages.properties").contains("frame.title"));
	}

	@Test
	public void test_contextMenu_addLocale() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				SWTBotTable table = shell.table();
				SWTBotRootMenu tableMenu = table.getTableItem(0).click().contextMenu();
				final SWTBotMenu addLocaleItem = tableMenu.menu("Add locale...");
				//
				context.execute(new FailableRunnable<Exception>() {
					@Override
					public void run() {
						addLocaleItem.click();
					}
				});
				{
					SWTBot shell2 = bot.shell("Choose Locale").bot();
					// select 'it' language
					SWTBotCTableCombo languagesCombo = getLanguageCombo(shell2);
					for (int i = 0; i < languagesCombo.getItemCount(); i++) {
						String item = languagesCombo.getItem(i);
						if (item.startsWith("it - ")) {
							languagesCombo.select(i);
							break;
						}
					}
					// click "OK"
					shell2.button("OK").click();
				}
				// check items
				assertColumns(table, "Key", "(default)", "it");
				assertItems(table, new String[] { "frame.title", "My JFrame", "My JFrame" });
				shell.button("OK").click();
			}
		});
		// we should have new locale - 'it'
		assertTrue(getFileSrc("test/messages_it.properties").exists());
	}

	@Test
	public void test_contextMenu_addLocaleWithButton() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				context.execute(new FailableRunnable<Exception>() {
					@Override
					public void run() {
						shell.button("New locale...").click();
					}
				});
				bot.shell("Choose Locale").bot().button("Cancel").click();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SourceComposite
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_SourceComposite_edit() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		setFileContentSrc("test/messages_it.properties", getSourceDQ(""));
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
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				//
				SWTBotEditableSource editableSource = getEditableSource(shell);
				SWTBotTable table = shell.table();
				// check initial items
				{
					assertColumns(table, "Key", "(default)", "it");
					assertItems(table, new String[] { "frame.title", "My JFrame", "" });
				}
				// click to activate cell editor
				{
					// click on value to start edit
					table.click(0, 1);
					// send new text and CR
					{
						SWTBotText text = shell.text();
						text.setText("New title");
						closeCellEditor(shell);
						waitEventLoop(10);
					}
				}
				// check after edit
				{
					assertTrue(editableSource.getKeys().contains("frame.title"));
					assertEquals("New title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
					//
					assertItems(table, new String[] { "frame.title", "New title", "" });
				}
				// rename key
				{
					table.click(0, 0);
					{
						SWTBotText text = shell.text();
						text.setText("frame.title2");
						closeCellEditor(shell);
						waitEventLoop(10);
					}
					// check
					{
						assertFalse(editableSource.getKeys().contains("frame.title"));
						assertTrue(editableSource.getKeys().contains("frame.title2"));
						assertEquals("New title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title2"));
						//
						assertItems(table, new String[] { "frame.title2", "New title", "" });
					}
				}
				// update 'it'
				{
					LocaleInfo localeInfo = new LocaleInfo(Locale.ITALIAN);
					assertNull(editableSource.getValue(localeInfo, "frame.title2"));
					// modify
					{
						table.click(0, 2);
						SWTBotText text = shell.text();
						text.setText("title IT");
						closeCellEditor(shell);
						waitEventLoop(10);
					}
					// check
					{
						assertEquals("title IT", editableSource.getValue(localeInfo, "frame.title2"));
						assertItems(
								table,
								new String[] { "frame.title2", "New title", "title IT" });
					}
					shell.button("OK").click();
				}
				// wait UI
				//waitEventLoop(5000);
			}
		});
	}

	@Test
	public void test_SourceComposite_update_externalize() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
						setTitle(ResourceBundle.getBundle("test.messages").getString("frame.title")); //$NON-NLS-1$ //$NON-NLS-2$
						setName("My name");
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				//
				SWTBotEditableSource editableSource = getEditableSource(shell);
				SWTBotTable table = shell.table();
				// check initial items
				{
					assertColumns(table, "Key", "(default)");
					assertItems(table, new String[] { "frame.title", "My JFrame" });
				}
				// externalize "name"
				{
					GenericProperty nameProperty =
							(GenericProperty) m_contentJavaInfo.getPropertyByTitle("name");
					editableSource.externalize(new StringPropertyInfo(nameProperty), true);
				}
				// check items
				{
					assertColumns(table, "Key", "(default)");
					assertItems(
							table,
							new String[] { "frame.title", "My JFrame" },
							new String[] { "Test.this.name", "My name" });
				}
			}
		});
	}

	@Test
	public void test_SourceComposite_update_renameOver() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
						setTitle(ResourceBundle.getBundle("test.messages").getString("frame.title")); //$NON-NLS-1$ //$NON-NLS-2$
						setName(ResourceBundle.getBundle("test.messages").getString("frame.name")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				assertTrue(shell.tabItem("test.messages").isActive());
				//
				final SWTBotEditableSource editableSource = getEditableSource(shell);
				SWTBotTable table = shell.table();
				// check initial items
				{
					assertColumns(table, "Key", "(default)");
					assertItems(
							table,
							new String[] { "frame.name", "My name" },
							new String[] { "frame.title", "My JFrame" });
				}
				// rename "frame.name" -> "frame.title"
				editableSource.renameKey("frame.name", "frame.title");
				bot.shell("Confirm").bot().button("Yes, keep existing value").click();
				// check items
				{
					assertColumns(table, "Key", "(default)");
					assertItems(table, new String[] { "frame.title", "My JFrame" });
				}
			}
		});
	}

	@Test
	public void test_SourceComposite_onlyCurrentForm() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
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
				assertTrue(shell.tabItem("test.messages").isActive());
				//
				//waitEventLoop(5000);
				SWTBotTable table = shell.table();
				// check initial items
				assertItems(
						table,
						new String[] { "frame.name", "My name" },
						new String[] { "frame.title", "My JFrame" });
				// check "Show strings only for current form"
				{
					SWTBotCheckBox onlyFormButton = shell.checkBox("Show strings only for current form");
					onlyFormButton.click();
				}
				// only 'frame.title' expected
				assertItems(table, new String[] { "frame.title", "My JFrame" });
			}
		});
	}

	@Test
	public void test_SourceComposite_navigation() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("#Direct ResourceBundle", "key.1=1 1", "key.2=2 1"));
		setFileContentSrc(
				"test/messages_it.properties",
				getSourceDQ("#Direct ResourceBundle", "key.1=1 2", "key.2=2 2"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws InterruptedException {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				// No externalizable strings
				assertFalse(shell.tabItem("test.messages").isActive());
				shell.tabItem("test.messages").activate();
				SWTBotTable table = shell.table();
				// check initial items
				assertItems(
						table,
						new String[] { "key.1", "1 1", "1 2" },
						new String[] { "key.2", "2 1", "2 2" });
				// check next column
				{
					// activate editor at (1, 0)
					table.click(0, 1);
					// navigate next column - (1, 2)
					shell.text().traverse(Traverse.TAB_NEXT);
					// set text
					SWTBotText text = shell.text();
					text.setText("a b");
					closeCellEditor(shell);
					waitEventLoop(10);
					// check
					assertItems(
							table,
							new String[] { "key.1", "1 1", "a b" },
							new String[] { "key.2", "2 1", "2 2" });
				}
				// check next row
				{
					// activate editor at (2, 0)
					table.click(0, 2);
					// navigate next row - (2, 2)
					keyDown(shell.text(), SWT.ARROW_DOWN, (char)0);
					waitEventLoop(10);
					// set text
					SWTBotText text = shell.text();
					text.setText("b b");
					closeCellEditor(shell);
					waitEventLoop(10);
					// check
					assertItems(
							table,
							new String[] { "key.1", "1 1", "a b" },
							new String[] { "key.2", "2 1", "b b" });
				}
				// prev column/row
				{
					// activate editor at (2, 1)
					table.click(1, 2);
					// prev column
					shell.text().traverse(Traverse.TAB_PREVIOUS);
					waitEventLoop(10);
					shell.text().setText("b a");
					// prev row
					keyDown(shell.text(), SWT.ARROW_UP, (char)0);
					waitEventLoop(10);
					SWTBotText text = shell.text();
					text.setText("a a");
					closeCellEditor(shell);
					// check
					waitEventLoop(10);
					assertItems(
							table,
							new String[] { "key.1", "a a", "a b" },
							new String[] { "key.2", "b a", "b b" });
				}
			}
		});
	}

	/**
	 * @return {@link SWTBotEditableSource} of this dialog.
	 */
	private static SWTBotEditableSource getEditableSource(SWTBot shell) throws Exception {
		SourceComposite composite = shell.getFinder().findControls(widgetOfType(SourceComposite.class)).get(0);
		IEditableSource editableSource = (IEditableSource) ReflectionUtils.getFieldObject(composite, "m_source");
		return new SWTBotEditableSource(editableSource);
	}

	/**
	 * @return {@link SWTBotCTableCombo} of this dialog.
	 */
	private static SWTBotCTableCombo getLanguageCombo(SWTBot shell) {
		CTableCombo widget = shell.getFinder().findControls(widgetOfType(CTableCombo.class)).get(0);
		if (widget == null) {
			return null;
		}
		return new SWTBotCTableCombo(widget);
	}

	/**
	 * @return {@link SWTBotTableTooltipProvider} of this dialog.
	 */
	private static SWTBotTableTooltipProvider getTableToolTipProvider(SWTBot shell) throws Exception {
		return new SWTBotTableTooltipProvider(shell);
	}

	/**
	 * Creates a key event and notifies the given widget. This shouldn't be done by
	 * calling the "key pressed" methods provided by SWTBot, as this may lead to
	 * concurrency issues and crashes on GTK.
	 */
	private static void keyDown(AbstractSWTBotControl<?> bot, int key, char c) {
		EventSender eventSender = new EventSender(bot.widget);
		UIThreadRunnable.syncExec(() -> eventSender.keyDown(key, c));
	}

	/**
	 * Closes the currently active cell editor. This shouldn't be done by simulating
	 * an {@code Enter} event, as this may lead to concurrency issues and crashes on
	 * GTK.
	 */
	private static void closeCellEditor(SWTBot shell) {
		TableViewer tableViewer = getTableViewer(shell);
		UIThreadRunnable.syncExec(() -> tableViewer.applyEditorValue());
	}

	private static TableViewer getTableViewer(SWTBot shell) {
		SourceComposite composite = shell.widget(WidgetOfType.widgetOfType(SourceComposite.class));
		return (TableViewer) ReflectionUtils.getFieldObject(composite, "m_viewer");
	}

	/**
	 * Wrapper for the {@link ITableTooltipProvider} to support creation of
	 * tool-tips outside the UI thread.
	 */
	private static class SWTBotTableTooltipProvider extends SWTBot implements Closeable {
		private final Shell shell;
		private final ITableTooltipProvider provider;

		public SWTBotTableTooltipProvider(SWTBot parent) throws Exception {
			SourceComposite composite = parent.getFinder().findControls(widgetOfType(SourceComposite.class)).get(0);
			provider = (ITableTooltipProvider) ReflectionUtils.invokeMethod(composite, "createTooltipProvider()");
			shell = UIThreadRunnable.syncExec((Result<Shell>) Shell::new);
		}

		public Control createTooltipControl(SWTBotTableItem tableItem, int column) {
			return UIThreadRunnable.syncExec(() -> provider.createTooltipControl(tableItem.widget, shell, column));
		}

		public Control[] getChildren() {
			return UIThreadRunnable.syncExec(shell::getChildren);
		}

		@Override
		public void close() throws IOException {
			UIThreadRunnable.syncExec(shell::dispose);
		}
	}

	/**
	 * Waits given number of milliseconds and runs events loop every 1 millisecond.
	 * At least one events loop will be executed.
	 */
	protected static void waitEventLoop(int time) {
		UIThreadRunnable.syncExec(() -> waitEventLoop(time, 0));
	}
}
