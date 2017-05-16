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
package org.eclipse.wb.internal.core.editor.multi;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.core.editor.IEditorPageFactory;
import org.eclipse.wb.core.editor.IMultiMode;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * The mode for presentation source/design parts of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class MultiMode implements IMultiMode {
  protected final DesignerEditor m_editor;
  protected final SourcePage m_sourcePage;
  protected final DesignPage m_designPage;
  protected final List<IEditorPage> m_additionalPages = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiMode(DesignerEditor editor) {
    m_editor = editor;
    m_sourcePage = new SourcePage(m_editor);
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
   * @return <code>true</code> if "Source" page is active.
   */
  public abstract boolean isSourceActive();

  /**
   * @return <code>true</code> if "Design" page is active.
   */
  public abstract boolean isDesignActive();

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
    // create additional pages
    List<IEditorPageFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IEditorPageFactory.class,
            "org.eclipse.wb.core.designPageFactories",
            "factory");
    for (IEditorPageFactory factory : factories) {
      factory.createPages(m_editor, m_additionalPages);
    }
    // initialize create additional pages
    for (IEditorPage page : m_additionalPages) {
      page.initialize(m_editor);
    }
  }

  /**
   * {@link DesignerEditor} was fully created and activated. This is good time to show default page,
   * move focus, etc.
   */
  abstract void editorActivatedFirstTime();

  /**
   * Asks to take focus within the workbench.
   */
  abstract void setFocus();

  /**
   * Disposes this {@link MultiMode}.
   */
  void dispose() {
    m_sourcePage.dispose();
    m_designPage.dispose();
    for (IEditorPage page : m_additionalPages) {
      page.dispose();
    }
  }

  /**
   * Notifies that {@link DesignerEditor} has now new input, so new {@link ICompilationUnit} to
   * parse.
   */
  public abstract void onSetInput();

  /**
   * Notifies that {@link DesignerEditor} was saved.
   */
  public void afterSave() {
  }
}