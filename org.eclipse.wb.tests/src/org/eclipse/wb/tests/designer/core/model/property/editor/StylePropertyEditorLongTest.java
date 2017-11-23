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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

/**
 * Test for {@link StylePropertyEditor}.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class StylePropertyEditorLongTest extends SwingModelTest {
  private GenericProperty m_styleProperty;
  private StylePropertyEditor m_editor;
  private Property[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    setFileContentSrc(
        "test/StylePanel.java",
        getTestSource(
            "public class StylePanel extends JPanel {",
            "  public StylePanel(long style) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/SWT.java",
        getTestSource(
            "public interface SWT {",
            "  long NONE = 0;",
            "  long B0 = 1 << 0;",
            "  long B1 = 1 << 1;",
            "  long B2 = 1 << 2;",
            "  long B3 = 1 << 3;",
            "  // mix",
            "  long B0_B1 = B0 | B1;",
            "  long B0_B2 = B0 | B2;",
            "  //",
            "  long R1 = 1 << 4;",
            "  long R2 = 1 << 5;",
            "  long R3 = 1 << 6;",
            "  long R4 = 1 << 7;",
            "}"));
    forgetCreatedResources();
  }

  // TODO clear all fields in DesignerEditorTestCase
  @Override
  protected void tearDown() throws Exception {
    m_styleProperty = null;
    m_editor = null;
    m_properties = null;
    styleItems = null;
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
  // Set
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_StyleProperty_Set_1() throws Exception {
    parseStyleProperties("set|B0 B1 B2:specialName");
    {
      assertThat(m_properties).hasSize(3);
      // titles
      assertEquals("b0", m_properties[0].getTitle());
      assertEquals("b1", m_properties[1].getTitle());
      assertEquals("specialName", m_properties[2].getTitle());
      // values
      for (Property property : m_properties) {
        assertInstanceOf(BooleanPropertyEditor.class, property.getEditor());
        assertEquals(false, property.getValue());
      }
    }
    // initial state
    assertEditorText("[]");
    // "B0" := true
    m_properties[0].setValue(Boolean.TRUE);
    assertEquals(Boolean.TRUE, m_properties[0].getValue());
    assertStyleSource("SWT.B0");
    assertEditorText("[B0]");
    // "B2" := true
    m_properties[2].setValue(Boolean.TRUE);
    assertEquals(Boolean.TRUE, m_properties[2].getValue());
    assertStyleSource("SWT.B0 | SWT.B2");
    assertEditorText("[B0, B2]");
    // "B0" := unknown
    m_properties[0].setValue(Property.UNKNOWN_VALUE);
    assertEquals(Boolean.FALSE, m_properties[0].getValue());
    assertStyleSource("SWT.B2");
    assertEditorText("[B2]");
    // "B2" := unknown
    m_properties[2].setValue(Property.UNKNOWN_VALUE);
    assertEquals(Boolean.FALSE, m_properties[2].getValue());
    assertStyleSource("SWT.NONE");
    assertEditorText("[]");
  }

  public void test_StyleProperty_Set_2() throws Exception {
    parseStyleProperties("set|B0 noSuchField");
    // check
    String[] names = PropertyUtils.getTitles(m_properties);
    assertThat(names).containsOnly("b0");
    // warnings expected
    checkWarning("StylePropertyEditor: can not find field test.SWT.noSuchField");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Select
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Select_1() throws Exception {
    parseStyleProperties("set|B0 B1", "select0|align R1 R1 R2 R3");
    // check sub properties
    String[] subNames = {"b0", "b1", "align"};
    assertEquals(subNames.length, m_properties.length);
    for (int i = 0; i < 2; i++) {
      Property property = m_properties[i];
      assertEquals(subNames[i], property.getTitle());
      assertInstanceOf(BooleanPropertyEditor.class, property.getEditor());
      assertEquals(false, property.getValue());
    }
    //
    Property alignProperty = m_properties[2];
    assertEquals(subNames[2], alignProperty.getTitle());
    assertEquals("R1", alignProperty.getValue());
    // "B0" := true
    m_properties[0].setValue(true);
    assertEquals(true, m_properties[0].getValue());
    assertStyleSource("SWT.B0");
    assertEditorText("[B0]");
    // "align" := R2
    alignProperty.setValue("R2");
    assertEquals("R2", alignProperty.getValue());
    assertStyleSource("SWT.B0 | SWT.R2");
    assertEditorText("[B0, R2]");
    // "align" := R3
    alignProperty.setValue("R3");
    assertEquals("R3", alignProperty.getValue());
    assertStyleSource("SWT.B0 | SWT.R3");
    assertEditorText("[B0, R3]");
    // "B0" := false
    m_properties[0].setValue(false);
    assertEquals(false, m_properties[0].getValue());
    assertStyleSource("SWT.R3");
    assertEditorText("[R3]");
    // "align" := R1
    alignProperty.setValue("R1");
    assertEquals("R1", alignProperty.getValue());
    assertStyleSource("SWT.NONE");
    assertEditorText("[]");
    // "align" := R2
    alignProperty.setValue("R2");
    assertEquals("R2", alignProperty.getValue());
    assertStyleSource("SWT.R2");
    assertEditorText("[R2]");
    // "align" := unknown
    alignProperty.setValue(Property.UNKNOWN_VALUE);
    assertEquals("R1", alignProperty.getValue());
    assertStyleSource("SWT.NONE");
    assertEditorText("[]");
  }

  public void test_Select_2() throws Exception {
    parseStyleProperties("select0|align 15 15 R1 noSuchField");
    assertThat(PropertyUtils.getTitles(m_properties)).containsOnly("align");
    Property alignProperty = m_properties[0];
    //
    assertEquals("align", alignProperty.getTitle());
    assertEquals("15", alignProperty.getValue());
    //
    assertInstanceOf(StringComboPropertyEditor.class, alignProperty.getEditor());
    String[] items =
        (String[]) ReflectionUtils.getFieldObject(alignProperty.getEditor(), "m_items");
    assertEquals(2, items.length);
    assertEquals("15", items[0]);
    assertEquals("R1", items[1]);
    // check warnings
    checkWarning("StylePropertyEditor: can not find field test.SWT.noSuchField");
  }

  public void test_Select_for_methodProperty() throws Exception {
    setFileContentSrc(
        "test/TestComposite.java",
        getTestSource(
            "public class TestComposite extends JPanel {",
            "  private long m_format;",
            "  public long getFormat() {",
            "    return m_format;",
            "  }",
            "  public void setFormat(long format) {",
            "    m_format = format;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setFormat(long)'>",
            "    <editor id='style'>",
            "      <parameter name='class'>test.SWT</parameter>",
            "      <parameter name='select0'>type NONE NONE R1 R2</parameter>",
            "    </editor>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseStyleProperties0("format", new String[]{
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestComposite composite = new TestComposite();",
        "    add(composite);",
        "  }",
        "}"});
    assertThat(m_properties).hasSize(1);
    Property alignProperty = m_properties[0];
    // initially no style
    assertEditorText("[]");
    // := R1
    alignProperty.setValue("R1");
    assertEquals("R1", alignProperty.getValue());
    assertEditorText("[R1]");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestComposite composite = new TestComposite();",
        "    composite.setFormat(SWT.R1);",
        "    add(composite);",
        "  }",
        "}");
    // := R2
    alignProperty.setValue("R2");
    assertEquals("R2", alignProperty.getValue());
    assertEditorText("[R2]");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestComposite composite = new TestComposite();",
        "    composite.setFormat(SWT.R2);",
        "    add(composite);",
        "  }",
        "}");
    // := NONE
    alignProperty.setValue("NONE");
    assertEquals("NONE", alignProperty.getValue());
    assertEditorText("[]");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestComposite composite = new TestComposite();",
        "    add(composite);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Macro
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Macro_1() throws Exception {
    parseStyleProperties("set|B0 B1 B2 B3", "macro0|mix B0_B1 B0_B2 R1");
    Property property_B0 = m_properties[0];
    Property property_B1 = m_properties[1];
    Property property_B2 = m_properties[2];
    Property property_B3 = m_properties[3];
    Property propertyMix = m_properties[4];
    // "B0" := true
    property_B0.setValue(true);
    assertStyleSource("SWT.B0");
    assertEquals("", propertyMix.getValue());
    assertEditorText("[B0]");
    // "mix" := B0_B1
    propertyMix.setValue("B0_B1");
    assertEquals("B0_B1", propertyMix.getValue());
    assertEquals(true, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(false, property_B3.getValue());
    assertStyleSource("SWT.B0_B1");
    assertEditorText("[B0_B1]");
    // add "B3"
    property_B3.setValue(true);
    assertEquals("B0_B1", propertyMix.getValue());
    assertEquals(true, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(true, property_B3.getValue());
    assertStyleSource("SWT.B0_B1 | SWT.B3");
    assertEditorText("[B0_B1, B3]");
    // set "R1", change style fully
    propertyMix.setValue("R1");
    assertEquals("R1", propertyMix.getValue());
    assertEquals(false, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(false, property_B3.getValue());
    assertStyleSource("SWT.R1");
    assertEditorText("[R1]");
  }

  public void test_Macro_2() throws Exception {
    parseStyleProperties("set|B0 B1", "macro0|mix B0_B1 R1");
    Property property_B0 = m_properties[0];
    Property property_B1 = m_properties[1];
    Property propertyMix = m_properties[2];
    // "mix" := B0_B1
    propertyMix.setValue("B0_B1");
    assertEquals("B0_B1", propertyMix.getValue());
    assertEquals(true, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertStyleSource("SWT.B0_B1");
    assertEditorText("[B0_B1]");
    // remove "B0"
    property_B0.setValue(false);
    assertEquals("", propertyMix.getValue());
    assertEquals(false, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertStyleSource("SWT.B1");
    assertEditorText("[B1]");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enumeration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_enum() throws Exception {
    parseStyleProperties("SWT.R1", new String[]{"set|B0 B1", "enum0|en 0xF0 R1 R2 R3"});
    // "B0"
    Property property_B0 = m_properties[0];
    assertEquals("b0", property_B0.getTitle());
    assertInstanceOf(BooleanPropertyEditor.class, property_B0.getEditor());
    assertEquals(false, property_B0.getValue());
    // "B1"
    Property property_B1 = m_properties[1];
    assertEquals("b1", property_B1.getTitle());
    assertInstanceOf(BooleanPropertyEditor.class, property_B1.getEditor());
    assertEquals(false, property_B1.getValue());
    // "enum"
    Property propertyEnum = m_properties[2];
    assertEquals("en", propertyEnum.getTitle());
    assertEquals("R1", propertyEnum.getValue());
    // "B0" := true
    property_B0.setValue(true);
    assertEquals(true, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals("R1", propertyEnum.getValue());
    assertStyleSource("SWT.B0 | SWT.R1");
    assertEditorText("[B0, R1]");
    // "enum" := R2
    propertyEnum.setValue("R2");
    assertEquals(true, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals("R2", propertyEnum.getValue());
    assertStyleSource("SWT.B0 | SWT.R2");
    assertEditorText("[B0, R2]");
    // "B0" := false
    property_B0.setValue(false);
    assertEquals(false, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals("R2", propertyEnum.getValue());
    assertStyleSource("SWT.R2");
    assertEditorText("[R2]");
    // "enum" := R1
    propertyEnum.setValue("R1");
    assertEquals(false, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals("R1", propertyEnum.getValue());
    assertStyleSource("SWT.R1");
    assertEditorText("[R1]");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup
  //
  ////////////////////////////////////////////////////////////////////////////
  IContributionItem[] styleItems;

  private void prepareStyleItems(boolean cascade) throws Exception {
    // prepare "Style" menu
    IMenuManager styleMenu;
    {
      MenuManager manager = getDesignerMenuManager();
      m_editor.contributeActions(m_styleProperty, manager, "Style", cascade);
      styleMenu = findChildMenuManager(manager, "Style");
      assertNotNull(styleMenu);
    }
    // remember items
    styleItems = styleMenu.getItems();
  }

  public void test_popup() throws Exception {
    parseStyleProperties("set|B0 B1 B2", "select0|align R1 R1 R2 R3");
    prepareStyleItems(false);
    // check items
    assertThat(styleItems).hasSize(7);
    IContributionItem item_B0 = styleItems[0];
    IContributionItem item_R2 = styleItems[5];
    checkItem("b0", IAction.AS_CHECK_BOX, false, item_B0);
    checkItem("b1", IAction.AS_CHECK_BOX, false, styleItems[1]);
    checkItem("b2", IAction.AS_CHECK_BOX, false, styleItems[2]);
    assertInstanceOf(Separator.class, styleItems[3]);
    checkItem("R1", IAction.AS_RADIO_BUTTON, true, styleItems[4]);
    checkItem("R2", IAction.AS_RADIO_BUTTON, false, item_R2);
    checkItem("R3", IAction.AS_RADIO_BUTTON, false, styleItems[6]);
    // run "B0"
    ((ActionContributionItem) item_B0).getAction().run();
    assertStyleSource("SWT.B0");
    // select "R2"
    ((ActionContributionItem) item_R2).getAction().setChecked(true);
    ((ActionContributionItem) item_R2).getAction().run();
    assertStyleSource("SWT.B0 | SWT.R2");
  }

  public void test_popup_cascade() throws Exception {
    parseStyleProperties("set|B0 B1", "select0|align R1 R1 R2 R3");
    prepareStyleItems(true);
    //
    assertThat(styleItems).hasSize(4);
    checkItem("b0", IAction.AS_CHECK_BOX, false, styleItems[0]);
    checkItem("b1", IAction.AS_CHECK_BOX, false, styleItems[1]);
    assertInstanceOf(Separator.class, styleItems[2]);
    assertInstanceOf(MenuManager.class, styleItems[3]);
    //
    IMenuManager alignMenu = (IMenuManager) styleItems[3];
    IContributionItem[] alignItems = alignMenu.getItems();
    assertThat(alignItems).hasSize(4);
    assertInstanceOf(Separator.class, alignItems[0]);
    checkItem("R1", IAction.AS_RADIO_BUTTON, true, alignItems[1]);
    checkItem("R2", IAction.AS_RADIO_BUTTON, false, alignItems[2]);
    checkItem("R3", IAction.AS_RADIO_BUTTON, false, alignItems[3]);
  }

  public void test_popup_enum() throws Exception {
    parseStyleProperties("set|B0 B1", "enum0|enum 0xF0 R1 R2 R3");
    prepareStyleItems(false);
    //
    assertThat(styleItems).hasSize(2 + 1 + 3);
    checkItem("b0", IAction.AS_CHECK_BOX, false, styleItems[0]);
    checkItem("b1", IAction.AS_CHECK_BOX, false, styleItems[1]);
    assertInstanceOf(Separator.class, styleItems[2]);
    checkItem("R1", IAction.AS_RADIO_BUTTON, false, styleItems[3]);
    checkItem("R2", IAction.AS_RADIO_BUTTON, false, styleItems[4]);
    checkItem("R3", IAction.AS_RADIO_BUTTON, false, styleItems[5]);
  }

  public void test_popup_macro() throws Exception {
    parseStyleProperties("set|B0 B1", "macro0|mix B0_B1 B0_B2 R1");
    prepareStyleItems(false);
    //
    assertThat(styleItems).hasSize(2 + 1 + 3);
    checkItem("b0", IAction.AS_CHECK_BOX, false, styleItems[0]);
    checkItem("b1", IAction.AS_CHECK_BOX, false, styleItems[1]);
    assertInstanceOf(Separator.class, styleItems[2]);
    checkItem("B0_B1", IAction.AS_RADIO_BUTTON, false, styleItems[3]);
    checkItem("B0_B2", IAction.AS_RADIO_BUTTON, false, styleItems[4]);
    checkItem("R1", IAction.AS_RADIO_BUTTON, false, styleItems[5]);
  }

  private static void checkItem(String text, int style, boolean value, IContributionItem item)
      throws Exception {
    assertInstanceOf(ActionContributionItem.class, item);
    ActionContributionItem contributionItem = (ActionContributionItem) item;
    IAction action = contributionItem.getAction();
    assertNotNull(action);
    assertEquals(text, action.getText());
    assertEquals(style, action.getStyle());
    assertEquals(value, action.isChecked());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IValueSourcePropertyEditor() throws Exception {
    int value = 1 << 0 | 1 << 2;
    parseStyleProperties("set|B0 B1 B2");
    // check "value -> source" directly
    {
      IValueSourcePropertyEditor sourceProvider =
          (IValueSourcePropertyEditor) m_styleProperty.getEditor();
      assertEquals("test.SWT.B0 | test.SWT.B2", sourceProvider.getValueSource(value));
    }
    // initial state
    assertStyleSource("SWT.NONE");
    // use setValue()
    m_styleProperty.setValue(value);
    assertStyleSource("SWT.B0 | SWT.B2");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IClipboardSourceProvider() throws Exception {
    parseStyleProperties("SWT.B0 | SWT.B2", new String[]{"set|B0 B1 B2"});
    // check clipboard source
    IClipboardSourceProvider sourceProvider =
        (IClipboardSourceProvider) m_styleProperty.getEditor();
    assertEquals("test.SWT.B0 | test.SWT.B2", sourceProvider.getClipboardSource(m_styleProperty));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void assertEditorText(String text) throws Exception {
    assertEquals(text, getPropertyText(m_styleProperty));
  }

  private void assertStyleSource(String styleSource) {
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new TestComposite(" + styleSource + "));",
        "  }",
        "}");
  }

  private void parseStyleProperties(String... styleLines) throws Exception {
    parseStyleProperties("SWT.NONE", styleLines);
  }

  private void parseStyleProperties(String styleSource, String[] styleLines0) throws Exception {
    setFileContentSrc(
        "test/TestComposite.java",
        getTestSource(
            "public class TestComposite extends JPanel {",
            "  public TestComposite(long style) {",
            "  }",
            "}"));
    // convert: name|value into "parameter" line
    String[] styleLines = new String[styleLines0.length];
    for (int i = 0; i < styleLines0.length; i++) {
      String line = styleLines0[i];
      int index = line.indexOf('|');
      String name = line.substring(0, index);
      String value = line.substring(index + 1);
      styleLines[i] = MessageFormat.format("<parameter name=''{0}''>{1}</parameter>", name, value);
    }
    // prepare description
    String[] lines;
    lines =
        new String[]{
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='long' defaultSource='test.SWT.NONE'>",
            "        <editor id='style'>",
            "          <parameter name='class'>test.SWT</parameter>"};
    lines = CodeUtils.join(lines, styleLines);
    lines =
        CodeUtils.join(lines, new String[]{
            "        </editor>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"});
    setFileContentSrc("test/TestComposite.wbp-component.xml", getSource(lines));
    waitForAutoBuild();
    // parse
    parseStyleProperties0("Constructor/style", new String[]{
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new TestComposite(" + styleSource + "));",
        "  }",
        "}"});
  }

  private void parseStyleProperties0(String propertyPath, String[] lines) throws Exception {
    ContainerInfo panel = parseContainer(lines);
    panel.refresh();
    // prepare "style" property
    ComponentInfo component = panel.getChildrenComponents().get(0);
    m_styleProperty = (GenericProperty) PropertyUtils.getByPath(component, propertyPath);
    // use its editor
    m_editor = (StylePropertyEditor) m_styleProperty.getEditor();
    m_properties = m_editor.getProperties(m_styleProperty);
  }

  private void checkWarning(String message) throws Exception {
    boolean find = false;
    for (EditorWarning warning : m_lastState.getWarnings()) {
      find = message.equals(warning.getMessage());
      if (find) {
        break;
      }
    }
    assertTrue(find);
  }
}