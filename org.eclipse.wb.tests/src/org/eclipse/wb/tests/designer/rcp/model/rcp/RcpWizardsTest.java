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

import org.eclipse.wb.internal.rcp.wizards.rcp.editor.EditorPartWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.view.ViewPartWizard;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.ViewPart;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RCP wizards, such as {@link ViewPart}.
 * 
 * @author scheglov_ke
 */
public class RcpWizardsTest extends RcpModelTest {
  private IPackageFragment m_packageFragment;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_packageFragment = m_testProject.getPackage("test");
  }

  @Override
  protected void tearDown() throws Exception {
    waitEventLoop(10);
    super.tearDown();
  }

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
  // ViewPart
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_ViewPart_isPDE() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null, null);
    animate_ViewPart();
    {
      String pluginFile = getFileContent("plugin.xml");
      assertThat(pluginFile).contains("org.eclipse.ui.views");
      assertThat(pluginFile).contains("<view");
      assertThat(pluginFile).contains("class=\"test.MyViewPart\"");
      assertThat(pluginFile).contains("id=\"test.MyViewPart\"");
      assertThat(pluginFile).contains("name=\"New ViewPart\"");
    }
  }

  @DisposeProjectAfter
  public void test_ViewPart_notPDE() throws Exception {
    animate_ViewPart();
    assertFileNotExists("plugin.xml");
  }

  private void animate_ViewPart() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new ViewPartWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New Eclipse RCP ViewPart");
        context.getTextByLabel("Name:").setText("MyViewPart");
        context.clickButton("Finish");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditorPart
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_EditorPart_isPDE() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_project, null, null);
    animate_EditorPart();
    {
      String pluginFile = getFileContent("plugin.xml");
      assertThat(pluginFile).contains("org.eclipse.ui.editors");
      assertThat(pluginFile).contains("<editor");
      assertThat(pluginFile).contains("class=\"test.MyEditorPart\"");
      assertThat(pluginFile).contains("id=\"test.MyEditorPart\"");
      assertThat(pluginFile).contains("name=\"New EditorPart\"");
    }
  }

  @DisposeProjectAfter
  public void test_EditorPart_notPDE() throws Exception {
    animate_EditorPart();
    assertFileNotExists("plugin.xml");
  }

  private void animate_EditorPart() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new EditorPartWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New Eclipse RCP EditorPart");
        context.getTextByLabel("Name:").setText("MyEditorPart");
        context.clickButton("Finish");
      }
    });
  }
}