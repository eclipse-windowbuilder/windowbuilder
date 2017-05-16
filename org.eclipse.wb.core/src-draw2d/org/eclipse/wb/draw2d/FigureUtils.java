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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.RootFigure;

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
  public static final void translateFigureToFigure(Figure source,
      Figure target,
      Translatable translatable) {
    translateFigureToAbsolute(source, translatable);
    translateAbsoluteToFigure(target, translatable);
  }

  /**
   * Translates given {@link Translatable} from <code>source</code> {@link Figure} local coordinates
   * to local coordinates from <code>target</code> {@link Figure}.
   */
  public static final void translateFigureToFigure2(Figure source,
      Figure target,
      Translatable translatable) {
    translateFigureToAbsolute2(source, translatable);
    translateAbsoluteToFigure2(target, translatable);
  }

  /**
   * Translates given {@link Translatable} from this {@link Figure} bounds coordinates to absolute (
   * {@link RootFigure} relative) coordinates.
   */
  public static final void translateFigureToAbsolute(Figure figure, Translatable translatable) {
    for (Figure parent = figure.getParent(); parent != null; parent = parent.getParent()) {
      translatable.translate(parent.getInsets());
      translatable.translate(parent.getLocation());
    }
  }

  /**
   * Translates given {@link Translatable} from this {@link Figure} local coordinates to absolute (
   * {@link RootFigure} relative) coordinates.
   */
  public static final void translateFigureToAbsolute2(Figure figure, Translatable translatable) {
    for (; figure != null; figure = figure.getParent()) {
      translatable.translate(figure.getInsets());
      translatable.translate(figure.getLocation());
    }
  }

  /**
   * Translates given {@link Translatable} from this {@link Figure} local coordinates to absolute (
   * {@link RootFigure} relative) coordinates.
   */
  public static final void translateFigureToCanvas(Figure figure, Translatable translatable) {
    translateFigureToAbsolute2(figure, translatable);
    FigureCanvas figureCanvas = figure.getFigureCanvas();
    translatable.translate(
        -figureCanvas.getHorizontalScrollModel().getSelection(),
        -figureCanvas.getVerticalScrollModel().getSelection());
  }

  /**
   * Translates given {@link Translatable} from this absolute ({@link RootFigure} relative)
   * coordinates to bounds {@link Figure} coordinates.
   */
  public static final void translateAbsoluteToFigure(Figure figure, Translatable translatable) {
    for (Figure parent = figure.getParent(); parent != null; parent = parent.getParent()) {
      translatable.translate(parent.getLocation().negate());
      translatable.translate(parent.getInsets().getNegated());
    }
  }

  /**
   * Translates given {@link Translatable} from this absolute ({@link RootFigure} relative)
   * coordinates to local {@link Figure} coordinates.
   */
  public static final void translateAbsoluteToFigure2(Figure figure, Translatable translatable) {
    for (; figure != null; figure = figure.getParent()) {
      translatable.translate(figure.getLocation().negate());
      translatable.translate(figure.getInsets().getNegated());
    }
  }

  /**
   * @return the bounds of given {@link Figure} on screen.
   */
  public static Rectangle getScreenBounds(Figure figure) {
    FigureCanvas figureCanvas = figure.getFigureCanvas();
    Rectangle bounds = figure.getBounds();
    translateFigureToCanvas(figure.getParent(), bounds);
    org.eclipse.swt.graphics.Point location = figureCanvas.toDisplay(bounds.x, bounds.y);
    return new Rectangle(location.x, location.y, bounds.width, bounds.height);
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