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
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.EditorAreaInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IPageLayout;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

/**
 * Abstract container for shortcut-like methods in {@link IPageLayout}, such as
 * {@link IPageLayout#addFastView(String)}, {@link IPageLayout#addShowViewShortcut(String)} and
 * {@link IPageLayout#addPerspectiveShortcut(String)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class AbstractShortcutContainerInfo extends ObjectInfo {
	protected final PageLayoutInfo m_page;
	private final int m_toolBarStyle;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractShortcutContainerInfo(PageLayoutInfo page, int toolBarStyle) throws Exception {
		m_page = page;
		m_page.addChild(this);
		m_toolBarStyle = toolBarStyle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return getPresentationText();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the root {@link PageLayoutInfo}.
	 */
	public PageLayoutInfo getPage() {
		return m_page;
	}

	/**
	 * @return the {@link AbstractShortcutInfo} children.
	 */
	public List<AbstractShortcutInfo> getShortcuts() {
		return getChildren(AbstractShortcutInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultObjectPresentation(this) {
		@Override
		public ImageDescriptor getIcon() {
			return CoreImages.FOLDER_OPEN;
		}

		@Override
		public String getText() throws Exception {
			return getPresentationText();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	/**
	 * @return the text to show in components tree.
	 */
	protected abstract String getPresentationText();

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	private Composite m_composite;
	private ToolBar m_toolBar;

	/**
	 * @return the {@link Composite} that is used as object for this
	 *         {@link AbstractShortcutContainerInfo}.
	 */
	final Composite getComposite() {
		return m_composite;
	}

	/**
	 * @return the {@link ToolBar} widget of this {@link AbstractShortcutContainerInfo}.
	 */
	final ToolBar getToolBar() {
		return m_toolBar;
	}

	/**
	 * Renders this {@link AbstractShortcutContainerInfo}, i.e. creates its {@link Control}'s.
	 */
	public final Control render(Composite parent) throws Exception {
		m_composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.create(m_composite).margins(3);
		{
			m_toolBar = new ToolBar(m_composite, m_toolBarStyle | SWT.FLAT | SWT.RIGHT);
			m_composite.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					org.eclipse.swt.graphics.Rectangle r = m_toolBar.getBounds();
					GC gc = e.gc;
					gc.setForeground(ColorConstants.buttonDarker);
					gc.drawRoundRectangle(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 5, 5);
				}
			});
		}
		// return to allow external layout
		return m_composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	private Rectangle m_bounds;

	/**
	 * @return the bounds of {@link EditorAreaInfo} relative to {@link PageLayoutInfo}.
	 */
	public Rectangle getBounds() {
		return m_bounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		m_composite = null;
		m_bounds = null;
		super.refresh_dispose();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		m_bounds = CoordinateUtils.getBounds(m_page.getComposite(), m_composite);
		super.refresh_fetch();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link AbstractShortcutInfo}.
	 *
	 * @return the created {@link AbstractShortcutInfo}.
	 */
	protected final <T extends AbstractShortcutInfo> T command_CREATE(String id,
			Class<T> itemClass,
			T nextItem,
			String shortcutsMethodName,
			String shortcutMethodName) throws Exception {
		AstEditor editor = m_page.getEditor();
		// prepare target
		StatementTarget target;
		String layoutSource;
		if (nextItem != null) {
			MethodInvocation nextInvocation = (MethodInvocation) nextItem.getCreationSupport().getNode();
			Statement nextStatement = AstNodeUtils.getEnclosingStatement(nextInvocation);
			target = new StatementTarget(nextStatement, true);
			layoutSource = editor.getSource(nextInvocation.getExpression());
		} else {
			MethodDeclaration shortcutsMethod;
			{
				TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_page);
				shortcutsMethod =
						AstNodeUtils.getMethodBySignature(typeDeclaration, shortcutsMethodName
								+ "(org.eclipse.ui.IPageLayout)");
				if (shortcutsMethod == null) {
					shortcutsMethod =
							editor.addMethodDeclaration(
									"private void " + shortcutsMethodName + "(org.eclipse.ui.IPageLayout layout)",
									Collections.emptyList(),
									new BodyDeclarationTarget(typeDeclaration, false));
					// add shortcuts method invocation into "createInitialLayout"
					{
						MethodDeclaration layoutMethod =
								AstNodeUtils.getMethodBySignature(
										typeDeclaration,
										"createInitialLayout(org.eclipse.ui.IPageLayout)");
						Assert.isNotNull(
								layoutMethod,
								"No createInitialLayout() method in %s.",
								editor.getSource());
						String layoutSource2 =
								DomGenerics.parameters(layoutMethod).get(0).getName().getIdentifier();
						editor.addStatement(
								shortcutsMethodName + "(" + layoutSource2 + ");",
								new StatementTarget(layoutMethod, true));
					}
				}
			}
			// target = shortcuts method
			target = new StatementTarget(shortcutsMethod, false);
			layoutSource = DomGenerics.parameters(shortcutsMethod).get(0).getName().getIdentifier();
		}
		// add new MethodInvocation
		MethodInvocation newInvocation;
		{
			String source =
					layoutSource
					+ "."
					+ shortcutMethodName
					+ "("
					+ StringConverter.INSTANCE.toJavaSource(m_page, id)
					+ ");";
			ExpressionStatement newStatement = (ExpressionStatement) editor.addStatement(source, target);
			newInvocation = (MethodInvocation) newStatement.getExpression();
		}
		// create shortcut model
		T shortcut;
		{
			Constructor<T> constructor =
					itemClass.getConstructor(PageLayoutInfo.class, getClass(), MethodInvocation.class);
			shortcut = constructor.newInstance(m_page, this, newInvocation);
			// "shortcut" was added as last child, move before "nextItem"
			moveChild(shortcut, nextItem);
		}
		// add related nodes
		{
			shortcut.bindToExpression(newInvocation);
			shortcut.addRelatedNodes(newInvocation);
			m_page.addRelatedNodes(newInvocation);
		}
		// OK, we have ready shortcut
		return shortcut;
	}

	/**
	 * Moves existing {@link AbstractShortcutInfo}.
	 */
	protected final <T extends AbstractShortcutInfo> void command_MOVE(T item,
			T nextItem,
			String shortcutsMethodName) throws Exception {
		AstEditor editor = m_page.getEditor();
		// prepare target
		StatementTarget target;
		if (nextItem != null) {
			MethodInvocation nextInvocation = (MethodInvocation) nextItem.getCreationSupport().getNode();
			Statement nextStatement = AstNodeUtils.getEnclosingStatement(nextInvocation);
			target = new StatementTarget(nextStatement, true);
		} else {
			MethodDeclaration shortcutsMethod;
			{
				TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_page);
				shortcutsMethod =
						AstNodeUtils.getMethodBySignature(typeDeclaration, shortcutsMethodName
								+ "(org.eclipse.ui.IPageLayout)");
			}
			// target = shortcuts method
			target = new StatementTarget(shortcutsMethod, false);
		}
		// move Statement
		{
			Statement statement = AstNodeUtils.getEnclosingStatement(item.getCreationSupport().getNode());
			editor.moveStatement(statement, target);
		}
		// move model
		moveChild(item, nextItem);
	}
}
