/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;

import org.junit.Test;

/**
 * Test for reparsing on dependency modification, for example modification of used component (it
 * same project).
 *
 * @author scheglov_ke
 */
public class ReparseOnModificationTest extends SwingGefTest {
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
	 * If we:
	 * <ul>
	 * <li>Open Design page for Form that uses MyComponent.</li>
	 * <li>Open MyComponent new new editor, change it and save.</li>
	 * <li>Again activate Form (still on Design page).</li>
	 * </ul>
	 * Then reparse of Form should happen.
	 */
	@Test
	public void test_modifyComponent_whenDesign() throws Exception {
		ICompilationUnit componentUnit =
				createModelCompilationUnit(
						"test",
						"MyComponent.java",
						getTestSource("""
								public class MyComponent extends JPanel {
									public MyComponent() {
									}
								}"""));
		waitForAutoBuild();
		// initial state, Design visible
		ContainerInfo initialPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						add(new MyComponent());
					}
				}""");
		// open MyComponent
		IEditorPart componentEditor = JavaUI.openInEditor(componentUnit);
		// modify MyComponent
		componentUnit.getBuffer().replace(0, 0, "// comment\n");
		componentEditor.doSave(null);
		// switch to Test, reparse should happen
		{
			ICompilationUnit unit = initialPanel.getEditor().getModelUnit();
			JavaUI.openInEditor(unit);
			waitEventLoop(0);
		}
		fetchContentFields();
		assertNotSame(initialPanel, m_contentJavaInfo);
	}

	/**
	 * If we:
	 * <ul>
	 * <li>Open Design page for Form that uses MyComponent.</li>
	 * <li>Switch Form back to Source page.</li>
	 * <li>Open MyComponent new new editor, change it and save.</li>
	 * <li>Again activate Form (still on Source page).</li>
	 * </ul>
	 * Then reparse of Form should NOT happen.
	 */
	@Test
	public void test_modifyComponent_whenSource() throws Exception {
		ICompilationUnit componentUnit =
				createModelCompilationUnit(
						"test",
						"MyComponent.java",
						getTestSource("""
								public class MyComponent extends JPanel {
									public MyComponent() {
									}
								}"""));
		waitForAutoBuild();
		// open editor
		ContainerInfo initialPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						add(new MyComponent());
					}
				}""");
		// initial state, Design visible
		// switch to Source page
		openSourcePage();
		// open MyComponent
		IEditorPart componentEditor = JavaUI.openInEditor(componentUnit);
		// modify MyComponent
		componentUnit.getBuffer().replace(0, 0, "// comment\n");
		componentEditor.doSave(null);
		// switch to Test, not reparse expected
		{
			ICompilationUnit unit = initialPanel.getEditor().getModelUnit();
			JavaUI.openInEditor(unit);
		}
		fetchContentFields();
		assertSame(initialPanel, m_contentJavaInfo);
		// switch to Design, so now reparse should happen
		openDesignPage();
		fetchContentFields();
		assertNotSame(initialPanel, m_contentJavaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for reparsing on modification of component directly used on form.
	 */
	@Test
	public void test_modifyComponent_direct() throws Exception {
		ICompilationUnit myComponentUnit =
				createModelCompilationUnit(
						"test",
						"MyComponent.java",
						getTestSource("""
								public class MyComponent extends JPanel {
									public MyComponent() {
									}
								}"""));
		waitForAutoBuild();
		//
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						add(new MyComponent());
					}
				}""");
		// no modifications yet
		assertFalse(shouldReparse_editorActivated(panel));
		// modify and check
		myComponentUnit.getBuffer().replace(0, 0, "// comment\n");
		myComponentUnit.save(null, true);
		assertTrue(shouldReparse_editorActivated(panel));
	}

	/**
	 * Test for reparsing on modification of component in-directly used on form.
	 */
	@Test
	public void test_modifyComponent_secondLevel() throws Exception {
		ICompilationUnit myComponentInnerUnit =
				createModelCompilationUnit(
						"test",
						"MyComponent_inner.java",
						getTestSource("""
								public class MyComponent_inner extends JPanel {
									public MyComponent_inner() {
									}
								}"""));
		setFileContentSrc(
				"test/MyComponent.java",
				getTestSource("""
						public class MyComponent extends JPanel {
							public MyComponent() {
								add(new MyComponent_inner());
							}
						}"""));
		waitForAutoBuild();
		//
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						add(new MyComponent());
					}
				}""");
		// no modifications yet
		assertFalse(shouldReparse_editorActivated(panel));
		// modify and check
		myComponentInnerUnit.getBuffer().replace(0, 0, "// comment\n");
		myComponentInnerUnit.save(null, true);
		assertTrue(shouldReparse_editorActivated(panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ContainerInfo openContainer(String lines) throws Exception {
		ContainerInfo container = super.openContainer(lines);
		waitDependencyInformation(container);
		return container;
	}

	private static void waitDependencyInformation(ContainerInfo container) throws Exception {
		for (int i = 0; i < 100; i++) {
			if (JavaInfoUtils.hasDependencyInformation(container)) {
				break;
			}
			Thread.sleep(10);
		}
	}

	/**
	 * @return <code>true</code> if {@link EditorActivatedListener#invoke(boolean[])} requested
	 *         reparsing.
	 */
	private static boolean shouldReparse_editorActivated(JavaInfo panel) throws Exception {
		EditorActivatedRequest request = new EditorActivatedRequest();
		panel.getBroadcast(EditorActivatedListener.class).invoke(request);
		return request.isReparseRequested();
	}
}
