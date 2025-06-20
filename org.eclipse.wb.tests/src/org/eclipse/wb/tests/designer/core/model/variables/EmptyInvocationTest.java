/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;

/**
 * Test for adding using {@link EmptyVariableSupport}.
 *
 * @author scheglov_ke
 */
public class EmptyInvocationTest extends AbstractVariableTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// create new button
		ComponentInfo button =
				(ComponentInfo) JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						JButton.class,
						new ConstructorCreationSupport());
		// add button
		JavaInfoUtils.add(
				button,
				new EmptyInvocationVariableSupport(button, "%parent%.add(%child%)", 0),
				PureFlatStatementGenerator.INSTANCE,
				AssociationObjects.invocationChildNull(),
				panel,
				null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new JButton('New button'));",
				"  }",
				"}");
		// delete new button
		button.delete();
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}
}
