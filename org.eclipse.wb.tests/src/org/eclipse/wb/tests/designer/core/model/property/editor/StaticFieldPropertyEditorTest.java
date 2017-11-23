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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.easymock.EasyMock.expect;

import org.apache.commons.lang.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.util.List;

import javax.swing.SwingConstants;

/**
 * Test for {@link StaticFieldPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class StaticFieldPropertyEditorTest extends SwingModelTest {
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
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Top level class, all fields are valid.
   */
  public void test_configure_1() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "RIGHT"});
    //
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", "RIGHT"};
    Object[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Top level class, one field does not exist, should be skipped.
   */
  public void test_configure_2() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "noSuchField", "RIGHT"});
    //
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", "RIGHT"};
    Object[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Top level class, all fields are valid. Specify title in field description.
   */
  public void test_configure_3() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT:asLeft", "RIGHT:asRight"});
    //
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", "RIGHT"};
    Object[] e_titles = new String[]{"asLeft", "asRight"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Special <code>*remove</code> field.
   */
  public void test_configure_4() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "*remove", "RIGHT"});
    //
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", null, "RIGHT"};
    Object[] e_titles = new String[]{"LEFT", "", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, null, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Inner class with fields.
   */
  public static class Foo {
    public static final int A = 1;
    public static final int B = 2;
  }

  /**
   * Inner class.
   */
  public void test_configure_5() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(Foo.class, new String[]{"A", "B"});
    //
    Class<?> e_class = Foo.class;
    String m_classSourceName = StaticFieldPropertyEditorTest.class.getName() + ".Foo";
    Object[] e_names = new String[]{"A", "B"};
    Object[] e_titles = new String[]{"A", "B"};
    Object[] e_values = new Object[]{Foo.A, Foo.B};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#configure(EditorState, java.util.Map)}.<br>
   * Fields in single {@link String} as <code>"fields"</code> parameter.
   */
  public void test_configure_6() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // create StaticFieldPropertyEditor
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(m_lastState, ImmutableMap.<String, Object>of(
        "class",
        "javax.swing.SwingConstants",
        "fields",
        "LEFT RIGHT"));
    // check state
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", "RIGHT"};
    Object[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#configure(EditorState, java.util.Map)}.<br>
   * Fields in {@link List} as <code>"field"</code> parameter.
   */
  public void test_configure_7() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // create StaticFieldPropertyEditor
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(m_lastState, ImmutableMap.<String, Object>of(
        "class",
        "javax.swing.SwingConstants",
        "field",
        ImmutableList.of("LEFT", "RIGHT")));
    // check state
    Class<?> e_class = SwingConstants.class;
    String m_classSourceName = "javax.swing.SwingConstants";
    Object[] e_names = new String[]{"LEFT", "RIGHT"};
    Object[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_class, m_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#configure(EditorState, java.util.Map)}.<br>
   * No fields, so exception.
   */
  public void test_configure_8() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // create StaticFieldPropertyEditor
    try {
      StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
      editor.configure(
          m_lastState,
          ImmutableMap.<String, Object>of("class", "javax.swing.SwingConstants"));
      fail();
    } catch (DesignerException e) {
    }
  }

  /**
   * Asserts that given {@link StaticFieldPropertyEditor} has expected configuration.
   */
  private void assertConfiguration(StaticFieldPropertyEditor editor,
      Class<?> e_class,
      String e_classSourceName,
      Object[] e_names,
      Object[] e_titles,
      Object[] e_values) throws Exception {
    assertSame(e_class, getFieldValue(editor, "m_class"));
    assertEquals(e_classSourceName, getFieldValue(editor, "m_classSourceName"));
    Assertions.<Object>assertThat((String[]) getFieldValue(editor, "m_names")).containsOnly(e_names);
    Assertions.<Object>assertThat((String[]) getFieldValue(editor, "m_titles")).containsOnly(e_titles);
    assertTrue(ArrayUtils.isEquals(e_values, getFieldValue(editor, "m_values")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StaticFieldPropertyEditor#getValueSource(Object)}.
   */
  public void test_getValueSource() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "*remove", "RIGHT"});
    //
    assertEquals("javax.swing.SwingConstants.LEFT", editor.getValueSource(SwingConstants.LEFT));
    assertNull(editor.getValueSource(null));
    assertEquals("javax.swing.SwingConstants.RIGHT", editor.getValueSource(SwingConstants.RIGHT));
    assertNull(editor.getValueSource(SwingConstants.NORTH));
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#getText(Property)}.
   */
  public void test_getText() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT:asLeft", "*remove", "RIGHT"});
    // prepare for mocking
    IMocksControl mocksControl = EasyMock.createStrictControl();
    Property property = mocksControl.createMock(Property.class);
    // LEFT:asLeft
    {
      mocksControl.reset();
      expect(property.getValue()).andReturn(SwingConstants.LEFT);
      mocksControl.replay();
      //
      assertEquals("asLeft", editor.getText(property));
      mocksControl.verify();
    }
    // RIGHT
    {
      mocksControl.reset();
      expect(property.getValue()).andReturn(SwingConstants.RIGHT);
      mocksControl.replay();
      //
      assertEquals("RIGHT", editor.getText(property));
      mocksControl.verify();
    }
    // UNKNOWN_VALUE
    {
      mocksControl.reset();
      expect(property.getValue()).andReturn(Property.UNKNOWN_VALUE);
      mocksControl.replay();
      //
      assertEquals(null, editor.getText(property));
      mocksControl.verify();
    }
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#getClipboardSource(GenericProperty)}.
   */
  public void test_getClipboardSource() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "RIGHT"});
    // prepare for mocking
    IMocksControl mocksControl = EasyMock.createStrictControl();
    GenericProperty property = mocksControl.createMock(GenericProperty.class);
    // LEFT
    {
      mocksControl.reset();
      expect(property.getValue()).andReturn(SwingConstants.LEFT);
      mocksControl.replay();
      //
      assertEquals("javax.swing.SwingConstants.LEFT", editor.getClipboardSource(property));
      mocksControl.verify();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setValue()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StaticFieldPropertyEditor#setValue(Property, Object)}.<br>
   * For {@link GenericProperty}.
   */
  public void test_setValue_1() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "RIGHT"});
    // prepare for mocking
    IMocksControl mocksControl = EasyMock.createStrictControl();
    GenericProperty property = mocksControl.createMock(GenericProperty.class);
    // LEFT
    {
      mocksControl.reset();
      property.setExpression("javax.swing.SwingConstants.LEFT", SwingConstants.LEFT);
      mocksControl.replay();
      //
      editor.setValue(property, SwingConstants.LEFT);
      mocksControl.verify();
    }
  }

  /**
   * Test for {@link StaticFieldPropertyEditor#setValue(Property, Object)}.<br>
   * For simple {@link Property}.
   */
  public void test_setValue_2() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "RIGHT"});
    // prepare for mocking
    IMocksControl mocksControl = EasyMock.createStrictControl();
    Property property = mocksControl.createMock(Property.class);
    // LEFT
    {
      mocksControl.reset();
      property.setValue(SwingConstants.LEFT);
      mocksControl.replay();
      //
      editor.setValue(property, SwingConstants.LEFT);
      mocksControl.verify();
    }
  }
}
