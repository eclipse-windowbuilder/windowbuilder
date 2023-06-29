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

import org.eclipse.wb.core.controls.CTableCombo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.NlsDialog;
import org.eclipse.wb.internal.core.nls.ui.SourceComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils.ITableTooltipProvider;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

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
	// XXX
	public void _test_ITableTooltipProvider() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				Table table = getSourceTable(context, tabItems[0]);
				// prepare provider
				ITableTooltipProvider provider;
				{
					SourceComposite sourceComposite = getSourceComposite(context, tabItems[0]);
					provider = (ITableTooltipProvider) ReflectionUtils.invokeMethod(
							sourceComposite,
							"createTooltipProvider()");
				}
				// check items
				assertItems(
						table,
						new String[][]{
							new String[]{"frame.name", "My name"},
							new String[]{"frame.title", "My JFrame"},});
				//
				Shell newShell = new Shell();
				try {
					// not first column
					{
						Control control = provider.createTooltipControl(table.getItem(1), newShell, 1);
						assertNull(control);
					}
					// no components
					{
						Control control = provider.createTooltipControl(table.getItem(0), newShell, 0);
						assertNull(control);
					}
					// one component
					{
						Control control = provider.createTooltipControl(table.getItem(1), newShell, 0);
						assertNotNull(control);
						assertTrue(newShell.getChildren().length > 0);
					}
				} finally {
					newShell.dispose();
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_contextMenu_removeLocale() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		setFileContentSrc(
				"test/messages_it.properties",
				getSourceDQ("frame.title=My JFrame IT", "frame.name=My name IT"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				Table table = getSourceTable(context, tabItems[0]);
				Menu tableMenu = table.getMenu();
				EventSender eventSender = new EventSender(table);
				//
				eventSender.postMouseMove(getItemLocation(table, 2, 10));
				tableMenu.notifyListeners(SWT.Show, null);
				final MenuItem removeLocaleItem = findMenuItem(tableMenu, "Remove locale...");
				assertNotNull(removeLocaleItem);
				// don't confirm, no changes expected
				{
					context.executeAndCheck(new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							removeLocaleItem.notifyListeners(SWT.Selection, null);
						}
					}, new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							ctx.useShell("Confirm");
							ctx.clickButton("Cancel");
							ctx.popShell();
						}
					});
					// check items
					assertItems(
							table,
							new String[][]{
								new String[]{"frame.name", "My name", "My name IT"},
								new String[]{"frame.title", "My JFrame", "My JFrame IT"},});
				}
				// confirm
				{
					context.executeAndCheck(new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							removeLocaleItem.notifyListeners(SWT.Selection, null);
						}
					}, new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							ctx.useShell("Confirm");
							ctx.clickButton("OK");
							ctx.popShell();
						}
					});
					// check items
					assertItems(
							table,
							new String[][]{
								new String[]{"frame.name", "My name"},
								new String[]{"frame.title", "My JFrame"},});
				}
			}
		});
		// 'it' properties should be deleted
		assertFalse(getFileSrc("test/messages_it.properties").exists());
	}

	public void test_contextMenu_internalizeKey() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				Table table = getSourceTable(context, tabItems[0]);
				Menu tableMenu = table.getMenu();
				EventSender eventSender = new EventSender(table);
				//
				table.select(0);
				eventSender.postMouseMove(getItemLocation(table, 0, 0));
				tableMenu.notifyListeners(SWT.Show, null);
				final MenuItem internalizeItem = findMenuItem(tableMenu, "Internalize key...");
				assertNotNull(internalizeItem);
				// don't confirm, no changes expected
				{
					context.executeAndCheck(new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							internalizeItem.notifyListeners(SWT.Selection, null);
						}
					}, new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							ctx.useShell("Confirm");
							ctx.clickButton("Cancel");
							ctx.popShell();
						}
					});
					// check items
					assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame"}});
				}
				// confirm
				{
					context.executeAndCheck(new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							internalizeItem.notifyListeners(SWT.Selection, null);
						}
					}, new UIRunnable() {
						@Override
						public void run(UiContext ctx) throws Exception {
							ctx.useShell("Confirm");
							ctx.clickButton("OK");
							ctx.popShell();
						}
					});
					// check items
					assertItems(table, new String[][]{});
				}
			}
		});
		// check source
		assertEditor(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle('My JFrame');",
				"  }",
				"}");
		assertFalse(getFileContentSrc("test/messages.properties").contains("frame.title"));
	}

	public void test_contextMenu_addLocale() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				Table table = getSourceTable(context, tabItems[0]);
				Menu tableMenu = table.getMenu();
				EventSender eventSender = new EventSender(table);
				//
				table.select(0);
				eventSender.postMouseMove(getItemLocation(table, 0, 10));
				tableMenu.notifyListeners(SWT.Show, null);
				final MenuItem addLocaleItem = findMenuItem(tableMenu, "Add locale...");
				assertNotNull(addLocaleItem);
				//
				context.executeAndCheck(new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						addLocaleItem.notifyListeners(SWT.Selection, null);
					}
				}, new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						ctx.useShell("Choose Locale");
						// select 'it' language
						CTableCombo languagesCombo = ctx.findFirstWidget(CTableCombo.class);
						for (int i = 0; i < languagesCombo.getItemCount(); i++) {
							String item = languagesCombo.getItem(i);
							if (item.startsWith("it - ")) {
								languagesCombo.select(i);
								languagesCombo.notifyListeners(SWT.Selection, null);
								break;
							}
						}
						// click "OK"
						ctx.clickButton("OK");
						ctx.popShell();
					}
				});
				// check items
				assertColumns(table, new String[]{"Key", "(default)", "it"});
				assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame", "My JFrame"},});
			}
		});
		// we should have new locale - 'it'
		assertTrue(getFileSrc("test/messages_it.properties").exists());
	}

	public void test_contextMenu_addLocaleWithButton() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				context.executeAndCheck(new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						ctx.clickButton("New locale...");
					}
				}, new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						ctx.useShell("Choose Locale");
						ctx.clickButton("Cancel");
						ctx.popShell();
					}
				});
			}
		});
	}

	/**
	 * @return the {@link MenuItem} with given text or <code>null</code>.
	 */
	private static MenuItem findMenuItem(Menu menu, String text) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			MenuItem item = items[i];
			if (item.getText().equals(text)) {
				return item;
			}
		}
		// not found
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SourceComposite
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_SourceComposite_edit() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		setFileContentSrc("test/messages_it.properties", getSourceDQ(""));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				assertEquals(0, tabFolder.getSelectionIndex());
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				//
				SourceComposite sourceComposite = getSourceComposite(context, tabItems[0]);
				IEditableSource editableSource =
						(IEditableSource) ReflectionUtils.getFieldObject(sourceComposite, "m_source");
				Table table = getSourceTable(context, tabItems[0]);
				// check initial items
				{
					assertColumns(table, new String[]{"Key", "(default)", "it"});
					assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame", ""},});
				}
				// click to activate cell editor
				{
					// click on value to start edit
					clickItem(table, 1, 0, 1);
					// send new text and CR
					{
						UiContext.findFirstWidget(table, Text.class).setFocus();
						EventSender.sendText("New title");
						EventSender.sendKey(SWT.CR);
						waitEventLoop(10);
					}
				}
				// check after edit
				{
					assertTrue(editableSource.getKeys().contains("frame.title"));
					assertEquals("New title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title"));
					//
					assertItems(table, new String[][]{new String[]{"frame.title", "New title", ""},});
				}
				// rename key
				{
					clickItem(table, 0, 0, 1);
					{
						EventSender.sendText("frame.title2");
						EventSender.sendKey(SWT.CR);
						waitEventLoop(10);
					}
					// check
					{
						assertFalse(editableSource.getKeys().contains("frame.title"));
						assertTrue(editableSource.getKeys().contains("frame.title2"));
						assertEquals("New title", editableSource.getValue(LocaleInfo.DEFAULT, "frame.title2"));
						//
						assertItems(table, new String[][]{new String[]{"frame.title2", "New title", ""},});
					}
				}
				// update 'it'
				{
					LocaleInfo localeInfo = new LocaleInfo(Locale.ITALIAN);
					assertNull(editableSource.getValue(localeInfo, "frame.title2"));
					// modify
					{
						clickItem(table, 2, 0, 1);
						EventSender.sendText("title IT");
						EventSender.sendKey(SWT.CR);
						waitEventLoop(10);
					}
					// check
					{
						assertEquals("title IT", editableSource.getValue(localeInfo, "frame.title2"));
						assertItems(
								table,
								new String[][]{new String[]{"frame.title2", "New title", "title IT"},});
					}
				}
				// wait UI
				//waitEventLoop(5000);
			}
		});
	}

	public void test_SourceComposite_update_externalize() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"    setName(\"My name\");",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				assertEquals(0, tabFolder.getSelectionIndex());
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				//
				SourceComposite sourceComposite = getSourceComposite(context, tabItems[0]);
				IEditableSource editableSource =
						(IEditableSource) ReflectionUtils.getFieldObject(sourceComposite, "m_source");
				Table table = getSourceTable(context, tabItems[0]);
				// check initial items
				{
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame"},});
				}
				// externalize "name"
				{
					GenericProperty nameProperty =
							(GenericProperty) m_contentJavaInfo.getPropertyByTitle("name");
					editableSource.externalize(new StringPropertyInfo(nameProperty), true);
				}
				// check items
				{
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(
							table,
							new String[][]{
								new String[]{"frame.title", "My JFrame"},
								new String[]{"Test.this.name", "My name"}});
				}
			}
		});
	}

	public void test_SourceComposite_update_renameOver() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"    setName(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.name\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				assertEquals(0, tabFolder.getSelectionIndex());
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				//
				SourceComposite sourceComposite = getSourceComposite(context, tabItems[0]);
				final IEditableSource editableSource =
						(IEditableSource) ReflectionUtils.getFieldObject(sourceComposite, "m_source");
				Table table = getSourceTable(context, tabItems[0]);
				// check initial items
				{
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(
							table,
							new String[][]{
								new String[]{"frame.name", "My name"},
								new String[]{"frame.title", "My JFrame"},});
				}
				// rename "frame.name" -> "frame.title"
				context.executeAndCheck(new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						editableSource.renameKey("frame.name", "frame.title");
					}
				}, new UIRunnable() {
					@Override
					public void run(UiContext ctx) throws Exception {
						ctx.useShell("Confirm");
						ctx.clickButton("Yes, keep existing value");
						ctx.popShell();
					}
				});
				// check items
				{
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame"}});
				}
			}
		});
	}

	public void test_SourceComposite_onlyCurrentForm() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("frame.title=My JFrame", "frame.name=My name"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"import java.util.ResourceBundle;",
				"public class Test extends JFrame {",
				"  public Test() {",
				"    setTitle(ResourceBundle.getBundle(\"test.messages\").getString(\"frame.title\")); //$NON-NLS-1$ //$NON-NLS-2$",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				assertEquals(0, tabFolder.getSelectionIndex());
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				//
				//waitEventLoop(5000);
				SourceComposite sourceComposite = getSourceComposite(context, tabItems[0]);
				Table table = getSourceTable(context, tabItems[0]);
				// check initial items
				assertItems(
						table,
						new String[][]{
							new String[]{"frame.name", "My name"},
							new String[]{"frame.title", "My JFrame"},});
				// check "Show strings only for current form"
				{
					Button onlyFormButton =
							context.getButtonByText(sourceComposite, "Show strings only for current form");
					onlyFormButton.setSelection(true);
					context.click(onlyFormButton);
				}
				// only 'frame.title' expected
				assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame"}});
			}
		});
	}

	public void test_SourceComposite_navigation() throws Exception {
		setFileContentSrc(
				"test/messages.properties",
				getSourceDQ("#Direct ResourceBundle", "key.1=1 1", "key.2=2 1"));
		setFileContentSrc(
				"test/messages_it.properties",
				getSourceDQ("#Direct ResourceBundle", "key.1=1 2", "key.2=2 2"));
		waitForAutoBuild();
		//
		String initialSource = getTestSource(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  public Test() {",
				"  }",
				"}");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				tabFolder.setSelection(0);
				Table table = getSourceTable(context, tabFolder.getItems()[0]);
				// check initial items
				assertItems(
						table,
						new String[][]{
							new String[]{"key.1", "1 1", "1 2"},
							new String[]{"key.2", "2 1", "2 2"},});
				// check next column
				{
					// activate editor at (1, 0)
					clickItem(table, 1, 0, 1);
					// navigate next column - (1, 2)
					EventSender.sendKey(SWT.TAB);
					// set text
					EventSender.sendText("a b");
					EventSender.sendKey(SWT.CR);
					waitEventLoop(10);
					// check
					assertItems(
							table,
							new String[][]{
								new String[]{"key.1", "1 1", "a b"},
								new String[]{"key.2", "2 1", "2 2"},});
				}
				// check next row
				{
					// activate editor at (2, 0)
					clickItem(table, 2, 0, 1);
					// navigate next row - (2, 2)
					EventSender.sendKey(SWT.ARROW_DOWN);
					// set text
					EventSender.sendText("b b");
					EventSender.sendKey(SWT.CR);
					waitEventLoop(10);
					// check
					assertItems(
							table,
							new String[][]{
								new String[]{"key.1", "1 1", "a b"},
								new String[]{"key.2", "2 1", "b b"},});
				}
				// prev column/row
				{
					// activate editor at (2, 1)
					clickItem(table, 2, 1, 1);
					// prev column
					EventSender.sendKey(SWT.SHIFT, SWT.TAB);
					EventSender.sendText("b a");
					// prev row
					EventSender.sendKey(SWT.ARROW_UP);
					EventSender.sendText("a a");
					EventSender.sendKey(SWT.CR);
					// check
					waitEventLoop(10);
					assertItems(
							table,
							new String[][]{
								new String[]{"key.1", "a a", "a b"},
								new String[]{"key.2", "b a", "b b"},});
				}
			}
		});
	}
}
