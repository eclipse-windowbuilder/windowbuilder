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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.EnumCustomPropertyEditor;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link EnumCustomPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class EnumCustomPropertyEditorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  private static enum MyEnum {
    A, B, C
  }

  /**
   * Test for {@link EnumCustomPropertyEditor#configure(Class)}.
   */
  public void test_configure_withClass() throws Exception {
    EnumCustomPropertyEditor editor = new EnumCustomPropertyEditor();
    editor.configure(MyEnum.class);
    assertSame(MyEnum.class, ReflectionUtils.getFieldObject(editor, "m_class"));
    {
      MyEnum[] enumValues =
          (MyEnum[]) ReflectionUtils.invokeMethod2(editor, "getElements", Property.class, null);
      String[] enumStrings = GenericsUtils.getEnumStrings(enumValues);
      assertThat(enumStrings).isEqualTo(new String[]{"A", "B", "C"});
    }
  }

  /**
   * Test for {@link EnumCustomPropertyEditor#configure(Enum[])}.
   */
  public void test_configure_withElements() throws Exception {
    EnumCustomPropertyEditor editor = new EnumCustomPropertyEditor();
    editor.configure(new MyEnum[]{MyEnum.A, MyEnum.C});
    assertSame(MyEnum.class, ReflectionUtils.getFieldObject(editor, "m_class"));
    {
      MyEnum[] enumValues =
          (MyEnum[]) ReflectionUtils.invokeMethod2(editor, "getElements", Property.class, null);
      String[] enumStrings = GenericsUtils.getEnumStrings(enumValues);
      assertThat(enumStrings).isEqualTo(new String[]{"A", "C"});
    }
  }
}
