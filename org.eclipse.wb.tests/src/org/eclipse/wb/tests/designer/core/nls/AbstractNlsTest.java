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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Abstract test for NLS.
 *
 * @author scheglov_ke
 */
public class AbstractNlsTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    // process UI messages (without this we have exception from Java UI)
    waitEventLoop(1);
    //
    super.tearDown();
    if (m_testProject != null) {
      deleteFiles(m_testProject.getJavaProject().getProject().getFolder("src"));
      waitForAutoBuild();
    }
  }
}
