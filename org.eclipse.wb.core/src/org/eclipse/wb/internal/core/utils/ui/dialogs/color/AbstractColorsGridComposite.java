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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract {@link Composite} for color selection.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractColorsGridComposite extends AbstractColorsComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractColorsGridComposite(Composite parent, int style, AbstractColorDialog colorDialog) {
    super(parent, style, colorDialog);
    //
    setLayout(new GridLayout());
    createColorHint();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Color hint
  //
  ////////////////////////////////////////////////////////////////////////////
  private ColorPreviewCanvas m_colorHintCanvas;

  /**
   * Creates {@link ColorPreviewCanvas} for hovered {@link ColorInfo}.
   */
  private void createColorHint() {
    Group group = new Group(this, SWT.NONE);
    group.setText(Messages.AbstractColorsGridComposite_colorHintGroup);
    group.setLayout(new FillLayout());
    group.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    //
    m_colorHintCanvas = new ColorPreviewCanvas(group, SWT.NONE, showShortTextInColorPreview());
  }

  /**
   * Overridden to return <code>true</code> if the created {@link ColorPreviewCanvas} should have
   * the color shown with the short-text version.
   */
  protected boolean showShortTextInColorPreview() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Color grids
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ColorsGridComposite> m_colorsGrids = Lists.newArrayList();

  /**
   * Adds new {@link Group} with given title and fills it with {@link ColorsGridComposite} with
   * given colors.
   *
   * @return creates {@link ColorsGridComposite} to allow futher configuring it.
   */
  protected final ColorsGridComposite createColorsGroup(Composite parent,
      String title,
      ColorInfo[] colors) {
    // create group
    Group group = new Group(parent, SWT.NONE);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new FillLayout());
    if (title != null) {
      group.setText(title);
    }
    // create colors grid
    return createColorsGrid(group, colors);
  }

  /**
   * @return the new {@link ColorsGridComposite} created on given parent.
   */
  protected final ColorsGridComposite createColorsGrid(Composite parent, ColorInfo[] colors) {
    ColorsGridComposite colorsGrid = new ColorsGridComposite(parent, SWT.NONE);
    colorsGrid.setColors(colors);
    m_colorsGrids.add(colorsGrid);
    // add listeners
    colorsGrid.addListener(SWT.DefaultSelection, new Listener() {
      public void handleEvent(Event event) {
        ColorInfo colorInfo = (ColorInfo) event.data;
        m_colorHintCanvas.setColor(colorInfo);
      }
    });
    colorsGrid.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        ColorInfo colorInfo = (ColorInfo) event.data;
        m_colorDialog.setResultColor(colorInfo);
      }
    });
    colorsGrid.addListener(SWT.MouseDoubleClick, new Listener() {
      public void handleEvent(Event event) {
        m_colorDialog.closeOk();
      }
    });
    //
    return colorsGrid;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sorting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates group for sorting colors in added {@link ColorsGridComposite}'s.
   */
  protected final Group createSortGroup(Composite parent,
      List<String> titles,
      List<ColorInfoComparator> comparators) {
    Group group = new Group(parent, SWT.NONE);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(titles.size(), false));
    group.setText(Messages.AbstractColorsGridComposite_sortGroup);
    //
    for (int i = 0; i < titles.size(); i++) {
      final String title = titles.get(i);
      final ColorInfoComparator comparator = comparators.get(i);
      // create radio button
      Button button = new Button(group, SWT.RADIO);
      button.setText(title);
      if (i == 0) {
        button.setSelection(true);
      }
      // add listener
      button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          setComparator(comparator);
        }
      });
    }
    //
    return group;
  }

  /**
   * Sorts colors in all added {@link ColorsGridComposite}'s using given comparator.
   */
  protected final void setComparator(ColorInfoComparator comparator) {
    for (ColorsGridComposite colorsGrid : m_colorsGrids) {
      Arrays.sort(colorsGrid.getColors(), comparator);
      colorsGrid.redraw();
    }
  }
}
