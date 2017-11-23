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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescriptionKey;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import javax.swing.JButton;

/**
 * Tests for {@link CreationDescription} as object.
 *
 * @author scheglov_ke
 */
public class CreationDescriptionTest extends DesignerTestCase {
  private static final Image TYPE_ICON = new Image(null, 1, 1);
  private static final Image CREATION_ICON = new Image(null, 1, 1);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for default "description text" for {@link ComponentDescription}.
   */
  public void test_defaultComponentDescription() throws Exception {
    {
      ComponentDescriptionKey key = new ComponentDescriptionKey(JButton.class);
      ComponentDescription component = new ComponentDescription(key);
      assertEquals("javax.swing.JButton", component.getDescription());
    }
    {
      ComponentDescription component = new ComponentDescription(null);
      assertNull(component.getDescription());
    }
  }

  /**
   * Test new {@link CreationDescription} with explicit name.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_newExplicitName() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    assertEquals("myId", creation.getId());
    assertEquals("myName", creation.getName());
    // final verification
    verify(component);
  }

  /**
   * Test new {@link CreationDescription} without explicit name, so name of class is used.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_newImplicitName() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", null);
    assertEquals("myId", creation.getId());
    assertEquals("JButton", creation.getName());
    // final verification
    verify(component);
  }

  /**
   * Test for using icon/description from {@link ComponentDescription}, when no
   * {@link CreationDescription} specific values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_fromComponentDescription() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      expect(component.getIcon()).andReturn(TYPE_ICON);
      expect(component.getDescription()).andReturn("type description");
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // check icon/description
    assertSame(TYPE_ICON, creation.getIcon());
    assertEquals("type description", creation.getDescription());
    // check generation
    assertNull(creation.getSource());
    assertEquals(0, creation.getInvocations().size());
    // final verification
    verify(component);
  }

  /**
   * Test for using icon/description from {@link ComponentDescription}, when no
   * {@link CreationDescription} specific values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_specificIconDescription() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // check icon
    creation.setIcon(CREATION_ICON);
    assertSame(CREATION_ICON, creation.getIcon());
    // check description
    creation.setDescription("creation description");
    assertEquals("creation description", creation.getDescription());
    // final verification
    verify(component);
  }

  /**
   * Test that when we set description, we normalize its whitespace.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_normalizeDescription() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    creation.setDescription("creation \r\n     \t  description");
    assertEquals("creation description", creation.getDescription());
    // final verification
    verify(component);
  }

  /**
   * Test for set/get source.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_source() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // check creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // fully qualified source
    {
      creation.setSource("new javax.swing.JButton()");
      assertEquals("new javax.swing.JButton()", creation.getSource());
    }
    // source with "%component.class%"
    {
      creation.setSource("new %component.class%()");
      assertEquals("new javax.swing.JButton()", creation.getSource());
    }
    // final verification
    verify(component);
  }

  /**
   * We should use canonical {@link Class} name.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_getSource_innerClass() throws Exception {
    class MyInnerClass {
    }
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) MyInnerClass.class);
      replay(component);
    }
    // check creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // source with "%component.class%"
    {
      creation.setSource("new %component.class%()");
      assertEquals(
          "new " + ReflectionUtils.getCanonicalName(MyInnerClass.class) + "()",
          creation.getSource());
    }
    // final verification
    verify(component);
  }

  /**
   * Test for {@link CreationInvocationDescription}.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_invocations() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // check creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // no invocations initially
    assertEquals(0, creation.getInvocations().size());
    // add invocation
    CreationInvocationDescription invocation;
    {
      invocation = new CreationInvocationDescription();
      invocation.setSignature("setText(java.lang.String)");
      invocation.setArguments("\"my text\"");
    }
    creation.addInvocation(invocation);
    // check for invocations
    {
      List<CreationInvocationDescription> invocations = creation.getInvocations();
      assertEquals(1, invocations.size());
      assertTrue(invocations.contains(invocation));
    }
    // final verification
    verify(component);
  }

  /**
   * Test for {@link CreationDescription} specific parameters.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void test_specificParameters() throws Exception {
    // prepare ComponentDescription
    ComponentDescription component;
    {
      component = createStrictMock(ComponentDescription.class);
      expect(component.getComponentClass()).andReturn((Class) JButton.class);
      replay(component);
    }
    // prepare creation
    CreationDescription creation = new CreationDescription(component, "myId", "myName");
    // initially no parameters
    assertTrue(creation.getParameters().isEmpty());
    // put parameters
    creation.addParameter("name_1", "value_1");
    creation.addParameter("name_2", "value_2");
    assertThat(creation.getParameters()).contains(
        entry("name_1", "value_1"),
        entry("name_2", "value_2"));
    // final verification
    verify(component);
  }
}
