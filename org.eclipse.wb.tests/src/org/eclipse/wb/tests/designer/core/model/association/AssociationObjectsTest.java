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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualAssociation;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AssociationObjects}.
 * 
 * @author scheglov_ke
 */
public class AssociationObjectsTest extends SwingModelTest {
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
   * Test for {@link AssociationObjects#no()}.
   */
  public void test_no() throws Exception {
    AssociationObject associationObject = AssociationObjects.no();
    assertEquals("NO", associationObject.toString());
    assertNull(associationObject.getAssociation());
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#empty()}.
   */
  public void test_empty() throws Exception {
    AssociationObject associationObject = AssociationObjects.empty();
    assertEquals("empty", associationObject.toString());
    assertThat(associationObject.getAssociation()).isInstanceOf(EmptyAssociation.class);
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#nonVisual()}.
   */
  public void test_nonVisual() throws Exception {
    AssociationObject associationObject = AssociationObjects.nonVisual();
    assertEquals("nonVisual", associationObject.toString());
    assertThat(associationObject.getAssociation()).isInstanceOf(NonVisualAssociation.class);
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#constructorChild()}.
   */
  public void test_constructorChild() throws Exception {
    AssociationObject associationObject = AssociationObjects.constructorChild();
    assertEquals("constructorChild", associationObject.toString());
    assertThat(associationObject.getAssociation()).isInstanceOf(ConstructorChildAssociation.class);
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#invocationVoid()}.
   */
  public void test_invocationVoid() throws Exception {
    AssociationObject associationObject = AssociationObjects.invocationVoid();
    assertEquals("invocationVoid", associationObject.toString());
    assertThat(associationObject.getAssociation()).isInstanceOf(InvocationVoidAssociation.class);
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#invocationChildNull()}.
   */
  public void test_invocationChildNull() throws Exception {
    AssociationObject associationObject = AssociationObjects.invocationChildNull();
    assertEquals("invocationChildNull", associationObject.toString());
    assertThat(associationObject.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
    assertFalse(associationObject.isRequired());
  }

  /**
   * Test for {@link AssociationObjects#invocationChild(String, boolean)}.
   */
  public void test_invocationChild_withRequired() throws Exception {
    String source = "%parent%.add(%child%)";
    {
      AssociationObject associationObject = AssociationObjects.invocationChild(source, false);
      assertEquals(source, associationObject.toString());
      assertThat(associationObject.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
      assertFalse(associationObject.isRequired());
    }
    {
      AssociationObject associationObject = AssociationObjects.invocationChild(source, true);
      assertEquals(source, associationObject.toString());
      assertThat(associationObject.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
      assertTrue(associationObject.isRequired());
    }
  }
}
