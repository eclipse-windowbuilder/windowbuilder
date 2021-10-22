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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.DatePropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for {@link DatePropertyEditor}.
 *
 * @author sablin_aa
 */
public class DatePropertyEditorTest extends AbstractTextPropertyEditorTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_configure_default() throws Exception {
    HashMap<String, Object> parameters = Maps.newHashMap();
    /*DatePropertyEditor editor = */createEditor(DatePropertyEditor.class, parameters);
  }

  public void test_configure() throws Exception {
    /*DatePropertyEditor editor = */createEditor(DatePropertyEditor.class, getEditorParameters());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setValue_default() throws Exception {
    configureContents(false);
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    DateComponent component = new DateComponent();",
            "    add(component);",
            "  }",
            "}");
    ComponentInfo componentInfo = container.getChildrenComponents().get(0);
    // check property
    Property dateProperty = componentInfo.getPropertyByTitle("date");
    assertNotNull(dateProperty);
    // check editor
    assertInstanceOf(DatePropertyEditor.class, dateProperty.getEditor());
    DatePropertyEditor dateEditor = (DatePropertyEditor) dateProperty.getEditor();
    // set date
    long currentTimeMillis = System.currentTimeMillis();
    Date rightNow = new Date(currentTimeMillis);
    dateProperty.setValue(rightNow);
    // check source
    assertEquals(new SimpleDateFormat().format(rightNow), dateEditor.getText(dateProperty));
    assertEditor(
        "import java.util.Date;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    DateComponent component = new DateComponent();",
        "    component.setDate(new Date(" + new Long(currentTimeMillis).toString() + "L));",
        "    add(component);",
        "  }",
        "}");
  }

  public void test_setValue_custom() throws Exception {
    configureContents(true);
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    DateComponent component = new DateComponent();",
            "    add(component);",
            "  }",
            "}");
    ComponentInfo componentInfo = container.getChildrenComponents().get(0);
    // check property
    Property dateProperty = componentInfo.getPropertyByTitle("date");
    assertNotNull(dateProperty);
    // check editor
    assertInstanceOf(DatePropertyEditor.class, dateProperty.getEditor());
    DatePropertyEditor dateEditor = (DatePropertyEditor) dateProperty.getEditor();
    // set value
    Date currentDate = new Date(System.currentTimeMillis());
    String currentDateStr = new SimpleDateFormat("dd.MM.yyyy").format(currentDate);
    dateProperty.setValue(currentDate);
    // check source
    assertEquals(currentDateStr, dateEditor.getText(dateProperty));
    assertEditor(
        "import java.text.SimpleDateFormat;",
        "import java.text.ParsePosition;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    DateComponent component = new DateComponent();",
        "    component.setDate(new SimpleDateFormat('dd.MM.yyyy').parse('"
            + currentDateStr
            + "', new ParsePosition(0)));",
        "    add(component);",
        "  }",
        "}");
  }

  public void test_setText_default() throws Exception {
    configureContents(false);
    long currentTimeMillis = System.currentTimeMillis();
    ContainerInfo container =
        parseContainer(
            "import java.util.Date;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    DateComponent component = new DateComponent();",
            "    component.setDate(new Date(" + new Long(currentTimeMillis).toString() + "L));",
            "    add(component);",
            "  }",
            "}");
    ComponentInfo componentInfo = container.getChildrenComponents().get(0);
    // check property
    Property dateProperty = componentInfo.getPropertyByTitle("date");
    assertNotNull(dateProperty);
    assertEquals(new Date(currentTimeMillis), dateProperty.getValue());
    // check editor
    assertInstanceOf(DatePropertyEditor.class, dateProperty.getEditor());
    DatePropertyEditor dateEditor = (DatePropertyEditor) dateProperty.getEditor();
    // set editor text
    Date newDate = new Date(0L);
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    String newDateStr = dateFormat.format(newDate);
    ReflectionUtils.invokeMethod(
        dateEditor,
        "setEditorText(org.eclipse.wb.internal.core.model.property.Property,java.lang.String)",
        dateProperty,
        newDateStr);
    // check source
    assertEquals(newDateStr, dateEditor.getText(dateProperty));
    assertEquals(newDate, dateProperty.getValue());
    assertEditor(
        "import java.util.Date;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    DateComponent component = new DateComponent();",
        "    component.setDate(new Date(0L));",
        "    add(component);",
        "  }",
        "}");
  }

  public void test_setText_custom() throws Exception {
    configureContents(true);
    ContainerInfo container =
        parseContainer(
            "import java.util.Date;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    DateComponent component = new DateComponent();",
            "    component.setDate(new Date());",
            "    add(component);",
            "  }",
            "}");
    ComponentInfo componentInfo = container.getChildrenComponents().get(0);
    // check property
    Property dateProperty = componentInfo.getPropertyByTitle("date");
    assertNotNull(dateProperty);
    // check editor
    assertInstanceOf(DatePropertyEditor.class, dateProperty.getEditor());
    DatePropertyEditor dateEditor = (DatePropertyEditor) dateProperty.getEditor();
    // set editor text
    String newDateStr = "22.07.1941";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    Date newDate = dateFormat.parse(newDateStr);
    ReflectionUtils.invokeMethod(
        dateEditor,
        "setEditorText(org.eclipse.wb.internal.core.model.property.Property,java.lang.String)",
        dateProperty,
        newDateStr);
    // check source
    assertEquals(newDateStr, dateEditor.getText(dateProperty));
    assertEquals(newDate, dateProperty.getValue());
    assertEditor(
        "import java.util.Date;",
        "import java.text.SimpleDateFormat;",
        "import java.text.ParsePosition;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    DateComponent component = new DateComponent();",
        "    component.setDate(new SimpleDateFormat('dd.MM.yyyy').parse('"
            + newDateStr
            + "', new ParsePosition(0)));",
        "    add(component);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, Object> getEditorParameters() {
    //<editor id="customDate">
    //	<parameter name="functions">import javax.swing.JTextField; import java.text.SimpleDateFormat;</parameter>
    //	<parameter name="toString">(new SimpleDateFormat(\"dd.MM.yyyy\")).format(value)</parameter>
    //	<parameter name="toDate">(new SimpleDateFormat(\"dd.MM.yyyy\")).parse(value)</parameter>
    //	<parameter name="source">new java.util.Date(\"%value%\")</parameter>
    //</editor>
    HashMap<String, Object> params = Maps.newHashMap();
    params.put(
        "functions",
        getSourceDQ("import javax.swing.JTextField;", "import java.text.SimpleDateFormat;"));
    params.put("toString", "(new SimpleDateFormat(\"dd.MM.yyyy\")).format(value)");
    params.put("toDate", "(new SimpleDateFormat(\"dd.MM.yyyy\")).parse(value)");
    params.put("source", "new java.util.Date(\"%value%\")");
    return params;
  }

  private void configureContents(boolean custom) throws Exception {
    setJavaContentSrc(
        "test",
        "DateComponent",
        new String[]{
            "public class DateComponent extends JComponent {",
            "  public DateComponent(){",
            "  }",
            "  public void setDate(java.util.Date value){",
            "  }",
            "}"},
        custom
            ? new String[]{
                "<?xml version='1.0' encoding='UTF-8'?>",
                "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
                "  <property id='setDate(java.util.Date)'>",
                "    <editor id='customDate'>",
                "      <parameter name='functions'>import javax.swing.JTextField; import java.text.SimpleDateFormat;</parameter>",
                "      <parameter name='toString'>(new SimpleDateFormat(\"dd.MM.yyyy\")).format(value)</parameter>",
                "      <parameter name='toDate'>(new SimpleDateFormat(\"dd.MM.yyyy\")).parse(value)</parameter>",
                "      <parameter name='source'>new java.text.SimpleDateFormat(\"dd.MM.yyyy\").parse(\"%value%\", new java.text.ParsePosition(0))</parameter>",
                "    </editor>",
                "  </property>",
                "</component>"}
            : new String[]{
                "<?xml version='1.0' encoding='UTF-8'?>",
                "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
                "  <property id='setDate(java.util.Date)'>",
                "    <editor id='customDate'/>",
                "  </property>",
                "</component>"});
    waitForAutoBuild();
  }
}
