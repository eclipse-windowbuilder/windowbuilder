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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.rcp.model.rcp.AbstractSplashHandlerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link AbstractSplashHandlerInfo}.
 * 
 * @author scheglov_ke
 */
public class AbstractSplashHandlerTest extends RcpModelTest {
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
    AbstractSplashHandlerInfo splash =
        parseJavaInfo(
            "import org.eclipse.ui.splash.AbstractSplashHandler;",
            "public class Test extends AbstractSplashHandler {",
            "  public Test() {",
            "  }",
            "  public void init(Shell splash) {",
            "    super.init(splash);",
            "    Composite container = new Composite(getSplash(), SWT.NULL);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.ui.splash.AbstractSplashHandler} {this} {/new Composite(getSplash(), SWT.NULL)/}",
        "  {parameter} {splash} {/super.init(splash)/}",
        "    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(getSplash(), SWT.NULL)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
    // refresh()
    splash.refresh();
    assertNoErrors(splash);
  }

  /**
   * Test for {@link AbstractSplashHandler_TopBoundsSupport}.
   * 
   * @throws Exception
   */
  public void test_topBoundsSupport() throws Exception {
    AbstractSplashHandlerInfo splash =
        parseJavaInfo(
            "import org.eclipse.ui.splash.AbstractSplashHandler;",
            "public class Test extends AbstractSplashHandler {",
            "  public Test() {",
            "  }",
            "  public void init(Shell splash) {",
            "    super.init(splash);",
            "    Composite container = new Composite(getSplash(), SWT.NULL);",
            "  }",
            "}");
    splash.refresh();
    CompositeInfo container = getJavaInfoByName("container");
    // check bounds
    assertThat(splash.getBounds().width).isEqualTo(450);
    assertThat(splash.getBounds().height).isEqualTo(300);
    assertThat(container.getBounds().width).isGreaterThan(400);
    assertThat(container.getBounds().height).isGreaterThan(250);
    // set bounds
    splash.getTopBoundsSupport().setSize(600, 500);
    splash.refresh();
    assertThat(splash.getBounds().width).isEqualTo(600);
    assertThat(splash.getBounds().height).isEqualTo(500);
  }
}