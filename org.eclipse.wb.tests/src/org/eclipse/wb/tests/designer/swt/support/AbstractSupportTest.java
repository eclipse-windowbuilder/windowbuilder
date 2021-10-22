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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.AbstractSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Abstract test for any {@link AbstractSupport}.
 *
 * @author lobas_av
 */
public abstract class AbstractSupportTest extends RcpModelTest {
  protected CompositeInfo m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    {
      String[] lines = getTestSource();
      m_shell = parseComposite(lines);
      m_shell.refresh();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (m_shell != null) {
      m_shell.refresh_dispose();
      m_shell = null;
    }
  }

  protected String[] getTestSource() {
    return new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "  // filler",
        "  }",
        "}"};
  }
}