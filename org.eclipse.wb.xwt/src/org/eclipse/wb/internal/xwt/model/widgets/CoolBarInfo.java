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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.support.ControlSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

import java.util.List;

/**
 * Model for {@link CoolBar}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class CoolBarInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CoolBarInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
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
		return ControlSupport.hasStyle(getControl(), SWT.HORIZONTAL);
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
			CoolItem item = (CoolItem) itemInfo.getObject();
			// has size
			if (hasExplicitSize(itemInfo)) {
				continue;
			}
			// use reasonable size
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

	private static boolean hasExplicitSize(CoolItemInfo item) {
		DocumentElement element = item.getCreationSupport().getElement();
		return element.getAttribute("size") != null || element.getAttribute("preferredSize") != null;
	}
}
