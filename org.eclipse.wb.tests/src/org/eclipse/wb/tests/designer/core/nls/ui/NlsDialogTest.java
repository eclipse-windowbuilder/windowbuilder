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
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link NlsDialog}.
 *
 * @author scheglov_ke
 */
@Ignore
public class NlsDialogTest extends AbstractDialogTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Sources
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_noSources() throws Exception {
		String initialSource = getTestSource("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		openDialogNLS(initialSource, new NLSDialogRunnable() {
			@Override
			public void run(UiContext context, NlsDialog dialog, TabFolder tabFolder) throws Exception {
				assertEquals(0, tabFolder.getSelectionIndex());
				assertItems(tabFolder, new String[]{"Properties"});
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
				assertEquals(2, tabFolder.getSelectionIndex());
				TabItem[] tabItems =
						assertItems(tabFolder, new String[]{"test.messages", "test.messages2", "Properties"});
				// check possible sources: 0
				{
					Table table = getSourceTable(context, tabItems[0]);
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(table, new String[][]{new String[]{"frame.title", "My JFrame"}});
				}
				// check possible sources: 1
				{
					Table table = getSourceTable(context, tabItems[1]);
					assertColumns(table, new String[]{"Key", "(default)"});
					assertItems(table, new String[][]{new String[]{"frame.name", "My name"}});
				}
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
				assertEquals(0, tabFolder.getSelectionIndex());
				TabItem[] tabItems = assertItems(tabFolder, new String[]{"test.messages", "Properties"});
				// check source
				Table table = getSourceTable(context, tabItems[0]);
				assertColumns(table, new String[]{"Key", "(default)", "it"});
				assertItems(table, new String[][]{
					new String[]{"frame.name", "My name", ""},
					new String[]{"frame.title", "My JFrame", "My JFrame IT"},});
			}
		});
	}
}
