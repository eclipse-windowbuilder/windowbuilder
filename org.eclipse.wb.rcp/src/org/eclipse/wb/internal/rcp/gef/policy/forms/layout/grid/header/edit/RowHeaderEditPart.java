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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.rcp.gef.GefMessages;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions.SetAlignmentAction;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions.SetGrabAction;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapLayoutInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapRowInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * {@link EditPart} for {@link TableWrapRowInfo} header of {@link ITableWrapLayout_Info<C>}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class RowHeaderEditPart<C extends IControlInfo> extends DimensionHeaderEditPart<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowHeaderEditPart(ITableWrapLayoutInfo<C> layout,
			TableWrapRowInfo<C> row,
			IFigure containerFigure) {
		super(layout, row, containerFigure);
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
				graphics.setForegroundColor(ColorConstants.buttonDarker);
				graphics.drawLine(r.x, r.y, r.right(), r.y);
				graphics.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
				// draw row index
				int titleTop;
				int titleBottom;
				{
					String title = "" + (1 + m_dimension.getIndex());
					Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(title, graphics.getFont());
					if (r.height < 3 + textExtents.height + 3) {
						return;
					}
					// draw title
					titleTop = r.y + (r.height - textExtents.height) / 2;
					titleBottom = titleTop + textExtents.height;
					int x = r.x + (r.width - textExtents.width) / 2;
					graphics.setForegroundColor(ColorConstants.black);
					graphics.drawText(title, x, titleTop);
				}
				//
				try {
					// draw alignment indicator
					{
						Integer alignmentValue = m_dimension.getAlignment();
						if (alignmentValue != null && titleTop - r.y > 3 + 7 + 3) {
							int alignment = alignmentValue.intValue();
							Image image;
							if (alignment == TableWrapData.TOP) {
								image = getViewer().getResourceManager().create(CoreImages.ALIGNMENT_V_SMALL_TOP);
							} else if (alignment == TableWrapData.MIDDLE) {
								image = getViewer().getResourceManager().create(CoreImages.ALIGNMENT_V_SMALL_CENTER);
							} else if (alignment == TableWrapData.BOTTOM) {
								image = getViewer().getResourceManager().create(CoreImages.ALIGNMENT_V_SMALL_BOTTOM);
							} else {
								image = getViewer().getResourceManager().create(CoreImages.ALIGNMENT_V_SMALL_FILL);
							}
							//
							int y = r.y + 2;
							drawCentered(graphics, image, y);
						}
					}
					// draw grow indicator
					if (m_dimension.getGrab()) {
						if (titleBottom + 3 + 7 + 3 < r.bottom()) {
							Image image = getViewer().getResourceManager().create(CoreImages.ALIGNMENT_V_SMALL_GROW);
							drawCentered(graphics, image, r.bottom() - 3 - image.getBounds().height);
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			private void drawCentered(Graphics graphics, Image image, int y) {
				int x = (getBounds().width - image.getBounds().width) / 2;
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
		// prepare column interval
		Interval interval;
		{
			int index = m_dimension.getIndex();
			IGridInfo gridInfo = m_layout.getGridInfo();
			interval = gridInfo.getRowIntervals()[index];
		}
		// prepare bounds
		Rectangle bounds;
		{
			bounds =
					new Rectangle(0,
							interval.begin(),
							((GraphicalEditPart) getParent()).getFigure().getSize().width,
							interval.length()+ 1);
			bounds.performTranslate(0, getOffset().y);
		}
		// set bounds
		getFigure().setBounds(bounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHeaderMenuProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void buildContextMenu(IMenuManager manager) {
		// grab
		{
			manager.add(new SetGrabAction<>(this,
					GefMessages.RowHeaderEditPart_actionGrab,
					CoreImages.ALIGNMENT_V_MENU_GROW));
		}
		// alignment
		{
			manager.add(new Separator());
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.RowHeaderEditPart_alignmentTop,
					CoreImages.ALIGNMENT_V_MENU_TOP,
					TableWrapData.TOP));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.RowHeaderEditPart_alignmentCenter,
					CoreImages.ALIGNMENT_V_MENU_CENTER,
					TableWrapData.MIDDLE));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.RowHeaderEditPart_alignmentBottom,
					CoreImages.ALIGNMENT_V_MENU_BOTTOM,
					TableWrapData.BOTTOM));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.RowHeaderEditPart_alignmentFill,
					CoreImages.ALIGNMENT_V_MENU_FILL,
					TableWrapData.FILL));
		}
		// operations
		{
			manager.add(new Separator());
			manager.add(new DimensionHeaderAction<>(this, GefMessages.RowHeaderEditPart_actionDelete,
					CoreImages.ALIGNMENT_V_MENU_DELETE) {
				@Override
				protected void run(TableWrapDimensionInfo<C> dimension) throws Exception {
					m_layout.command_deleteRow(dimension.getIndex(), true);
				}
			});
		}
	}
}
