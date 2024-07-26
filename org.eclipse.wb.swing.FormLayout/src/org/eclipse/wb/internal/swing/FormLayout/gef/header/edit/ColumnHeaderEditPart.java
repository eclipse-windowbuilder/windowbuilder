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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.edit;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.DimensionHeaderAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetAlignmentAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.actions.SetGrowAction;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.ColumnEditDialog;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.layout.ColumnSpec;

import java.util.List;

/**
 * {@link EditPart} for {@link FormColumnInfo} header of {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public class ColumnHeaderEditPart extends DimensionHeaderEditPart<FormColumnInfo> {
	private final FormColumnInfo m_column;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnHeaderEditPart(FormLayoutInfo layout, FormColumnInfo column, Figure containerFigure) {
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
				graphics.setForegroundColor(ColorConstants.buttonDarker);
				graphics.drawLine(r.x, r.y, r.x, r.bottom());
				graphics.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
				// draw column index
				int titleLeft;
				int titleRight;
				{
					String title = "" + (1 + getIndex());
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
					Image image;
					if (m_column.getAlignment() == ColumnSpec.LEFT) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_LEFT);
					} else if (m_column.getAlignment() == ColumnSpec.RIGHT) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_RIGHT);
					} else if (m_column.getAlignment() == ColumnSpec.CENTER) {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_CENTER);
					} else {
						image = getViewer().getResourceManager().get(CoreImages.ALIGNMENT_H_SMALL_FILL);
					}
					//
					int x = r.x + 2;
					drawCentered(graphics, image, x);
				}
				// draw grow indicator
				if (m_column.hasGrow()) {
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
		newFigure.setFont(DEFAULT_FONT);
		newFigure.setOpaque(true);
		return newFigure;
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		int index = getIndex();
		Interval interval = m_layout.getGridInfo().getColumnIntervals()[index];
		Rectangle bounds =
				new Rectangle(interval.begin(),
						0,
						interval.length() + 1,
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
		if (!m_layout.canChangeDimensions()) {
			return;
		}
		// operations
		{
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_insertColumn) {
				@Override
				protected void run(FormColumnInfo dimension) throws Exception {
					int index = m_layout.getColumns().indexOf(dimension);
					m_layout.insertColumn(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_appendColumn) {
				@Override
				protected void run(FormColumnInfo dimension) throws Exception {
					int index = m_layout.getColumns().indexOf(dimension);
					m_layout.insertColumn(index + 1);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_deleteColumn) {
				@Override
				protected void run(FormColumnInfo dimension) throws Exception {
					int index = m_layout.getColumns().indexOf(dimension);
					m_layout.deleteColumn(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_DeleteContents) {
				@Override
				protected void run(FormColumnInfo dimension) throws Exception {
					int index = m_layout.getColumns().indexOf(dimension);
					m_layout.deleteColumnContents(index);
				}
			});
			manager.add(new DimensionHeaderAction<>(this,
					GefMessages.ColumnHeaderEditPart_splitColumn) {
				@Override
				protected void run(FormColumnInfo dimension) throws Exception {
					int index = m_layout.getColumns().indexOf(dimension);
					m_layout.splitColumn(index);
				}
			});
		}
		// alignment
		{
			manager.add(new Separator());
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.ColumnHeaderEditPart_haLeft,
					CoreImages.ALIGNMENT_H_MENU_LEFT,
					ColumnSpec.LEFT));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.ColumnHeaderEditPart_haFill,
					CoreImages.ALIGNMENT_H_MENU_FILL,
					ColumnSpec.FILL));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.ColumnHeaderEditPart_haCenter,
					CoreImages.ALIGNMENT_H_MENU_CENTER,
					ColumnSpec.CENTER));
			manager.add(new SetAlignmentAction<>(this,
					GefMessages.ColumnHeaderEditPart_haRight,
					CoreImages.ALIGNMENT_H_MENU_RIGHT,
					ColumnSpec.RIGHT));
		}
		// grow
		{
			manager.add(new Separator());
			manager.add(new SetGrowAction<>(this,
					GefMessages.ColumnHeaderEditPart_grow,
					CoreImages.ALIGNMENT_H_MENU_GROW));
		}
		// templates
		{
			manager.add(new Separator());
			addTemplateActions(manager, m_dimension.getTemplates(true));
			{
				IMenuManager otherManager =
						new MenuManager(GefMessages.ColumnHeaderEditPart_otherTemplates);
				manager.add(otherManager);
				addTemplateActions(otherManager, m_dimension.getTemplates(false));
			}
		}
		// group
		{
			manager.add(new Separator());
			{
				DimensionHeaderAction<FormColumnInfo> action =
						new DimensionHeaderAction<>(this, GefMessages.ColumnHeaderEditPart_group) {
					@Override
					protected void run(List<FormColumnInfo> dimensions) throws Exception {
						m_layout.groupColumns(dimensions);
					}
				};
				action.setEnabled(getViewer().getSelectedEditParts().size() >= 2);
				manager.add(action);
			}
			{
				DimensionHeaderAction<FormColumnInfo> action =
						new DimensionHeaderAction<>(this,
								GefMessages.ColumnHeaderEditPart_unGroup) {
					@Override
					protected void run(List<FormColumnInfo> dimensions) throws Exception {
						m_layout.unGroupColumns(dimensions);
					}
				};
				manager.add(action);
				// check if there is grouped dimension selected
				boolean hasGroup = false;
				for (EditPart editPart : getViewer().getSelectedEditParts()) {
					ColumnHeaderEditPart headerEditPart = (ColumnHeaderEditPart) editPart;
					if (m_layout.getColumnGroup(headerEditPart.m_column) != null) {
						hasGroup = true;
						break;
					}
				}
				// enable action
				action.setEnabled(hasGroup);
			}
		}
		// properties
		{
			manager.add(new Separator());
			manager.add(new ObjectInfoAction(m_layout, GefMessages.ColumnHeaderEditPart_properties) {
				@Override
				protected void runEx() throws Exception {
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
