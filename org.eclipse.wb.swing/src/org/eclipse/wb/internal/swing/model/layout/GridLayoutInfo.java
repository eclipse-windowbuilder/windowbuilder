/*******************************************************************************
 * Copyright (c) 2011. 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.HorizontalAlignment;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.VerticalAlignment;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.widgets.Composite;

import java.awt.GridLayout;
import java.util.List;

/**
 * Model for {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class GridLayoutInfo extends GenericFlowLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// GridLayout uses "columns" only when "rows == 0"
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void setPropertyExpression(GenericPropertyImpl property,
					String[] source,
					Object[] value,
					boolean[] shouldSet) throws Exception {
				if (property.getJavaInfo() == GridLayoutInfo.this && property.getTitle().equals("columns")) {
					getPropertyByTitle("rows").setValue(0);
				}
			}
		});
		// alignment support
		new LayoutAssistantSupport(this) {
			@Override
			protected AbstractAssistantPage createLayoutPage(Composite parent) {
				return new GridLayoutAssistantPage(parent, m_layout);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		super.onSet();
		GridLayoutConverter.convert(getContainer(), this);
	}

	@Override
	public GridLayout getLayoutManager() {
		return (GridLayout) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void storeLayoutData(ComponentInfo component) throws Exception {
		if (isManagedObject(component)) {
			GeneralLayoutData generalLayoutData = new GeneralLayoutData();
			{
				// calculate cell
				List<ComponentInfo> components = getComponents();
				int rowCount = getLayoutManager().getRows();
				int colCount;
				if (rowCount > 0) {
					colCount = (components.size() - 1) / rowCount + 1;
				} else {
					colCount = getLayoutManager().getColumns();
				}
				int index = components.indexOf(component);
				generalLayoutData.gridX = index % colCount;
				generalLayoutData.gridY = index / colCount;
			}
			generalLayoutData.spanX = 1;
			generalLayoutData.spanY = 1;
			generalLayoutData.horizontalGrab = null;
			generalLayoutData.verticalGrab = null;
			// alignments
			generalLayoutData.horizontalAlignment = HorizontalAlignment.FILL;
			generalLayoutData.verticalAlignment = VerticalAlignment.FILL;
			generalLayoutData.putToInfo(component);
		}
	}
}
