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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.rcp.palette.ActionUseEntryInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Model for {@link IAction}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ActionInfo extends JavaInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// add to palette
		addBroadcastListener(new PaletteEventListener() {
			@Override
			public void entries(CategoryInfo category, List<EntryInfo> entries) throws Exception {
				if (category.getId().equals("org.eclipse.wb.rcp.jface.actions")) {
					entries.add(new ActionUseEntryInfo(ActionInfo.this));
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		super.refresh_dispose();
		// dispose ImageDescriptor
		m_icon = null;
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// update ImageDescriptor
		m_icon = (ImageDescriptor) ReflectionUtils.invokeMethod2(getObject(), "getImageDescriptor");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The SWT {@link ImageDescriptor}, may be <code>null</code>.
	 */
	private ImageDescriptor m_icon;
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public ImageDescriptor getIcon() throws Exception {
			if (m_icon != null) {
				return m_icon;
			}
			if (getCreationSupport() instanceof IActionIconProvider) {
				IActionIconProvider iconProvider = (IActionIconProvider) getCreationSupport();
				return new ImageImageDescriptor(iconProvider.getActionIcon());
			}
			return super.getIcon();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}
}
