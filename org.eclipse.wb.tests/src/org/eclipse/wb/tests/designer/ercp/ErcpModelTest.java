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
package org.eclipse.wb.tests.designer.ercp;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.ercp.ErcpToolkitDescription;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Abstract super class for eRCP tests.
 * 
 * @author scheglov_ke
 */
public abstract class ErcpModelTest extends AbstractJavaInfoTest {
  protected final ToolkitDescription m_toolkit = ErcpToolkitDescription.INSTANCE;
  private boolean m_convertSingleQuotesToDouble = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
      ETestUtils.configure(m_testProject);
      configureNewProject();
    }
    configureForTestPreferences(m_toolkit.getPreferences());
  }

  @Override
  protected void configureToolkits() {
    super.configureToolkits();
    if (EnvironmentUtils.IS_WINDOWS && !EnvironmentUtils.IS_64BIT_OS) {
      configureDefaults(org.eclipse.wb.internal.ercp.ToolkitProvider.DESCRIPTION);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    configureDefaultPreferences(m_toolkit.getPreferences());
    super.tearDown();
  }

  /**
   * Configures test values for core/toolkit preferences.
   * 
   * @param preferences
   *          the eRCP toolkit preferences.
   */
  protected void configureForTestPreferences(IPreferenceStore preferences) {
    preferences.setValue(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
    preferences.setValue(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
        SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
  }

  /**
   * Configures default values for core/toolkit preferences.
   * 
   * @param preferences
   *          the eRCP toolkit preferences.
   */
  protected void configureDefaultPreferences(IPreferenceStore preferences) {
    preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
    preferences.setToDefault(org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
  }

  /**
   * Configures created project.
   */
  protected void configureNewProject() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dontConvertSingleQuotesToDouble() {
    m_convertSingleQuotesToDouble = false;
  }

  /**
   * @return the {@link CompositeInfo} for parsed RCP source.
   */
  protected final CompositeInfo parseComposite(String... lines) throws Exception {
    return parseJavaInfo(lines);
  }

  /**
   * @return the {@link JavaInfo} for parsed RCP source.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends JavaInfo> T parseJavaInfo(String... lines) throws Exception {
    return (T) parseSource("test", "Test.java", getTestSource(lines));
  }

  /**
   * Asserts that active {@link AstEditor} has expected Swing source.
   */
  public final void assertEditor(String... lines) {
    assertEditor(getTestSource(lines), m_lastEditor);
  }

  /**
   * @return the source for RCP.
   */
  protected final String getTestSource(String... lines) {
    if (m_convertSingleQuotesToDouble) {
      lines = getDoubleQuotes(lines);
    }
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