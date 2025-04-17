/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.core.editor.palette.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Model of entry on palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class EntryInfo extends AbstractElementInfo {
	protected IEditPartViewer m_editPartViewer;
	protected JavaInfo m_rootJavaInfo;
	protected AstEditor m_editor;
	protected IJavaProject m_javaProject;
	protected EditorState m_state;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes this {@link EntryInfo}, prepares it for future calls of methods.
	 *
	 * @return <code>true</code> if this {@link EntryInfo} is successfully activated.
	 */
	public boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
		m_editPartViewer = editPartViewer;
		m_rootJavaInfo = rootJavaInfo;
		m_editor = m_rootJavaInfo.getEditor();
		m_javaProject = m_editor.getJavaProject();
		m_state = EditorState.get(m_editor);
		return true;
	}

	/**
	 * Sometimes we want to show entry, but don't allow to select it.
	 *
	 * @return <code>true</code> if this {@link EntryInfo} is enabled.
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * @return the icon for visual.
	 */
	public abstract ImageDescriptor getIcon();

	////////////////////////////////////////////////////////////////////////////
	//
	// Activation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs operation when user selects this entry on palette.
	 *
	 * @param reload is {@code true} if entry should be automatically reloaded after
	 *               successful using.
	 *
	 * @return <code>true</code> if entry was successfully activated.
	 * @deprecated Use {@link #createTool(boolean)} instead and check the returned
	 *             {@link Tool} for {@code null}.
	 */
	@Deprecated(forRemoval=true, since="1.12.100")
	public boolean activate(boolean reload) {
		return createTool(reload) != null;
	}

	/**
	 * Performs operation when user selects this entry on palette.
	 *
	 * @param reload is {@code true} if entry should be automatically reloaded after
	 *               successful using.
	 * @return the {@link Tool} that should be set on activation, or {@code null} if
	 *         no {@link Tool} can be activated.
	 */
	public abstract Tool createTool(boolean reload);

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IPaletteSite} for palette of this entry.
	 */
	protected final IPaletteSite getSite() {
		return IPaletteSite.Helper.getSite(m_rootJavaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Category
	//
	////////////////////////////////////////////////////////////////////////////
	private CategoryInfo m_category;

	/**
	 * @return the parent {@link CategoryInfo}.
	 */
	public CategoryInfo getCategory() {
		return m_category;
	}

	/**
	 * Sets the parent {@link CategoryInfo}.
	 */
	void setCategory(CategoryInfo category) {
		m_category = category;
	}
}
