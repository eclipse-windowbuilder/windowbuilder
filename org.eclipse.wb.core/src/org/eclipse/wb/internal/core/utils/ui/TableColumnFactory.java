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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Helper for convenient creation/modification of {@link TableColumn}.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public final class TableColumnFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TableColumnFactory} for new {@link TableColumn} of given {@link Table}.
   */
  public static TableColumnFactory create(Table table) {
    return create(table, SWT.NONE);
  }

  /**
   * @return the {@link TableColumnFactory} for new {@link TableColumn} of given {@link Table}.
   */
  public static TableColumnFactory create(Table table, int style) {
    return new TableColumnFactory(new TableColumn(table, style));
  }

  /**
   * @return the {@link TableColumnFactory} for modifying given {@link TableColumn}.
   */
  public static TableColumnFactory create(TableColumn column) {
    return new TableColumnFactory(column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final TableColumn m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  private TableColumnFactory(TableColumn column) {
    m_column = column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the <code>image</code> property.
   */
  public TableColumnFactory image(Image image) {
    m_column.setImage(image);
    return this;
  }

  /**
   * Sets the <code>text</code> property.
   */
  public TableColumnFactory text(String text) {
    m_column.setText(text);
    return this;
  }

  /**
   * Sets the <code>width</code> property.
   */
  public TableColumnFactory width(int pixels) {
    m_column.setWidth(pixels);
    return this;
  }

  /**
   * Sets the <code>width</code> property in characters.
   */
  public TableColumnFactory widthC(int chars) {
    PixelConverter converter = new PixelConverter(m_column.getParent());
    return width(converter.convertWidthInCharsToPixels(chars));
  }

  /**
   * Invokes {@link TableColumn#pack()} method.
   */
  public TableColumnFactory pack() {
    m_column.pack();
    return this;
  }
}
