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

import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.UiContext;
import org.eclipse.wb.tests.utils.SWTBotExternalizeDropDownButton;

import static org.eclipse.swtbot.swt.finder.matchers.WithTooltip.withTooltip;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.SWTBot;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;

/**
 * Abstract test for NLS UI.
 *
 * @author scheglov_ke
 */
public abstract class AbstractNlsUiTest extends SwingGefTest {
	protected SWTBotExternalizeDropDownButton m_dialogItem;

	////////////////////////////////////////////////////////////////////////////
	//
	// Design
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void fetchContentFields() {
		super.fetchContentFields();
		{
			// NLS dialog item
			SWTBot bot = new SWTBot(m_designerEditor.getRootControl());
			ToolItem widget = (ToolItem) bot.getFinder().findControls(withTooltip("Externalize strings")).getFirst();
			m_dialogItem = new SWTBotExternalizeDropDownButton(widget);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates compilation unit, opens Design page, opens NLS dialog and then run
	 * given {@link FailableBiConsumer}.
	 */
	protected final void openDialogNLS(String initialSource, FailableBiConsumer<UiContext, SWTBot, Exception> consumer)
			throws Exception {
		openDialogNLS("test", initialSource, consumer);
	}

	/**
	 * Creates compilation unit, opens Design page, opens NLS dialog and then run
	 * given {@link FailableBiConsumer}.
	 */
	protected final void openDialogNLS(String packageName, String initialSource,
			FailableBiConsumer<UiContext, SWTBot, Exception> consumer)
			throws Exception {
		ICompilationUnit unit = createModelCompilationUnit(packageName, "Test.java", initialSource);
		openDesign(unit);
		// click on "Externalize strings" item
		UiContext context = new UiContext();
		context.executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				m_dialogItem.click();
			}
		}, new FailableConsumer<SWTBot, Exception> () {
			@Override
			public void accept(SWTBot bot) throws Exception {
				consumer.accept(context, bot);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		// process UI messages (without this we have exception from Java UI)
		waitEventLoop(1);
		//
		super.tearDown();
		if (m_testProject != null) {
			deleteFiles(m_testProject.getJavaProject().getProject().getFolder("src"));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Project life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeAll
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}
}
