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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.core.editor.DesignerState;
import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.views.PaletteView;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.editor.XmlExceptionComposite;
import org.eclipse.wb.internal.core.xml.editor.XmlWarningComposite;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.gef.core.CancelOperationError;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Test for {@link XmlDesignPage} with {@link XmlExceptionComposite} and {@link XmlWarningComposite}
 * .
 * 
 * @author scheglov_ke
 */
public class XmlDesignPageTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    DesignerExceptionUtils.flushErrorEntriesCache();
    DesignerPlugin.setDisplayExceptionOnConsole(true);
    EnvironmentUtils.setTestingTime(true);
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getUndoManager() throws Exception {
    openEditor("<Shell/>");
    // we can access it
    assertNotNull(m_designPage.getUndoManager());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that palette can be extracted into separate view.
   */
  public void test_extractPalette() throws Exception {
    openEditor("<Shell/>");
    // make editor not maximized, or palette will not be extracted
    IWorkbenchPage activePage = DesignerPlugin.getActivePage();
    activePage.toggleZoom(activePage.getActivePartReference());
    waitEventLoop(0);
    // prepare UIContext
    UiContext context = new UiContext();
    Control palette = findPalette(context);
    // palette is on "Design" page
    assertTrue(UiUtils.isChildOf(m_designPage.getControl(), palette));
    // show "Palette" view
    activePage.showView(PaletteView.ID);
    waitEventLoop(0);
    try {
      // palette is NOT on "Design" page
      assertFalse(UiUtils.isChildOf(m_designPage.getControl(), palette));
    } finally {
      TestUtils.closeAllViews();
    }
    // "Palette" view was closed, so palette is again on "Design" page
    assertTrue(UiUtils.isChildOf(m_designPage.getControl(), palette));
  }

  /**
   * @return the palette {@link Control}.
   */
  private Control findPalette(UiContext context) {
    return context.findWidgets(PaletteComposite.class).get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerState
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String PROCESSORS_POINT_ID = "org.eclipse.wb.core.xml.rootProcessors";

  /**
   * Test for correctness of {@link DesignerState} changing.
   */
  public void test_DesignerState() throws Exception {
    {
      IFile file =
          setFileContentSrc(
              "test/Test.xwt",
              getTestSource(
                  "// filler filler filler filler filler",
                  "// filler filler filler filler filler",
                  "<Shell/>"));
      openEditor(file);
    }
    // "Design" page is not opened yet
    assertSame(DesignerState.Undefined, m_designPage.getDesignerState());
    // install "root processor" to get "parsing" state
    {
      MyDesignerStateProcessor.designPage = m_designPage;
      String contribution =
          "  <processor class='" + MyDesignerStateProcessor.class.getName() + "'/>";
      TestUtils.addDynamicExtension(PROCESSORS_POINT_ID, contribution);
    }
    // open "Design"
    try {
      openDesignPage();
    } finally {
      MyDesignerStateProcessor.designPage = null;
      TestUtils.removeDynamicExtension(PROCESSORS_POINT_ID);
    }
    // during parsing we see "Parsing" state
    assertSame(DesignerState.Parsing, MyDesignerStateProcessor.state);
    // successful parsing
    assertSame(DesignerState.Successful, m_designPage.getDesignerState());
  }

  public static final class MyDesignerStateProcessor implements IRootProcessor {
    private static XmlDesignPage designPage;
    private static DesignerState state;

    public void process(XmlObjectInfo root) throws Exception {
      state = designPage.getDesignerState();
    }
  }

  /**
   * Test for {@link DesignerState#Error} state.
   */
  public void test_DesignerState_Error() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open, exception will happen
    openEditor("<Shell>");
    // so, "Error" state
    assertSame(DesignerState.Error, m_designPage.getDesignerState());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignPageSite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link IDesignPageSite} is installed by {@link XmlDesignPage} and works.
   */
  public void test_DesignPageSite() throws Exception {
    openEditor("<Shell/>");
    IDesignPageSite site = IDesignPageSite.Helper.getSite(m_lastObject);
    assertNotNull(site);
    // go to position in XML source
    {
      site.showSourcePosition(1);
      waitEventLoop(0);
      assertXMLSelection(1, 0);
      assertEquals(1, m_designerEditor.getActivePage());
    }
    // open position in XML source
    {
      site.openSourcePosition(2);
      waitEventLoop(0);
      assertXMLSelection(2, 0);
      assertEquals(0, m_designerEditor.getActivePage());
    }
    // reparse()
    {
      XmlObjectInfo oldObject = m_lastObject;
      site.reparse();
      fetchContentFields();
      assertNotSame(oldObject, m_lastObject);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExceptionCompositeXML
  //
  ////////////////////////////////////////////////////////////////////////////
  private XmlExceptionComposite getVisibleExceptionCompositeXML(UiContext context) {
    XmlExceptionComposite exceptionCompositeXML =
        context.findFirstWidget(XmlExceptionComposite.class);
    assertNotNull(exceptionCompositeXML);
    assertTrue(exceptionCompositeXML.isVisible());
    assertEquals(1, m_designerEditor.getActivePage());
    return exceptionCompositeXML;
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Use "Switch to code" button.
   */
  public void test_ExceptionCompositeXML_switchToCode() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open, exception will happen
    openEditor("<Shell>");
    // prepare context
    UiContext context = new UiContext();
    XmlExceptionComposite exceptionCompositeXML = getVisibleExceptionCompositeXML(context);
    // switch to "XML" page
    context.clickButton("Switch to code");
    assertEquals(0, m_designerEditor.getActivePage());
    // ExceptionCompositeXML is not visible
    assertFalse(exceptionCompositeXML.isVisible());
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Use "Reparse" button.
   */
  public void test_ExceptionCompositeXML_reparse() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open, exception will happen
    openEditor("<Shell>");
    // prepare context
    UiContext context = new UiContext();
    XmlExceptionComposite exceptionCompositeXML = getVisibleExceptionCompositeXML(context);
    // no "last Object"
    assertNull(m_lastObject);
    // replace with valid XML
    setFileContentSrc("test/Test.xwt", "<Shell/>");
    // use "Reparse"
    context.clickButton("Reparse");
    // "Design" is active and no "error"
    assertEquals(1, m_designerEditor.getActivePage());
    assertFalse(exceptionCompositeXML.isVisible());
    // has "last Object"
    fetchContentFields();
    assertNotNull(m_lastObject);
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Use "Contact Support..." button.
   */
  public void test_ExceptionCompositeXML_contactSupport() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open, exception will happen
    openEditor("<Shell>");
    // ExceptionCompositeXML is visible
    {
      UiContext context = new UiContext();
      getVisibleExceptionCompositeXML(context);
    }
    // open "Create Report" dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.clickButton("Create Report...");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Create Report");
        context.clickButton("Cancel");
      }
    });
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Cause exception during editing operation, so {@link XmlExceptionComposite} should be displayed,
   * with "post-mortem" screenshot.
   */
  public void test_ExceptionCompositeXML_withScreenshot() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open
    openEditor("<Shell/>");
    // prepare UIContext
    UiContext context = new UiContext();
    // no ExceptionCompositeXML yet
    assertSame(null, context.findFirstWidget(XmlExceptionComposite.class));
    // run "edit operation" which causes exception
    EnvironmentUtils.setTestingTime(false);
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        throw new Error();
      }
    });
    // ExceptionCompositeXML is visible
    XmlExceptionComposite exceptionCompositeXML = getVisibleExceptionCompositeXML(context);
    // ...and has screenshot
    Image screenshot =
        (Image) ReflectionUtils.getFieldObject(exceptionCompositeXML, "m_screenshotImage");
    assertNotNull(screenshot);
    assertFalse(screenshot.isDisposed());
    // close all editors, so dispose ExceptionCompositeXML, so screenshot
    TestUtils.closeAllEditors();
    assertTrue(screenshot.isDisposed());
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Cause exception with position during editing operation, so {@link XmlExceptionComposite} should
   * be displayed, with "Go to problem" button.
   */
  public void test_ExceptionCompositeXML_withPosition() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open
    openEditor("<Shell/>");
    // run "edit operation" which causes exception
    EnvironmentUtils.setTestingTime(false);
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        Throwable e = new Error();
        DesignerExceptionUtils.setSourcePosition(e, 5);
        ReflectionUtils.propagate(e);
      }
    });
    // prepare UIContext
    UiContext context = new UiContext();
    // ExceptionCompositeXML is visible
    getVisibleExceptionCompositeXML(context);
    // use "Go to problem" button
    context.clickButton("Go to problem");
    waitEventLoop(0);
    assertEquals(0, m_designerEditor.getActivePage());
    assertXMLSelection(5, 0);
  }

  /**
   * Test for {@link XmlExceptionComposite}.
   * <p>
   * Cause exception in GEF {@link Command}, so {@link XmlExceptionComposite} should be displayed.
   */
  public void test_ExceptionCompositeXML_exceptionInCommand() throws Exception {
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // open
    openEditor("<Shell/>");
    // run "Command" which causes exception
    EnvironmentUtils.setTestingTime(false);
    try {
      m_viewerCanvas.getEditDomain().executeCommand(new Command() {
        @Override
        public void execute() throws Exception {
          throw new Error();
        }
      });
      fail();
    } catch (CancelOperationError e) {
    }
    // prepare UIContext
    UiContext context = new UiContext();
    // ExceptionCompositeXML is visible
    getVisibleExceptionCompositeXML(context);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WarningCompositeXML
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlWarningComposite}.
   * <p>
   * Use "Switch to code" button.
   */
  public void test_WarningCompositeXML_switchToCode() throws Exception {
    // ensure WarningCompositeXML opened
    generateWarning();
    // prepare context
    UiContext context = new UiContext();
    XmlWarningComposite warningCompositeXML = getVisibleWarningCompositeXML(context);
    // switch to "XML" page
    context.clickButton("Switch to code");
    // "Source" is active and no "warning"
    assertEquals(0, m_designerEditor.getActivePage());
    assertFalse(warningCompositeXML.isVisible());
  }

  /**
   * Test for {@link XmlWarningComposite}.
   * <p>
   * Use "Reparse" button.
   */
  public void test_WarningCompositeXML_reparse() throws Exception {
    // ensure WarningCompositeXML opened
    generateWarning();
    XmlObjectInfo oldObject = m_lastObject;
    assertNotNull(oldObject);
    // prepare context
    UiContext context = new UiContext();
    XmlWarningComposite warningCompositeXML = getVisibleWarningCompositeXML(context);
    // use "Reparse"
    context.clickButton("Reparse");
    // "Design" is active and no "warning"
    assertEquals(1, m_designerEditor.getActivePage());
    assertFalse(warningCompositeXML.isVisible());
    // has "last Object"
    fetchContentFields();
    assertNotNull(m_lastObject);
    assertNotSame(oldObject, m_lastObject);
  }

  private void generateWarning() throws Exception {
    // open, OK
    openEditor("<Shell/>");
    // disable logging
    removeExceptionsListener();
    DesignerPlugin.setDisplayExceptionOnConsole(false);
    // work with "warning"
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSource(
              "<exceptions>",
              "  <exception id='-1000' title='My title' warning='true'></exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      // generate "warning"
      final DesignerException designerException = new DesignerException(-1000);
      try {
        EnvironmentUtils.setTestingTime(false);
        ExecutionUtils.run(m_lastObject, new RunnableEx() {
          public void run() throws Exception {
            throw designerException;
          }
        });
      } catch (Throwable e) {
        assertSame(designerException, e);
      }
    } finally {
      testBundle.dispose();
    }
  }

  private XmlWarningComposite getVisibleWarningCompositeXML(UiContext context) {
    XmlWarningComposite warningCompositeXML = context.findFirstWidget(XmlWarningComposite.class);
    assertNotNull(warningCompositeXML);
    assertTrue(warningCompositeXML.isVisible());
    assertEquals(1, m_designerEditor.getActivePage());
    return warningCompositeXML;
  }
}
