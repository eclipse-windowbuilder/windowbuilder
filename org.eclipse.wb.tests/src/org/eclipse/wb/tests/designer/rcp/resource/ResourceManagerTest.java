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
package org.eclipse.wb.tests.designer.rcp.resource;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Test for RCP <code>ResourceManager</code>.
 *
 * @author scheglov_ke
 */
public class ResourceManagerTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    // make plugin
    setFileContentSrc(
        "testplugin/Activator.java",
        getSourceDQ(
            "package testplugin;",
            "import org.eclipse.ui.plugin.AbstractUIPlugin;",
            "public class Activator extends AbstractUIPlugin {",
            "  public Activator() {",
            "  }",
            "  public static Activator getDefault() {",
            "    return null;",
            "  }",
            "}"));
    ((List<?>) ReflectionUtils.getFieldObject(this, "m_createdResources")).clear();
    waitForAutoBuild();
    PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");

	// ensure org.eclipse.wb.swt.ResourceManager is available in the created project
	ToolkitDescriptionJava toolKit = RcpToolkitDescription.INSTANCE;
	ManagerUtils.ensure_ResourceManager(m_javaProject, toolKit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // add ResourceManager
    ManagerUtils.ensure_ResourceManager(shell);
    // "shell" JavaInfo will be disposed, but ResourceManager still will be available
  }

  /**
   * Test for execution of <code>ResourceManager.getPluginImage(String,String)</code>.
   * <p>
   * We should be able not only parse this method, but also able to render image when we _use_
   * composite that executes this method.
   */
  public void test_getPluginImage_StringString() throws Exception {
    ensureFolderExists("icons");
    TestUtils.createImagePNG(m_testProject, "icons/1.png", 10, 20);
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  private Button button;",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      button = new Button(this, SWT.NONE);",
            "      button.setImage(org.eclipse.wb.swt.ResourceManager.getPluginImage('TestProject', 'icons/1.png'));",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    MyComposite myComposite = new MyComposite(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo myComposite = (CompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = myComposite.getChildrenControls().get(0);
    // "image" should be set for Button
    Image image = (Image) ReflectionUtils.invokeMethod(button.getObject(), "getImage()");
    assertNotNull(image);
    assertEquals(10, image.getBounds().width);
    assertEquals(20, image.getBounds().height);
  }

  /**
   * Test for parsing of <code>ResourceManager.getPluginImage(Object,String)</code>.
   * <p>
   * This is old method, but we still should be able to parse it and show image.
   */
  public void test_getPluginImage_ObjectString() throws Exception {
    ensureFolderExists("icons");
    TestUtils.createImagePNG(m_testProject, "icons/2.png", 10, 20);
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "import testplugin.Activator;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setImage(org.eclipse.wb.swt.ResourceManager.getPluginImage(Activator.getDefault(), 'icons/2.png'));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    // test property
    {
      Property property = button.getPropertyByTitle("image");
      assertTrue(property.isModified());
      assertEquals("Plugin: TestProject icons/2.png", getPropertyText(property));
    }
    // "image" should be set for Button
    Image image = (Image) ReflectionUtils.invokeMethod(button.getObject(), "getImage()");
    assertNotNull(image);
    assertEquals(10, image.getBounds().width);
    assertEquals(20, image.getBounds().height);
  }
}
