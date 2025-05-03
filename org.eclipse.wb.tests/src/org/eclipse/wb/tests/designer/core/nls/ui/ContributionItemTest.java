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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.ExternalizeStringsContributionItem;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import javax.swing.JFrame;

/**
 * Tests for {@link ExternalizeStringsContributionItem}.
 *
 * @author scheglov_ke
 */
@Ignore
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
			UiContext context = new UiContext();
			// check locales combo, and switch to "it" locale
			assertNotNull("NLS dialog item not found.", m_dialogItem);
			// initialize menu creation
			context.click(m_dialogItem.widget, SWT.ARROW);
			// find locales menu inside display popups
			Menu localesMenu = context.getLastPopup();
			assertNotNull("Can not find locales menu.", localesMenu);
			{
				// check available locales
				final String[] requiredLocales = new String[]{"(default)", "it"};
				MenuItem[] availableLocaleItems = localesMenu.getItems();
				assertEquals(requiredLocales.length, availableLocaleItems.length);
				for (int i = 0; i < requiredLocales.length; i++) {
					assertEquals(requiredLocales[i], availableLocaleItems[i].getText());
					if (i == /*selecting*/1) {
						context.selectMenuItem(availableLocaleItems[i]);
					}
				}
			}
			localesMenu.setVisible(false);
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
		openDialogNLS("", initialSource, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("Can't Externalize");
				// click "OK"
				List<Button> buttons = context.findWidgets(Button.class);
				context.click(buttons.get(0));
				// main window shell expected
				assertSame(DesignerPlugin.getShell(), context.getActiveShell());
			}
		});
	}
}
