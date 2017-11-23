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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import static org.assertj.core.api.Assertions.assertThat;

import java.applet.Applet;

import javax.swing.JApplet;
import javax.swing.JFrame;

/**
 * Test for {@link Applet} and {@link JApplet}.
 * 
 * @author scheglov_ke
 */
public class AppletTest extends SwingModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Java thinks that {@link Applet} is root of hierarchy, but we wrap it in {@link JFrame}. So, we
   * test that we handle {@link Applet} correctly.
   */
  public void test_Applet_bounds() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "import java.applet.Applet;",
            "public class Test extends Applet {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    applet.refresh();
    ComponentInfo button = getJavaInfoByName("button");
    //
    assertEquals(new Rectangle(0, 0, 450, 300), applet.getBounds());
    {
      Rectangle bounds = button.getBounds();
      assertThat(bounds.x).isGreaterThan(100).isLessThan(300);
      assertThat(bounds.y).isEqualTo(5);
    }
  }

  /**
   * Java thinks that {@link JApplet} is root of hierarchy, but we wrap it in {@link JFrame}. So, we
   * test that we handle {@link JApplet} correctly.
   */
  public void test_JApplet_bounds() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JApplet {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      getContentPane().add(button);",
            "    }",
            "  }",
            "}");
    applet.refresh();
    ComponentInfo contentPane = applet.getChildrenComponents().get(0);
    ComponentInfo button = getJavaInfoByName("button");
    //
    assertEquals(new Rectangle(0, 0, 450, 300), applet.getBounds());
    assertEquals(new Rectangle(0, 0, 450, 300), contentPane.getBounds());
    // no constraints for JButton, so it fills content pane
    assertEquals(new Rectangle(0, 0, 450, 300), button.getBounds());
  }

  /**
   * {@link JApplet#getParent()} returns <code>null</code>, but we should not fail, and it is not
   * included into hierarchy.
   */
  public void test_JApplet_getParent() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "public class Test extends JApplet {",
            "  public Test() {",
            "  }",
            "  public void init() {",
            "    super.getParent();",
            "  }",
            "}");
    applet.refresh();
    assertNoErrors(applet);
    assertHierarchy(
        "{this: javax.swing.JApplet} {this} {}",
        "  {method: public java.awt.Container javax.swing.JApplet.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Java thinks that {@link Applet} is root of hierarchy, but we wrap it in {@link JFrame}. So, we
   * test that we get correct screen shot of {@link Applet}.
   */
  public void test_Applet_screenShot() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "import java.applet.Applet;",
            "public class Test extends Applet {",
            "  public Test() {",
            "    setBackground(Color.RED);",
            "  }",
            "}");
    applet.refresh();
    //
    assertHasRedPixel00(applet);
  }

  /**
   * Java thinks that {@link JApplet} is root of hierarchy, but we wrap it in {@link JFrame}. So, we
   * test that we get correct screen shot of {@link JApplet}.
   */
  public void test_JApplet_screenShot() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JApplet {",
            "  public Test() {",
            "    getContentPane().setBackground(Color.RED);",
            "  }",
            "}");
    applet.refresh();
    assertHasRedPixel00(applet);
  }

  private static void assertHasRedPixel00(ContainerInfo applet) {
    ImageData imageData = applet.getImage().getImageData();
    int pixel = imageData.getPixel(0, 0);
    RGB rgb = imageData.palette.getRGB(pixel);
    assertEquals(new RGB(255, 0, 0), rgb);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution flow
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_GUI_inConstructor() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "public class Test extends JApplet {",
            "  public Test() {",
            "    getContentPane().add(new JButton());",
            "  }",
            "}");
    assertNoErrors(applet);
    assertHierarchy(
        "{this: javax.swing.JApplet} {this} {}",
        "  {method: public java.awt.Container javax.swing.JApplet.getContentPane()} {property} {/getContentPane().add(new JButton())/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/getContentPane().add(new JButton())/}");
    // refresh()
    applet.refresh();
    assertNotNull(applet.getComponent());
    assertNotNull(applet.getChildrenComponents().get(0).getComponent());
  }

  public void test_GUI_inMethod_init() throws Exception {
    ContainerInfo applet =
        parseContainer(
            "public class Test extends JApplet {",
            "  public Test() {",
            "  }",
            "  public void init() {",
            "    getContentPane().add(new JButton());",
            "  }",
            "}");
    assertNoErrors(applet);
    assertHierarchy(
        "{this: javax.swing.JApplet} {this} {}",
        "  {method: public java.awt.Container javax.swing.JApplet.getContentPane()} {property} {/getContentPane().add(new JButton())/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/getContentPane().add(new JButton())/}");
    // refresh()
    applet.refresh();
    assertNotNull(applet.getComponent());
    assertNotNull(applet.getChildrenComponents().get(0).getComponent());
  }

  public void test_GUI_initInExecutionFlow() throws Exception {
    setFileContentSrc(
        "test/MyApplet.java",
        getTestSource(
            "public class MyApplet extends JApplet {",
            "  public void init() {",
            "    myInit();",
            "  }",
            "  protected void myInit() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo applet =
        parseContainer(
            "public class Test extends MyApplet {",
            "  public Test() {",
            "  }",
            "  protected void myInit() {",
            "    getContentPane().add(new JButton());",
            "  }",
            "}");
    applet.refresh();
    assertNoErrors(applet);
    assertHierarchy(
        "{this: test.MyApplet} {this} {}",
        "  {method: public java.awt.Container javax.swing.JApplet.getContentPane()} {property} {/getContentPane().add(new JButton())/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/getContentPane().add(new JButton())/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_applet_in_applet() throws Exception {
    // this should just parse
    ContainerInfo applet =
        parseContainer(
            "public class Test extends JApplet {",
            "  private JApplet internalApplet = new JApplet();",
            "  public Test() {",
            "  }",
            "  public void init() {",
            "  }",
            "}");
    applet.refresh();
  }
}
