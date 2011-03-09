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
package org.eclipse.wb.tests.designer.ercp.gef;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;
import org.eclipse.wb.tests.designer.ercp.ETestUtils;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Abstract test for eRCO in editor.
 * 
 * @author scheglov_ke
 */
public abstract class ErcpGefTest extends DesignerEditorTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureDefaults(org.eclipse.wb.internal.ercp.ToolkitProvider.DESCRIPTION);
    if (m_testProject == null) {
      do_projectCreate();
      ETestUtils.configure(m_testProject);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final CompositeInfo openComposite(String... lines) throws Exception {
    ICompilationUnit unit = createModelCompilationUnit("test", "Test.java", getTestSource(lines));
    openDesign(unit);
    // prepare models
    return (CompositeInfo) m_contentJavaInfo;
  }

  /**
   * Asserts that active {@link AstEditor} has expected eRCP source.
   */
  protected final void assertEditor(String... lines) {
    assertEditor(getTestSource(lines), m_lastEditor);
  }

  /**
   * @return the source for eRCP.
   */
  protected final String getTestSource(String... lines) {
    lines = getDoubleQuotes(lines);
    lines = getTestSource_decorate(lines);
    return getSource(lines);
  }

  /**
   * "Decorates" given lines of source, usually adds required imports.
   */
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.events.*;",
            "import org.eclipse.swt.graphics.*;",
            "import org.eclipse.swt.widgets.*;",
            "import org.eclipse.swt.layout.*;",
            "import org.eclipse.ercp.swt.mobile.*;",
            "import org.eclipse.jface.viewers.*;",
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.jface.resource.*;",
            "import org.eclipse.ui.part.*;"}, lines);
    return lines;
  }
}
