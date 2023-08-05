/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.databinding.parser.ParseState;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;
import org.eclipse.wb.internal.core.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.editor.structure.components.IComponentsTree;
import org.eclipse.wb.internal.core.gef.part.DesignRootEditPart;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.internal.gef.tree.TreeViewer;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoRelatedTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;
import org.eclipse.wb.tests.gef.TreeRobot;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;

import java.util.List;

/**
 * {@link TestCase} for {@link DesignPage} and its usage.
 *
 * @author scheglov_ke
 */
public abstract class DesignerEditorTestCase extends AbstractJavaInfoRelatedTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		waitEventLoop(1);
		System.setProperty(DesignerPalette.FLAG_NO_PALETTE, "true");
		addExceptionsListener();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		try {
			System.clearProperty(DesignerPalette.FLAG_NO_PALETTE);
			waitEventLoop(0);
			TestUtils.closeAllEditors();
			waitEventLoop(0);
			// check for exceptions
			{
				removeExceptionsListener();
				assertNoLoggedExceptions();
			}
		} finally {
			// continue
			waitEventLoop(0);
			super.tearDown();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GEF utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected DesignerEditor m_designerEditor;
	protected IDesignPage m_designPage;
	protected DesignPageActions m_designPageActions;
	protected IComponentsTree m_componentsTree;
	protected PropertyTable m_propertyTable;
	// GEF
	protected JavaInfo m_contentJavaInfo;
	protected EditPart m_contentEditPart;
	// canvas event sender
	protected GraphicalViewer m_viewerCanvas;
	protected GraphicalRobot canvas;
	// headers
	protected GraphicalViewer m_headerHorizontal;
	protected GraphicalViewer m_headerVertical;
	// tree event sender
	protected TreeViewer m_viewerTree;
	protected TreeRobot tree;

	/**
	 * Asserts that two {@link Dimension}'s are equal.
	 */
	protected static void assertEquals(Dimension expected, Dimension actual) {
		assertEquals((Object) expected, (Object) actual);
	}

	/**
	 * Asserts that {@link Dimension} is same as given width/height.
	 */
	protected static void assertEquals(Dimension dimension, int width, int height) {
		assertEquals(new Dimension(width, height), dimension);
	}

	/**
	 * Opens given {@link ICompilationUnit} in new Designer editor and shows "Design" page.
	 */
	protected final void openDesign(ICompilationUnit unit) throws Exception {
		openEditor(unit);
		openDesignPage();
		fetchDesignViewers();
		fetchContentFields();
	}

	/**
	 * Opens {@link DesignerEditor} with given {@link ICompilationUnit}.
	 */
	protected final void openEditor(ICompilationUnit unit) throws Exception {
		// prepare MultiPageEditor
		IWorkbenchPage activePage = DesignerPlugin.getActiveWorkbenchWindow().getActivePage();
		m_designerEditor =
				(DesignerEditor) IDE.openEditor(
						activePage,
						(IFile) unit.getUnderlyingResource(),
						IDesignerEditor.ID);
		assertNotNull(m_designerEditor);
		// maximize editor
		activePage.toggleZoom(activePage.getActivePartReference());
		waitEventLoop(1);
	}

	/**
	 * Opens "Source" page of current {@link DesignerEditor}.
	 */
	protected final void openSourcePage() throws Exception {
		m_designerEditor.getMultiMode().showSource();
		waitEventLoop(1);
	}

	/**
	 * Opens "Design" page of current {@link DesignerEditor}.
	 */
	protected final void openDesignPage() throws Exception {
		m_designerEditor.getMultiMode().showDesign();
		waitEventLoop(1);
	}

	/**
	 * Fills design field - edit part viewers, etc. Creating robots.<br>
	 * We should do this after opening "Design" page.
	 */
	protected void fetchDesignViewers() {
		// prepare DesignPage and DesignComposite
		m_designPage = m_designerEditor.getMultiMode().getDesignPage();
		Object designComposite = ReflectionUtils.getFieldObject(m_designPage, "m_designComposite");
		// DesignPageActions
		m_designPageActions =
				(DesignPageActions) ReflectionUtils.getFieldObject(designComposite, "m_pageActions");
		// prepare GraphicalViewer
		{
			Object viewersComposite =
					ReflectionUtils.getFieldObject(designComposite, "m_viewersComposite");
			m_viewerCanvas =
					(GraphicalViewer) ReflectionUtils.getFieldObject(viewersComposite, "m_viewer");
			assertNotNull(m_viewerCanvas);
			assertNotNull(m_viewerCanvas.getEditDomain());
			// prepare sender
			canvas = new GraphicalRobot(m_viewerCanvas);
			// headers
			m_headerHorizontal =
					(GraphicalViewer) ReflectionUtils.getFieldObject(viewersComposite, "m_horizontalViewer");
			m_headerVertical =
					(GraphicalViewer) ReflectionUtils.getFieldObject(viewersComposite, "m_verticalViewer");
		}
		// prepare TreeViewer
		{
			Object componentsComposite =
					ReflectionUtils.getFieldObject(designComposite, "m_componentsComposite");
			Object treePage = ReflectionUtils.getFieldObject(componentsComposite, "m_treePage");
			m_viewerTree = (TreeViewer) ReflectionUtils.getFieldObject(treePage, "m_viewer");
			assertNotNull(m_viewerTree);
			assertNotNull(m_viewerTree.getEditDomain());
			tree = new TreeRobot(m_viewerTree);
		}
		assertSame(m_viewerCanvas.getEditDomain(), m_viewerTree.getEditDomain());
	}

	/**
	 * Fills content field - {@link #m_contentEditPart}, etc.<br>
	 * We should do this after opening "Design" page and after undo/redo.
	 */
	protected void fetchContentFields() {
		DesignRootEditPart designRootEditPart =
				(DesignRootEditPart) m_viewerCanvas.getRootContainer().getContent();
		m_contentEditPart = designRootEditPart.getJavaRootEditPart();
		m_contentJavaInfo = (JavaInfo) m_contentEditPart.getModel();
		m_lastEditor = m_contentJavaInfo.getEditor();
		{
			UiContext uiContext = new UiContext();
			uiContext.useShell(DesignerPlugin.getShell().getText());
			m_propertyTable = uiContext.findFirstWidget(PropertyTable.class);
		}
		// DesignPageSite
		{
			DesignPageSite designPageSite = DesignPageSite.Helper.getSite(m_contentJavaInfo);
			m_componentsTree = designPageSite.getComponentTree();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// More utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that {@link IComponentsTree} has given models selected.
	 */
	protected final void assertTreeSelectionModels(Object... models) {
		IStructuredSelection selection =
				(IStructuredSelection) m_componentsTree.getSelectionProvider().getSelection();
		Object[] actualModels = selection.toArray();
		assertTrue(ArrayUtils.isEquals(models, actualModels));
	}

	/**
	 * Asserts that {@link EditPart}'s selected in GEF viewer has given models.
	 */
	protected final void assertSelectionModels(Object... models) {
		List<EditPart> editParts = m_viewerCanvas.getSelectedEditParts();
		assertEquals(models.length, editParts.size());
		for (int i = 0; i < models.length; i++) {
			Object model = models[i];
			EditPart editPart = editParts.get(i);
			assertSame(model, editPart.getModel());
		}
	}

	/**
	 * Loads {@link CreationTool} for creating component with given class name.
	 */
	protected final <T extends JavaInfo> T loadCreationTool(String componentClassName)
			throws Exception {
		return loadCreationTool(componentClassName, null);
	}

	/**
	 * Loads {@link CreationTool} for creating component with given class name.
	 */
	@SuppressWarnings("unchecked")
	protected final <T extends JavaInfo> T loadCreationTool(String componentClassName,
			String creationId) throws Exception {
		// prepare new component
		final JavaInfo newComponent;
		{
			Class<?> componentClass =
					EditorState.get(m_lastEditor).getEditorLoader().loadClass(componentClassName);
			CreationSupport creationSupport = new ConstructorCreationSupport(creationId, true);
			newComponent =
					JavaInfoUtils.getWrapped(JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							componentClass,
							creationSupport));
			newComponent.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
		}
		// load CreationTool
		ICreationFactory factory = new ICreationFactory() {
			@Override
			public void activate() {
			}

			@Override
			public Object getNewObject() {
				return newComponent;
			}
		};
		CreationTool creationTool = new CreationTool(factory);
		m_viewerCanvas.getEditDomain().setActiveTool(creationTool);
		// return component that will be added
		return (T) newComponent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the data bindings provider of the current compilation unit.
	 */
	protected final DatabindingsProvider getDatabindingsProvider() throws Exception {
		ParseState parseState = DatabindingRootProcessor.STATES.get(m_lastEditor.getModelUnit());
		assertNotNull(parseState);
		assertNotNull(parseState.databindingsProvider);
		assertInstanceOf(DatabindingsProvider.class, parseState.databindingsProvider);
		return (DatabindingsProvider) parseState.databindingsProvider;
	}

	/**
	 * Asserts existence of an SWT widget with the given name.
	 */
	protected final void assertJavaInfo(String infoName) throws Exception {
		DatabindingsProvider provider = getDatabindingsProvider();
		WidgetsObserveTypeContainer container = (WidgetsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
		assertNotNull(container);
		assertNotNull(container.resolve(getJavaInfoByName(infoName)));
	}

	/**
	 * Asserts selection range in "Java" editor.
	 */
	protected final void assertJavaSelection(int expectedOffset, int expectedLength) {
		ISelectionProvider selectionProvider = m_designerEditor.getSelectionProvider();
		ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
		assertEquals(expectedOffset, selection.getOffset());
		assertEquals(expectedLength, selection.getLength());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Actions access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "delete" action.
	 */
	protected final IAction getDeleteAction() {
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		return actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId());
	}

	/**
	 * @return the "cut" action.
	 */
	protected final IAction getCutAction() {
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		return actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
	}

	/**
	 * @return the "copy" action.
	 */
	protected final IAction getCopyAction() {
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		return actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
	}

	/**
	 * @return the "paste" action.
	 */
	protected final IAction getPasteAction() {
		IActionBars actionBars = m_designerEditor.getEditorSite().getActionBars();
		return actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
	}

	/**
	 * Selects single {@link JavaInfo} and then uses Copy/Paste actions.
	 */
	protected final void doCopyPaste(JavaInfo javaInfo) {
		// copy
		{
			// select "javaInfo"
			canvas.select(javaInfo);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
	}
}
