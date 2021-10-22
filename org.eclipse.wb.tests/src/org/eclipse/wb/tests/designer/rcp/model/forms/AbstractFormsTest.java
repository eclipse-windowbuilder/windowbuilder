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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import junit.framework.TestCase;

/**
 * Abstract {@link TestCase} for all "Forms API" tests.
 *
 * @author scheglov_ke
 */
public class AbstractFormsTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>FormToolkit</code> instance from last parsed {@link JavaInfo}.
   */
  protected final InstanceFactoryInfo getToolkit() throws Exception {
    return InstanceFactoryInfo.getFactories(
        m_lastParseInfo,
        m_lastLoader.loadClass("org.eclipse.ui.forms.widgets.FormToolkit")).get(0);
  }
}