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

import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;

/**
 * Tests for {@link EmptyAssociation}.
 *
 * @author scheglov_ke
 */
public class EmptyAssociationTest extends SwingModelTest {
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
	@Test
	public void test() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// create JButton with EmptyAssociation... Ha! it is really not related with our JPanel :-)
		ComponentInfo button = createJButton();
		panel.addChild(button);
		EmptyAssociation association = new EmptyAssociation();
		button.setAssociation(association);
		// check association
		assertSame(button, association.getJavaInfo());
		assertTrue(association.canDelete());
		// no getSource()
		try {
			association.getSource();
			fail();
		} catch (NotImplementedException e) {
		}
		// no getStatement()
		assertNull(association.getStatement());
		// can not be moved
		try {
			association.move(null);
			fail();
		} catch (NotImplementedException e) {
		}
		// can not be reparented
		try {
			association.setParent(null);
			fail();
		} catch (NotImplementedException e) {
		}
		// can not be morphed
		try {
			association.getCopy();
			fail();
		} catch (NotImplementedException e) {
		}
		// can not be removed
		{
			association.remove();
			assertSame(association, button.getAssociation());
		}
	}
}
