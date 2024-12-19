/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Tree;

import java.util.List;

/**
 * Model for "big" SWT {@link Tree}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class TreeInfo extends org.eclipse.wb.internal.swt.model.widgets.TreeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		contributeToClipboardCopy();
	}

	@Override
	public Tree getWidget() {
		return (Tree) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasts
	//
	////////////////////////////////////////////////////////////////////////////
	private void contributeToClipboardCopy() {
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
					throws Exception {
				// copy TreeColumn's
				if (javaInfo == TreeInfo.this) {
					for (TreeColumnInfo column : getColumns()) {
						final JavaInfoMemento columnMemento = JavaInfoMemento.createMemento(column);
						commands.add(new ClipboardCommand() {
							private static final long serialVersionUID = 0L;

							@Override
							public void execute(JavaInfo javaInfo) throws Exception {
								TreeColumnInfo column = (TreeColumnInfo) columnMemento.create(javaInfo);
								JavaInfoUtils.add(column, null, javaInfo, null);
								columnMemento.apply();
							}
						});
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private static int BORDER_WIDTH = 1;

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// prepare metrics
		int headerHeight;
		{
			headerHeight = getWidget().getHeaderHeight();
		}
		// prepare columns bounds
		int x = BORDER_WIDTH;
		int y = BORDER_WIDTH;
		{
			for (TreeColumnInfo column : getColumns()) {
				int columnWidth = column.getWidget().getWidth();
				Rectangle bounds = new Rectangle(x, y, columnWidth, headerHeight);
				column.setModelBounds(bounds);
				column.setBounds(bounds);
				x += columnWidth;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link TreeColumnInfo} children.
	 */
	public List<TreeColumnInfo> getColumns() {
		return getChildren(TreeColumnInfo.class);
	}
}
