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
package org.eclipse.wb.internal.core.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Implementation of LabelProvider with {@link IColorProvider} and {@link IFontProvider} for
 * {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class ObserveDecoratingLabelProvider extends ObserveLabelProvider
    implements
      IColorProvider,
      IFontProvider {
  private final Font m_italicFont;
  private final Font m_boldFont;
  private final Font m_boldItalicFont;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveDecoratingLabelProvider(StructuredViewer viewer) {
    Font baseFont = viewer.getControl().getFont();
    m_boldFont = DrawUtils.getBoldFont(baseFont);
    m_italicFont = DrawUtils.getItalicFont(baseFont);
    m_boldItalicFont = DrawUtils.getBoldItalicFont(baseFont);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    super.dispose();
    m_italicFont.dispose();
    m_boldFont.dispose();
    m_boldItalicFont.dispose();
  }

  private static IObserveDecorator getDecorator(Object element) {
    IObserveDecoration observe = (IObserveDecoration) element;
    return observe.getDecorator();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IColorDecorator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Color getBackground(Object element) {
    return getDecorator(element).getBackground();
  }

  public Color getForeground(Object element) {
    return getDecorator(element).getForeground();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFontDecorator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Font getFont(Object element) {
    return getDecorator(element).getFont(m_italicFont, m_boldFont, m_boldItalicFont);
  }
}