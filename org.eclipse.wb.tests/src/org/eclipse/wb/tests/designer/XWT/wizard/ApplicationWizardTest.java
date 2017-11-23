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
package org.eclipse.wb.tests.designer.XWT.wizard;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.xwt.wizards.ApplicationWizard;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.model.rcp.AbstractPdeTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.viewers.StructuredSelection;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Tests for {@link ApplicationWizard}.
 * 
 * @author scheglov_ke
 */
public class ApplicationWizardTest extends XwtWizardTest {
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
  // XWT libraries
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * PDE project - RCP plugin.
   */
  @DisposeProjectAfter
  public void test_libraries_forPlugin() throws Exception {
    // prepare default PDE/RCP project
    {
      do_projectDispose();
      do_projectCreate();
      BTestUtils.configure(m_testProject);
      PdeProjectConversionUtils.convertToPDE(m_project, null, null);
    }
    // animate
    animateWizard();
    // libraries in classpath
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.e4.xwt.XWT"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.e4.xwt.forms.XWTForms"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.pushingpixels.trident.Timeline"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.core.databinding.Binding"));
    assertTrue(ProjectUtils.hasType(
        m_javaProject,
        "org.eclipse.core.databinding.observable.IObservable"));
    assertTrue(ProjectUtils.hasType(
        m_javaProject,
        "org.eclipse.jface.databinding.swt.SWTObservables"));
    // libraries in manifest
    {
      String manifest = AbstractPdeTest.getManifest();
      assertEquals(manifest, 1, StringUtils.countMatches(manifest, "org.pushingpixels.trident_"));
      assertEquals(manifest, 1, StringUtils.countMatches(manifest, "org.eclipse.e4.xwt_"));
      assertEquals(manifest, 1, StringUtils.countMatches(manifest, "org.eclipse.e4.xwt.forms_"));
    }
  }

  /**
   * Not PDE project.
   */
  @DisposeProjectAfter
  public void test_libraries_forProject() throws Exception {
    // prepare default SWT project
    {
      do_projectDispose();
      do_projectCreate();
      BTestUtils.configure(m_testProject);
    }
    // animate
    animateWizard();
    // libraries
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.e4.xwt.XWT"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.e4.xwt.forms.XWTForms"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.pushingpixels.trident.Timeline"));
    assertTrue(ProjectUtils.hasType(m_javaProject, "org.eclipse.core.databinding.Binding"));
    assertTrue(ProjectUtils.hasType(
        m_javaProject,
        "org.eclipse.core.databinding.observable.IObservable"));
    assertTrue(ProjectUtils.hasType(
        m_javaProject,
        "org.eclipse.jface.databinding.swt.SWTObservables"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contents() throws Exception {
    animateWizard();
    // Java
    {
      String content = getFileContentSrc("test/MyApp.java");
      assertThat(content).contains("main(String args[])");
      assertThat(content).contains("XWT.load");
      assertThat(content).contains(".readAndDispatch()");
    }
    // XWT
    {
      String content = getFileContentSrc("test/MyApp.xwt");
      assertThat(content).contains("<Shell");
      assertThat(content).contains("<RowLayout/>");
      assertThat(content).contains("<Button text=");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void animateWizard() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new ApplicationWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New XWT Application");
        context.getTextByLabel("Name:").setText("MyApp");
        context.clickButton("Finish");
      }
    });
  }
}