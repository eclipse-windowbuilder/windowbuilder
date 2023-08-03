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

import org.eclipse.wb.internal.xwt.wizards.FormsCompositeWizard;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.viewers.StructuredSelection;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for {@link FormsCompositeWizard}.
 *
 * @author scheglov_ke
 */
public class FormsCompositeWizardTest extends XwtWizardTest {
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
	@DisposeProjectAfter
	@Test
	public void test_onlyForXWT() throws Exception {
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				TestUtils.runWizard(new FormsCompositeWizard(), new StructuredSelection(m_packageFragment));
			}
		},
				new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("New XWT Composite");
				context.getTextByLabel("Name:").setText("MyComposite");
				context.clickButton("Finish");
			}
		});
		// Java
		{
			String content = getFileContentSrc("test/MyComposite.java");
			Assertions.assertThat(content).contains("public class MyComposite extends Composite {");
			Assertions.assertThat(content).contains("XWTForms.loadWithOptions(");
		}
		// XWT
		{
			String content = getFileContentSrc("test/MyComposite.xwt");
			Assertions.assertThat(content).contains("<Composite ");
			Assertions.assertThat(content).contains("x:Class=\"test.MyComposite\"");
			Assertions.assertThat(content).contains("<RowLayout/>");
			Assertions.assertThat(content).contains("<Button text=");
			Assertions.assertThat(content).contains("<!-- Forms API -->");
		}
	}
}