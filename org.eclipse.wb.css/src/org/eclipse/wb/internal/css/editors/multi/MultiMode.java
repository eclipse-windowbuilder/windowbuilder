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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.swt.widgets.Composite;

/**
 * The mode for presentation source/design parts of {@link MultiPageEditor}.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
public abstract class MultiMode {
  protected final MultiPageEditor m_editor;
  protected final SourcePage m_sourcePage;
  protected final DesignPage m_designPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiMode(MultiPageEditor editor) {
    m_editor = editor;
    m_sourcePage = new SourcePage();
    m_designPage = new DesignPage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link SourcePage}.
   */
  public final SourcePage getSourcePage() {
    return m_sourcePage;
  }

  /**
   * @return the {@link DesignPage}.
   */
  public final DesignPage getDesignPage() {
    return m_designPage;
  }

  /**
   * Activates "Source" page of editor.
   */
  public abstract void showSource();

  /**
   * Activates "Design" page of editor.
   */
  public abstract void showDesign();

  /**
   * Switches between "Source" and "Design" pages.
   */
  public abstract void switchSourceDesign();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates GUI for this mode.
   */
  void create(Composite parent) {
    m_sourcePage.initialize(m_editor);
    m_designPage.initialize(m_editor);
  }

  /**
   * {@link MultiPageEditor} was fully created and activated. This is good time to show default
   * page, move focus, etc.
   */
  abstract void editorActivatedFirstTime();

  /**
   * Asks to take focus within the workbench.
   */
  abstract void setFocus();

  /**
   * Disposes this {@link MultiMode}.
   */
  final void dispose() {
    m_sourcePage.dispose();
    m_designPage.dispose();
  }
}