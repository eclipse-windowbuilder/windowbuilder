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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.edit;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetAlignmentColumnAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.actions.SetSizeAction;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.ColumnEditDialog;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link MigColumnInfo} header of {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public class ColumnHeaderEditPart extends DimensionHeaderEditPart<MigColumnInfo> {
	private static final String GROW_SMALL_PATH = "alignment/h/small/grow.gif";
	private static final String GROW_MENU_PATH = "alignment/h/menu/grow.gif";
	private static final String PREF_TITLE = "[pref!]";
	private static final String PREF_CODE = "pref!";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final MigColumnInfo m_column;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnHeaderEditPart(MigLayoutInfo layout, MigColumnInfo column, Figure containerFigure) {
		super(layout, column, containerFigure);
		m_column = column;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		Figure newFigure = new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				Rectangle r = getClientArea();
				// draw rectangle
				graphics.setForegroundColor(IColorConstants.buttonDarker);
				graphics.drawLine(r.x, r.y, r.x, r.bottom());
				graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
				// draw column index
				int titleLeft;
				int titleRight;
				{
					int index = getIndex();
					String title = Integer.toString(index);
					Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(title, graphics.getFont());
					if (r.width < 3 + textExtents.width + 3) {
						return;
					}
					// draw title
					titleLeft = r.x + (r.width - textExtents.width) / 2;
					titleRight = titleLeft + textExtents.width;
					int y = r.y + (r.height - textExtents.height) / 2;
					graphics.setForegroundColor(IColorConstants.black);
					graphics.drawText(title, titleLeft, y);
				}
				// draw alignment indicator
				if (titleLeft - r.x > 3 + 7 + 3) {
					Image image = m_column.getAlignment(true).getSmallImageDescriptor().createImage();
					int x = r.x + 2;
					drawCentered(graphics, image, x);
					image.dispose();
				}
				// draw grow indicator
				if (m_column.hasGrow()) {
					if (titleRight + 3 + 7 + 3 < r.right()) {
						Image image = getImage(GROW_SMALL_PATH);
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
		newFigure.setFont(DEFAULT_FONT);
		newFigure.setOpaque(true);
		return newFigure;
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		Figure figure = getFigure();
		// bounds
		{
			int index = getIndex();
			Interval interval = m_layout.getGridInfo().getColumnIntervals()[index];
			Rectangle bounds =
					new Rectangle(interval.begin(),
							0,
							interval.length()+ 1,
							((GraphicalEditPart) getParent()).getFigure().getSize().height);
			bounds.performTranslate(getOffset().x, 0);
			figure.setBounds(bounds);
		}
		// tooltip
		figure.setToolTipText(m_column.getTooltip());
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
		if (!m_layout.canChangeDimensions()) {
			return;
		}
		// operations
		{
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_insertColumn) {
				@Override
				protected void run(MigColumnInfo dimension, int index) throws Exception {
					m_layout.insertColumn(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_appendColumn) {
				@Override
				protected void run(MigColumnInfo dimension, int index) throws Exception {
					m_layout.insertColumn(index + 1);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_deleteColumn) {
				@Override
				protected void run(MigColumnInfo dimension, int index) throws Exception {
					m_layout.deleteColumn(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_clearColumn) {
				@Override
				protected void run(MigColumnInfo dimension, int index) throws Exception {
					m_layout.clearColumn(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_splitColumn) {
				@Override
				protected void run(MigColumnInfo dimension, int index) throws Exception {
					m_layout.splitColumn(index);
				}
			});
		}
		// alignment
		{
			manager.add(new Separator());
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.DEFAULT));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.LEFT));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.CENTER));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.RIGHT));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.FILL));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.LEADING));
			manager.add(new SetAlignmentColumnAction(this, MigColumnInfo.Alignment.TRAILING));
		}
		// grow
		{
			manager.add(new Separator());
			manager.add(new SetGrowAction<>(this,
					GefMessages.ColumnHeaderEditPart_grow,
					Activator.getImageDescriptor(GROW_MENU_PATH)));
		}
		// size
		{
			manager.add(new Separator());
			manager.add(new SetSizeAction<>(this,
					GefMessages.ColumnHeaderEditPart_defaultSize,
					null));
			manager.add(new SetSizeAction<>(this, PREF_TITLE, PREF_CODE));
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
