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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.laf.IBaselineSupport;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.live.SwingLiveManager;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;

/**
 * Tests for Swing {@link SwingLiveManager}.
 * 
 * @author mitin_aa
 */
public class SwingLiveManagerTest extends SwingModelTest {
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
   * Using {@link SwingLiveManager} should not change {@link GlobalState}.
   */
  public void test_GlobalState() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // initial "active"
    assertSame(panel, GlobalState.getActiveObject());
    // use "live" manager
    {
      ComponentInfo button = createJButton();
      Component component = button.getComponent();
      assertNotNull(component);
    }
    // "active" is not changed
    assertSame(panel, GlobalState.getActiveObject());
  }

  /**
   * Test for "live" component during create.
   */
  public void test_create() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button_1 = createJButton();
    ComponentInfo button_2 = createJButton();
    Component component_1 = button_1.getComponent();
    Component component_2 = button_2.getComponent();
    assertNotNull(component_1);
    assertNotNull(component_2);
    assertSame(component_1, component_2);
  }

  /**
   * Test for using <code>liveComponent.forcedSize.width</code> and
   * <code>liveComponent.forcedSize.height</code> parameters.
   */
  public void test_liveImage_forcedSize() throws Exception {
    setFileContentSrc(
        "test/MyCanvas.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyCanvas extends Canvas {",
            "}"));
    setFileContentSrc(
        "test/MyCanvas.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.width'>100</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>50</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo newComponent =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.MyCanvas"),
            new ConstructorCreationSupport());
    // ask image
    {
      Image image = newComponent.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(100);
      assertThat(image.getBounds().height).isEqualTo(50);
    }
  }

  /**
   * If exception happens during live image creation, we still should return some image (not
   * <code>null</code>) to prevent problems on other levels.
   */
  public void test_whenException() throws Exception {
    setFileContentSrc(
        "test/MyCanvas.java",
        getTestSource(
            "public class MyCanvas extends Canvas {",
            "  public void paint(Graphics g) {",
            "    throw new IllegalStateException('Problem in constructor');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    String originalSource = m_lastEditor.getSource();
    // ask "live" first time
    {
      ILog log = DesignerPlugin.getDefault().getLog();
      ILogListener logListener = new ILogListener() {
        public void logging(IStatus status, String plugin) {
          assertEquals(IStatus.ERROR, status.getSeverity());
          Throwable exception = status.getException();
          assertThat(exception).isExactlyInstanceOf(IllegalStateException.class);
          assertEquals("Problem in constructor", exception.getMessage());
        }
      };
      // temporary intercept logging
      try {
        log.addLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(false);
        // prepare new component
        ComponentInfo newComponent =
            (ComponentInfo) JavaInfoUtils.createJavaInfo(
                m_lastEditor,
                m_lastLoader.loadClass("test.MyCanvas"),
                new ConstructorCreationSupport());
        // ask image
        {
          Image image = newComponent.getImage();
          assertNotNull(image);
          assertThat(image.getBounds().width).isEqualTo(200);
          assertThat(image.getBounds().height).isEqualTo(50);
        }
        // no changes in editor
        assertEditor(originalSource, m_lastEditor);
      } finally {
        log.removeLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(true);
      }
    }
    // second request for some component class does not cause any exception, we use cached result
    {
      ILog log = DesignerPlugin.getDefault().getLog();
      ILogListener logListener = new ILogListener() {
        public void logging(IStatus status, String plugin) {
          fail();
        }
      };
      // temporary intercept logging
      try {
        log.addLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(false);
        // prepare new component
        ComponentInfo newComponent =
            (ComponentInfo) JavaInfoUtils.createJavaInfo(
                m_lastEditor,
                m_lastLoader.loadClass("test.MyCanvas"),
                new ConstructorCreationSupport());
        // ask image
        {
          Image image = newComponent.getImage();
          assertNotNull(image);
          assertThat(image.getBounds().width).isEqualTo(200);
          assertThat(image.getBounds().height).isEqualTo(50);
        }
      } finally {
        log.removeLogListener(logListener);
        DesignerPlugin.setDisplayExceptionOnConsole(true);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Baseline
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "live" baseline.
   */
  public void test_liveBaseline() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare new JButton
    ComponentInfo newButton = createJavaInfo("javax.swing.JButton");
    // prepare "live" baseline
    int liveBaseline = newButton.getBaseline();
    assertThat(liveBaseline).isNotEqualTo(IBaselineSupport.NO_BASELINE).isPositive();
    // drop JButton
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // same baseline as "live"
    int baseline = newButton.getBaseline();
    assertThat(baseline).isEqualTo(liveBaseline);
  }
}
