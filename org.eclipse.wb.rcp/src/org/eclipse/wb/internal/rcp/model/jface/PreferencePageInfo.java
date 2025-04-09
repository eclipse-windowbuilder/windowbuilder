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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;

/**
 * Model for {@link PreferencePage}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class PreferencePageInfo extends DialogPageInfo implements IJavaInfoRendering {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PreferencePageInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		JavaInfoUtils.scheduleSpecialRendering(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	Shell getShell() {
		if (m_shell == null) {
			return m_preferenceDialog.getShell();
		}
		return super.getShell();
	}

	/**
	 * Convenience method for accessing the {@link IPreferencePage} contained by
	 * this {@link PreferencePageInfo}.
	 */
	public IPreferencePage getPreferencePage() {
		return (IPreferencePage) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	private static Shell m_parentShell;
	private PreferenceDialog m_preferenceDialog;

	@Override
	public void render() throws Exception {
		// prepare PreferenceNode
		PreferenceNode preferenceNode = new PreferenceNode("__wbp", getPreferencePage());
		// prepare PreferenceManager
		PreferenceManager preferenceManager = new PreferenceManager();
		// add this PreferencePage
		preferenceManager.addToRoot(preferenceNode);
		// prepare parent Shell for PreferenceDialog
		if (m_parentShell == null) {
			m_parentShell = new Shell();
		}
		// create PreferenceDialog
		m_preferenceDialog = new PreferenceDialog(m_parentShell, preferenceManager);
		// open PreferenceDialog, so perform PreferencePage GUI creation
		m_preferenceDialog.create();
		m_shell = m_preferenceDialog.getShell();
		configureShell();
	}

	/**
	 * Allows configuring {@link #m_shell} after opening {@link PreferenceDialog}.
	 */
	protected void configureShell() throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		// dispose PreferenceDialog
		if (m_preferenceDialog != null) {
			ReflectionUtils.invokeMethod(m_preferenceDialog, "close()");
			m_shell = null;
		}
		// call "super"
		super.refresh_dispose();
	}
}
