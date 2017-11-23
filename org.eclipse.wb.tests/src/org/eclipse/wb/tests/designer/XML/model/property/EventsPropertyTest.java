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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.model.property.event.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.EventsProperty;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link EventsProperty}.
 * 
 * @author scheglov_ke
 */
public class EventsPropertyTest extends XwtModelTest {
  private static final ToolkitDescription TOOLKIT = RcpToolkitDescription.INSTANCE;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setPreferencesDefaults();
  }

  @Override
  protected void tearDown() throws Exception {
    DesignerPlugin.getActivePage().closeAllEditors(false);
    super.tearDown();
  }

  private void setPreferencesDefaults() {
    IPreferenceStore preferences = TOOLKIT.getPreferences();
    preferences.setToDefault(IPreferenceConstants.P_DECORATE_ICON);
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
  public void test_hasKeyDown() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    XmlObjectInfo shell = parse("<Shell KeyDownEvent='onKeyDown' x:Class='test.Test'/>");
    // has "Events" property
    {
      Property property = PropertyUtils.getByPath(shell, "Events");
      assertNotNull(property);
      // no "value"
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      property.setValue(null);
      assertXML("<Shell KeyDownEvent='onKeyDown' x:Class='test.Test'/>");
    }
    // has "KeyDown" event
    {
      Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
      assertNotNull(property);
      assertEquals("onKeyDown", getPropertyText(property));
    }
  }

  public void test_decorateIcon() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    parse(
        "<Shell x:Class='test.Test'>",
        "  <Button wbp:name='button_1' KeyDownEvent='onKeyDown'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    // be default decoration enabled
    assertNotSame(
        button_1.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(button_1));
    assertSame(
        button_2.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(button_2));
    // disable decoration, no decoration expected
    TOOLKIT.getPreferences().setValue(IPreferenceConstants.P_DECORATE_ICON, false);
    assertSame(
        button_1.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(button_1));
    assertSame(
        button_2.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // openListener()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No corresponding Java file, i.e. no "x:Class" attribute, so do thing.
   */
  public void test_openListener_noJava() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    //
    openListener(property);
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertSame(null, activeEditor);
  }

  /**
   * Open existing method for existing event.
   */
  public void test_openListener_existingMethod() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    XmlObjectInfo shell = parse("<Shell KeyDownEvent='onKeyDown' x:Class='test.Test'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    //
    openListener(property);
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    // not changes in Java
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onKeyDown(Event event) {",
            "  }",
            "}"),
        getJavaFile());
    // method is under cursor
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("public void onKeyDown(Event event) {");
  }

  /**
   * Open new event and generate new method.
   */
  public void test_openListener_newMethod() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    //
    openListener(property);
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    // "onKeyDown()" method was added
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onKeyDown(Event event) {",
            "  }",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>");
    // method is under cursor
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("public void onKeyDown(Event event) {");
  }

  public void test_openListener_usingPath() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test'/>");
    // open listener
    {
      EventsProperty eventsProperty = (EventsProperty) shell.getPropertyByTitle("Events");
      eventsProperty.openListener("KeyDown");
    }
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onKeyDown(Event event) {",
            "  }",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>");
    // method is under cursor
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("public void onKeyDown(Event event) {");
  }

  public void test_openListener_usingDoubleClick() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    // open listener
    doPropertyDoubleClick(property, null);
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onKeyDown(Event event) {",
            "  }",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>");
    // method is under cursor
    IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
    assertThat(activeEditor).isInstanceOf(CompilationUnitEditor.class);
    String source = getJavaSource_afterCursor(activeEditor);
    assertThat(source).startsWith("public void onKeyDown(Event event) {");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>");
    //
    IMenuManager contextMenu = getContextMenu(shell);
    // check action for existing "KeyDown" event
    {
      IAction action = findChildAction(contextMenu, "KeyDown -> onKeyDown");
      assertNotNull(action);
      assertSame(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR, action.getImageDescriptor());
      // run, no change expected
      String expectedSource = m_lastContext.getContent();
      action.run();
      assertEquals(expectedSource, m_lastContext.getContent());
    }
    // add new method using action
    {
      IMenuManager manager2 = findChildMenuManager(contextMenu, "Add event handler");
      IAction action = findChildAction(manager2, "KeyUp");
      assertNotNull(action);
      // run, new method should be added
      action.run();
      DesignerPlugin.getActiveEditor().doSave(null);
      assertEquals(
          getJavaSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "public class Test {",
              "  public void onKeyDown(Event event) {",
              "  }",
              "  public void onKeyUp(Event event) {",
              "  }",
              "}"),
          getJavaFile());
      assertXML("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown' KeyUpEvent='onKeyUp'/>");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_deleteEvent_noJava() throws Exception {
    XmlObjectInfo shell = parse("<Shell KeyDownEvent='onKeyDown'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    // do delete
    deleteEventPropertyWithGUI(property);
    assertXML("<Shell/>");
  }

  public void test_deleteEvent_existingMethod() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    // do delete
    deleteEventPropertyWithGUI(property);
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test'/>");
  }

  public void test_deleteEvent_noMethod() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "}");
    XmlObjectInfo shell = parse("<Shell x:Class='test.Test'/>");
    Property property = PropertyUtils.getByPath(shell, "Events/KeyDown");
    // do delete
    deleteEventPropertyWithGUI(property);
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test'/>");
  }

  public void test_deleteComponent() throws Exception {
    createTestClass(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public void onKeyDown(Event event) {",
        "  }",
        "}");
    parse(
        "<Shell x:Class='test.Test'>",
        "  <Button wbp:name='button' KeyDownEvent='onKeyDown'/>",
        "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    // do delete
    button.delete();
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "}"),
        getJavaFile());
    assertXML("<Shell x:Class='test.Test'/>");
  }

  /**
   * Deletes value of given {@link Property} and clicks "OK" in confirmation dialog.
   */
  private static void deleteEventPropertyWithGUI(final Property property) throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        property.setValue(Property.UNKNOWN_VALUE);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Confirm");
        context.clickButton("OK");
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createTestClass(String... lines) throws Exception {
    setFileContentSrc("test/Test.java", getJavaSource(lines));
    waitForAutoBuild();
  }

  private static void openListener(Property property) throws Exception {
    ReflectionUtils.invokeMethod(property, "openListener()");
  }

  private static String getJavaFile() throws Exception {
    return getFileContentSrc("test/Test.java");
  }

  private String getJavaSource_afterCursor(IEditorPart activeEditor) throws Exception {
    ITextEditor textEditor = (ITextEditor) activeEditor;
    ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
    return getJavaFile().substring(selection.getOffset());
  }
}