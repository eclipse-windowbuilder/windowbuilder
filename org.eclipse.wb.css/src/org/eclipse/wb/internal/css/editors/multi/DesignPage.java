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

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.css.Activator;
import org.eclipse.wb.internal.css.editors.multi.StylesEditComposite.ICommandExceptionHandler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * "Design" page of {@link MultiPageEditor}.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
public final class DesignPage implements IDesignPage {
  private MultiPageEditor m_editor;
  private IDocument m_document;
  private boolean m_active = false;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize(MultiPageEditor cssEditor) {
    m_editor = cssEditor;
    IEditorInput editorInput = m_editor.getEditorInput();
    IDocumentProvider documentProvider = m_editor.getDocumentProvider();
    m_document = documentProvider.getDocument(editorInput);
  }

  /**
   * Disposes this {@link DesignPage}.
   */
  public void dispose() {
    disposeDesign();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return "Design";
  }

  public Image getImage() {
    return Activator.getImage("editor_design_page.png");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  private PageBook m_pageBook;
  private StylesEditComposite m_designComposite;

  /**
   * Creates the SWT control(s) for this page.
   */
  public Control createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container).noMargins().noSpacing();
    createControl_pageBook(container);
    return container;
  }

  private void createControl_pageBook(Composite container) {
    m_pageBook = new PageBook(container, SWT.NONE);
    GridDataFactory.create(m_pageBook).grab().fill();
    m_designComposite =
        new StylesEditComposite(m_pageBook, SWT.NONE, new ICommandExceptionHandler() {
          public void handleException(Throwable exception) {
            showExceptionOnDesignPane(exception);
          }
        });
    try {
      m_designComposite.setDocument(m_document);
    } catch (Exception e) {
      showExceptionOnDesignPane(e);
    }
  }

  public Control getControl() {
    return m_pageBook;
  }

  /**
   * Asks this page to take focus.
   */
  public void setFocus() {
    // do set focus
    m_designComposite.setFocus();
  }

  /**
   * @return the {@link StylesEditComposite} of this {@link DesignPage}.
   */
  public StylesEditComposite getDesignComposite() {
    return m_designComposite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handles activation/deactivation of {@link DesignPage} in {@link MultiPageEditor}.
   */
  public void handleActiveState(boolean activate) {
    if (activate) {
      handleActiveState_True();
    } else {
      handleActiveState_False();
    }
  }

  private void handleActiveState_True() {
    m_active = true;
    m_pageBook.showPage(m_designComposite);
    getDocument().addDocumentListener(m_documentChangeListener);
    m_designComposite.onActivate();
  }

  private void handleActiveState_False() {
    closeDesign();
    m_active = false;
  }

  private void closeDesign() {
    getDocument().removeDocumentListener(m_documentChangeListener);
    if (m_active && m_pageBook.getPage() == m_designComposite) {
      m_designComposite.onDeactivate();
    }
  }

  /**
   * Listener for external changes.
   */
  private final IDocumentListener m_documentChangeListener = new IDocumentListener() {
    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    public void documentChanged(DocumentEvent event) {
      if (!m_designComposite.isModificationProcessing()) {
        refreshDesign();
      }
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes design and model.
   */
  private void disposeDesign() {
    closeDesign();
    m_designComposite.disposeDesign();
  }

  /**
   * Parses {@link IDocument} and displays design page.
   */
  public void refreshDesign() {
    m_designComposite.refreshDesign();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IDocument getDocument() {
    return m_document;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exception
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Displays the error information .
   * 
   * @param e
   *          the {@link Throwable} to display.
   */
  ErrorComposite m_errorComposite;

  private void showExceptionOnDesignPane(Throwable e) {
    // dispose current state to prevent any further exceptions
    disposeDesign();
    // show Throwable
    try {
      Activator.getDefault().getLog().log(
          new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "CSS Editor : "
              + e.getMessage(), e) {
            @Override
            public boolean isMultiStatus() {
              return true;
            }
          });
      if (m_errorComposite == null) {
        m_errorComposite = new ErrorComposite(m_pageBook, SWT.NONE);
      }
      m_errorComposite.setExeption(e);
      m_pageBook.showPage(m_errorComposite);
    } catch (Throwable e1) {
      // ignore, prevent error while showing the error.
    }
  }
}