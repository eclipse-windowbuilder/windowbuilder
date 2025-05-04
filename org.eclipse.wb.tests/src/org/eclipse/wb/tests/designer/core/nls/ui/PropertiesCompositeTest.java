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

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.NewSourceDialog;
import org.eclipse.wb.internal.core.nls.ui.NlsDialog;
import org.eclipse.wb.internal.core.nls.ui.PropertiesComposite;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link PropertiesComposite}.
 *
 * @author scheglov_ke
 */
@Ignore
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
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, "Properties");
				List sourcesList = UiContext.findFirstWidget(tabItems[0], List.class);
				assertEquals(0, sourcesList.getItemCount());
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
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems =
						assertItems(tabFolder, "test.messages", "test.messages2", "Properties");
				List sourcesList = UiContext.findFirstWidget(tabItems[2], List.class);
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
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, "test.messages", "Properties");
				List sourcesList = UiContext.findFirstWidget(tabItems[1], List.class);
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
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, "test.messages", "Properties");
				PropertiesComposite propertiesComposite = (PropertiesComposite) tabItems[1].getControl();
				// sources list
				List sourcesList = UiContext.findFirstWidget(propertiesComposite, List.class);
				assertNotNull(sourcesList);
				// properties tree
				Tree propertiesTree = UiContext.findFirstWidget(propertiesComposite, Tree.class);
				assertNotNull(propertiesTree);
				// "Externalize" button
				Button externalizeButton = context.getButtonByText("E&xternalize");
				assertNotNull(externalizeButton);
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
				TreeItem buttonItem = getItem(
						propertiesTree,
						"(javax.swing.JFrame)", "getContentPane()", "button");
				TreeItem buttonTextItem = getItem(buttonItem, new String[]{"text: New button"}, 0);
				// set checked "button" item
				{
					// check initial states
					assertFalse(externalizeButton.isEnabled());
					assertTrue(buttonItem.getGrayed());
					assertFalse(buttonItem.getChecked());
					assertFalse(buttonTextItem.getChecked());
					// check "button" item
					setChecked(buttonItem, true);
					// check state
					assertTrue(buttonItem.getChecked());
					assertTrue(buttonTextItem.getChecked());
					assertTrue(externalizeButton.isEnabled());
				}
				// clear selection in sources - "Externalize" button should be disabled
				{
					assertTrue(externalizeButton.isEnabled());
					sourcesList.deselectAll();
					sourcesList.notifyListeners(SWT.Selection, null);
					assertFalse(externalizeButton.isEnabled());
				}
				// select sole source - "Externalize" button should be enabled
				{
					sourcesList.select(0);
					sourcesList.notifyListeners(SWT.Selection, null);
					assertTrue(externalizeButton.isEnabled());
				}
				// check "&Enable all"
				{
					context.clickButton("&Enable all");
					assertTrue(buttonTextItem.getChecked());
				}
				// check "D&isable all"
				{
					context.clickButton("D&isable all");
					assertFalse(buttonTextItem.getChecked());
				}
				// do externalize
				{
					setChecked(buttonItem, true);
					context.clickButton("E&xternalize");
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
					IEditableSource editableSource = (IEditableSource) ReflectionUtils.invokeMethod(
							propertiesComposite,
							"getSelectedSource()");
					assertEquals(
							"New button",
							editableSource.getValue(LocaleInfo.DEFAULT, "Test.button.text"));
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
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				TabItem[] tabItems = assertItems(tabFolder, "Properties");
				List sourcesList = UiContext.findFirstWidget(tabItems[0], List.class);
				assertEquals(0, sourcesList.getItemCount());
				//
				context.executeAndCheck(new UIRunnable() {
					@Override
					public void run(UiContext context2) throws Exception {
						context2.clickButton("&New...");
					}
				}, new UIRunnable() {
					@Override
					public void run(UiContext context2) throws Exception {
						context2.useShell("New source");
						context2.clickButton("OK");
					}
				});
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link TreeItem} of given {@link Tree} on given path.
	 */
	private static TreeItem getItem(Tree tree, String... pathElements) {
		TreeItem[] treeItems = tree.getItems();
		assertTrue(treeItems.length <= 1);
		if (treeItems.length == 1) {
			TreeItem rootItem = treeItems[0];
			if (rootItem.getText().equals(pathElements[0])) {
				return getItem(rootItem, pathElements, 1);
			}
		}
		return null;
	}

	/**
	 * @return child {@link TreeItem} of given {@link TreeItem} on given path.
	 */
	private static TreeItem getItem(TreeItem item, String[] pathElements, int pathIndex) {
		if (pathElements.length == pathIndex) {
			return item;
		}
		String pathElement = pathElements[pathIndex];
		// check each child TreeItem
		TreeItem[] children = item.getItems();
		for (int i = 0; i < children.length; i++) {
			TreeItem child = children[i];
			if (child.getText().equals(pathElement)) {
				return getItem(child, pathElements, pathIndex + 1);
			}
		}
		// no child for current path element
		return null;
	}

	/**
	 * Sets the check state of {@link TreeItem}.
	 */
	private static void setChecked(TreeItem item, boolean checked) {
		item.setChecked(checked);
		// send notification
		{
			Event event = new Event();
			event.item = item;
			event.detail = SWT.CHECK;
			//
			Tree tree = item.getParent();
			tree.notifyListeners(SWT.Selection, event);
		}
	}
}
