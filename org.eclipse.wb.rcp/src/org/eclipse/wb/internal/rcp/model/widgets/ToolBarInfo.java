/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;

import java.util.List;

/**
 * Model for {@link ToolBar}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class ToolBarInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ToolBarInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link ToolBarInfo} has horizontal layout.
	 */
	public boolean isHorizontal() {
		return ControlSupport.isStyle(getObject(), SWT.HORIZONTAL);
	}

	/**
	 * @return the {@link ToolItemInfo} children.
	 */
	public List<ToolItemInfo> getItems() {
		return getChildren(ToolItemInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenTree() throws Exception {
			List<ObjectInfo> children = Lists.newArrayList(super.getChildrenTree());
			removeItemControls(children);
			return children;
		}

		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			List<ObjectInfo> children = Lists.newArrayList(super.getChildrenGraphical());
			removeItemControls(children);
			return children;
		}

		private void removeItemControls(List<ObjectInfo> children) {
			for (ToolItemInfo item : getItems()) {
				children.remove(item.getControl());
			}
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}
}
