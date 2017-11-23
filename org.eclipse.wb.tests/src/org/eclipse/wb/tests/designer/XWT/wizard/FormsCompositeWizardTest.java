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

import static org.assertj.core.api.Assertions.assertThat;

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
  public void test_onlyForXWT() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new FormsCompositeWizard(), new StructuredSelection(m_packageFragment));
      }
    },
        new UIRunnable() {
          public void run(UiContext context) throws Exception {
            context.useShell("New XWT Composite");
            context.getTextByLabel("Name:").setText("MyComposite");
            context.clickButton("Finish");
          }
        });
    // Java
    {
      String content = getFileContentSrc("test/MyComposite.java");
      assertThat(content).contains("public class MyComposite extends Composite {");
      assertThat(content).contains("XWTForms.loadWithOptions(");
    }
    // XWT
    {
      String content = getFileContentSrc("test/MyComposite.xwt");
      assertThat(content).contains("<Composite ");
      assertThat(content).contains("x:Class=\"test.MyComposite\"");
      assertThat(content).contains("<RowLayout/>");
      assertThat(content).contains("<Button text=");
      assertThat(content).contains("<!-- Forms API -->");
    }
  }
}