/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.UndoManager;
import org.eclipse.wb.internal.core.editor.structure.components.IComponentsTree;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.RefactoringTestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.gef.EditPart;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.JdtActionConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Test for {@link UndoManager}.
 *
 * @author scheglov_ke
 */
public class UndoManagerTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		{
			IPreferenceStore preferences = DesignerPlugin.getPreferences();
			preferences.setToDefault(IPreferenceConstants.P_EDITOR_LAYOUT);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection/expanded restoring after reparse
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that selection is restored after undo/redo.
	 */
	@Test
	public void test_undoRedo() throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		String initialSource = m_lastEditor.getSource();
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		// do edit
		String source_withButton;
		{
			ContainerInfo frame = (ContainerInfo) m_contentJavaInfo;
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			assertTrue(m_viewerCanvas.getSelectedEditParts().isEmpty());
			// select content pane
			canvas.moveTo(contentPane, 10, 10);
			canvas.click();
			assertSelectionModels(contentPane);
			// create JButton
			JavaInfo newButton;
			{
				newButton = loadCreationTool("javax.swing.JButton");
				assertTrue(m_viewerCanvas.getEditDomain().getActiveTool() instanceof CreationTool);
				// use creation tool
				canvas.moveTo(contentPane, 100, 50);
				canvas.click();
				assertEditor("""
						// filler filler filler
						public class Test extends JFrame {
							public Test() {
								{
									JButton button = new JButton("New button");
									getContentPane().add(button, BorderLayout.NORTH);
								}
							}
						}""");
				source_withButton = m_lastEditor.getSource();
			}
			// check selection on new JButton
			assertSelectionModels(newButton);
			assertExpandedComponents(frame, contentPane);
		}
		// undo
		{
			actionBars.getGlobalActionHandler("undo").run();
			waitEventLoop(0);
			fetchContentFields();
			// check source
			assertEquals(initialSource, m_lastEditor.getSource());
			// check selection
			ContainerInfo frame = (ContainerInfo) m_contentJavaInfo;
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			assertSelectionModels(contentPane);
		}
		// redo
		{
			actionBars.getGlobalActionHandler("redo").run();
			waitEventLoop(0);
			fetchContentFields();
			// check source
			assertEquals(source_withButton, m_lastEditor.getSource());
			ContainerInfo frame = (ContainerInfo) m_contentJavaInfo;
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			// check selection/expansion
			assertSelectionModels(contentPane.getChildrenComponents().get(0));
			assertExpandedComponents(frame, contentPane);
		}
	}

	/**
	 * Test for case when we modify only logic of application, but not its GUI, so can keep old
	 * selection.
	 */
	@Test
	public void test_modifyLogicAndSwitchDesign() throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		// prepare ICompilationUnit
		ICompilationUnit compilationUnit;
		{
			compilationUnit = (ICompilationUnit) ReflectionUtils.getFieldObject(m_designPage, "m_compilationUnit");
		}
		//
		{
			ContainerInfo frame = (ContainerInfo) m_contentJavaInfo;
			ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
			//
			assertTrue(m_viewerCanvas.getSelectedEditParts().isEmpty());
			// select content pane
			canvas.click(150, 100, 1);
			assertSelectionModels(contentPane);
			// switch to Source
			openSourcePage();
			// modify ICompilationUnit
			compilationUnit.getBuffer().replace(0, 0, "// abc\n");
			waitEventLoop(0);
			// switch to Design
			openDesignPage();
			// "contentPane" should be selected, despite reparsing
			{
				List<? extends EditPart> selectedEditParts = m_viewerCanvas.getSelectedEditParts();
				assertEquals(1, selectedEditParts.size());
				EditPart selectedEditPart = selectedEditParts.get(0);
				assertEquals(contentPane.toString(), selectedEditPart.getModel().toString());
			}
		}
	}

	/**
	 * When document is changed, but not WBP editor is active (for example Java editor is also
	 * opened), then we should not parse. But when WBP editor is activated later, then we should
	 * parse.
	 */
	@Test
	public void test_modifyInParallelJavaEditor() throws Exception {
		IWorkbenchPage activePage = DesignerPlugin.getActivePage();
		ContainerInfo originalContainer = openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		// open in Java editor
		ISourceViewer sourceViewer;
		{
			IFile file = getFileSrc("test/Test.java");
			IEditorPart javaEditor =
					activePage.openEditor(
							new FileEditorInput(file),
							JavaUI.ID_CU_EDITOR,
							true,
							IWorkbenchPage.MATCH_NONE);
			sourceViewer = (ISourceViewer) ReflectionUtils.invokeMethod(javaEditor, "getViewer()");
		}
		// update document, WBP editor is not active, so no changes yet
		sourceViewer.getDocument().replace(0, 0, "// comment\n");
		fetchContentFields();
		assertSame(originalContainer, m_contentJavaInfo);
		// active WBP editor, document was changed, so reparse
		activePage.activate(m_designerEditor);
		waitEventLoop(0);
		fetchContentFields();
		assertNotSame(originalContainer, m_contentJavaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expansion
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that when we select component, its parent become expanded.
	 */
	@Test
	public void test_expandOnSelection() throws Exception {
		ContainerInfo frame = openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		IComponentsTree componentTree = DesignPageSite.Helper.getSite(frame).getComponentTree();
		// collapse all
		{
			componentTree.setExpandedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
			assertEquals(0, componentTree.getExpandedElements().length);
		}
		// select "contentPane", "frame" should be expanded
		{
			canvas.select(contentPane);
			assertExpandedComponents(frame);
		}
	}

	/**
	 * Sometimes expansion paths are remembered for wrong dump, so this causes later exception.
	 */
	@Ignore
	@Test
	public void test_expandRemembered_bug_0() throws Exception {
		ContainerInfo frame = openContainer("""
				public class Test extends JFrame {
					public Test() {
						JPanel panel = new JPanel();
						getContentPane().add(panel);
						panel.add(new JButton());
					}
				}""");
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		ContainerInfo panel = (ContainerInfo) contentPane.getChildrenComponents().get(0);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// select "button", "frame" and "contentPane" are expanded
		canvas.select(button);
		assertExpandedComponents(frame, contentPane, panel);
		// delete "panel" with "button"
		canvas.select(panel);
		actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId()).run();
		assertEquals(
				getSource(
						"package test;",
						"import java.awt.*;",
						"import java.awt.event.*;",
						"import javax.swing.*;",
						"import javax.swing.border.*;",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}"),
				m_lastEditor.getModelUnit().getSource());
		// switch to "Source"
		openSourcePage();
		// organize imports
		actionBars.getGlobalActionHandler(JdtActionConstants.ORGANIZE_IMPORTS).run();
		assertEquals(
				getSourceDQ(
						"package test;",
						"import javax.swing.JFrame;",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}"),
				m_lastEditor.getModelUnit().getSource());
		// switch to "Design", no exception expected
		openDesignPage();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expand on open
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When we open editor, it automatically expands components from root, until there is one element.<br>
	 * Here we have {@link JFrame} with empty "contentPane", so {@link JFrame} should be expanded.
	 */
	@Test
	public void test_expandOnOpen_1() throws Exception {
		ContainerInfo frame = openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		// "frame" should be expanded
		assertExpandedComponents(frame);
	}

	/**
	 * When we open editor, it automatically expands components from root, until there is one element.<br>
	 * Here we have {@link JFrame} with "contentPane" as its single child, so {@link JFrame} and
	 * "contentPane" should be expanded.
	 */
	@Test
	public void test_expandOnOpen_2() throws Exception {
		ContainerInfo frame = openContainer("""
				public class Test extends JFrame {
					public Test() {
						getContentPane().add(new JButton());
						getContentPane().add(new JButton());
					}
				}""");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		// "frame" and "contentPane" should be expanded
		assertExpandedComponents(frame, contentPane);
	}

	/**
	 * When we open editor, it automatically expands components from root, until there is one element.<br>
	 * Here we have {@link JFrame} with "contentPane" as its single child, so {@link JFrame} and
	 * "contentPane" should be expanded. But {@link JPanel} on "content" pane is not only child of
	 * "contentPane", so should be not expanded.
	 */
	@Test
	public void test_expandOnOpen_3() throws Exception {
		ContainerInfo frame = openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
						{
							JPanel panel = new JPanel();
							getContentPane().add(panel);
							panel.add(new JButton());
						}
						getContentPane().add(new JButton());
					}
				}""");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		// "frame" and "contentPane" should be expanded
		assertExpandedComponents(frame, contentPane);
	}

	/**
	 * Assert that {@link IComponentsTree} has expanded expanded objects.
	 */
	private void assertExpandedComponents(ObjectInfo... expandedObjects) {
		IComponentsTree componentTree =
				DesignPageSite.Helper.getSite(m_contentJavaInfo).getComponentTree();
		assertTrue(Arrays.equals(expandedObjects, componentTree.getExpandedElements()));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Restore selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that we restore selection after reparse.
	 */
	@Test
	public void test_restoreSelection_good() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button_1 = new JButton();
							add(button_1);
						}
						{
							JButton button_2 = new JButton();
							add(button_2);
						}
					}
				}""");
		// select
		tree.select(getJavaInfoByName("button_2"));
		tree.assertPrimarySelected(getJavaInfoByName("button_2"));
		// reparse
		{
			IDesignPageSite.Helper.getSite(m_contentJavaInfo).reparse();
			fetchContentFields();
		}
		// check selection
		tree.assertPrimarySelected(getJavaInfoByName("button_2"));
	}

	/**
	 * Test that if we can not restore selection, we just ignore this, without exceptions.
	 */
	@Test
	public void test_restoreSelection_whenComponentDisappears() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource("""
						public class MyPanel extends JPanel {
							private JButton m_button = new JButton();
							public MyPanel() {
								add(m_button);
							}
							public JButton getButton() {
								return m_button;
							}
						}"""));
		waitForAutoBuild();
		// parse
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						add(new MyPanel());
					}
				}""");
		// select
		{
			ComponentInfo button = getJavaInfoByName("getButton()");
			tree.select(button);
			tree.assertPrimarySelected(button);
		}
		// use "MyPanel" which does not expose "getButton()"
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource("""
						public class MyPanel extends JPanel {
							private JButton m_button = new JButton();
							public MyPanel() {
								add(m_button);
							}
						}"""));
		waitForAutoBuild();
		// reparse
		{
			IDesignPageSite.Helper.getSite(m_contentJavaInfo).reparse();
			fetchContentFields();
		}
		// can not restore selection of "getButton()", because it is not visible in "MyPanel" anymore
		tree.assertSelectedEmpty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Read-Only
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test edit read-only file.
	 */
	@Test
	public void test_readOnly_Yes() throws Exception {
		String expectedSource = """
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
					void foo() {
					}
				}""";
		test_readOnly(IDialogConstants.YES_LABEL, false, expectedSource, expectedSource);
	}

	/**
	 * Test edit read-only file.
	 */
	@Test
	public void test_readOnly_No() throws Exception {
		test_readOnly(IDialogConstants.NO_LABEL, true, """
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
					void foo() {
					}
				}""", """
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
	}

	private void test_readOnly(final String buttonId,
			boolean expectedAccess,
			String expectedSource1, String expectedSource2) throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		ICompilationUnit unit = m_lastEditor.getModelUnit();
		// modify file access to read-only mode
		IFile unitFile = (IFile) unit.getUnderlyingResource();
		ResourceAttributes attributes = new ResourceAttributes();
		attributes.setReadOnly(true);
		unitFile.setResourceAttributes(attributes);
		// change AST
		TypeDeclaration typeDeclaration = (TypeDeclaration) m_lastEditor.getAstUnit().types().get(0);
		m_lastEditor.addMethodDeclaration(
				"void foo()",
				new ArrayList<>(),
				new BodyDeclarationTarget(typeDeclaration, false));
		// do commit changes
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				m_lastEditor.commitChanges();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Read-only File Encountered").bot();
				shell.button(buttonId).click();
			}
		});
		//
		waitEventLoop(0);
		waitForAutoBuild();
		// check result
		assertEquals(expectedAccess, unitFile.isReadOnly());
		assertEquals(getTestSource(expectedSource1), m_lastEditor.getSource());
		assertEquals(getTestSource(expectedSource2), unit.getBuffer().getContents());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cases
	//
	////////////////////////////////////////////////////////////////////////////
	@DisposeProjectAfter
	@Test
	public void test_showDesign_switchToSource_rename_showDesign() throws Exception {
		openContainer("""
				// filler filler filler
				public class Test extends JFrame {
					public Test() {
					}
				}""");
		ICompilationUnit unit = m_lastEditor.getModelUnit();
		// switch to "Source"
		openSourcePage();
		// rename compilation unit
		RefactoringTestUtils.renameType(unit.getType("Test"), "Test_2");
		waitEventLoop(0);
		// again switch to "Design"
		openDesignPage();
	}

	/**
	 * Test that renaming {@link ICompilationUnit} while "Design" page is active does not cause
	 * exceptions.
	 */
	@DisposeProjectAfter
	@Ignore
	@Test
	public void test_showDesign_rename() throws Exception {
		openContainer("""
				public class Test extends JFrame {
					public static void main2(String[] args) {
						Test frame = new Test();
						frame.setVisible(true);
					}
					public Test() {
					}
				}""");
		// rename
		{
			ICompilationUnit unit = m_lastEditor.getModelUnit();
			RefactoringTestUtils.renameType(unit.getType("Test"), "Test_2");
		}
		// OK?
		for (int i = 0; i < 10; i++) {
			waitEventLoop(0);
		}
		assertNoLoggedExceptions();
	}
}
