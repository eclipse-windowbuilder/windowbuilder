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
package org.eclipse.wb.tests.designer.XML;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.ITextValuePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract superclass for any XML test - model or GEF based.
 * 
 * @author scheglov_ke
 */
public abstract class AbstractXmlObjectTest extends AbstractJavaProjectTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
      configureNewProject();
    }
  }

  /**
   * Configures created project.
   */
  protected abstract void configureNewProject() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java source
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the Java source.
   */
  protected final String getJavaSource(String... lines) {
    lines = getDoubleQuotes(lines);
    lines = getJavaSource_decorate(lines);
    return getSource(lines);
  }

  /**
   * Asserts that Java source has expected content.
   */
  protected final void assertJava(String... lines) {
    String source = getJavaSource(lines);
    assertEquals(source, getJavaSourceToAssert());
  }

  /**
   * @return the Java source related to current XML.
   */
  protected final void printJavaLinesSource() {
    String[] lines = StringUtils.split(getJavaSourceToAssert(), "\r\n");
    System.out.println(getLinesForSourceDQ(lines));
  }

  /**
   * @return the Java source related to current XML.
   */
  protected abstract String getJavaSourceToAssert();

  /**
   * "Decorates" given lines of source, usually adds required imports.
   */
  protected abstract String[] getJavaSource_decorate(String... lines);

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML source
  //
  ////////////////////////////////////////////////////////////////////////////
  protected XmlObjectInfo m_lastObject;
  protected EditorContext m_lastContext;
  protected ClassLoader m_lastLoader;

  /**
   * Asserts that XML document has expected content.
   */
  protected final void assertXML(String... lines) {
    String source = getTestSource(lines);
    assertEquals(source, m_lastContext.getContent());
  }

  /**
   * @return the XML source for parsing and checking in {@link #assertXML(String...)}.
   */
  protected final String getTestSource(String... lines) {
    lines = removeFillerLines(lines);
    lines = getDoubleQuotes(lines);
    lines = getTestSource_decorate(lines);
    return getSource(lines);
  }

  /**
   * Removes liens which start with "// filler".
   */
  protected static String[] removeFillerLines(String... lines) {
    while (lines.length != 0 && lines[0].startsWith("// filler")) {
      lines = (String[]) ArrayUtils.remove(lines, 0);
    }
    return lines;
  }

  /**
   * "Decorates" given lines of XML. By default adds required namespace declarations, see
   * {@link #getTestSource_namespaces()}.
   */
  protected final String[] getTestSource_decorate(String... lines) {
    // try to find line where name spaces should be inserted
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.startsWith("<?xml")) {
        continue;
      }
      // prepare position for namespaces
      int index = StringUtils.indexOfAny(line, " />");
      if (index > 0) {
        if (line.charAt(index - 1) == '-') {
          continue;
        }
      }
      // insert namespaces into line
      line = getXMLSource_insertNameSpaces_intoGivenLine(line, index);
      // modify copy
      lines = lines.clone();
      lines[i] = line;
      // done
      break;
    }
    return lines;
  }

  private String getXMLSource_insertNameSpaces_intoGivenLine(String line, int index) {
    String ns = getSourceDQ(getTestSource_namespaces());
    String before = line.substring(0, index);
    String after = line.substring(index);
    // if NS end with white space, then remove whitespace in "after" part
    if (!ns.isEmpty()) {
      int nsLength = ns.length();
      char nsLastChar = ns.charAt(nsLength - 1);
      if (Character.isWhitespace(nsLastChar)) {
        after = after.trim();
      }
    }
    // done
    return before + ns + after;
  }

  /**
   * @return the namespace declarations to add into test source (using single quotes).
   */
  protected abstract String getTestSource_namespaces();

  /**
   * Prints XML in {@link #m_lastContext}, ready to paste {@link #assertXML(String...)} invocation.
   */
  protected void printEditorLinesSource() {
    StringBuffer buffer = new StringBuffer();
    // lines
    String[] lines = StringUtils.split(m_lastContext.getContent(), "\r\n");
    for (String line : lines) {
      buffer.append('"');
      line = StringUtils.replace(line, "\t", "  ");
      {
        line = line.replace('"', '\'');
        buffer.append(StringUtilities.escapeForJavaSource(line));
      }
      buffer.append('"');
      buffer.append(",\n");
    }
    // end
    String result = buffer.toString();
    result = StringUtils.removeEnd(result, ",\n");
    {
      // remove name spaces
      result = result.replaceAll("\\s*xmlns:*\\w*=\\s*'[^']*'", "");
      // remove empty lines in root object (after name space in separate lines)
      {
        result = result.replaceAll("\"\",\n", "");
        result = result.replaceAll("\",\n\">\",\n", ">\",\n");
      }
      // add filler
      {
        String filler = "\"// filler filler filler filler filler\",\n";
        result = filler + result;
        result = filler + result;
      }
    }
    System.out.println(result);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prints hierarchy of {@link #m_lastObject}, ready to paste as arguments for
   * {@link #assertHierarchy(String...)}.
   */
  public void printHierarchySource() {
    printHierarchySource(m_lastObject);
  }

  /**
   * Asserts that {@link #m_lastObject} has expected hierarchy.
   */
  public void assertHierarchy(String... lines) {
    String expected = getSourceDQ(lines);
    // remove "filler"
    while (expected.startsWith("// filler")) {
      expected = StringUtils.substringAfter(expected, "\n");
    }
    // assert
    String actual = printHierarchy(m_lastObject);
    assertEquals(expected, actual);
  }

  /**
   * Creates string for hierarchy of {@link ObjectInfo}'s starting from given root.
   */
  protected static String printHierarchy(ObjectInfo root) {
    final StringBuffer buffer = new StringBuffer();
    root.accept(new ObjectInfoVisitor() {
      private int m_level;

      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        buffer.append(StringUtils.repeat("\t", m_level));
        buffer.append(objectInfo.toString());
        buffer.append("\n");
        m_level++;
        return true;
      }

      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        m_level--;
      }
    });
    return buffer.toString();
  }

  /**
   * Creates source for {@link String}'s array for hierarchy of {@link ObjectInfo}'s starting from
   * given root.
   */
  private static void printHierarchySource(ObjectInfo root) {
    final StringBuffer buffer = new StringBuffer();
    root.accept(new ObjectInfoVisitor() {
      private int m_level;

      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        buffer.append('"');
        buffer.append(StringUtils.repeat("  ", m_level));
        {
          String line = objectInfo.toString();
          line = line.replace('"', '\'');
          buffer.append(StringUtilities.escapeJava(line));
        }
        buffer.append('"');
        buffer.append(",\n");
        m_level++;
        return true;
      }

      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        m_level--;
      }
    });
    String result = buffer.toString();
    result = StringUtils.removeEnd(result, ",\n");
    {
      String filler = "\"// filler filler filler filler filler\",\n";
      result = filler + result;
      result = filler + result;
    }
    System.out.println(result);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Refreshes last hierarchy.
   */
  protected final void refresh() throws Exception {
    m_lastObject.refresh();
  }

  /**
   * @return {@link XmlObjectInfo} with given value in <code>wbp:name</code> attribute.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends XmlObjectInfo> T getObjectByName(final String name) {
    final XmlObjectInfo[] result = new XmlObjectInfo[1];
    m_lastObject.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo object) throws Exception {
        if (object instanceof XmlObjectInfo) {
          XmlObjectInfo xmlObject = (XmlObjectInfo) object;
          CreationSupport creationSupport = xmlObject.getCreationSupport();
          if (!(creationSupport instanceof IImplicitCreationSupport)) {
            DocumentElement element = creationSupport.getElement();
            if (name.equals(element.getAttribute("wbp:name"))) {
              result[0] = xmlObject;
            }
          }
        }
      }
    });
    return (T) result[0];
  }

  /**
   * Asserts that {@link #m_lastContext} has no any parse/refresh error or warnings.
   */
  protected void assertNoErrors() {
    // warnings
    List<EditorWarning> warnings = m_lastContext.getWarnings();
    if (!warnings.isEmpty()) {
      for (EditorWarning warning : warnings) {
        System.out.println("------------------ warning ------------------");
        System.out.println(warning.getMessage());
        if (warning.getException() != null) {
          warning.getException().printStackTrace();
        }
      }
      fail("No warnings expected.");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return sub {@link Property}'s of given Property with {@link IComplexPropertyEditor}.
   */
  protected static Property[] getSubProperties(Property property) throws Exception {
    return ((IComplexPropertyEditor) property.getEditor()).getProperties(property);
  }

  /**
   * @return the text from {@link TextDisplayPropertyEditor} for given {@link Property}.
   */
  protected static String getPropertyText(Property property) throws Exception {
    return (String) ReflectionUtils.invokeMethod2(
        property.getEditor(),
        "getText",
        Property.class,
        property);
  }

  /**
   * Set value of {@link ITextValuePropertyEditor} using text.
   */
  protected static void setPropertyText(Property property, String text) throws Exception {
    ReflectionUtils.invokeMethod2(
        property.getEditor(),
        "setText",
        Property.class,
        String.class,
        property,
        text);
  }

  /**
   * @return the {@link IClipboardSourceProvider} source from {@link PropertyEditor}.
   */
  protected static String getPropertyClipboardSource(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    PropertyEditor propertyEditor = property.getEditor();
    IClipboardSourceProvider sourceProvider = (IClipboardSourceProvider) propertyEditor;
    return sourceProvider.getClipboardSource(genericProperty);
  }

  /**
   * @return the text of {@link PropertyTooltipTextProvider} that should implement given
   *         {@link IAdaptable}.
   */
  protected static String getPropertyTooltipText(Property property) throws Exception {
    PropertyTooltipProvider provider = property.getAdapter(PropertyTooltipProvider.class);
    assertInstanceOf(PropertyTooltipTextProvider.class, provider);
    PropertyTooltipTextProvider textProvider = (PropertyTooltipTextProvider) provider;
    return (String) ReflectionUtils.invokeMethod(
        textProvider,
        "getText(org.eclipse.wb.internal.core.model.property.Property)",
        property);
  }

  /**
   * @return the text for {@link AbstractTextPropertyEditor}.
   */
  public static String getTextEditorText(Property property) throws Exception {
    PropertyEditor editor = property.getEditor();
    return (String) ReflectionUtils.invokeMethod(
        editor,
        "getEditorText(org.eclipse.wb.internal.core.model.property.Property)",
        property);
  }

  /**
   * Updates {@link Property} by setting text for {@link AbstractTextPropertyEditor}.
   */
  public static void setTextEditorText(Property property, String text) throws Exception {
    PropertyEditor editor = property.getEditor();
    ReflectionUtils.invokeMethod(
        editor,
        "setEditorText(org.eclipse.wb.internal.core.model.property.Property,java.lang.String)",
        property,
        text);
  }

  /**
   * Activates {@link PropertyEditor}.
   */
  protected static void activateProperty(Property property, org.eclipse.swt.graphics.Point location)
      throws Exception {
    PropertyEditor propertyEditor = property.getEditor();
    propertyEditor.activate(null, property, location);
  }

  /**
   * Opens dialog of {@link TextDialogPropertyEditor}.
   */
  protected static void openPropertyDialog(Property property) throws Exception {
    ReflectionUtils.invokeMethod(
        property.getEditor(),
        "openDialog(org.eclipse.wb.internal.core.model.property.Property)",
        property);
  }

  /**
   * Calls {@link PropertyEditor#doubleClick(Property, org.eclipse.swt.graphics.Point)}.
   */
  protected static void doPropertyDoubleClick(Property property, Point location) throws Exception {
    property.getEditor().doubleClick(property, location != null ? location.getSwtPoint() : null);
  }

  /**
   * Asks "null" type from {@link ExpressionAccessor}, to have coverage.
   */
  protected static void callExpressionAccessor_getAdapter_withWrongType(Property property) {
    assertSame(
        null,
        ((GenericPropertyImpl) property).getDescription().getAccessor().getAdapter(null));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo property editor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Shell TEST_COMBO_SHELL = new Shell();
  private static final CCombo3 TEST_COMBO = new CCombo3(TEST_COMBO_SHELL, SWT.NONE);

  /**
   * Fill combo with items.
   */
  protected static void addComboPropertyItems(Property property) {
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "addItems("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CCombo3)";
    TEST_COMBO.removeAll();
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
  }

  /**
   * @return items from combo.
   */
  protected static List<String> getComboPropertyItems() {
    List<String> items = Lists.newArrayList();
    int itemCount = TEST_COMBO.getItemCount();
    for (int i = 0; i < itemCount; i++) {
      items.add(TEST_COMBO.getItem(i));
    }
    return items;
  }

  /**
   * @return the selection index in combo.
   */
  protected static int getComboPropertySelection() {
    return TEST_COMBO.getSelectionIndex();
  }

  /**
   * Sets the selection index in combo, usually to use then
   * {@link #setComboPropertySelection(Property)} and validate result using
   * {@link #getComboPropertySelection()}.
   */
  protected static void setComboPropertySelection(int index) {
    TEST_COMBO.select(index);
  }

  /**
   * Sets selection which corresponds to the value of {@link Property}.
   */
  protected static void setComboPropertySelection(Property property) {
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "selectItem("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CCombo3)";
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
  }

  /**
   * Simulates user selection of item with given index, updates {@link Property}.
   */
  protected static void setComboPropertyValue(Property property, int index) {
    PropertyEditor propertyEditor = property.getEditor();
    String signature =
        "toPropertyEx("
            + "org.eclipse.wb.internal.core.model.property.Property,"
            + "org.eclipse.wb.core.controls.CCombo3,"
            + "int)";
    ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO, index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the context menu for {@link ObjectInfo} selection.
   */
  public static IMenuManager getContextMenu(ObjectInfo... objectsArray) throws Exception {
    IMenuManager manager = getDesignerMenuManager();
    List<ObjectInfo> objects = ImmutableList.copyOf(objectsArray);
    ObjectInfo object = objectsArray[0];
    object.getBroadcastObject().addContextMenu(objects, object, manager);
    return manager;
  }

  /**
   * @return the selection actions for empty selection.
   */
  public static List<Object> getSelectionActions_noSelection(ObjectInfo root) throws Exception {
    List<Object> actions = Lists.newArrayList();
    ImmutableList<ObjectInfo> objects = ImmutableList.<ObjectInfo>of();
    root.getBroadcastObject().addSelectionActions(objects, actions);
    return actions;
  }

  /**
   * @return the selection actions, displayed on editor toolbar.
   */
  public static List<Object> getSelectionActions(ObjectInfo... objectsArray) throws Exception {
    List<Object> actions = Lists.newArrayList();
    if (objectsArray.length != 0) {
      ObjectInfo object = objectsArray[0];
      List<ObjectInfo> objects = ImmutableList.copyOf(objectsArray);
      object.getBroadcastObject().addSelectionActions(objects, actions);
    }
    return actions;
  }
}