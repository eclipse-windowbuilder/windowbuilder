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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.ITextValuePropertyEditor;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.editor.EnumPropertyEditor;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;
import org.eclipse.wb.tests.designer.tests.common.PropertyNoValue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test for {@link EnumPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class EnumPropertyEditorTest extends AbstractCoreTest {
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
   * Test using {@link EnumPropertyEditor}.
   */
  public void test_externalEnum() throws Exception {
    prepare_Foo_MyComponent();
    ObjectInfo panel = parse("<t:MyComponent foo='B'/>");
    refresh();
    // prepare property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
    EnumPropertyEditor editor = (EnumPropertyEditor) property.getEditor();
    // text
    assertEquals("B", getPropertyText(property));
    // IValueSourcePropertyEditor
    {
      Class<?> classFoo = m_lastLoader.loadClass("test.Foo");
      assertEquals("B", editor.getValueExpression(property, classFoo.getEnumConstants()[1]));
      assertNull(editor.getValueExpression(property, this));
    }
    // IClipboardSourceProvider
    assertEquals("B", getPropertyClipboardSource(property));
    // elements
    {
      Enum<?>[] enums =
          (Enum<?>[]) ReflectionUtils.invokeMethod(
              editor,
              "getElements(org.eclipse.wb.internal.core.model.property.Property)",
              property);
      String[] enumStrings = GenericsUtils.getEnumStrings(enums);
      assertThat(enumStrings).isEqualTo(new String[]{"A", "B", "C"});
    }
  }

  public void test_getText_noValue() throws Exception {
    Property property = new PropertyNoValue(EnumPropertyEditor.INSTANCE);
    assertEquals(null, getPropertyText(property));
  }

  /**
   * Test for {@link ITextValuePropertyEditor}.
   */
  public void test_setText() throws Exception {
    prepare_Foo_MyComponent();
    ObjectInfo panel = parse("<t:MyComponent foo='B'/>");
    refresh();
    // prepare property
    Property property = panel.getPropertyByTitle("foo");
    // set value
    setPropertyText(property, "C");
    assertXML("<t:MyComponent foo='C'/>");
  }

  public void test_comboMethods() throws Exception {
    prepare_Foo_MyComponent();
    ObjectInfo panel = parse("<t:MyComponent foo='C'/>");
    refresh();
    // prepare property
    Property property = panel.getPropertyByTitle("foo");
    // add items
    addComboPropertyItems(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("A", "B", "C");
    }
    // select current item
    {
      setComboPropertySelection(1);
      setComboPropertySelection(property);
      assertEquals(2, getComboPropertySelection());
    }
    // set non-default value
    {
      setComboPropertyValue(property, 1);
      assertXML("<t:MyComponent foo='B'/>");
    }
    // set default value
    {
      setComboPropertyValue(property, 0);
      assertXML("<t:MyComponent/>");
    }
  }

  /**
   * Test for using simple {@link Property}, not {@link GenericProperty}.
   */
  public void test_simpleProperty() throws Exception {
    prepare_Foo_MyComponent();
    parse("<t:MyComponent foo='C'/>");
    refresh();
    // prepare property
    final AtomicReference<Object> newValue = new AtomicReference<Object>();
    class MyProperty extends PropertyNoValue implements ITypedProperty {
      public MyProperty() {
        super(EnumPropertyEditor.INSTANCE);
      }

      public Class<?> getType() {
        try {
          return m_lastLoader.loadClass("test.Foo");
        } catch (Throwable e) {
          throw ReflectionUtils.propagate(e);
        }
      }

      @Override
      public void setValue(Object value) throws Exception {
        newValue.set(value);
      }
    }
    Property property = new MyProperty();
    // add items
    addComboPropertyItems(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("A", "B", "C");
    }
    // set non-default value
    {
      setComboPropertyValue(property, 1);
      assertEquals("B", newValue.get().toString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures project for standalone <code>test.Foo</code> enum.
   */
  private void prepare_Foo_MyComponent() throws Exception {
    prepare_Foo();
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public Foo foo = Foo.A;"});
    waitForAutoBuild();
  }

  /**
   * Prepares "Foo" enum.
   */
  private void prepare_Foo() throws Exception {
    setFileContentSrc(
        "test/Foo.java",
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "public enum Foo {",
            "  A, B, C",
            "}"));
  }
}
