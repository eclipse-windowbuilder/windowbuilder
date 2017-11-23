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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.model.rcp.ExtensionElementProperty;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ExtensionElementProperty}.
 * 
 * @author scheglov_ke
 */
public class ExtensionElementPropertyTest extends AbstractPdeTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_existingProperty() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' value='some value' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // listener
    final int[] setValueCount = new int[]{0};
    RunnableEx setValueListener = new RunnableEx() {
      public void run() throws Exception {
        setValueCount[0]++;
      }
    };
    // check property
    ExtensionElementProperty<String> property =
        new ExtensionElementProperty<String>(setValueListener,
            StringPropertyEditor.INSTANCE,
            "title",
            m_project,
            "org.eclipse.ui.views",
            "view",
            "C_1",
            "value",
            ExtensionElementProperty.IDENTITY,
            ExtensionElementProperty.IDENTITY,
            Property.UNKNOWN_VALUE);
    assertEquals("title", property.getTitle());
    // access
    {
      assertSame(m_project, property.getProject());
      assertTrue(property.hasElement());
    }
    // getUtils()
    {
      PdeUtils utils = property.getUtils();
      assertNotNull(utils);
      assertNotNull(utils.getExtensionElementById("org.eclipse.ui.views", "view", "id_1"));
    }
    // current value
    assertTrue(property.isModified());
    assertEquals("some value", property.getValue());
    // try to set same value, should be ignored
    assertEquals(0, setValueCount[0]);
    property.setValue("some value");
    assertEquals(0, setValueCount[0]);
    // modify value
    property.setValue("new value");
    assertEquals(1, setValueCount[0]);
    assertPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' value='new value' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
  }

  public void test_noValue_setNew() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' class='C_1'/>",
        "  </extension>",
        "</plugin>"});
    // check property
    ExtensionElementProperty<String> property = createPropertyWithDefaults();
    assertTrue(property.hasElement());
    // initially no value
    assertFalse(property.isModified());
    assertEquals("defaultValue", property.getValue());
    // modify value
    property.setValue("new value");
    assertTrue(property.isModified());
    assertEquals("new value", property.getValue());
    // try to clear
    property.setValue(Property.UNKNOWN_VALUE);
    assertFalse(property.isModified());
    assertEquals("defaultValue", property.getValue());
    assertThat(getPluginXML()).contains("id=\"id_1\"").contains("class=\"C_1\"").doesNotContain("value=");
  }

  public void test_remoteAttribute_whenSetDefaultValue() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' class='C_1' value='some value'/>",
        "  </extension>",
        "</plugin>"});
    // check property
    ExtensionElementProperty<String> property = createPropertyWithDefaults();
    assertTrue(property.hasElement());
    // initial value
    assertTrue(property.isModified());
    assertEquals("some value", property.getValue());
    // set default
    property.setValue("defaultValue");
    assertFalse(property.isModified());
    assertEquals("defaultValue", property.getValue());
    assertThat(getPluginXML()).contains("id=\"id_1\"").contains("class=\"C_1\"").doesNotContain("value=");
  }

  private ExtensionElementProperty<String> createPropertyWithDefaults() {
    return new ExtensionElementProperty<String>(null,
        StringPropertyEditor.INSTANCE,
        "title",
        m_project,
        "org.eclipse.ui.views",
        "view",
        "C_1",
        "value",
        ExtensionElementProperty.IDENTITY,
        ExtensionElementProperty.IDENTITY,
        "defaultValue");
  }

  public void test_booleanValue() throws Exception {
    createPluginXML(new String[]{
        "<plugin>",
        "  <extension point='org.eclipse.ui.views'>",
        "    <view id='id_1' class='C_1' value='false'/>",
        "  </extension>",
        "</plugin>"});
    // check property
    ExtensionElementProperty<Boolean> property =
        new ExtensionElementProperty<Boolean>(null,
            StringPropertyEditor.INSTANCE,
            "title",
            m_project,
            "org.eclipse.ui.views",
            "view",
            "C_1",
            "value",
            ExtensionElementProperty.FROM_BOOLEAN,
            ExtensionElementProperty.TO_BOOLEAN,
            Boolean.FALSE);
    assertTrue(property.hasElement());
    // initial value
    assertFalse(property.isModified());
    assertEquals(Boolean.FALSE, property.getValue());
    // modify value
    property.setValue(Boolean.TRUE);
    assertTrue(property.isModified());
    assertEquals(Boolean.TRUE, property.getValue());
  }
}