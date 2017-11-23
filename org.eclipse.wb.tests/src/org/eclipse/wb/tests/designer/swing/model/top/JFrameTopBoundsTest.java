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
package org.eclipse.wb.tests.designer.swing.model.top;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.top.WindowTopBoundsSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.DimValue;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Basic tests for {@link WindowTopBoundsSupport}.
 * 
 * @author scheglov_ke
 */
public class JFrameTopBoundsTest extends SwingGefTest {
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
   * Size in properties of {@link IResource}.
   */
  public void test_defaultSize() throws Exception {
    Dimension oldSize = new Dimension(450, 300);
    Dimension resizeSize = new Dimension(350, 200);
    ICompilationUnit unit =
        check_resize("// no size", "// none", oldSize, resizeSize, resizeSize, "// no size");
    assert_sameSizeAfterReparse(unit, resizeSize);
  }

  /**
   * If {@link JFrame} has big size, which was set in superclass, then use this size.
   */
  public void test_JFrame_bigSize_setInSuperclass() throws Exception {
    setFileContentSrc(
        "test/MyBigFrame.java",
        getTestSource(
            "public class MyBigFrame extends JFrame {",
            "  public MyBigFrame() {",
            "    setSize(500, 400);",
            "  }",
            "}"));
    waitForAutoBuild();
    // 
    Dimension oldSize = new Dimension(500, 400);
    Dimension resizeSize = new Dimension(350, 200);
    ICompilationUnit unit =
        check_resize(
            "MyBigFrame",
            "// no size",
            "// none",
            oldSize,
            resizeSize,
            resizeSize,
            "// no size");
    assert_sameSizeAfterReparse(unit, resizeSize);
  }

  /**
   * If {@link JInternalFrame} has big size, which was set in superclass, then use this size.
   */
  public void test_JInternalFrame_bigSize_setInSuperclass() throws Exception {
    setFileContentSrc(
        "test/MyBigFrame.java",
        getTestSource(
            "public class MyBigFrame extends JInternalFrame {",
            "  public MyBigFrame() {",
            "    setSize(500, 400);",
            "  }",
            "}"));
    waitForAutoBuild();
    // 
    Dimension oldSize = new Dimension(500, 400);
    Dimension resizeSize = new Dimension(350, 200);
    ICompilationUnit unit =
        check_resize(
            "MyBigFrame",
            "// no size",
            "// none",
            oldSize,
            resizeSize,
            resizeSize,
            "// no size");
    assert_sameSizeAfterReparse(unit, resizeSize);
  }

  /**
   * Using {@link JFrame#pack()}.
   */
  public void test_resize_pack() throws Exception {
    Dimension packSize =
        Expectations.get(new Dimension(132, 89), new DimValue[]{
            new DimValue("flanker-windows", new Dimension(132, 83)),
            new DimValue("scheglov-win", new Dimension(132, 83)),});
    Dimension resizeSize = new Dimension(450, 300);
    ICompilationUnit unit =
        check_resize("// no size", "pack();", packSize, resizeSize, packSize, "// no size");
    assert_sameSizeAfterReparse(unit, packSize);
  }

  /**
   * There was request to handle {@link Window} as if there was "pack()" invocation, for example
   * when superclass has its invocation.
   */
  public void test_packAlways() throws Exception {
    setFileContentSrc(
        "test/MyFrame.java",
        getTestSource(
            "public class MyFrame extends JFrame {",
            "  protected void finishInit() {",
            "    pack();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyFrame.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='finishInit'/>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='topBounds.pack'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // open
    ContainerInfo frame =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyFrame {",
            "  public Test() {",
            "    finishInit();",
            "  }",
            "}");
    // assert that pack() was invoked and not overridden
    Dimension size = frame.getBounds().getSize();
    assertThat(size.width).isNotEqualTo(450);
    assertThat(size.height).isNotEqualTo(300);
  }

  /**
   * Size in setSize(int,int)
   */
  public void test_resize_setSize_ints() throws Exception {
    Dimension oldSize = new Dimension(300, 200);
    Dimension newSize = new Dimension(400, 300);
    check_resize("setSize(300, 200);", "", oldSize, newSize, newSize, "setSize(400, 300);");
  }

  /**
   * Size in setBounds(int,int,int,int)
   */
  public void test_resize_setBounds_ints() throws Exception {
    Dimension oldSize = new Dimension(300, 200);
    Dimension newSize = new Dimension(400, 300);
    check_resize(
        "setBounds(0, 0, 300, 200);",
        "",
        oldSize,
        newSize,
        newSize,
        "setBounds(0, 0, 400, 300);");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test resize of {@link JFrame}.
   */
  private ICompilationUnit check_resize(String oldSizeLine,
      String addSizeString,
      Dimension oldSize,
      Dimension resizeSize,
      Dimension newSize,
      String newSizeLine) throws Exception {
    return check_resize(
        "JFrame",
        oldSizeLine,
        addSizeString,
        oldSize,
        resizeSize,
        newSize,
        newSizeLine);
  }

  /**
   * Test resize of {@link JFrame} subclass.
   */
  private ICompilationUnit check_resize(String superClassName,
      String oldSizeLine,
      String addSizeString,
      Dimension oldSize,
      Dimension resizeSize,
      Dimension newSize,
      String newSizeLine) throws Exception {
    ContainerInfo frame =
        openContainer(
            "public class Test extends " + superClassName + " {",
            "  public Test() {",
            "    " + oldSizeLine,
            "    getContentPane().add(new JButton('Swing JButton'), BorderLayout.NORTH);",
            "    getContentPane().add(new Button('AWT Button'), BorderLayout.WEST);",
            "    " + addSizeString,
            "  }",
            "}");
    // check size
    assertEquals(oldSize, canvas.getSize(frame));
    waitEventLoop(50);
    // change size
    canvas.beginResize(frame, IPositionConstants.EAST);
    canvas.dragTo(frame, resizeSize.width, 0).endDrag();
    canvas.beginResize(frame, IPositionConstants.SOUTH);
    canvas.dragTo(frame, 0, resizeSize.height).endDrag();
    // check new size
    assertEquals(newSize, canvas.getSize(frame));
    assertEditor(
        "public class Test extends " + superClassName + " {",
        "  public Test() {",
        "    " + newSizeLine,
        "    getContentPane().add(new JButton('Swing JButton'), BorderLayout.NORTH);",
        "    getContentPane().add(new Button('AWT Button'), BorderLayout.WEST);",
        "    " + addSizeString,
        "  }",
        "}");
    //
    return m_lastEditor.getModelUnit();
  }

  /**
   * Close editor, reopen and check for size - it should be same as we set.
   */
  private void assert_sameSizeAfterReparse(ICompilationUnit unit, Dimension resizeSize)
      throws Exception {
    TestUtils.closeAllEditors();
    openDesign(unit);
    assertEquals(resizeSize, canvas.getSize(m_contentEditPart));
  }
}
