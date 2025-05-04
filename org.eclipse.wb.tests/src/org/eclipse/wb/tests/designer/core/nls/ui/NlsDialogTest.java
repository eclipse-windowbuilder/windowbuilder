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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.Test;

/**
 * Tests for {@link NlsDialog}.
 *
 * @author scheglov_ke
 */
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
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) {
				SWTBot shell = bot.shell("Externalize strings").bot();
				SWTBotTabItem properties = shell.tabItem("Properties");
				assertTrue(properties.isActive());
				assertItems(shell, "Properties");
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
				SWTBotTabItem messagesTab = shell.tabItem("test.messages");
				assertFalse(messagesTab.isActive());
				SWTBotTabItem messages2Tab = shell.tabItem("test.messages2");
				assertFalse(messages2Tab.isActive());
				SWTBotTabItem properties = shell.tabItem("Properties");
				assertTrue(properties.isActive());
				// check possible sources: 0
				{
					messagesTab.activate();
					SWTBotTable table = shell.tableWithLabel("Strings:");
					assertColumns(table, "Key", "(default)");
					assertItems(table, new String[] { "frame.title", "My JFrame" });
				}
				// check possible sources: 1
				{
					messages2Tab.activate();
					SWTBotTable table = shell.tableWithLabel("Strings:");
					assertColumns(table, "Key", "(default)");
					assertItems(table, new String[] { "frame.name", "My name" });
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
		openDialogNLS(initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) {
				SWTBot shell = bot.shell("Externalize strings").bot();
				assertItems(shell, "test.messages", "Properties");
				SWTBotTabItem messagesTab = shell.tabItem("test.messages");
				assertTrue(messagesTab.isActive());
				// check source
				SWTBotTable table = shell.tableWithLabel("Strings:");
				assertColumns(table, "Key", "(default)", "it");
				assertItems(table,
						new String[] { "frame.name", "My name", "" },
						new String[] { "frame.title", "My JFrame", "My JFrame IT" });
			}
		});
	}
}
