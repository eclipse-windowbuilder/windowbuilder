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
package org.eclipse.wb.tests.designer.XWT.wizard;

import org.eclipse.wb.internal.xwt.wizards.FormsApplicationWizard;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.viewers.StructuredSelection;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for {@link FormsApplicationWizard}.
 *
 * @author scheglov_ke
 */
public class FormsApplicationWizardTest extends XwtWizardTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contents() throws Exception {
		animateWizard();
		// Java
		{
			String content = getFileContentSrc("test/MyApp.java");
			Assertions.assertThat(content).contains("main(String args[])");
			Assertions.assertThat(content).contains("XWTForms.load");
			Assertions.assertThat(content).contains(".readAndDispatch()");
		}
		// XWT
		{
			String content = getFileContentSrc("test/MyApp.xwt");
			Assertions.assertThat(content).contains("<Shell");
			Assertions.assertThat(content).contains("<RowLayout/>");
			Assertions.assertThat(content).contains("<Button text=");
			Assertions.assertThat(content).contains("<!-- Forms API -->");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void animateWizard() throws Exception {
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				TestUtils.runWizard(
						new FormsApplicationWizard(),
						new StructuredSelection(m_packageFragment));
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("New XWT Application");
				context.getTextByLabel("Name:").setText("MyApp");
				context.clickButton("Finish");
			}
		});
	}
}