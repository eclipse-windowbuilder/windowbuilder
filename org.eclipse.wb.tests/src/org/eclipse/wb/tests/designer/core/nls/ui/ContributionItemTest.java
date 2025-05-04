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

import org.eclipse.wb.internal.core.nls.ExternalizeStringsContributionItem;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.Test;

import java.util.List;

import javax.swing.JFrame;

/**
 * Tests for {@link ExternalizeStringsContributionItem}.
 *
 * @author scheglov_ke
 */
public class ContributionItemTest extends AbstractNlsUiTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_localesCombo() throws Exception {
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
		waitForAutoBuild();
		// open editor
		openContainer("""
				import java.util.ResourceBundle;
				public class Test extends JFrame {
					public Test() {
						setTitle(ResourceBundle.getBundle("test.messages").getString("frame.title")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}""");
		// check default title
		assertEquals("My JFrame", ((JFrame) m_contentJavaInfo.getObject()).getTitle());
		{
			// check locales combo, and switch to "it" locale
			assertNotNull("NLS dialog item not found.", m_dialogItem);
			// initialize menu creation
			new UiContext().executeAndCheck(() -> {
				// noop
			}, bot -> {
				SWTBotRootMenu contextMenu = m_dialogItem.externalizeMenu();
				final String[] requiredLocales = new String[] { "(default)", "it" };
				List<String> availableLocaleItems = contextMenu.menuItems();
				assertEquals(requiredLocales.length, availableLocaleItems.size());
				for (int i = 0; i < requiredLocales.length; i++) {
					assertEquals(requiredLocales[i], availableLocaleItems.get(i));
					if (i == /*selecting*/1) {
						contextMenu.menu(requiredLocales[i]).click();
					}
				}
				UIThreadRunnable.syncExec(() -> contextMenu.widget.dispose());
			});
		}
		// check title=it
		assertEquals("My JFrame IT", ((JFrame) m_contentJavaInfo.getObject()).getTitle());
		{
			// do refresh, to check that "it" locale still active
			LocaleInfo localeInfo = AbstractSource.getLocaleInfo(m_contentJavaInfo);
			m_designPage.refreshGEF();
			assertEquals(localeInfo, AbstractSource.getLocaleInfo(m_contentJavaInfo));
			// FIXME assertEquals("My JFrame IT", ((JFrame) m_contentJavaInfo.getObject()).getTitle());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// NLS dialog
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Strings in default package can not be externalized.
	 */
	@Test
	public void test_defaultPackage() throws Exception {
		String initialSource =
				getSourceDQ(
						"public class Test extends javax.swing.JFrame {",
						"  public Test() {",
						"    setTitle('My JFrame');",
						"  }",
						"}");
		openDialogNLS("", initialSource, new FailableBiConsumer<UiContext, SWTBot, Exception>() {
			@Override
			public void accept(UiContext context, SWTBot bot) throws Exception {
				// click "OK"
				bot.shell("Can't Externalize").bot().button("OK").click();
			}
		});
	}
}
