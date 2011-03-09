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
package org.eclipse.wb.tests.designer.rcp.model.util;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.core.resources.IFile;

/**
 * Test for {@link Activator_getImages_ByteCodeProcessor}.
 * 
 * @author lobas_av
 */
public class ActivatorImagesTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
    // create activator
    setFileContentSrc(
        "testplugin/Activator.java",
        getSourceDQ(
            "package testplugin;",
            "import org.eclipse.jface.resource.ImageDescriptor;",
            "import org.eclipse.swt.graphics.Image;",
            "import org.eclipse.ui.plugin.AbstractUIPlugin;",
            "public class Activator extends AbstractUIPlugin {",
            "  public Activator() {",
            "  }",
            "  public static Activator getDefault() {",
            "    return null;",
            "  }",
            "  public static ImageDescriptor getImageDescriptor(String path) {",
            "    return null;",
            "  }",
            "  public static Image getImage(String path) {",
            "    return null;",
            "  }",
            "}"));
    //
    IOUtils2.ensureFolderExists(m_testProject.getProject(), "icons");
    IOUtils2.ensureFolderExists(m_testProject.getProject(), "images");
    waitForAutoBuild();
    // copy some image to test
    {
      IFile file = getFile("icons", "1.png");
      setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
    }
    {
      IFile file = getFile("images", "2.png");
      setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
    }
    // keep resources
    forgetCreatedResources();
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
  // Tests: Image
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getImage_icons() throws Exception {
    test_getImage("icons/1.png");
  }

  public void test_getImage_win_icons() throws Exception {
    test_getImage("icons\\\\1.png");
  }

  public void test_getImage_icons_slash() throws Exception {
    test_getImage("/icons/1.png");
  }

  public void test_getImage_win_icons_slash() throws Exception {
    test_getImage("\\\\icons\\\\1.png");
  }

  public void test_getImage_images() throws Exception {
    test_getImage("images/2.png");
  }

  public void test_getImage_win_images() throws Exception {
    test_getImage("images\\\\2.png");
  }

  public void test_getImage_images_slash() throws Exception {
    test_getImage("/images/2.png");
  }

  public void test_getImage_win_images_slash() throws Exception {
    test_getImage("\\\\images\\\\2.png");
  }

  private void test_getImage(String path) throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setImage(testplugin.Activator.getImage('" + path + "'));",
            "  }",
            "}");
    shell.refresh();
    Object image = ReflectionUtils.invokeMethod(shell.getObject(), "getImage()");
    assertNotNull(image);
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
  }

  public void test_getImage_wrongPath() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setImage(testplugin.Activator.getImage('icons/3.png'));",
            "  }",
            "}");
    shell.refresh();
    assertNull(ReflectionUtils.invokeMethod(shell.getObject(), "getImage()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test: ImageDescriptor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getImageDescriptor_icons() throws Exception {
    test_getImageDescriptor("icons/1.png", true);
  }

  public void test_getImageDescriptor_win_icons() throws Exception {
    test_getImageDescriptor("icons\\\\1.png", true);
  }

  public void test_getImageDescriptor_icons_slash() throws Exception {
    test_getImageDescriptor("/icons/1.png", true);
  }

  public void test_getImageDescriptor_win_icons_slash() throws Exception {
    test_getImageDescriptor("\\\\icons\\\\1.png", true);
  }

  public void test_getImageDescriptor_images() throws Exception {
    test_getImageDescriptor("images/2.png", true);
  }

  public void test_getImageDescriptor_win_images() throws Exception {
    test_getImageDescriptor("images\\\\2.png", true);
  }

  public void test_getImageDescriptor_images_slash() throws Exception {
    test_getImageDescriptor("/images/2.png", true);
  }

  public void test_getImageDescriptor_win_images_slash() throws Exception {
    test_getImageDescriptor("\\\\images\\\\2.png", true);
  }

  public void test_getImageDescriptor_wrongPath() throws Exception {
    test_getImageDescriptor("icons/3.png", false);
  }

  private void test_getImageDescriptor(String path, boolean checkNotNull) throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  private ImageDescriptor m_imageDescriptor;",
            "  public void setID(ImageDescriptor id) {",
            "    m_imageDescriptor = id;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    CompositeInfo shell =
        parseComposite(
            "public class Test extends MyShell {",
            "  public Test() {",
            "    setID(testplugin.Activator.getImageDescriptor('" + path + "'));",
            "  }",
            "}");
    shell.refresh();
    //
    Object shellObject = shell.getObject();
    Object imageDescriptor = ReflectionUtils.getFieldObject(shellObject, "m_imageDescriptor");
    if (checkNotNull) {
      assertNotNull(imageDescriptor);
    } else {
      assertNull(imageDescriptor);
    }
  }
}