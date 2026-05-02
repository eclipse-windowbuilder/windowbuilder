/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

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
		assertThrows(NotImplementedException.class, association::getSource);
		// no getStatement()
		assertNull(association.getStatement());
		// can not be moved
		assertThrows(NotImplementedException.class, () -> association.move(null));
		// can not be reparented
		assertThrows(NotImplementedException.class, () -> association.setParent(null));
		// can not be morphed
		assertThrows(NotImplementedException.class, association::getCopy);
		// can not be removed
		{
			association.remove();
			assertSame(association, button.getAssociation());
		}
	}
}
