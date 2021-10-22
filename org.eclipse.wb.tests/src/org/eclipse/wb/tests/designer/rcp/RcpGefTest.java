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
package org.eclipse.wb.tests.designer.rcp;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.widgets.Button;

/**
 * Abstract test for RCP in editor.
 *
 * @author scheglov_ke
 */
public class RcpGefTest extends DesignerEditorTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    configureDefaults(org.eclipse.wb.internal.rcp.ToolkitProvider.DESCRIPTION);
    if (m_testProject == null) {
      do_projectCreate();
      BTestUtils.configure(m_testProject);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final CompositeInfo openComposite(String... lines) throws Exception {
    return (CompositeInfo) openJavaInfo(lines);
  }

  @SuppressWarnings("unchecked")
  protected final <T extends JavaInfo> T openJavaInfo(String... lines) throws Exception {
    String source = getTestSource2(lines);
    ICompilationUnit unit = createModelCompilationUnit("test", "Test.java", source);
    openDesign(unit);
    return (T) m_contentJavaInfo;
  }

  /**
   * Asserts that active {@link AstEditor} has expected Swing source.
   */
  protected final void assertEditor(String... lines) {
    AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
    String expectedSource = getTestSource2(lines);
    assertEditor(expectedSource, editor);
  }

  /**
   * @return the source for RCP.
   */
  protected final String getTestSource2(String... lines) {
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
            "import org.eclipse.swt.custom.*;",
            "import org.eclipse.jface.viewers.*;",
            "import org.eclipse.jface.preference.*;",
            "import org.eclipse.jface.resource.*;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;"}, lines);
    return lines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Box
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void prepareComponent() throws Exception {
    prepareComponent(100, 50);
  }

  protected void prepareComponent(int width, int height) throws Exception {
    setFileContentSrc(
        "test/Button.java",
        getTestSource2(
            "public class Button extends org.eclipse.swt.widgets.Button {",
            "  public Button(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void checkSubclass () {",
            "  }",
            "  public Point computeSize (int wHint, int hHint, boolean changed) {",
            "    return new Point(" + width + ", " + height + ");",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  protected ControlInfo loadCreationButton() throws Exception {
    return loadCreationTool("test.Button");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tools
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} with {@link Button} without text.
   */
  protected final void loadButton() throws Exception {
    loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
  }

  /**
   * Loads {@link CreationTool} with {@link Button} with text.
   */
  protected final void loadButtonWithText() throws Exception {
    loadCreationTool("org.eclipse.swt.widgets.Button");
  }
}
