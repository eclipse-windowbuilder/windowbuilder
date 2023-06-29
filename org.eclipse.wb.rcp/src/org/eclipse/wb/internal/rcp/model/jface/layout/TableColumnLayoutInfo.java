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
package org.eclipse.wb.internal.rcp.model.jface.layout;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Model for {@link org.eclipse.jface.layout.TableColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.layout
 */
public final class TableColumnLayoutInfo extends AbstractColumnLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableColumnLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate0() throws Exception {
		ensureTable();
		super.refresh_afterCreate0();
	}

	/**
	 * Users often try to drop {@link TableColumnLayout} on {@link Composite} and see exception. So,
	 * we should automatically create {@link Table} and say that this is not good. :-)
	 */
	private void ensureTable() {
		Composite composite = (Composite) getComposite().getObject();
		if (composite.getChildren().length == 0) {
			Table table = new Table(composite, SWT.BORDER);
			{
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(ModelMessages.TableColumnLayoutInfo_errLine1);
			}
			{
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(ModelMessages.TableColumnLayoutInfo_errLine2);
			}
		}
	}
}
