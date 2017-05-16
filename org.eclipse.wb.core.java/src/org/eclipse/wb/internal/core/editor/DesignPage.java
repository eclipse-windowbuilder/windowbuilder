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
package org.eclipse.wb.internal.core.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.controls.PageBook;
import org.eclipse.wb.core.editor.DesignerEditorListener;
import org.eclipse.wb.core.editor.DesignerState;
import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.errors.JavaExceptionComposite;
import org.eclipse.wb.internal.core.editor.errors.JavaWarningComposite;
import org.eclipse.wb.internal.core.editor.errors.MultipleConstructorsComposite;
import org.eclipse.wb.internal.core.editor.errors.NoEntryPointComposite;
import org.eclipse.wb.internal.core.editor.errors.NotUiJavaWarningComposite;
import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;
import org.eclipse.wb.internal.core.editor.structure.PartListenerAdapter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * "Design" page of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.editor
 */
public final class DesignPage implements IDesignPage {
  private boolean m_disposed;
  private DesignerEditor m_designerEditor;
  private ICompilationUnit m_compilationUnit;
  private boolean m_showProgress = true;
  private JavaInfo m_rootObject;
  private boolean m_active;
  private UndoManager m_undoManager;
  private DesignerState m_designerState = DesignerState.Undefined;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Listener for reparsing on external modifications, when our editor is activated.<br>
   * Checks for that really THIS editor instance activated.
   *
   * Sets active JavaInfo.
   */
  private final IPartListener m_partListener = new PartListenerAdapter() {
    @Override
    public void partActivated(IWorkbenchPart part) {
      if (part == m_designerEditor) {
        ExecutionUtils.runAsync(new RunnableEx() {
          public void run() throws Exception {
            if (!isDisposed()) {
              GlobalStateJava.activate(m_rootObject);
              if (m_active) {
                checkDependenciesOnDesignPageActivation();
                m_undoManager.deactivate();
                m_undoManager.activate();
              }
            }
          }
        });
      }
    }
  };

  public void initialize(IDesignerEditor designerEditor) {
    m_designerEditor = (DesignerEditor) designerEditor;
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        m_compilationUnit = m_designerEditor.getCompilationUnit();
        m_undoManager = new UndoManager(DesignPage.this, m_compilationUnit);
      }
    });
    // reparse on external modification
    m_designerEditor.getEditorSite().getPage().addPartListener(m_partListener);
  }

  /**
   * @return <code>true</code> if this {@link DesignPage} is disposed.
   */
  public boolean isDisposed() {
    return m_disposed;
  }

  /**
   * Disposes this {@link DesignPage}.
   */
  public void dispose() {
    m_disposed = true;
    m_undoManager.deactivate();
    m_designerEditor.getEditorSite().getPage().removePartListener(m_partListener);
    disposeAll(true);
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
    return DesignerPlugin.getImage("editor_design_page.png");
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  private PageBook m_pageBook;
  private JavaDesignComposite m_designComposite;
  private final Map<Class<?>, Composite> m_errorCompositesMap = Maps.newHashMap();

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
    // design composite
    ICommandExceptionHandler exceptionHandler = new ICommandExceptionHandler() {
      public void handleException(Throwable exception) {
        handleDesignException(exception);
      }
    };
    m_designComposite =
        new JavaDesignComposite(m_pageBook, SWT.NONE, m_designerEditor, exceptionHandler);
    // show "design" initially
    m_pageBook.showPage(m_designComposite);
  }

  /**
   * Creates and caches the composites for displaying some error/warning messages.
   */
  @SuppressWarnings("unchecked")
  private <T extends Composite> T getErrorComposite(Class<T> compositeClass) throws Exception {
    T composite = (T) m_errorCompositesMap.get(compositeClass);
    if (composite == null) {
      Constructor<T> constructor = compositeClass.getConstructor(Composite.class, int.class);
      composite = constructor.newInstance(m_pageBook, SWT.NONE);
      m_errorCompositesMap.put(compositeClass, composite);
    }
    return composite;
  }

  public Control getControl() {
    return m_pageBook;
  }

  /**
   * Makes this page disabled (during refresh) and again enabled.
   */
  private void setEnabled(boolean enabled) {
    m_pageBook.getParent().setRedraw(enabled);
  }

  /**
   * Asks this page to take focus.
   */
  public void setFocus() {
    m_designComposite.setFocus();
  }

  /**
   * @return the {@link DesignComposite} of this {@link DesignPage}.
   */
  public JavaDesignComposite getDesignComposite() {
    return m_designComposite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handles activation/deactivation of {@link DesignPage} in {@link DesignerEditor}.
   */
  public void handleActiveState(boolean activate) {
    m_active = activate;
    if (activate) {
      handleActiveState_True();
    } else {
      handleActiveState_False();
    }
  }

  public void setSourceModelSynchronizationEnabled(boolean active) {
    if (active) {
      m_undoManager.activate();
    } else {
      m_undoManager.deactivate();
    }
  }

  private void handleActiveState_True() {
    // ensure that underlying ICompilationUnit is valid
    if (!m_compilationUnit.isOpen()) {
      IDesignerEditor designerEditor = m_designerEditor;
      dispose();
      initialize(designerEditor);
    }
    // OK, activate Design
    m_undoManager.activate();
    m_designComposite.onActivate();
    // check dependencies
    checkDependenciesOnDesignPageActivation();
  }

  private void handleActiveState_False() {
    m_undoManager.deactivate();
    m_designComposite.onDeActivate();
  }

  /**
   * This editor and its "Design" page are activated. Check if some external dependencies are
   * changed so that reparse or refresh should be performed.
   */
  private void checkDependenciesOnDesignPageActivation() {
    if (m_rootObject != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          EditorActivatedRequest request = new EditorActivatedRequest();
          m_rootObject.getBroadcast(EditorActivatedListener.class).invoke(request);
          if (request.isReparseRequested()) {
            refreshGEF();
          } else if (request.isRefreshRequested()) {
            m_rootObject.refresh();
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle listener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all registered {@link EditorLifeCycleListener}s.
   */
  private List<EditorLifeCycleListener> getLifeCycleListeners() {
    return ExternalFactoriesHelper.getElementsInstances(
        EditorLifeCycleListener.class,
        "org.eclipse.wb.core.editorLifeCycleListeners",
        "listener");
  }

  /**
   * @return <code>false</code> if any of
   *         {@link EditorLifeCycleListener#parseWithProgress(Object, ICompilationUnit)} returned
   *         <code>false</code>.
   */
  private boolean isLifeCycleProgressRequired() {
    for (EditorLifeCycleListener listener : getLifeCycleListeners()) {
      if (!listener.parseWithProgress(this, m_compilationUnit)) {
        return false;
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies if parsing should be done in progress dialog.
   */
  public void setShowProgress(boolean showProgress) {
    m_showProgress = showProgress;
  }

  /**
   * Disposes design and model.
   */
  private void disposeAll(final boolean force) {
    // dispose design
    if (!m_pageBook.isDisposed()) {
      dispose_beforePresentation();
      m_designComposite.disposeDesign();
    }
    // dispose model
    if (m_rootObject != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_rootObject.refresh_dispose();
          m_rootObject.getBroadcastObject().dispose();
          disposeContext(force);
          GlobalStateJava.deactivate(m_rootObject);
        }
      });
      m_rootObject = null;
    }
  }

  /**
   * Sends notification that presentation will be disposed.
   */
  private void dispose_beforePresentation() {
    if (m_rootObject != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_rootObject.getBroadcastObject().dispose_beforePresentation();
        }
      });
    }
  }

  /**
   * Notifies listeners that hierarchy is disposed or editor is closing.
   *
   * @param force
   *          is <code>true</code> if user closes editor or explicitly requests re-parsing.
   */
  protected void disposeContext(final boolean force) {
    for (final EditorLifeCycleListener listener : getLifeCycleListeners()) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          listener.disposeContext(DesignPage.this, force);
        }
      });
    }
  }

  /**
   * Parses {@link ICompilationUnit} and displays it in GEF.
   */
  public void refreshGEF() {
    disposeContext(true);
    m_undoManager.refreshDesignerEditor();
    // notify listeners
    {
      List<DesignerEditorListener> designPageListeners =
          ImmutableList.copyOf(m_designerEditor.getDesignPageListeners());
      for (DesignerEditorListener listener : designPageListeners) {
        listener.reparsed();
      }
    }
  }

  /**
   * Parses {@link ICompilationUnit} and displays it in GEF.
   *
   * @return <code>true</code> if parsing was successful.
   */
  boolean internal_refreshGEF() {
    setEnabled(false);
    try {
      disposeAll(false);
      // do parse
      m_designerState = DesignerState.Parsing;
      if (m_showProgress && isLifeCycleProgressRequired()) {
        internal_refreshGEF_withProgress();
      } else {
        internal_refreshGEF(new NullProgressMonitor());
      }
      // success, show Design
      m_pageBook.showPage(m_designComposite);
      m_designerState = DesignerState.Successful;
      return true;
    } catch (Throwable e) {
      // extract "real" exception
      if (e instanceof InvocationTargetException) {
        e = ((InvocationTargetException) e).getTargetException();
      }
      // show exception in editor
      showExceptionOnDesignPane(e, null);
      // failure
      return false;
    } finally {
      setEnabled(true);
    }
  }

  private void internal_refreshGEF_withProgress() throws Exception {
    final Display display = Display.getCurrent();
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        monitor.beginTask("Opening Design page.", 7);
        //
        try {
          DesignPageSite.setProgressMonitor(monitor);
          display.syncExec(new Runnable() {
            public void run() {
              try {
                internal_refreshGEF(monitor);
              } catch (Throwable e) {
                ReflectionUtils.propagate(e);
              }
            }
          });
        } catch (Throwable e) {
          ReflectionUtils.propagate(e);
        } finally {
          DesignPageSite.setProgressMonitor(null);
        }
        //
        monitor.done();
      }
    };
    try {
      new ProgressMonitorDialog(DesignerPlugin.getShell()).run(false, false, runnable);
    } catch (InvocationTargetException e) {
      ReflectionUtils.propagate(e.getCause());
    } catch (Throwable e) {
      ReflectionUtils.propagate(e);
    }
  }

  /**
   * Displays the error information on Design Pane.
   *
   * @param e
   *          the {@link Throwable} to display.
   * @param screenshot
   *          the {@link Image} of entire shell just before error. Can be <code>null</code> in case
   *          of parse error when no screenshot needed.
   *
   */
  private void showExceptionOnDesignPane(Throwable e, Image screenshot) {
    m_designerState = DesignerState.Error;
    // show Throwable
    try {
      e = DesignerExceptionUtils.rewriteException(e);
      Throwable designerCause = DesignerExceptionUtils.getDesignerCause(e);
      if (e instanceof MultipleConstructorsError) {
        MultipleConstructorsComposite composite =
            getErrorComposite(MultipleConstructorsComposite.class);
        composite.setException((MultipleConstructorsError) e);
        m_pageBook.showPage(composite);
      } else if (e instanceof NoEntryPointError) {
        NoEntryPointComposite composite = getErrorComposite(NoEntryPointComposite.class);
        composite.setException((NoEntryPointError) e);
        m_pageBook.showPage(composite);
      } else if (designerCause instanceof DesignerException
          && ((DesignerException) designerCause).getCode() == ICoreExceptionConstants.PARSER_NOT_GUI) {
        NotUiJavaWarningComposite composite = getErrorComposite(NotUiJavaWarningComposite.class);
        composite.setException(e);
        m_pageBook.showPage(composite);
      } else if (DesignerExceptionUtils.isWarning(e)) {
        JavaWarningComposite composite = getErrorComposite(JavaWarningComposite.class);
        composite.setException(e);
        m_pageBook.showPage(composite);
      } else {
        DesignerPlugin.log(e);
        JavaExceptionComposite composite = getErrorComposite(JavaExceptionComposite.class);
        composite.setException(e, screenshot, m_compilationUnit, m_rootObject);
        m_pageBook.showPage(composite);
      }
    } catch (Throwable e1) {
      // ignore, prevent error while showing the error.
    }
    m_designerEditor.getMultiMode().showDesign();
  }

  /**
   * Implementation of {@link #internal_refreshGEF()} with {@link IProgressMonitor}.
   */
  private void internal_refreshGEF(IProgressMonitor monitor) throws Exception {
    monitor.subTask("Initializing...");
    monitor.worked(1);
    // notify parseStart()
    for (EditorLifeCycleListener listener : getLifeCycleListeners()) {
      listener.parseStart(this);
    }
    // do parse
    try {
      long start = System.currentTimeMillis();
      monitor.subTask("Parsing...");
      Debug.print("Parsing...");
      m_rootObject = JavaInfoParser.parse(m_compilationUnit);
      monitor.worked(1);
      Debug.println("done: " + (System.currentTimeMillis() - start));
    } finally {
      // notify parseEnd()
      for (EditorLifeCycleListener listener : getLifeCycleListeners()) {
        listener.parseEnd(this);
      }
    }
    // install site
    {
      IDesignPageSite designPageSite = new DesignPageSite() {
        @Override
        public void showSourcePosition(int position) {
          m_designerEditor.showSourcePosition(position);
        }

        @Override
        public void openSourcePosition(int position) {
          m_designerEditor.showSourcePosition(position);
          m_designerEditor.getMultiMode().showSource();
        }

        // TODO(scheglov)
//        @Override
//        public void highlightVisitedNodes(Collection<ASTNode> nodes) {
//          m_designerEditor.highlightVisitedNodes(nodes);
//        }
        @Override
        public void handleException(Throwable e) {
          handleDesignException(e);
        }

        @Override
        public void reparse() {
          refreshGEF();
        }
      };
      DesignPageSite.Helper.setSite(m_rootObject, designPageSite);
    }
    // search dependencies in background
    schedule_rememberDependency();
    // refresh model (create GUI)
    {
      long start = System.currentTimeMillis();
      monitor.subTask("Refreshing...");
      m_rootObject.refresh();
      monitor.worked(1);
      Debug.println("refresh: " + (System.currentTimeMillis() - start));
    }
    // refresh design
    m_designComposite.refresh(m_rootObject, monitor);
    // configure helpers
    m_undoManager.setRoot(m_rootObject);
  }

  private void schedule_rememberDependency() {
    new Thread("WindowBuilder dependency search") {
      @Override
      public void run() {
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            JavaInfoUtils.rememberDependency(m_rootObject);
          }
        });
      }
    }.start();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerState getDesignerState() {
    return m_designerState;
  }

  public UndoManager getUndoManager() {
    return m_undoManager;
  }

  /**
   * @return <code>true</code> if this editor is active.
   */
  public boolean isActiveEditor() {
    return DesignerPlugin.getActiveEditor() == m_designerEditor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exception
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handles any exception happened on "Design" page, such as exceptions in GEF commands, property
   * table, components tree.
   */
  private void handleDesignException(Throwable e) {
    // at first, try to make post-mortem screenshot
    Image screenshot;
    try {
      screenshot = DesignerExceptionUtils.makeScreenshot();
    } catch (Throwable ex) {
      screenshot = null;
    }
    // dispose current state to prevent any further exceptions
    disposeAll(true);
    // show exception
    if (EnvironmentUtils.isTestingTime()) {
      e.printStackTrace();
    }
    showExceptionOnDesignPane(e, screenshot);
  }
}