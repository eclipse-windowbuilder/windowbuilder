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

import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Implementation of {@link MultiMode} for split mode.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
final class MultiSplitMode extends DefaultMultiMode {
  private SashForm m_sashForm;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiSplitMode(DesignerEditor editor) {
    super(editor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  void setFocus() {
    m_sashForm.setFocus();
  }

  @Override
  public void showSource() {
    showPage(m_sourcePage);
  }

  @Override
  public void showDesign() {
    if (m_sourcePage.isActive() || m_activePage != null && m_activePage != m_designPage) {
      if (m_additionalPages.isEmpty()) {
        m_designPage.setFocus();
      } else {
        showPage(m_designPage);
      }
    }
  }

  @Override
  public void switchSourceDesign() {
    if (m_sourcePage.isActive()) {
      showPage(m_designPage);
    } else {
      showPage(m_sourcePage);
    }
  }

  @Override
  public void onSetInput() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void create(Composite parent) {
    super.create(parent);
    boolean vertical = isSplitVerticalMode();
    m_sashForm = new SashForm(parent, vertical ? SWT.VERTICAL : SWT.HORIZONTAL);
    m_sashForm.setBackground(IColorConstants.buttonDarker);
    // parts
    if (isSourceFirst()) {
      createSource();
      createDesign();
      m_sashForm.setWeights(new int[]{40, 60});
    } else {
      createDesign();
      createSource();
      m_sashForm.setWeights(new int[]{60, 40});
    }
    // do first refresh and track document modifications
    m_designPage.setShowProgress(false);
    m_designPage.refreshGEF();
    parseOnDocumentChange();
    trackSourceActivation();
  }

  private void createDesign() {
    if (m_additionalPages.isEmpty()) {
      m_designPage.createControl(m_sashForm);
    } else {
      createTabFolder(m_sashForm);
      // create "Design" tab
      createTab(m_designPage);
      // create additional pages
      for (IEditorPage page : m_additionalPages) {
        createTab(page);
      }
    }
  }

  private void createSource() {
    m_sourcePage.createControl(m_sashForm);
  }

  private void trackSourceActivation() {
    final Composite sourceControl = (Composite) m_sourcePage.getControl();
    final Composite designControl = (Composite) m_designPage.getControl();
    final Display display = sourceControl.getDisplay();
    display.addFilter(SWT.MouseDown, new Listener() {
      public void handleEvent(Event event) {
        if (sourceControl.isDisposed()) {
          display.removeFilter(SWT.MouseDown, this);
          return;
        }
        if (UiUtils.isChildOf(sourceControl, event.widget)) {
          showPage(m_sourcePage);
        }
        if (UiUtils.isChildOf(designControl, event.widget)) {
          showPage(m_designPage);
        }
      }
    });
  }

  @Override
  void editorActivatedFirstTime() {
    showSource();
    showDesign();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Re-parse
  //
  ////////////////////////////////////////////////////////////////////////////
  private void parseOnDocumentChange() {
    m_editor.super_getSourceViewer().getDocument().addDocumentListener(new IDocumentListener() {
      private int m_lastModificationId;

      public void documentAboutToBeChanged(DocumentEvent event) {
      }

      public void documentChanged(DocumentEvent event) {
        final int modificationId = ++m_lastModificationId;
        int delay = getSyncDelay();
        if (delay > 0) {
          delay = Math.max(delay, 250);
          Display.getCurrent().timerExec(delay, new Runnable() {
            public void run() {
              if (modificationId == m_lastModificationId) {
                if (m_sourcePage.isActive()) {
                  parseWhenSourceActive();
                }
              }
            }
          });
        }
      }
    });
  }

  private void parseWhenSourceActive() {
    try {
      m_designPage.refreshGEF();
    } catch (Throwable ex) {
    }
  }

  @Override
  public void afterSave() {
    super.afterSave();
    if (getSyncDelay() <= 0) {
      parseWhenSourceActive();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> "Source" page should be first.
   */
  private boolean isSourceFirst() {
    int layout = DesignerPlugin.getPreferences().getInt(IPreferenceConstants.P_EDITOR_LAYOUT);
    return layout == IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_SOURCE
        || layout == IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE;
  }

  /**
   * @return <code>true</code> if source/design part should be located above each other.
   */
  private boolean isSplitVerticalMode() {
    int layout = DesignerPlugin.getPreferences().getInt(IPreferenceConstants.P_EDITOR_LAYOUT);
    return layout == IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE
        || layout == IPreferenceConstants.V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN;
  }

  /**
   * @return the time in milliseconds, to wait before parsing, when editor was changed. If -1, then
   *         disabled, parse on save.
   */
  private int getSyncDelay() {
    return DesignerPlugin.getPreferences().getInt(IPreferenceConstants.P_EDITOR_LAYOUT_SYNC_DELAY);
  }
}
