/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.junit.jupiter.api.Test;

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
	@Test
	public void test_no() throws Exception {
		AssociationObject associationObject = AssociationObjects.no();
		assertEquals("NO", associationObject.toString());
		assertNull(associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#empty()}.
	 */
	@Test
	public void test_empty() throws Exception {
		AssociationObject associationObject = AssociationObjects.empty();
		assertEquals("empty", associationObject.toString());
		assertInstanceOf(EmptyAssociation.class, associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#nonVisual()}.
	 */
	@Test
	public void test_nonVisual() throws Exception {
		AssociationObject associationObject = AssociationObjects.nonVisual();
		assertEquals("nonVisual", associationObject.toString());
		assertInstanceOf(NonVisualAssociation.class, associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#constructorChild()}.
	 */
	@Test
	public void test_constructorChild() throws Exception {
		AssociationObject associationObject = AssociationObjects.constructorChild();
		assertEquals("constructorChild", associationObject.toString());
		assertInstanceOf(ConstructorChildAssociation.class, associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#invocationVoid()}.
	 */
	@Test
	public void test_invocationVoid() throws Exception {
		AssociationObject associationObject = AssociationObjects.invocationVoid();
		assertEquals("invocationVoid", associationObject.toString());
		assertInstanceOf(InvocationVoidAssociation.class, associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#invocationChildNull()}.
	 */
	@Test
	public void test_invocationChildNull() throws Exception {
		AssociationObject associationObject = AssociationObjects.invocationChildNull();
		assertEquals("invocationChildNull", associationObject.toString());
		assertInstanceOf(InvocationChildAssociation.class, associationObject.getAssociation());
		assertFalse(associationObject.isRequired());
	}

	/**
	 * Test for {@link AssociationObjects#invocationChild(String, boolean)}.
	 */
	@Test
	public void test_invocationChild_withRequired() throws Exception {
		String source = "%parent%.add(%child%)";
		{
			AssociationObject associationObject = AssociationObjects.invocationChild(source, false);
			assertEquals(source, associationObject.toString());
			assertInstanceOf(InvocationChildAssociation.class, associationObject.getAssociation());
			assertFalse(associationObject.isRequired());
		}
		{
			AssociationObject associationObject = AssociationObjects.invocationChild(source, true);
			assertEquals(source, associationObject.toString());
			assertInstanceOf(InvocationChildAssociation.class, associationObject.getAssociation());
			assertTrue(associationObject.isRequired());
		}
	}
}
