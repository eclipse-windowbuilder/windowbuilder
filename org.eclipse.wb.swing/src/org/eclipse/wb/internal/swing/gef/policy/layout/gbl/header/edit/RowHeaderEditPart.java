/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetAlignmentRowAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo.Alignment;
import org.eclipse.wb.internal.swing.model.layout.gbl.ui.RowEditDialog;
import org.eclipse.wb.swing.SwingImages;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link RowInfo} header of {@link AbstractGridBagLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class RowHeaderEditPart extends DimensionHeaderEditPart<RowInfo> {
	private final RowInfo m_row;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowHeaderEditPart(AbstractGridBagLayoutInfo layout, RowInfo row, IFigure containerFigure) {
		super(layout, row, containerFigure);
		m_row = row;
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
				graphics.drawLine(r.x, r.y, r.right(), r.y);
				graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
				// draw row index
				int titleTop;
				int titleBottom;
				{
					String title = "" + getIndex();
					Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(title, graphics.getFont());
					if (r.height < textExtents.height) {
						return;
					}
					titleTop = r.y + (r.height - textExtents.height) / 2;
					titleBottom = titleTop + textExtents.height;
					int x = r.x + (r.width - textExtents.width) / 2;
					graphics.setForegroundColor(ColorConstants.black);
					graphics.drawText(title, x, titleTop);
				}
				// draw alignment indicator
				if (titleTop - r.y > 3 + 7 + 3) {
					Image image = null;
					Alignment alignment = m_dimension.getAlignment();
					if (alignment == RowInfo.Alignment.TOP) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_V_SMALL_TOP);
					} else if (alignment == RowInfo.Alignment.BOTTOM) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_V_SMALL_BOTTOM);
					} else if (alignment == RowInfo.Alignment.CENTER) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_V_SMALL_CENTER);
					} else if (alignment == RowInfo.Alignment.FILL) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_V_SMALL_FILL);
					} else if (alignment == RowInfo.Alignment.BASELINE) {
						image = getViewer().getResourceManager().get(SwingImages.ALIGNMENT_V_SMALL_BASELINE);
					} else if (alignment == RowInfo.Alignment.BASELINE_ABOVE) {
						image = getViewer().getResourceManager().get(SwingImages.ALIGNMENT_V_SMALL_BASELINE_ABOVE);
					} else if (alignment == RowInfo.Alignment.BASELINE_BELOW) {
						image = getViewer().getResourceManager().get(SwingImages.ALIGNMENT_V_SMALL_BASELINE_BELOW);
					}
					if (image != null) {
						int y = r.y + 2;
						drawCentered(graphics, image, y);
					}
				}
				// draw grow indicator
				if (m_dimension.hasWeight()) {
					if (titleBottom + 3 + 7 + 3 < r.bottom()) {
						Image image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_V_SMALL_GROW);
						drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
					}
				}
			}

			private void drawCentered(Graphics graphics, Image image, int y) {
				int x = (getBounds().width - image.getBounds().width) / 2;
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
		Interval interval = m_layout.getGridInfo().getRowIntervals()[index];
		Rectangle bounds =
				new Rectangle(0,
						interval.begin(),
						((GraphicalEditPart) getParent()).getFigure().getSize().width,
						interval.length()+ 1);
		bounds.performTranslate(0, getOffset().y);
		getFigure().setBounds(bounds);
	}

	@Override
	public int getIndex() {
		return m_layout.getRows().indexOf(m_row);
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
			manager.add(new DimensionHeaderAction<>(this, GefMessages.RowHeaderEditPart_insertRow) {
				@Override
				protected void run(RowInfo row) throws Exception {
					m_layout.getRowOperations().insert(row.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this, GefMessages.RowHeaderEditPart_appendRow) {
				@Override
				protected void run(RowInfo row) throws Exception {
					m_layout.getRowOperations().insert(row.getIndex() + 1);
				}
			});
			manager.add(new DimensionHeaderAction<>(this, GefMessages.RowHeaderEditPart_deleteRow,
					CoreImages.ALIGNMENT_V_MENU_DELETE) {
				@Override
				protected void run(RowInfo row) throws Exception {
					m_layout.getRowOperations().delete(row.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.RowHeaderEditPart_deleteContents) {
				@Override
				protected void run(RowInfo row) throws Exception {
					m_layout.getRowOperations().clear(row.getIndex());
				}
			});
			manager.add(new DimensionHeaderAction<>(this, GefMessages.RowHeaderEditPart_splitRow) {
				@Override
				protected void run(RowInfo row) throws Exception {
					m_layout.getRowOperations().split(row.getIndex());
				}
			});
		}
		// alignment
		{
			manager.add(new Separator());
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaTop,
					CoreImages.ALIGNMENT_V_MENU_TOP,
					RowInfo.Alignment.TOP));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaCenter,
					CoreImages.ALIGNMENT_V_MENU_CENTER,
					RowInfo.Alignment.CENTER));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaBottom,
					CoreImages.ALIGNMENT_V_MENU_BOTTOM,
					RowInfo.Alignment.BOTTOM));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaFill,
					CoreImages.ALIGNMENT_V_MENU_FILL,
					RowInfo.Alignment.FILL));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaBaseline,
					SwingImages.ALIGNMENT_V_MENU_BASELINE,
					RowInfo.Alignment.BASELINE));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaAboveBaseline,
					SwingImages.ALIGNMENT_V_MENU_BASELINE_ABOVE,
					RowInfo.Alignment.BASELINE_ABOVE));
			manager.add(new SetAlignmentRowAction(this,
					GefMessages.RowHeaderEditPart_vaBelowBaseline,
					SwingImages.ALIGNMENT_V_MENU_BASELINE_BELOW,
					RowInfo.Alignment.BASELINE_BELOW));
		}
		// grow
		{
			manager.add(new Separator());
			manager.add(new SetGrowAction<>(this,
					GefMessages.RowHeaderEditPart_grow,
					CoreImages.ALIGNMENT_V_MENU_GROW));
		}
		// properties
		{
			manager.add(new Separator());
			manager.add(new Action(GefMessages.RowHeaderEditPart_properties) {
				@Override
				public void run() {
					editDimension();
				}
			});
		}
	}

	@Override
	protected void editDimension() {
		new RowEditDialog(DesignerPlugin.getShell(), m_layout, m_row).open();
	}
}
