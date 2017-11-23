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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.ScrolledFormInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ScrolledFormInfo}.
 * 
 * @author scheglov_ke
 */
public class ScrolledFormTest extends AbstractFormsTest {
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
  public void test_0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ScrolledForm form = new ScrolledForm(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);",
            "  }",
            "}");
    shell.refresh();
    ScrolledFormInfo form = (ScrolledFormInfo) shell.getChildrenControls().get(0);
    // we use constructor with style, so we have "Style" property
    assertNotNull(form.getPropertyByTitle("Style"));
    // we have single child CompositeInfo for getBody()
    {
      List<ControlInfo> controls = form.getChildrenControls();
      assertThat(controls).hasSize(1);
      CompositeInfo body = (CompositeInfo) controls.get(0);
      assertThat(body.toString()).contains("getBody()");
    }
  }
}