/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.RootFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Collection of utilities for {@link Figure}'s.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Manipulations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes given {@link Figure} from its parent.
	 */
	public static void removeFigure(Figure figure) {
		if (figure != null && figure.getParent() != null) {
			figure.getParent().remove(figure);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Coordinates
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Translates given {@link Translatable} from <code>source</code> {@link Figure} bounds
	 * coordinates to bounds coordinates from <code>target</code> {@link Figure}.
	 */
	public static final void translateFigureToFigure(IFigure source, IFigure target,
			Translatable translatable) {
		translateFigureToAbsolute(source, translatable);
		translateAbsoluteToFigure(target, translatable);
	}

	/**
	 * Translates given {@link Translatable} from <code>source</code> {@link IFigure} local coordinates
	 * to local coordinates from <code>target</code> {@link IFigure}.
	 */
	public static final void translateFigureToFigure2(IFigure source,
			IFigure target,
			Translatable translatable) {
		translateFigureToAbsolute2(source, translatable);
		translateAbsoluteToFigure2(target, translatable);
	}

	/**
	 * Translates given {@link Translatable} from this {@link Figure} bounds coordinates to absolute (
	 * {@link RootFigure} relative) coordinates.
	 */
	public static final void translateFigureToAbsolute(IFigure figure, Translatable translatable) {
		for (IFigure parent = figure.getParent(); parent != null; parent = parent.getParent()) {
			translatable.performTranslate(parent.getInsets());
			translatable.performTranslate(parent.getLocation());
		}
	}

	/**
	 * Translates given {@link Translatable} from this {@link IFigure} local coordinates to absolute (
	 * {@link RootFigure} relative) coordinates.
	 */
	public static final void translateFigureToAbsolute2(IFigure figure, Translatable translatable) {
		for (; figure != null; figure = figure.getParent()) {
			translatable.performTranslate(figure.getInsets());
			translatable.performTranslate(figure.getLocation());
		}
	}

	/**
	 * Translates given {@link Translatable} from this {@link Figure} local coordinates to absolute (
	 * {@link RootFigure} relative) coordinates.
	 */
	public static final void translateFigureToCanvas(Figure figure, Translatable translatable) {
		translateFigureToAbsolute2(figure, translatable);
		FigureCanvas figureCanvas = figure.getFigureCanvas();
		translatable.performTranslate(
				-figureCanvas.getViewport().getHorizontalRangeModel().getValue(),
				-figureCanvas.getViewport().getVerticalRangeModel().getValue());
	}

	/**
	 * Translates given {@link Translatable} from this absolute ({@link RootFigure}
	 * relative) coordinates to bounds {@link IFigure} coordinates.
	 */
	public static final void translateAbsoluteToFigure(IFigure figure, Translatable translatable) {
		for (IFigure parent = figure.getParent(); parent != null; parent = parent.getParent()) {
			translatable.performTranslate(parent.getLocation().negate());
			translatable.performTranslate(parent.getInsets().getNegated());
		}
	}

	/**
	 * Translates given {@link Translatable} from this absolute ({@link RootFigure} relative)
	 * coordinates to local {@link IFigure} coordinates.
	 */
	public static final void translateAbsoluteToFigure2(IFigure figure, Translatable translatable) {
		for (; figure != null; figure = figure.getParent()) {
			translatable.performTranslate(figure.getLocation().negate());
			translatable.performTranslate(figure.getInsets().getNegated());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Dimension} of text for given {@link Font}.
	 */
	public static Dimension calculateTextSize(String text, Font font) {
		GC gc = createGC();
		try {
			gc.setFont(font);
			org.eclipse.swt.graphics.Point size = gc.textExtent(text);
			return new Dimension(size);
		} finally {
			gc.dispose();
		}
	}

	/**
	 * @return the new {@link GC} for active {@link Shell}.
	 */
	public static GC createGC() {
		// prepare Display
		Display display = Display.getCurrent();
		if (display == null) {
			return new GC(Display.getDefault());
		}
		// prepare Shell
		Shell shell = display.getActiveShell();
		if (shell == null) {
			return new GC(display);
		}
		// create GC
		return new GC(shell);
	}
}