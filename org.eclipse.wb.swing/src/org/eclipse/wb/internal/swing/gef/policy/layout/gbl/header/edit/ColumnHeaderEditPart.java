/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetAlignmentColumnAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.model.layout.gbl.ui.ColumnEditDialog;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link ColumnInfo} header of {@link AbstractGridBagLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class ColumnHeaderEditPart extends DimensionHeaderEditPart<ColumnInfo> {
	private final ColumnInfo m_column;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnHeaderEditPart(AbstractGridBagLayoutInfo layout,
			ColumnInfo column,
			IFigure containerFigure) {
		super(layout, column, containerFigure);
		m_column = column;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		IFigure figure = new Figure() {
			@Override
			protected void paintFigure(Graphics graphics) {
				Rectangle r = getClientArea();
				// ignore paint when Layout already replaced, but event loop happens
				if (!m_layout.isActive()) {
					return;
				}
				super.paintFigure(graphics);
				// draw rectangle
				graphics.setForegroundColor(ColorConstants.buttonDarker);
				graphics.drawLine(r.x, r.y, r.x, r.bottom());
				graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
				// draw column index
				int titleLeft;
				int titleRight;
				{
					String title = "" + getIndex();
					Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(title, graphics.getFont());
					if (r.width < 3 + textExtents.width + 3) {
						return;
					}
					// draw title
					titleLeft = r.x + (r.width - textExtents.width) / 2;
					titleRight = titleLeft + textExtents.width;
					int y = r.y + (r.height - textExtents.height) / 2;
					graphics.setForegroundColor(ColorConstants.black);
					graphics.drawText(title, titleLeft, y);
				}
				// draw alignment indicator
				if (titleLeft - r.x > 3 + 7 + 3) {
					Image image = null;
					Alignment alignment = m_column.getAlignment();
					if (alignment == ColumnInfo.Alignment.LEFT) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_LEFT);
					} else if (alignment == ColumnInfo.Alignment.RIGHT) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_RIGHT);
					} else if (alignment == ColumnInfo.Alignment.CENTER) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_CENTER);
					} else if (alignment == ColumnInfo.Alignment.FILL) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_FILL);
					}
					if (image != null) {
						int x = r.x + 2;
						drawCentered(graphics, image, x);
					}
				}
				// draw grow indicator
				if (m_column.hasWeight()) {
					if (titleRight + 3 + 7 + 3 < r.right()) {
						Image image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_GROW);
						drawCentered(graphics, image, r.right() - 3 - image.getBounds().width);
					}
				}
			}

			private void drawCentered(Graphics graphics, Image image, int x) {
				int y = (getBounds().height - image.getBounds().height) / 2;
				graphics.drawImage(image, x, y);
			}
		};
		//
		figure.setOpaque(true);
		figure.setBackgroundColor(COLOR_NORMAL);
		figure.setFont(DEFAULT_FONT);
		return figure;
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		int index = getIndex();
		Interval interval = m_layout.getGridInfo().getColumnIntervals()[index];
		Rectangle bounds =
				new Rectangle(interval.begin(),
						0,
						interval.length()+ 1,
						((GraphicalEditPart) getParent()).getFigure().getSize().height);
		bounds.performTranslate(getOffset().x, 0);
		getFigure().setBounds(bounds);
	}

	@Override
	public int getIndex() {
		return m_layout.getColumns().indexOf(m_column);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHeaderMenuProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void buildContextMenu(IMenuManager manager) {
		// operations
		{
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_insertColumn) {
				@Override
				protected void run(ColumnInfo column) throws Exception {
					m_layout.getColumnOperations().insert(column.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_appendColumn) {
				@Override
				protected void run(ColumnInfo column) throws Exception {
					m_layout.getColumnOperations().insert(column.getIndex() + 1);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_deleteColumn,
					CoreImages.ALIGNMENT_H_MENU_DELETE) {
				@Override
				protected void run(ColumnInfo column) throws Exception {
					m_layout.getColumnOperations().delete(column.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_deleteContents) {
				@Override
				protected void run(ColumnInfo column) throws Exception {
					m_layout.getColumnOperations().clear(column.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_splitColumn) {
				@Override
				protected void run(ColumnInfo column) throws Exception {
					m_layout.getColumnOperations().split(column.getIndex());
				}
			});
		}
		// alignment
		{
			manager.add(new Separator());
			manager.add(new SetAlignmentColumnAction(this,
					GefMessages.ColumnHeaderEditPart_haLeft,
					CoreImages.ALIGNMENT_H_MENU_LEFT,
					ColumnInfo.Alignment.LEFT));
			manager.add(new SetAlignmentColumnAction(this,
					GefMessages.ColumnHeaderEditPart_haCenter,
					CoreImages.ALIGNMENT_H_MENU_CENTER,
					ColumnInfo.Alignment.CENTER));
			manager.add(new SetAlignmentColumnAction(this,
					GefMessages.ColumnHeaderEditPart_haRight,
					CoreImages.ALIGNMENT_H_MENU_RIGHT,
					ColumnInfo.Alignment.RIGHT));
			manager.add(new SetAlignmentColumnAction(this,
					GefMessages.ColumnHeaderEditPart_haFill,
					CoreImages.ALIGNMENT_H_MENU_FILL,
					ColumnInfo.Alignment.FILL));
		}
		// grow
		{
			manager.add(new Separator());
			manager.add(new SetGrowAction<>(this,
					GefMessages.ColumnHeaderEditPart_grow,
					CoreImages.ALIGNMENT_H_MENU_GROW));
		}
		// properties
		{
			manager.add(new Separator());
			manager.add(new Action(GefMessages.ColumnHeaderEditPart_properties) {
				@Override
				public void run() {
					editDimension();
				}
			});
		}
	}

	@Override
	protected void editDimension() {
		new ColumnEditDialog(DesignerPlugin.getShell(), m_layout, m_column).open();
	}
}
