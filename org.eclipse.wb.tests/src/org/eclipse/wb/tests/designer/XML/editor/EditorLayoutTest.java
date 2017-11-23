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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPage;
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPageFactory;
import org.eclipse.wb.internal.core.xml.editor.XmlEditorPage;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for editor layouts (pages or split) for {@link AbstractXmlEditor}.
 *
 * @author scheglov_ke
 */
public class EditorLayoutTest extends XwtGefTest {
  private static final int SYNC_DELAY = 250;

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    {
      IPreferenceStore preferences = DesignerPlugin.getPreferences();
      preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT);
      preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Pages mode, "Source" first.
   */
  public void test_pagesSource() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_SOURCE);
    openEditor0("<Shell/>");
    // "Source" ... "Design" order
    assertSourceDesignOrder(true);
    // "Source" is active, not parsed
    {
      fetchContentFields();
      assertNull(m_contentObject);
    }
    // open "Design", parsing happens
    {
      openDesignPage();
      assertParsed();
    }
  }

  /**
   * Pages mode, "Design" first.
   */
  public void test_pagesDesign() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_DESIGN);
    openEditor0("<Shell/>");
    // "Design" ... "Source" order
    assertSourceDesignOrder(false);
    // "Design" page is active, parsing already done
    assertParsed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split mode
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Split mode, horizontal, "Source" first.
   */
  public void test_splitHorizontalSource() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_SOURCE);
    openEditor0("<Shell/>");
    // ask pages
    {
      assertEquals(2, getMultiPageCount());
      // "Source" is active
      assertEquals(0, m_designerEditor.getActivePage());
      // ask controls
      assertSame(m_sourcePage.getControl(), getMultiControl(0));
      assertSame(m_designPage.getControl(), getMultiControl(1));
      // ask editors
      assertSame(m_sourcePage.getXmlEditor(), getMultiEditor(0));
      assertSame(null, getMultiEditor(1));
    }
    // "Source" control is before "Design" control
    assertSourceDesignOrder(true);
    assertSplitOrientation(true);
    // always parsed in "split"
    assertParsed();
    // ask "active page" for "Design"
    {
      // click "Design"
      {
        EventSender eventSender = new EventSender(m_viewerCanvas.getControl());
        eventSender.moveTo(100, 100);
        eventSender.click();
      }
      // it is "0" index tab, but we emulate "1" as if there is still "Source" page
      assertEquals(1, m_designerEditor.getActivePage());
    }
  }

  /**
   * Split mode, horizontal, "Design" first.
   */
  public void test_splitHorizontalDesign() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_DESIGN);
    openEditor0("<Shell/>");
    // ask pages
    {
      assertEquals(2, getMultiPageCount());
      // "Source" is active
      assertEquals(1, m_designerEditor.getActivePage());
      // ask controls
      assertSame(m_designPage.getControl(), getMultiControl(0));
      assertSame(m_sourcePage.getControl(), getMultiControl(1));
      // ask editors
      assertSame(null, getMultiEditor(0));
      assertSame(m_sourcePage.getXmlEditor(), getMultiEditor(1));
    }
    // "Design" control is before "Source" control
    assertSourceDesignOrder(false);
    assertSplitOrientation(true);
    // always parsed in "split"
    assertParsed();
  }

  /**
   * Split mode, vertical, "Source" first.
   */
  public void test_splitVerticalSource() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE);
    openEditor0("<Shell/>");
    // "Source" control is before "Design" control
    assertSourceDesignOrder(true);
    assertSplitOrientation(false);
    // always parsed in "split"
    assertParsed();
  }

  /**
   * Split mode, vertical, "Design" first.
   */
  public void test_splitVerticalDesign() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
    openEditor0("<Shell/>");
    // "Design" control is before "Source" control
    assertSourceDesignOrder(false);
    assertSplitOrientation(false);
    // always parsed in "split"
    assertParsed();
  }

  /**
   * Split mode, vertical, "Source" first. Try to show different pages.
   */
  public void test_splitVerticalSource_showPages() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE);
    openEditor0("<Shell/>");
    // initially "Source" is active
    assertEquals(0, m_designerEditor.getActivePage());
    // show pages
    {
      // show "Design"
      m_designerEditor.showDesign();
      assertEquals(1, m_designerEditor.getActivePage());
      // show "Source"
      m_designerEditor.showSource();
      assertEquals(0, m_designerEditor.getActivePage());
    }
    // switch pages
    {
      // switch "Source" -> "Design"
      m_designerEditor.switchSourceDesign();
      assertEquals(1, m_designerEditor.getActivePage());
      // switch "Design" -> "Source"
      m_designerEditor.switchSourceDesign();
      assertEquals(0, m_designerEditor.getActivePage());
    }
  }

  /**
   * Asserts that split is done horizontally or vertically.
   */
  private void assertSplitOrientation(boolean horizontal) {
    Control sourceControl = m_sourcePage.getControl();
    SashForm sashForm = (SashForm) sourceControl.getParent().getParent();
    int sashStyle = sashForm.getStyle();
    int expected = horizontal ? SWT.HORIZONTAL : SWT.VERTICAL;
    assertEquals(expected, sashStyle & expected);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Custom pages
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class MyXmlEditorPageFactory implements IXmlEditorPageFactory {
    @Override
    public void createPages(AbstractXmlEditor editor, List<IXmlEditorPage> pages) {
      pages.add(new MyXmlEditorPage());
    }
  }
  public static class MyXmlEditorPage extends XmlEditorPage {
    private Button m_button;

    @Override
    public Control createControl(Composite parent) {
      m_button = new Button(parent, SWT.NONE);
      return m_button;
    }

    @Override
    public Control getControl() {
      return m_button;
    }

    @Override
    public String getName() {
      return "Custom";
    }

    @Override
    public Image getImage() {
      return null;
    }
  }

  /**
   * Split mode, vertical, "Source" first.
   */
  public void test_splitVerticalSource_customPage() throws Exception {
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE);
    // create editor with custom page
    TestBundle testBundle = new TestBundle();
    try {
      Class<?> factoryClass = MyXmlEditorPageFactory.class;
      Class<?> pageClass = MyXmlEditorPage.class;
      testBundle.addClass(factoryClass);
      testBundle.addClass(pageClass);
      {
        String line = "<factory class='" + factoryClass.getName() + "'/>";
        testBundle.addExtension("org.eclipse.wb.core.xml.XMLEditorPageFactories", line);
      }
      testBundle.install();
      {
        openEditor0("<Shell/>");
        // ask pages
        {
          assertEquals(3, getMultiPageCount());
          // "Source" is active
          assertEquals(0, m_designerEditor.getActivePage());
          // ask controls
          assertSame(m_sourcePage.getControl(), getMultiControl(0));
          assertSame(m_designPage.getControl(), getMultiControl(1));
          // ask editors
          assertSame(m_sourcePage.getXmlEditor(), getMultiEditor(0));
          assertSame(null, getMultiEditor(1));
        }
        // click "Design"
        {
          EventSender eventSender = new EventSender(m_viewerCanvas.getControl());
          eventSender.moveTo(100, 100);
          eventSender.click();
        }
        assertEquals(1, m_designerEditor.getActivePage());
        // click "Source"
        {
          Control control = UiContext.findFirstWidget(m_sourcePage.getControl(), StyledText.class);
          EventSender eventSender = new EventSender(control);
          eventSender.moveTo(100, 100);
          eventSender.click();
        }
        assertEquals(0, m_designerEditor.getActivePage());
        // click "Custom"
        {
          Control editorControl = m_designerEditor.getPartControl();
          CTabFolder tabFolder = UiContext.findFirstWidget(editorControl, CTabFolder.class);
          CTabItem customItem = tabFolder.getItem(1);
          assertEquals("Custom", customItem.getText());
          Rectangle customBounds = customItem.getBounds();
          {
            EventSender eventSender = new EventSender(tabFolder);
            eventSender.moveTo(customBounds.x, customBounds.y);
            eventSender.click();
          }
        }
        assertEquals(2, m_designerEditor.getActivePage());
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When "split" mode and "Design" is active, then changes cause immediate refresh.
   */
  public void test_splitRefresh_whenDesignActive() throws Exception {
    setSyncDelay(SYNC_DELAY);
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
    openEditor0("<Shell/>");
    // initial state
    fetchContentFields();
    XmlObjectInfo lastObject = m_lastObject;
    // click "Design"
    {
      EventSender eventSender = new EventSender(m_viewerCanvas.getControl());
      eventSender.moveTo(100, 100);
      eventSender.click();
    }
    // change source...
    m_lastContext.getDocument().replace(0, "<Shell".length(), "<Shell text='abc'");
    // ...applied immediately
    fetchContentFields();
    assertNotSame(lastObject, m_lastObject);
  }

  /**
   * When "split" mode and "Source" is active, then changes cause refresh after some delay.
   */
  public void test_splitRefresh_whenSourceActive() throws Exception {
    setSyncDelay(SYNC_DELAY);
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
    openEditor0("<Shell/>");
    // initial state
    fetchContentFields();
    XmlObjectInfo lastObject = m_lastObject;
    // click "Source"
    {
      Control control = UiContext.findFirstWidget(m_sourcePage.getControl(), StyledText.class);
      EventSender eventSender = new EventSender(control);
      eventSender.moveTo(100, 100);
      eventSender.click();
    }
    // change source...
    m_lastContext.getDocument().replace(0, "<Shell".length(), "<Shell text='abc'");
    // ...no refresh yet
    fetchContentFields();
    assertSame(lastObject, m_lastObject);
    // wait sync delay
    waitEventLoop(SYNC_DELAY + 50);
    // ...refreshed now
    fetchContentFields();
    assertNotSame(lastObject, m_lastObject);
  }

  /**
   * When "split" and "refresh on save" options.
   */
  public void test_splitRefresh_onSave() throws Exception {
    setSyncDelay(0);
    setEditorLayout(IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN);
    openEditor0("<Shell/>");
    // initial state
    fetchContentFields();
    XmlObjectInfo lastObject = m_lastObject;
    // click "Source"
    {
      Control control = UiContext.findFirstWidget(m_sourcePage.getControl(), StyledText.class);
      EventSender eventSender = new EventSender(control);
      eventSender.moveTo(100, 100);
      eventSender.click();
    }
    // change source...
    m_lastContext.getDocument().replace(0, "<Shell".length(), "<Shell text='abc'");
    // ...no refresh yet
    fetchContentFields();
    assertSame(lastObject, m_lastObject);
    // wait sync delay, still no refresh
    waitEventLoop(SYNC_DELAY + 50);
    fetchContentFields();
    assertSame(lastObject, m_lastObject);
    // do save, refreshed now
    m_designerEditor.doSave(null);
    fetchContentFields();
    assertNotSame(lastObject, m_lastObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void setEditorLayout(int value) {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    preferences.setValue(IPreferenceConstants.P_EDITOR_LAYOUT, value);
  }

  /**
   * Sets sync/refresh delay.
   */
  private static void setSyncDelay(int delay) {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    preferences.setValue(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY, delay);
  }

  /**
   * Opens {@link AbstractXmlEditor} with given XML source. Does not activates "Design".
   */
  protected void openEditor0(String... lines) throws Exception {
    IFile file = setFileContentSrc("test/Test.xwt", getTestSource(lines));
    openEditor(file);
  }

  /**
   * Asserts that editor is in "parsed" state.
   */
  private void assertParsed() {
    fetchContentFields();
    assertNotNull(m_contentObject);
  }

  /**
   * Assert that "Source" page is before "Design" (or vice versa).
   */
  private void assertSourceDesignOrder(boolean sourceFirst) {
    Control container = m_designerEditor.getPartControl();
    new UiContext();
    List<Control> controls = UiContext.findWidgets(container, Control.class);
    Control sourceControl = m_sourcePage.getControl();
    Control designControl = m_designPage.getControl();
    int sourceIndex = controls.indexOf(sourceControl);
    int designIndex = controls.indexOf(designControl);
    assertThat(sourceIndex).isPositive();
    assertThat(designIndex).isPositive();
    if (sourceFirst) {
      assertThat(sourceIndex).isLessThan(designIndex);
    } else {
      assertThat(designIndex).isLessThan(sourceIndex);
    }
  }

  private Object getMultiPageCount() throws Exception {
    return ReflectionUtils.invokeMethod(m_designerEditor, "getPageCount()");
  }

  private Control getMultiControl(int pageIndex) throws Exception {
    return (Control) ReflectionUtils.invokeMethod(m_designerEditor, "getControl(int)", pageIndex);
  }

  private IEditorPart getMultiEditor(int pageIndex) throws Exception {
    return (IEditorPart) ReflectionUtils.invokeMethod(m_designerEditor, "getEditor(int)", pageIndex);
  }
}
