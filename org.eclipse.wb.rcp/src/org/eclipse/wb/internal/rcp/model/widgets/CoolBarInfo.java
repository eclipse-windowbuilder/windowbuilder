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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for {@link CoolBar}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class CoolBarInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CoolBarInfo(AstEditor editor,
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
	 * @return <code>true</code> if this {@link CoolBarInfo} has horizontal layout.
	 */
	public boolean isHorizontal() {
		return (getWidget().getStyle() & SWT.HORIZONTAL) != 0;
	}

	/**
	 * @return the {@link CoolItemInfo} children.
	 */
	public List<CoolItemInfo> getItems() {
		return getChildren(CoolItemInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		// ensure that each CoolItem has reasonable size
		for (CoolItemInfo itemInfo : getItems()) {
			CoolItem item = itemInfo.getWidget();
			Control control = item.getControl();
			if (control == null) {
				item.setSize(20, 25);
			} else {
				Point control_preferredSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point item_preferredSize =
						item.computeSize(control_preferredSize.x, control_preferredSize.y);
				item.setSize(item_preferredSize);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenTree() throws Exception {
			List<ObjectInfo> children = new ArrayList<>(super.getChildrenTree());
			removeItemControls(children);
			return children;
		}

		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			List<ObjectInfo> children = new ArrayList<>(super.getChildrenGraphical());
			removeItemControls(children);
			return children;
		}

		private void removeItemControls(List<ObjectInfo> children) {
			for (CoolItemInfo item : getItems()) {
				children.remove(item.getControl());
			}
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}
}
