/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.e4;

import org.eclipse.wb.internal.rcp.model.e4.E4PartInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link E4PartInfo}.
 * 
 * @author scheglov_ke
 */
public class E4PartTest extends RcpModelTest {
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
    E4PartInfo part =
        parseJavaInfo(
            "import javax.annotation.PostConstruct;",
            "import javax.annotation.PreDestroy;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "  @PostConstruct",
            "  public void createControls(Composite parent) {",
            "    parent.setLayout(new GridLayout(1, false));",
            "    Button button = new Button(parent, SWT.NONE);",
            "  }",
            "}");
    assertHierarchy(
        "{parameter} {parent} {/parent.setLayout(new GridLayout(1, false))/ /new Button(parent, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.GridLayout} {empty} {/parent.setLayout(new GridLayout(1, false))/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(parent, SWT.NONE)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
    part.refresh();
    assertNoErrors(part);
    // check bounds
    assertThat(part.getBounds().width).isEqualTo(450);
    assertThat(part.getBounds().height).isEqualTo(300);
  }
}