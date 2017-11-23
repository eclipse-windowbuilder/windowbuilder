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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.laf.IBaselineSupport;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.XwtLiveManager;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XwtLiveManager}.
 * 
 * @author scheglov_ke
 */
public class ControlLiveManagerTest extends XwtModelTest {
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
  // "Live" image
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Dimension BUTTON_NO_TEXT_SIZE = new Dimension(12, 25);
  private static final Dimension BUTTON_TEXT_SIZE = new Dimension(75, 25);

  /**
   * {@link Control_LiveManager} should not change {@link GlobalState}.
   */
  public void test_GlobalState() throws Exception {
    ControlInfo shell = parse("<Shell/>");
    refresh();
    // initial "active"
    assertSame(shell, GlobalState.getActiveObject());
    // use "live" manager
    {
      ControlInfo button = createButton();
      Image image = button.getImage();
      assertNotNull(image);
    }
    // "active" is not changed
    assertSame(shell, GlobalState.getActiveObject());
  }

  /**
   * Check that after "live image" source is not changed.
   */
  public void test_liveImage_noSourceChange() throws Exception {
    parse("<Shell/>");
    String originalSource = m_lastContext.getContent();
    // prepare button
    ControlInfo button = createButton();
    // check image
    {
      Image image = button.getImage();
      assertNotNull(image);
      // no source modification expected
      assertEquals(originalSource, m_lastContext.getContent());
    }
    // check preferred size
    {
      Dimension preferredSize = button.getPreferredSize();
      assertNotNull(preferredSize);
    }
  }

  public void test_liveImage_onShell() throws Exception {
    parse("<Shell/>");
    // prepare buttons
    ControlInfo button_1 = createButton();
    ControlInfo button_2 = createButton();
    // check images
    {
      // button_1 has "live" image
      Image image_1 = button_1.getImage();
      assertNotNull(image_1);
      {
        Dimension size = new Rectangle(image_1.getBounds()).getSize();
        assertEquals(BUTTON_NO_TEXT_SIZE, size);
      }
      // button_2 has same "live" image
      Image image_2 = button_2.getImage();
      assertTrue(UiUtils.equals(image_1, image_2));
    }
    // check preferred size
    {
      // button_1 has "live" preferred size
      Dimension preferredSize_1 = button_1.getPreferredSize();
      assertNotNull(preferredSize_1);
      assertEquals(BUTTON_NO_TEXT_SIZE, preferredSize_1);
      // button_2 has same "live" preferred size
      assertEquals(preferredSize_1, button_2.getPreferredSize());
    }
  }

  public void test_buttonWithText() throws Exception {
    parse("<Shell/>");
    ControlInfo button = createButtonWithText();
    // check preferred size
    Dimension preferredSize = button.getPreferredSize();
    assertEquals(BUTTON_TEXT_SIZE, preferredSize);
  }

  /**
   * Test that live image is not disposed during refresh. Right now this means that if we cache live
   * images, we should use keep copy of cached image.
   */
  public void test_liveImage_noDispose() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    refresh();
    // add button
    {
      ControlInfo button = createButton();
      // we have live image
      Image image = button.getImage();
      assertNotNull(image);
      assertFalse(image.isDisposed());
      // do add
      shell.getLayout().command_CREATE(button, null);
      shell.refresh();
    }
    // check live image for new button
    {
      ControlInfo button = createButton();
      // we still have valid live image
      Image image = button.getImage();
      assertNotNull(image);
      assertFalse(image.isDisposed());
    }
  }

  public void test_liveImage_onComposite() throws Exception {
    parse("<Composite/>");
    //
    ControlInfo label = createObject("org.eclipse.swt.widgets.Label");
    assertNotNull(label.getImage());
  }

  /**
   * Sometimes component has zero or just too small size by default, so we need some way to force
   * its "live" size.
   */
  public void test_liveImage_forcedSize() throws Exception {
    setFileContentSrc(
        "test/MyCanvas.java",
        getJavaSource(
            "public class MyCanvas extends Canvas {",
            "  public MyCanvas(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyCanvas.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='liveComponent.forcedSize.width'>150</parameter>",
            "    <parameter name='liveComponent.forcedSize.height'>30</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parse("<Shell/>");
    // we should have forced "preferred" size
    ControlInfo myCanvas = createObject("test.MyCanvas");
    assertEquals(new Dimension(150, 30), myCanvas.getPreferredSize());
  }

  /**
   * If exception happens during live image creation, we still should return some image (not
   * <code>null</code>) to prevent problems on other levels.
   */
  public void test_liveImage_whenException() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getJavaSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public Rectangle getClientArea() {",
            "    throw new IllegalStateException('Problem in getClientArea()');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse("<Shell/>");
    // add log listener for exception validation
    ILog log = DesignerPlugin.getDefault().getLog();
    ILogListener logListener = new ILogListener() {
      public void logging(IStatus status, String plugin) {
        assertEquals(IStatus.ERROR, status.getSeverity());
        Throwable exception = status.getException();
        assertThat(exception).isExactlyInstanceOf(IllegalStateException.class);
        assertEquals("Problem in getClientArea()", exception.getMessage());
      }
    };
    // temporary intercept logging
    try {
      log.addLogListener(logListener);
      DesignerPlugin.setDisplayExceptionOnConsole(false);
      // prepare new component
      ControlInfo newComponent = createObject("test.MyComposite");
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

  public void test_liveImage_copyPaste() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' bounds='10, 10, 200, 100'/>",
        "</Shell>");
    refresh();
    // prepare memento
    XmlObjectMemento memento;
    {
      ControlInfo button = getObjectByName("button");
      memento = XmlObjectMemento.createMemento(button);
    }
    // do paste
    ControlInfo pasteButton = (ControlInfo) memento.create(m_lastObject);
    // get "live" image, from memento
    {
      Image image = pasteButton.getImage();
      assertThat(image.getBounds().width).isEqualTo(200);
      assertThat(image.getBounds().height).isEqualTo(100);
    }
    // check style
    {
      int actualStyle = pasteButton.getStyle();
      assertTrue(
          "SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.PUSH) == SWT.PUSH);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_liveStyle_standardControl() throws Exception {
    parse("<Shell/>");
    // PUSH
    {
      ControlInfo button = createObject("org.eclipse.swt.widgets.Button", null);
      int actualStyle = button.getStyle();
      assertTrue(
          "SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.PUSH) == SWT.PUSH);
    }
    // CHECK
    {
      ControlInfo button = createObject("org.eclipse.swt.widgets.Button", "check");
      int actualStyle = button.getStyle();
      assertTrue(
          "SWT.CHECK bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.CHECK) == SWT.CHECK);
    }
    // RADIO
    {
      ControlInfo button = createObject("org.eclipse.swt.widgets.Button", "radio");
      int actualStyle = button.getStyle();
      assertTrue(
          "SWT.RADIO bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.RADIO) == SWT.RADIO);
    }
    // Text: BORDER
    {
      ControlInfo text = createObject("org.eclipse.swt.widgets.Text");
      int actualStyle = text.getStyle();
      assertTrue(
          "SWT.BORDER bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.BORDER) == SWT.BORDER);
    }
  }

  public void test_liveStyle_customControl() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getJavaSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, SWT.BORDER | SWT.NO_RADIO_GROUP);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse("<Shell/>");
    // do checks
    ControlInfo myComposite = createObject("test.MyComposite");
    int actualStyle = myComposite.getStyle();
    assertTrue(
        "SWT.BORDER bit expected, but " + Integer.toHexString(actualStyle) + " found.",
        (actualStyle & SWT.BORDER) == SWT.BORDER);
    assertTrue("SWT.NO_RADIO_GROUP bit expected, but "
        + Integer.toHexString(actualStyle)
        + " found.", (actualStyle & SWT.NO_RADIO_GROUP) == SWT.NO_RADIO_GROUP);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Baseline
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_liveBaseline() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    refresh();
    // prepare button
    ControlInfo newButton = createButton();
    // get baseline
    int liveBaseline = newButton.getBaseline();
    assertThat(liveBaseline).isNotEqualTo(IBaselineSupport.NO_BASELINE).isPositive();
    // drop Button
    ((RowLayoutInfo) shell.getLayout()).command_CREATE(newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
    // same baseline as "live"
    int baseline = newButton.getBaseline();
    assertThat(baseline).isEqualTo(liveBaseline);
  }
}