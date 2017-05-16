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

import org.eclipse.wb.core.editor.DesignerEditorListener;
import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IMultiMode;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignComposite;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.views.IDesignCompositeProvider;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jdt.ui.actions.FormatAllAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.contexts.IContextService;

import java.util.Collection;
import java.util.List;

/**
 * Multi page editor with Java editor and {@link DesignPage}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.editor
 */
@SuppressWarnings("restriction")
public final class DesignerEditor extends CompilationUnitEditor
    implements
      IDesignerEditor,
      IDesignCompositeProvider {
  private static final String CONTEXT_ID = "org.eclipse.wb.core.java.editorScope";
  private MultiMode m_multiMode;
  private boolean m_firstActivation = true;
  private Composite m_rootControl;
  private VisitedLinesHighlighter m_linesHighlighter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerEditor() {
    DesignerPlugin.configurePreEditor();
    if (isPagesMode()) {
      m_multiMode = new MultiPagesMode(this);
    } else {
      m_multiMode = new MultiSplitMode(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createPartControl(Composite parent) {
    m_rootControl = parent;
    m_multiMode.create(parent);
    m_linesHighlighter = new VisitedLinesHighlighter(getSourceViewer());
    activateEditorContext();
  }

  /**
   * Activates context of our Java editor.
   */
  private void activateEditorContext() {
    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
    if (contextService != null) {
      contextService.activateContext(CONTEXT_ID);
    }
  }

  @Override
  public void setFocus() {
    m_multiMode.setFocus();
  }

  @Override
  protected void doSetInput(IEditorInput input) throws CoreException {
    super.doSetInput(input);
    if (!canDesignInput()) {
      m_multiMode = new MultiSourceMode(this);
    }
    m_multiMode.onSetInput();
  }

  /**
   * @return <code>false</code> if input is invalid (for example external file), so we can not
   *         design it.
   */
  private boolean canDesignInput() {
    ICompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null) {
      return false;
    }
    try {
      compilationUnit.getUnderlyingResource();
    } catch (Throwable e) {
      return false;
    }
    return true;
  }

  /**
   * {@link DesignerEditorContributor} notifies us about activation, with "Source" tab, so that Java
   * actions are now installed.
   */
  void activated() {
    if (m_firstActivation) {
      m_firstActivation = false;
      m_multiMode.editorActivatedFirstTime();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    m_multiMode.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> {@link MultiPagesMode} should be used or <code>false</code> for
   *         {@link MultiSplitMode}.
   */
  private boolean isPagesMode() {
    int layout = DesignerPlugin.getPreferences().getInt(IPreferenceConstants.P_EDITOR_LAYOUT);
    return layout == IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_SOURCE
        || layout == IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_DESIGN;
  }

  /**
   * @return the main editor {@link Composite}, root of all its {@link Control}s.
   */
  public Composite getRootControl() {
    return m_rootControl;
  }

  /**
   * Invokes "super" {@link #createPartControl(Composite)}.
   */
  void super_createPartControl(Composite parent) {
    super.createPartControl(parent);
  }

  /**
   * Invokes "super" {@link #getSourceViewer()}.
   */
  ISourceViewer super_getSourceViewer() {
    return super.getSourceViewer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves cursor to given position in Java editor.
   */
  public void showSourcePosition(final int position) {
    ExecutionUtils.runLogLater(new RunnableEx() {
      public void run() throws Exception {
        selectAndReveal(position, 0);
      }
    });
  }

  /**
   * Highlight lines with visited {@link ASTNode}s.
   */
  public void highlightVisitedNodes(final Collection<ASTNode> nodes) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        if (m_linesHighlighter != null) {
          m_linesHighlighter.setVisitedNodes(nodes);
        }
      }
    });
  }

  /**
   * @return the {@link MultiMode}.
   */
  public IMultiMode getMultiMode() {
    return m_multiMode;
  }

  /**
   * @return the {@link ICompilationUnit} opened in this editor.
   */
  public ICompilationUnit getCompilationUnit() {
    IWorkingCopyManager workingCopyManager = JavaUI.getWorkingCopyManager();
    return workingCopyManager.getWorkingCopy(getEditorInput());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<DesignerEditorListener> m_designPageListeners = Lists.newArrayList();

  public void addDesignPageListener(DesignerEditorListener listener) {
    m_designPageListeners.add(listener);
  }

  public void removeDesignPageListener(DesignerEditorListener listener) {
    m_designPageListeners.remove(listener);
  }

  public List<DesignerEditorListener> getDesignPageListeners() {
    return m_designPageListeners;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDesignCompositeProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignComposite getDesignComposite() {
    return m_multiMode.getDesignPage().getDesignComposite();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ProblemsLabelDecorator PROBLEMS_DECORATOR = new ProblemsLabelDecorator();

  @Override
  public void updatedTitleImage(Image image) {
    Image baseImage = DesignerPlugin.getImage("gui_editor.gif");
    Image decoratedImage = PROBLEMS_DECORATOR.decorateImage(baseImage, getCompilationUnit());
    if (decoratedImage == null) {
      decoratedImage = baseImage;
    }
    setTitleImage(decoratedImage);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doSave(IProgressMonitor progressMonitor) {
    if (DesignerPlugin.getPreferences().getBoolean(IPreferenceConstants.P_EDITOR_FORMAT_ON_SAVE)) {
      ICompilationUnit unit = getCompilationUnit();
      FormatAllAction formatAction = new FormatAllAction(getSite());
      formatAction.run(new StructuredSelection(unit));
    }
    super.doSave(progressMonitor);
    m_multiMode.afterSave();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setAction(String actionID, IAction action) {
    super.setAction(actionID, action);
    m_multiMode.getSourcePage().setAction(actionID, action);
  }

  void super_setAction(String actionID, IAction action) {
    super.setAction(actionID, action);
  }

  @Override
  public IAction getAction(String actionID) {
    if (m_multiMode.isSourceActive()) {
      return super.getAction(actionID);
    }
    if (m_multiMode.isDesignActive()) {
      return m_multiMode.getDesignPage().getDesignComposite().getAction(actionID);
    }
    return super.getAction(actionID);
  }
}