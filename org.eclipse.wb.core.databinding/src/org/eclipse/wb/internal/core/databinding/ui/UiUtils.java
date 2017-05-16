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
package org.eclipse.wb.internal.core.databinding.ui;

import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.apache.commons.lang.ArrayUtils;

/**
 * Internal UI utils.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class UiUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Table
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create table column that load/tracking/save it's width.
   */
  public static TableColumn createSmartColumn(Table table,
      final IDialogSettings settings,
      final String key,
      int defautWidth) {
    // create column
    final TableColumn column = new TableColumn(table, SWT.NONE);
    // restore width
    try {
      // load width
      int width = settings.getInt(key);
      if (width == 0) {
        width = defautWidth;
      }
      // set width
      column.setWidth(width);
    } catch (Throwable e) {
      // set default width
      column.setWidth(defautWidth);
      settings.put(key, defautWidth);
    }
    // handle resize
    column.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        settings.put(key, column.getWidth());
      }
    });
    //
    return column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SashForm
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets weights for sash from dialog settings.
   */
  public static void loadSashWeights(SashForm sash,
      IDialogSettings settings,
      String key,
      int[] defaultWeights) {
    try {
      // load values
      int x = settings.getInt(key + ".x");
      int y = settings.getInt(key + ".y");
      // set values
      sash.setWeights(new int[]{x, y});
    } catch (Throwable e) {
      // use default values
      sash.setWeights(defaultWeights);
      // save default values
      Assert.isLegal(defaultWeights.length == 2);
      settings.put(key + ".x", defaultWeights[0]);
      settings.put(key + ".y", defaultWeights[1]);
    }
  }

  /**
   * Put sash weights to dialog settings.
   */
  public static void saveSashWeights(SashForm sash, IDialogSettings settings, String key) {
    int[] weights = sash.getWeights();
    Assert.isLegal(weights.length == 2);
    settings.put(key + ".x", weights[0]);
    settings.put(key + ".y", weights[1]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Label
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets to label bold version of it font.
   */
  public static void setBoldFont(Label label) {
    label.setFont(SwtResourceManager.getBoldFont(label.getFont()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the section with the given name in given dialog settings.
   */
  public static IDialogSettings getSettings(IDialogSettings mainSettings, String sectionName) {
    IDialogSettings settings = mainSettings.getSection(sectionName);
    return settings == null ? mainSettings.addNewSection(sectionName) : settings;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns whether this selection is empty.
   */
  public static boolean isEmpty(ISelection selection) {
    return selection == null || selection.isEmpty();
  }

  public static IStructuredSelection getSelection(Viewer viewer) {
    return (IStructuredSelection) viewer.getSelection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialogs
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Open {@link SelectionDialog} for choose type.
   */
  public static String chooseType(Shell shell, IJavaProject project, String baseClassName, int style)
      throws Exception {
    // prepare scope
    IJavaSearchScope scope = null;
    if (project != null && baseClassName != null) {
      IType classType = project.findType(baseClassName);
      if (classType != null) {
        scope = SearchEngine.createHierarchyScope(classType);
      }
    }
    // open dialog
    return chooseType(shell, scope, style);
  }

  /**
   * Open {@link SelectionDialog} for choose type.
   */
  public static String chooseType(Shell shell,
      IJavaProject project,
      String[] baseClassNames,
      int style) throws Exception {
    // prepare scope
    IJavaSearchScope scope = null;
    if (project != null && !ArrayUtils.isEmpty(baseClassNames)) {
      IJavaSearchScope[] scopes = new IJavaSearchScope[baseClassNames.length];
      for (int i = 0; i < scopes.length; i++) {
        IType classType = project.findType(baseClassNames[i]);
        if (classType != null) {
          scopes[i] = SearchEngine.createHierarchyScope(classType);
        }
      }
      scope = scopes.length == 1 ? scopes[0] : new MultiHierarchyScope(scopes);
    }
    // open dialog
    return chooseType(shell, scope, style);
  }

  public static String chooseType(Shell shell, IJavaSearchScope scope, int style) throws Exception {
    // prepare dialog
    ProgressMonitorDialog context = new ProgressMonitorDialog(shell);
    SelectionDialog dialog = JavaUI.createTypeDialog(shell, context, scope, style, false);
    dialog.setTitle(Messages.UiUtils_openTypeTitle);
    dialog.setMessage(Messages.UiUtils_openTypeMessage);
    // open dialog
    if (dialog.open() == Window.OK) {
      IType type = (IType) dialog.getResult()[0];
      return type.getFullyQualifiedName().replace('$', '.');
    }
    return null;
  }

  private static class MultiHierarchyScope implements IJavaSearchScope {
    private final IJavaSearchScope[] m_scopes;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MultiHierarchyScope(IJavaSearchScope[] scopes) {
      m_scopes = scopes;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IJavaSearchScope
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean encloses(String resourcePath) {
      for (IJavaSearchScope scope : m_scopes) {
        if (scope != null && scope.encloses(resourcePath)) {
          return true;
        }
      }
      return false;
    }

    public boolean encloses(IJavaElement element) {
      for (IJavaSearchScope scope : m_scopes) {
        if (scope != null && scope.encloses(element)) {
          return true;
        }
      }
      return false;
    }

    public IPath[] enclosingProjectsAndJars() {
      for (IJavaSearchScope scope : m_scopes) {
        if (scope != null) {
          return scope.enclosingProjectsAndJars();
        }
      }
      return null;
    }

    public boolean includesBinaries() {
      return true;
    }

    public boolean includesClasspaths() {
      return true;
    }

    public void setIncludesBinaries(boolean includesBinaries) {
    }

    public void setIncludesClasspaths(boolean includesClasspaths) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserve
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getPresentationText(IObserveInfo object, IObserveInfo property)
      throws Exception {
    String objectPresentation = object.getPresentation().getTextForBinding();
    String propertyPresentation = property.getPresentation().getTextForBinding();
    String separator = propertyPresentation.length() == 0 ? "" : ".";
    return objectPresentation + separator + propertyPresentation;
  }
}