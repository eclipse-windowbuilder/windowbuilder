/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.palette.PaletteEditPartFactory;

import java.util.function.Function;

/**
 * This factory is used to inject our own {@link Border}s into the created
 * palette figure, which provide a 3D look.
 */
public class DesignerPaletteEditPartFactory extends PaletteEditPartFactory {
	@Override
	public EditPart createEditPart(EditPart parentEditPart, Object model) {
		EditPart editPart = super.createEditPart(parentEditPart, model);
		editPart.addEditPartListener(new EditPartListener.Stub() {
			@Override
			public void childAdded(EditPart child, int index) {
				if (child.getModel() instanceof PaletteDrawer) {
					updateFigure(child, DrawerBackground::new);
				} else if (child.getModel() instanceof ToolEntry) {
					updateFigure(child, ToolEntryBackground::new);
				}
			}
		});
		return editPart;
	}

	private static void updateFigure(EditPart editPart, Function<ButtonModel, AbstractBackground> factory) {
		Clickable toggle = editPart.getAdapter(Clickable.class);
		if (toggle != null) {
			ButtonModel toggleModel = toggle.getModel();
			toggle.setBorder(factory.apply(toggleModel));
		}
	}

	private static class ToolEntryBackground extends AbstractBackground {
		private final ButtonModel buttonModel;

		public ToolEntryBackground(ButtonModel buttonModel) {
			this.buttonModel = buttonModel;
		}

		@Override
		public void paintBackground(IFigure figure, Graphics g, Insets insets) {
			if (buttonModel.isMouseOver() || buttonModel.isSelected()) {
				Rectangle r = Rectangle.SINGLETON;
				r.setBounds(figure.getBounds()).shrink(insets);
				if (buttonModel.isSelected()) {
					g.setBackgroundColor(DesignerColorProvider.COLOR_ENTRY_SELECTED);
					g.fillRectangle(r);
				}
				drawRectangle3D(g, r, !buttonModel.isSelected());
			}
		}
	}

	private static class DrawerBackground extends AbstractBackground {
		private final ButtonModel buttonModel;

		public DrawerBackground(ButtonModel buttonModel) {
			this.buttonModel = buttonModel;
		}

		@Override
		public void paintBackground(IFigure figure, Graphics g, Insets insets) {
			Rectangle r = Rectangle.SINGLETON;
			r.setBounds(figure.getBounds()).shrink(insets);
			if (buttonModel.isMouseOver()) {
				g.setForegroundColor(DesignerColorProvider.COLOR_DRAWER_GRAD_END);
				g.setBackgroundColor(DesignerColorProvider.COLOR_DRAWER_GRAD_BEGIN);
			} else {
				g.setForegroundColor(DesignerColorProvider.COLOR_DRAWER_GRAD_BEGIN);
				g.setBackgroundColor(DesignerColorProvider.COLOR_DRAWER_GRAD_END);
			}
			g.fillGradient(r, true);
			drawRectangle3D(g, r, !buttonModel.isPressed());
		}
	}

	/**
	 * Draws 3D highlight rectangle.
	 */
	private static void drawRectangle3D(Graphics g, Rectangle r, boolean up) {
		int x = r.x;
		int y = r.y;
		int right = r.right() - 1;
		int bottom = r.bottom() - 1;
		//
		if (up) {
			g.setForegroundColor(ColorConstants.buttonLightest);
		} else {
			g.setForegroundColor(ColorConstants.buttonDarker);
		}
		g.drawLine(x, y, right, y);
		g.drawLine(x, y, x, bottom);
		//
		if (up) {
			g.setForegroundColor(ColorConstants.buttonDarker);
		} else {
			g.setForegroundColor(ColorConstants.buttonLightest);
		}
		g.drawLine(right, y, right, bottom);
		g.drawLine(x, bottom, right, bottom);
	}
}
