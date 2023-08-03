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

import org.eclipse.wb.core.model.association.SuperConstructorArgumentAssociation;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import org.junit.Test;

/**
 * Tests for {@link SuperConstructorArgumentAssociation}.
 *
 * @author scheglov_ke
 */
public class SuperConstructorArgumentAssociationTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    super(new BorderLayout());",
						"  }",
						"}");
		panel.refresh();
		// BorderLayout expected
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		SuperConstructorArgumentAssociation association =
				(SuperConstructorArgumentAssociation) layout.getAssociation();
		assertFalse(association.canDelete());
		assertInstanceOf(SuperConstructorInvocation.class, association.getStatement());
		assertEquals("super(new BorderLayout());", association.getSource());
	}
}
