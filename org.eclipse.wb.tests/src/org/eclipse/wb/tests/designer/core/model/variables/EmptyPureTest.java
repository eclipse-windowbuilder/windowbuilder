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
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test for adding using {@link EmptyPureVariableSupport}.
 *
 * @author scheglov_ke
 */
public class EmptyPureTest extends AbstractVariableTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(Container container) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <!-- CREATION -->",
						"  <creation>",
						"    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
						"  </creation>",
						"  <!-- CONSTRUCTORS -->",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='java.awt.Container' parent='true'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		waitForAutoBuild();
		// do parse
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
						m_lastLoader.loadClass("test.MyButton"),
						new ConstructorCreationSupport());
		// add button
		JavaInfoUtils.add(
				button,
				new EmptyPureVariableSupport(button),
				PureFlatStatementGenerator.INSTANCE,
				AssociationObjects.empty(),
				panel,
				null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    new MyButton(this);",
				"  }",
				"}");
	}
}
