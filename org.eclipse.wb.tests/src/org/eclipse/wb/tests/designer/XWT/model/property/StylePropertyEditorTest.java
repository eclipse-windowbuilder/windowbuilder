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
package org.eclipse.wb.tests.designer.XWT.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Test for {@link StylePropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class StylePropertyEditorTest extends XwtModelTest {
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
        "test/SWT.java",
        getSource(
            "package test;",
            "public interface SWT {",
            "  int NONE = 0;",
            "  int B0 = 1 << 0;",
            "  int B1 = 1 << 1;",
            "  int B2 = 1 << 2;",
            "  int B3 = 1 << 3;",
            "  //",
            "  int R1 = 1 << 4;",
            "  int R2 = 1 << 5;",
            "  int R3 = 1 << 6;",
            "  int R4 = 1 << 7;",
            "  // mix",
            "  int B0_B1 = B0 | B1;",
            "  int B0_B2 = B0 | B2;",
            "  int B0_R2 = B0 | R2;",
            "}"));
    waitForAutoBuild();
    forgetCreatedResources();
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
  public void test_Set_1() throws Exception {
    parseStyleProperties("set|B0 B1 B2:specialName");
    {
      assertThat(m_properties).hasSize(3);
      // titles
      assertEquals("b0", m_properties[0].getTitle());
      assertEquals("b1", m_properties[1].getTitle());
      assertEquals("specialName", m_properties[2].getTitle());
      // always modified
      assertTrue(m_properties[0].isModified());
      assertTrue(m_properties[1].isModified());
      assertTrue(m_properties[2].isModified());
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
    assertStyleSource("B0");
    assertEditorText("[B0]");
    // "B2" := true
    m_properties[2].setValue(Boolean.TRUE);
    assertEquals(Boolean.TRUE, m_properties[2].getValue());
    assertStyleSource("B0 | B2");
    assertEditorText("[B0, B2]");
    // "B0" := unknown
    m_properties[0].setValue(Property.UNKNOWN_VALUE);
    assertEquals(Boolean.FALSE, m_properties[0].getValue());
    assertStyleSource("B2");
    assertEditorText("[B2]");
    // "B2" := unknown
    m_properties[2].setValue(Property.UNKNOWN_VALUE);
    assertEquals(Boolean.FALSE, m_properties[2].getValue());
    assertStyleSource(null);
    assertEditorText("[]");
  }

  public void test_Set_ignoreIfNoSuchField() throws Exception {
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
    String[] subNames = {"align", "b0", "b1"};
    assertEquals(subNames.length, m_properties.length);
    for (int i = 1; i < 3; i++) {
      Property property = m_properties[i];
      assertEquals(subNames[i], property.getTitle());
      assertInstanceOf(BooleanPropertyEditor.class, property.getEditor());
      assertEquals(false, property.getValue());
    }
    //
    Property alignProperty = m_properties[0];
    assertEquals(subNames[0], alignProperty.getTitle());
    assertEquals("R1", alignProperty.getValue());
    // "B0" := true
    m_properties[1].setValue(true);
    assertEquals(true, m_properties[1].getValue());
    assertStyleSource("B0");
    assertEditorText("[B0]");
    // "align" := R2
    alignProperty.setValue("R2");
    assertEquals("R2", alignProperty.getValue());
    assertStyleSource("B0 | R2");
    assertEditorText("[B0, R2]");
    // "align" := R3
    alignProperty.setValue("R3");
    assertEquals("R3", alignProperty.getValue());
    assertStyleSource("B0 | R3");
    assertEditorText("[B0, R3]");
    // "B0" := false
    m_properties[1].setValue(false);
    assertEquals(false, m_properties[1].getValue());
    assertStyleSource("R3");
    assertEditorText("[R3]");
    // "align" := R1
    alignProperty.setValue("R1");
    assertEquals("R1", alignProperty.getValue());
    assertStyleSource(null);
    assertEditorText("[]");
    // "align" := R2
    alignProperty.setValue("R2");
    assertEquals("R2", alignProperty.getValue());
    assertStyleSource("R2");
    assertEditorText("[R2]");
    // "align" := unknown
    alignProperty.setValue(Property.UNKNOWN_VALUE);
    assertEquals("R1", alignProperty.getValue());
    assertStyleSource(null);
    assertEditorText("[]");
  }

  public void test_Select_ignoreInNoSuchField() throws Exception {
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
    assertStyleSource("B0");
    assertEquals("", propertyMix.getValue());
    assertEditorText("[B0]");
    // "mix" := B0_B1
    propertyMix.setValue("B0_B1");
    assertEquals("B0_B1", propertyMix.getValue());
    assertEquals(true, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(false, property_B3.getValue());
    assertStyleSource("B0_B1");
    assertEditorText("[B0_B1]");
    // add "B3"
    property_B3.setValue(true);
    assertEquals("B0_B1", propertyMix.getValue());
    assertEquals(true, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(true, property_B3.getValue());
    assertStyleSource("B0_B1 | B3");
    assertEditorText("[B0_B1, B3]");
    // set "R1", change style fully
    propertyMix.setValue("R1");
    assertEquals("R1", propertyMix.getValue());
    assertEquals(false, property_B0.getValue());
    assertEquals(false, property_B1.getValue());
    assertEquals(false, property_B2.getValue());
    assertEquals(false, property_B3.getValue());
    assertStyleSource("R1");
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
    assertStyleSource("B0_B1");
    assertEditorText("[B0_B1]");
    // remove "B0"
    property_B0.setValue(false);
    assertEquals("", propertyMix.getValue());
    assertEquals(false, property_B0.getValue());
    assertEquals(true, property_B1.getValue());
    assertStyleSource("B1");
    assertEditorText("[B1]");
  }

  public void test_Macro_ignoreIfNoSuchField() throws Exception {
    parseStyleProperties("macro0|mix B0_B1 noSuchField");
    assertThat(PropertyUtils.getTitles(m_properties)).containsOnly("mix");
    Property mixProperty = m_properties[0];
    //
    assertEquals("mix", mixProperty.getTitle());
    assertEquals("", mixProperty.getValue());
    //
    assertInstanceOf(StringComboPropertyEditor.class, mixProperty.getEditor());
    String[] items = (String[]) ReflectionUtils.getFieldObject(mixProperty.getEditor(), "m_items");
    assertThat(items).containsOnly("B0_B1", "");
    // check warnings
    checkWarning("StylePropertyEditor: can not find field test.SWT.noSuchField");
  }

  public void test_Macro_ifMacro_thenNoSelect() throws Exception {
    parseStyleProperties("", new String[]{"select0|align R1 R1 R2", "macro0|mix B0_R2"});
    Property propertySelect = m_properties[0];
    Property propertyMix = m_properties[1];
    // initially no values
    assertEquals("R1", propertySelect.getValue());
    assertEquals("", propertyMix.getValue());
    // set "R2"
    propertySelect.setValue("R2");
    assertEquals("R2", propertySelect.getValue());
    assertEquals("", propertyMix.getValue());
    assertStyleSource("R2");
    assertEditorText("[R2]");
    // "mix" := B0_R2
    propertyMix.setValue("B0_R2");
    assertEquals("R2", propertySelect.getValue());
    assertEquals("B0_R2", propertyMix.getValue());
    assertStyleSource("B0_R2");
    assertEditorText("[B0_R2]");
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
    assertStyleSource("B0");
    // select "R2"
    ((ActionContributionItem) item_R2).getAction().setChecked(true);
    ((ActionContributionItem) item_R2).getAction().run();
    assertStyleSource("B0 | R2");
  }

  public void test_popup_boolean() throws Exception {
    parseStyleProperties("B1", new String[]{"set|B0 B1 B2"});
    // check items
    {
      prepareStyleItems(false);
      assertThat(styleItems).hasSize(3);
      IContributionItem item_B0 = styleItems[0];
      IContributionItem item_B1 = styleItems[1];
      IContributionItem item_B2 = styleItems[2];
      checkItem("b0", IAction.AS_CHECK_BOX, false, item_B0);
      checkItem("b1", IAction.AS_CHECK_BOX, true, item_B1);
      checkItem("b2", IAction.AS_CHECK_BOX, false, item_B2);
      // run "B2"
      ((ActionContributionItem) item_B2).getAction().run();
    }
    // check again
    {
      prepareStyleItems(false);
      assertThat(styleItems).hasSize(3);
      IContributionItem item_B0 = styleItems[0];
      IContributionItem item_B1 = styleItems[1];
      IContributionItem item_B2 = styleItems[2];
      checkItem("b0", IAction.AS_CHECK_BOX, false, item_B0);
      checkItem("b1", IAction.AS_CHECK_BOX, true, item_B1);
      checkItem("b2", IAction.AS_CHECK_BOX, true, item_B2);
    }
  }

  public void test_popup_select() throws Exception {
    parseStyleProperties("R2", new String[]{"select0|align R1 R1 R2 R3"});
    // check items
    {
      prepareStyleItems(false);
      assertThat(styleItems).hasSize(4);
      assertInstanceOf(Separator.class, styleItems[0]);
      IContributionItem item_R1 = styleItems[1];
      IContributionItem item_R2 = styleItems[2];
      IContributionItem item_R3 = styleItems[3];
      checkItem("R1", IAction.AS_RADIO_BUTTON, false, item_R1);
      checkItem("R2", IAction.AS_RADIO_BUTTON, true, item_R2);
      checkItem("R3", IAction.AS_RADIO_BUTTON, false, item_R3);
      // run "R3"
      {
        IAction action_R3 = ((ActionContributionItem) item_R3).getAction();
        action_R3.setChecked(true);
        action_R3.run();
      }
    }
    // check again
    {
      prepareStyleItems(false);
      assertThat(styleItems).hasSize(4);
      assertInstanceOf(Separator.class, styleItems[0]);
      IContributionItem item_R1 = styleItems[1];
      IContributionItem item_R2 = styleItems[2];
      IContributionItem item_R3 = styleItems[3];
      checkItem("R1", IAction.AS_RADIO_BUTTON, false, item_R1);
      checkItem("R2", IAction.AS_RADIO_BUTTON, false, item_R2);
      checkItem("R3", IAction.AS_RADIO_BUTTON, true, item_R3);
    }
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

  public void test_popup_macro() throws Exception {
    parseStyleProperties("", new String[]{"set|B0", "macro0|mix B0_B1 B0_B2"});
    prepareStyleItems(false);
    // check items
    {
      assertThat(styleItems).hasSize(1 + 1 + 2);
      checkItem("b0", IAction.AS_CHECK_BOX, false, styleItems[0]);
      assertInstanceOf(Separator.class, styleItems[1]);
      checkItem("B0_B1", IAction.AS_RADIO_BUTTON, false, styleItems[2]);
      checkItem("B0_B2", IAction.AS_RADIO_BUTTON, false, styleItems[3]);
      // run "B0_B1"
      {
        IAction action_B0_B1 = ((ActionContributionItem) styleItems[2]).getAction();
        action_B0_B1.setChecked(true);
        action_B0_B1.run();
      }
    }
    // check again
    {
      prepareStyleItems(false);
      assertThat(styleItems).hasSize(1 + 1 + 2);
      checkItem("b0", IAction.AS_CHECK_BOX, true, styleItems[0]);
      assertInstanceOf(Separator.class, styleItems[1]);
      checkItem("B0_B1", IAction.AS_RADIO_BUTTON, true, styleItems[2]);
      checkItem("B0_B2", IAction.AS_RADIO_BUTTON, false, styleItems[3]);
    }
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
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button' x:Style='CHECK'/>",
            "</Shell>");
    refresh();
    // do copy/paste
    {
      ControlInfo button = getObjectByName("button");
      doCopyPaste(button, new PasteProcedure<ControlInfo>() {
        public void run(ControlInfo copy) throws Exception {
          shell.getLayout().command_CREATE(copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='My button' x:Style='CHECK'/>",
        "  <Button text='My button' x:Style='CHECK'/>",
        "</Shell>");
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
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:TestComposite" + getStylePropertySource(styleSource) + "/>",
        "</Shell>");
  }

  private static String getStylePropertySource(String styles) {
    String source;
    if (styles != null) {
      source = " x:Style='";
      for (String part : StringUtils.split(styles, " |")) {
        source += "(t:SWT)." + part;
        source += " | ";
      }
      source = StringUtils.removeEnd(source, " | ");
      source += "'";
    } else {
      source = "";
    }
    return source;
  }

  private void parseStyleProperties(String... styleLines) throws Exception {
    parseStyleProperties(null, styleLines);
  }

  private void parseStyleProperties(String styles, String[] styleLines0) throws Exception {
    setFileContentSrc(
        "test/TestComposite.java",
        getJavaSource(
            "public class TestComposite extends Composite {",
            "  public TestComposite(Composite parent, int style) {",
            "    super(parent, SWT.NONE);",
            "  }",
            "  protected void checkSubclass () {",
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
            "      <parameter type='int' defaultSource='test.SWT.NONE'>",
            "        <editor id='style'>",
            "          <parameter name='class'>test.SWT</parameter>"};
    lines = CodeUtils.join(lines, styleLines);
    lines =
        CodeUtils.join(lines, new String[]{
            "        <!-- filler filler filler filler filler -->",
            "        </editor>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"});
    setFileContentSrc("test/TestComposite.wbp-component.xml", getSource(lines));
    waitForAutoBuild();
    // parse
    parseStyleProperties0("Style", new String[]{
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:TestComposite" + getStylePropertySource(styles) + "/>",
        "</Shell>"});
  }

  private void parseStyleProperties0(String propertyPath, String[] lines) throws Exception {
    CompositeInfo panel = parse(lines);
    panel.refresh();
    // prepare "style" property
    ControlInfo component = panel.getChildrenControls().get(0);
    m_styleProperty = (GenericProperty) PropertyUtils.getByPath(component, propertyPath);
    // use its editor
    m_editor = (StylePropertyEditor) m_styleProperty.getEditor();
    m_properties = m_editor.getProperties(m_styleProperty);
    Arrays.sort(m_properties, new Comparator<Property>() {
      public int compare(Property o1, Property o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
  }

  private void checkWarning(String message) throws Exception {
    boolean find = false;
    for (EditorWarning warning : m_lastContext.getWarnings()) {
      find = message.equals(warning.getMessage());
      if (find) {
        break;
      }
    }
    assertTrue(find);
  }
}