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
package org.eclipse.wb.tests.designer.databinding.swing;

import org.eclipse.wb.internal.core.databinding.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.databinding.parser.ParseState;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 *
 * @author sablin_aa
 *
 */
public abstract class AbstractBindingTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    DatabindingTestUtils.configure(m_testProject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final DatabindingsProvider getDatabindingsProvider() throws Exception {
    ParseState parseState = DatabindingRootProcessor.STATES.get(m_lastEditor.getModelUnit());
    assertNotNull(parseState);
    assertNotNull(parseState.databindingsProvider);
    assertInstanceOf(DatabindingsProvider.class, parseState.databindingsProvider);
    return (DatabindingsProvider) parseState.databindingsProvider;
  }
}