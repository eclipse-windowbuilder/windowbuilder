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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ICreationSupportPermissions;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;

/**
 * Test for {@link OpaqueCreationSupport}.
 *
 * @author scheglov_ke
 */
public class OpaqueCreationSupportTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  public static void main(String[] args) {",
						"    JPanel panel = new JPanel();",
						"  }",
						"}");
		ClassInstanceCreation node = (ClassInstanceCreation) panel.getCreationSupport().getNode();
		//
		CreationSupport creationSupport = new OpaqueCreationSupport(node);
		assertEquals("opaque", creationSupport.toString());
		assertSame(node, creationSupport.getNode());
		// isJavaInfo()
		assertTrue(creationSupport.isJavaInfo(node));
		assertFalse(creationSupport.isJavaInfo(null));
		assertFalse(creationSupport.isJavaInfo(JavaInfoUtils.getTypeDeclaration(panel)));
		// permissions
		assertFalse(creationSupport.canReorder());
		assertFalse(creationSupport.canReparent());
	}

	/**
	 * Test for {@link OpaqueCreationSupport#add_getSource(NodeTarget)}.
	 */
	@Test
	public void test_add() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
		// add new JButton with OpaqueCreationSupport
		ComponentInfo newButton =
				(ComponentInfo) JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						JButton.class,
						new OpaqueCreationSupport("new JButton()"));
		flowLayout.add(newButton, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertEquals("new JButton()", m_lastEditor.getSource(newButton.getCreationSupport().getNode()));
		// assert that "button" is bound to AST
		{
			ASTNode node = getNode("button = ");
			assertTrue(newButton.isRepresentedBy(node));
		}
	}

	/**
	 * Test for using {@link ICreationSupportPermissions} by {@link OpaqueCreationSupport}.
	 */
	@Test
	public void test_permissions() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test {",
						"  public static void main(String[] args) {",
						"    JPanel panel = new JPanel();",
						"  }",
						"}");
		// create OpaqueCreationSupport
		OpaqueCreationSupport creationSupport;
		{
			ClassInstanceCreation node = (ClassInstanceCreation) panel.getCreationSupport().getNode();
			creationSupport = new OpaqueCreationSupport(node);
			creationSupport.setJavaInfo(panel);
		}
		//
		// set ICreationSupportPermissions
		ICreationSupportPermissions permissions;
		{
			permissions = mock(ICreationSupportPermissions.class);
			creationSupport.setPermissions(permissions);
		}
		// canDelete()
		{
			assertFalse(creationSupport.canDelete());
			//
			verify(permissions).canDelete(panel);
			verifyNoMoreInteractions(permissions);
		}
		// delete()
		{
			clearInvocations(permissions);
			//
			creationSupport.delete();
			//
			verify(permissions).delete(panel);
			verifyNoMoreInteractions(permissions);
		}
		// canReorder()
		{
			clearInvocations(permissions);
			//
			assertFalse(creationSupport.canReorder());
			//
			verify(permissions).canReorder(panel);
			verifyNoMoreInteractions(permissions);
		}
		// canReparent()
		{
			clearInvocations(permissions);
			//
			assertFalse(creationSupport.canReparent());
			//
			verify(permissions).canReparent(panel);
			verifyNoMoreInteractions(permissions);
		}
	}
}
