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
import org.eclipse.wb.core.model.association.AssociationObjectFactories;
import org.eclipse.wb.core.model.association.AssociationObjectFactory;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AssociationObjectFactories}.
 * 
 * @author scheglov_ke
 */
public class AssociationObjectFactoriesTest extends SwingModelTest {
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
   * Test for {@link AssociationObjectFactories#no()}.
   */
  public void test_no() throws Exception {
    AssociationObjectFactory factory = AssociationObjectFactories.no();
    assertEquals("NO", factory.toString());
    // check created AssociationObject
    {
      AssociationObject associationObject = factory.create();
      assertEquals("NO", associationObject.toString());
      assertNull(associationObject.getAssociation());
      assertFalse(associationObject.isRequired());
    }
  }

  /**
   * Test for {@link AssociationObjectFactories#invocationChild(String, boolean)}.
   */
  public void test_invocationChild() throws Exception {
    String source = "%parent%.add(%child%)";
    // check factory
    AssociationObjectFactory factory = AssociationObjectFactories.invocationChild(source, false);
    assertEquals(source, factory.toString());
    // check created AssociationObject
    {
      AssociationObject associationObject = factory.create();
      assertEquals(source, associationObject.toString());
      assertThat(associationObject.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
      assertFalse(associationObject.isRequired());
    }
  }
}
