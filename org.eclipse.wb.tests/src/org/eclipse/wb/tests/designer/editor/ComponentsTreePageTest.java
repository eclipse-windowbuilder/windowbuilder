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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.structure.components.ComponentsTreePage;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.gef.core.CancelOperationError;
import org.eclipse.wb.internal.gef.tree.dnd.TreeDropListener;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import javax.swing.JButton;

/**
 * Test for {@link ComponentsTreePage}.
 *
 * @author scheglov_ke
 */
public class ComponentsTreePageTest extends SwingGefTest {
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
	 * Test for {@link ObjectEventListener#select(java.util.List)}.
	 */
	@Test
	public void test_ObjectEventListener_select_existingComponent() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no initial selection
		assertTreeSelectionModels();
		assertSelectionModels();
		// use broadcast to select
		panel.getBroadcastObject().select(List.of(button));
		assertTreeSelectionModels(button);
		assertSelectionModels(button);
		// set empty selection
		panel.getBroadcastObject().select(Collections.emptyList());
		assertTreeSelectionModels();
		assertSelectionModels();
	}

	/**
	 * Test for {@link ObjectEventListener#select(java.util.List)}.
	 */
	@Test
	public void test_ObjectEventListener_select_newComponent() throws Exception {
		final ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
					}
					// filler filler filler
				}""");
		// no initial selection
		assertTreeSelectionModels();
		assertSelectionModels();
		// execute edit operation
		final ComponentInfo newButton = createJButton();
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				// add new JButton
				((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
				// use broadcast to select
				panel.getBroadcastObject().select(List.of(newButton));
			}
		});
		// assert selection
		assertTreeSelectionModels(newButton);
		assertSelectionModels(newButton);
		// set empty selection
		panel.getBroadcastObject().select(Collections.emptyList());
		assertTreeSelectionModels();
		assertSelectionModels();
	}

	/**
	 * There was problem: after some exception during drag operation {@link TreeDropListener} had
	 * state (list of {@link EditPart} to drag) remembered since last operation. So, when we reparse
	 * source again and try to perform some other drag this state was not updated - instead it was
	 * used to create {@link Command} and created weird effect during its execution.
	 */
	@Test
	public void test_TreeDropListener_dragAfterException() throws Exception {
		removeExceptionsListener();
		openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JButton button = new JButton();
							add(button, BorderLayout.NORTH);
						}
						{
							JPanel inner = new JPanel();
							add(inner, BorderLayout.CENTER);
						}
					}
				}""");
		// simulate exception
		{
			System.setProperty("wbp.EditDomain.simulateCommandException", "true");
			EnvironmentUtils.setTestingTime(false);
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			try {
				ComponentInfo button = getJavaInfoByName("button");
				ComponentInfo inner = getJavaInfoByName("inner");
				tree.startDrag(button).dragOn(inner).endDrag();
			} catch (CancelOperationError e) {
			} finally {
				System.clearProperty("wbp.EditDomain.simulateCommandException");
				EnvironmentUtils.setTestingTime(true);
				DesignerPlugin.setDisplayExceptionOnConsole(true);
			}
		}
		// reparse
		{
			IAction refreshAction = m_designPageActions.getRefreshAction();
			refreshAction.run();
			fetchContentFields();
		}
		// drag "button" on "inner"
		{
			ComponentInfo button = getJavaInfoByName("button");
			ComponentInfo inner = getJavaInfoByName("inner");
			tree.startDrag(button).dragOn(inner).endDrag();
		}
		assertEditor("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JPanel inner = new JPanel();
							add(inner, BorderLayout.CENTER);
							{
								JButton button = new JButton();
								inner.add(button);
							}
						}
					}
				}""");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the new empty {@link JButton}.
	 */
	private static ComponentInfo createJButton() throws Exception {
		return createJavaInfo("javax.swing.JButton");
	}
}
