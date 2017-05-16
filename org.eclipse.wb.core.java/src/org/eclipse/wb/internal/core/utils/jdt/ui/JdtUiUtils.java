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
package org.eclipse.wb.internal.core.utils.jdt.ui;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.lang.reflect.Method;

/**
 * Helper class for various JDT UI utils.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.util.jdt.ui
 */
public final class JdtUiUtils {
  private static IPreferenceStore m_combinedPreferenceStore;
  private static JavaTextTools m_javaTextTools;
  private static AbstractUIPlugin m_javaPlugin;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set given Java source for given source viewer.
   */
  public static void setJavaSourceForViewer(SourceViewer viewer, String source) {
    IDocument doc = new Document(source);
    getJavaTextTools().setupJavaDocumentPartitioner(doc);
    viewer.setInput(doc);
  }

  /**
   * Create Java source viewer (with highlighting) for given source.
   */
  public static SourceViewer createJavaSourceViewer(Composite parent, int style) {
    // create viewer
    SourceViewer viewer = null;
    IPreferenceStore store = getCombinedPreferenceStore();
    viewer = new SourceViewer(parent, null, style);
    // configure viewer
    IColorManager colorManager = getJavaTextTools().getColorManager();
    viewer.configure(new JavaSourceViewerConfiguration(colorManager, store, null, null));
    viewer.setEditable(false);
    // return viewer
    return viewer;
  }

  /**
   * @return the instance of "org.eclipse.jdt.ui" activator class, which is the instance of
   *         org.eclipse.jdt.internal.ui.JavaPlugin when version > 3.2
   */
  private static AbstractUIPlugin getJavaPlugin() {
    if (m_javaPlugin == null) {
      m_javaPlugin = ExecutionUtils.runObject(new RunnableObjectEx<AbstractUIPlugin>() {
        public AbstractUIPlugin runObject() throws Exception {
          return getJavaPlugin0();
        }
      });
    }
    return m_javaPlugin;
  }

  /**
   * @return the JavaPlugin instance depending on eclipse version (using preprocessor).
   */
  private static AbstractUIPlugin getJavaPlugin0() throws Exception {
    return getBundleActivator("org.eclipse.jdt.ui");
  }

  /**
   * @return the combined preference store as "IPreferenceStore
   *         org.eclipse.jdt.internal.ui.JavaPlugin.getCombinedPreferenceStore()".
   */
  private static IPreferenceStore getCombinedPreferenceStore() {
    if (m_combinedPreferenceStore == null) {
      IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
      m_combinedPreferenceStore =
          new ChainedPreferenceStore(new IPreferenceStore[]{
              getJavaPlugin().getPreferenceStore(),
              new PreferencesAdapter(JavaCore.getPlugin().getPluginPreferences()),
              generalTextStore});
    }
    return m_combinedPreferenceStore;
  }

  /**
   * @return {@link JavaTextTools} instance, like as "IPreferenceStore
   *         org.eclipse.jdt.internal.ui.JavaPlugin.getJavaTextTools()".
   */
  private static JavaTextTools getJavaTextTools() {
    if (m_javaTextTools == null) {
      m_javaTextTools =
          new JavaTextTools(getJavaPlugin().getPreferenceStore(),
              JavaCore.getPlugin().getPluginPreferences());
    }
    return m_javaTextTools;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Selects any {@link IType} (class or interface) in {@link IJavaProject}.
   *
   * @param superTypeName
   *          the name of superclass of {@link IType} to select.
   *
   * @return the selected {@link IType}, or <code>null</code> is no type selected.
   */
  public static IType selectSubType(final Shell shell,
      final IJavaProject javaProject,
      final String superTypeName) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<IType>() {
      public IType runObject() throws Exception {
        IType actionType = javaProject.findType(superTypeName);
        SubtypesScope scope = new SubtypesScope(actionType);
        return JdtUiUtils.selectType(DesignerPlugin.getShell(), scope);
      }
    }, null);
  }

  /**
   * Selects any {@link IType} (class or interface) in {@link IJavaProject}.
   *
   * @return the selected {@link IType}, or <code>null</code> is no type selected.
   */
  public static IType selectType(Shell shell, IJavaProject javaProject) throws Exception {
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]{javaProject});
    return selectType(shell, scope, IJavaElementSearchConstants.CONSIDER_ALL_TYPES);
  }

  /**
   * Selects any class (not interface) in {@link IJavaProject}.
   *
   * @return the selected {@link IType}, or <code>null</code> is no type selected.
   */
  public static IType selectClassType(Shell shell, IJavaProject javaProject) throws Exception {
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]{javaProject});
    return selectType(shell, scope);
  }

  /**
   * Selects any class (not interface) in inside of {@link IJavaSearchScope}.
   *
   * @return the selected {@link IType}, or <code>null</code> is no type selected.
   */
  public static IType selectType(Shell shell, IJavaSearchScope scope) throws Exception {
    return selectType(shell, scope, IJavaElementSearchConstants.CONSIDER_CLASSES);
  }

  /**
   * Selects any class (not interface) in inside of {@link IJavaSearchScope}.
   *
   * @param shell
   *          the parent {@link Shell} for dialog.
   * @param scope
   *          the {@link IJavaSearchScope} to limit types.
   * @param style
   *          the style of the dialog, see
   *          {@link JavaUI#createTypeDialog(Shell, org.eclipse.jface.operation.IRunnableContext, IJavaSearchScope, int, boolean)}
   *          .
   *
   * @return the selected {@link IType}, or <code>null</code> is no type selected.
   */
  public static IType selectType(Shell shell, IJavaSearchScope scope, int style) throws Exception {
    ProgressMonitorDialog context = new ProgressMonitorDialog(shell);
    SelectionDialog dialog = JavaUI.createTypeDialog(shell, context, scope, style, false);
    dialog.setTitle(Messages.JdtUiUtils_selectTypeTitle);
    dialog.setMessage(Messages.JdtUiUtils_selectTypeMessage);
    // open dialog
    if (dialog.open() == Window.OK) {
      return (IType) dialog.getResult()[0];
    }
    // no type selected
    return null;
  }

  /**
   * Selects any class (not interface) in {@link IJavaProject}.
   *
   * @return the fully qualified name of selected {@link IType}.
   */
  public static String selectTypeName(Shell shell, IJavaProject javaProject) throws Exception {
    IType type = selectClassType(shell, javaProject);
    return type != null ? type.getFullyQualifiedName() : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bundle activator by "getDefault()" method using reflection.
   */
  public static AbstractUIPlugin getBundleActivator(String bundleName) throws Exception {
    Bundle bundle = Platform.getBundle(bundleName);
    String pluginActivatorClassName = (String) bundle.getHeaders().get(Constants.BUNDLE_ACTIVATOR);
    Class<?> pluginClass = bundle.loadClass(pluginActivatorClassName);
    // get the it's instance using "getDefault" method. Possibly it is the usage of internal API :)
    Method getDefaultMethod = pluginClass.getMethod("getDefault", new Class[0]);
    return (AbstractUIPlugin) getDefaultMethod.invoke(null, new Object[0]);
  }
}
