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
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Model for {@link DialogPage} itself.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class DialogPageImplInfo extends DialogPageInfo implements IJavaInfoRendering {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogPageImplInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		JavaInfoUtils.scheduleSpecialRendering(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void render() throws Exception {
		m_shell = new Shell();
		m_shell.setLayout(new FillLayout());
		//
		ReflectionUtils.invokeMethod(
				getObject(),
				"createControl(org.eclipse.swt.widgets.Composite)",
				m_shell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		// clear LayoutData, because FillLayout does not want any
		{
			Control[] childControls = m_shell.getChildren();
			if (childControls.length == 1) {
				childControls[0].setLayoutData(null);
			}
		}
		// continue
		super.refresh_afterCreate();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		ControlInfo.refresh_fetch(this, new RunnableEx() {
			@Override
			public void run() throws Exception {
				DialogPageImplInfo.super.refresh_fetch();
			}
		});
	}

	@Override
	public void refresh_dispose() throws Exception {
		// dispose Shell
		if (m_shell != null) {
			m_shell.dispose();
			m_shell = null;
		}
		// call "super"
		super.refresh_dispose();
	}
}
