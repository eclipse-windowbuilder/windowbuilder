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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Selection and expanded state for {@link EditComposite}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class EditSelection {
  private int m_currentBindingIndex = -1;
  private final PageSelection m_targetPage = new PageSelection();
  private final PageSelection m_modelPage = new PageSelection();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Store
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Clear bindings selection.
   */
  public void clearBinding() {
    m_currentBindingIndex = -1;
  }

  /**
   * Store given bindings selection.
   */
  public void setBinding(IDatabindingsProvider databindingsProvider, IBindingInfo binding) {
    m_currentBindingIndex = databindingsProvider.getBindings().indexOf(binding);
  }

  /**
   * Store page index, selection and expanded for "Target" page.
   */
  public void setTarget(IDatabindingsProvider databindingsProvider,
      ObserveElementsComposite observeElementsComposite) {
    setToPage(databindingsProvider, observeElementsComposite, m_targetPage);
  }

  /**
   * Store page index, selection and expanded for "Model" page.
   */
  public void setModel(IDatabindingsProvider databindingsProvider,
      ObserveElementsComposite observeElementsComposite) {
    setToPage(databindingsProvider, observeElementsComposite, m_modelPage);
  }

  /**
   * Store page index, selection and expanded for given page.
   */
  private static void setToPage(IDatabindingsProvider databindingsProvider,
      ObserveElementsComposite observeElementsComposite,
      PageSelection page) {
    // store page index
    page.pageIndex =
        databindingsProvider.getTypes().indexOf(observeElementsComposite.getCurrentType());
    // prepare master objects
    TreeViewer masterViewer = observeElementsComposite.getMasterViewer();
    ITreeContentProvider masterProvider = (ITreeContentProvider) masterViewer.getContentProvider();
    Object[] masterInput = masterProvider.getElements(masterViewer.getInput());
    // store master selection
    page.masterSelection =
        objectToPath(masterProvider, masterInput, observeElementsComposite.getMasterObserve());
    // store master expanded
    page.masterExpanded =
        objectsToPaths(masterProvider, masterInput, masterViewer.getExpandedElements());
    // prepare properties objects
    TreeViewer propertiesViewer = observeElementsComposite.getPropertiesViewer();
    ITreeContentProvider propertiesProvider =
        (ITreeContentProvider) propertiesViewer.getContentProvider();
    Object[] propertiesInput = propertiesProvider.getElements(propertiesViewer.getInput());
    // store properties selection
    page.propertiesSelection =
        objectToPath(
            propertiesProvider,
            propertiesInput,
            observeElementsComposite.getPropertyObserve());
    // store properties expanded
    page.propertiesExpanded =
        objectsToPaths(propertiesProvider, propertiesInput, propertiesViewer.getExpandedElements());
  }

  /**
   * @return the array of paths for given array of objects.
   */
  private static int[][] objectsToPaths(ITreeContentProvider provider,
      Object[] input,
      Object[] objects) {
    int[][] paths = new int[objects.length][];
    for (int i = 0; i < objects.length; i++) {
      paths[i] = objectToPath(provider, input, objects[i]);
    }
    return paths;
  }

  /**
   * @return the path for given object in elements tree.
   */
  private static int[] objectToPath(ITreeContentProvider provider, Object[] input, Object object) {
    if (object == null) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
    ArrayIntList pathList = new ArrayIntList();
    while (true) {
      Object parent = provider.getParent(object);
      if (parent == null) {
        pathList.add(ArrayUtils.indexOf(input, object));
        break;
      }
      pathList.add(ArrayUtils.indexOf(provider.getChildren(parent), object));
      object = parent;
    }
    int[] path = pathList.toArray();
    ArrayUtils.reverse(path);
    return path;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Restore
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Restore selection for bindings, "Target" and "Model" pages.
   */
  public boolean setSelection(IDatabindingsProvider databindingsProvider,
      BindingElementsComposite bindingComposite,
      ObserveElementsComposite targetComposite,
      ObserveElementsComposite modelComposite) {
    boolean result = false;
    //
    List<IBindingInfo> bindings = databindingsProvider.getBindings();
    if (m_currentBindingIndex > -1 && m_currentBindingIndex < bindings.size()) {
      bindingComposite.getViewer().setSelection(
          new StructuredSelection(bindings.get(m_currentBindingIndex)),
          true);
      result = true;
    } else {
      if (setPageSelection(databindingsProvider, targetComposite, m_targetPage)) {
        result = true;
      }
      if (setPageSelection(databindingsProvider, modelComposite, m_modelPage)) {
        result = true;
      }
    }
    //
    return result;
  }

  /**
   * Restore page index, selection and expanded for given page.
   */
  private static boolean setPageSelection(IDatabindingsProvider databindingsProvider,
      ObserveElementsComposite observeElementsComposite,
      PageSelection page) {
    List<ObserveType> types = databindingsProvider.getTypes();
    if (page.pageIndex < 0 || page.pageIndex >= types.size()) {
      return false;
    }
    //
    try {
      observeElementsComposite.setRedraw(false);
      // show page
      observeElementsComposite.showPage(types.get(page.pageIndex));
      // prepare master objects
      TreeViewer masterViewer = observeElementsComposite.getMasterViewer();
      ITreeContentProvider masterProvider =
          (ITreeContentProvider) masterViewer.getContentProvider();
      Object[] masterInput = masterProvider.getElements(masterViewer.getInput());
      // restore master selection
      Object masterSelection = pathToObject(masterProvider, masterInput, page.masterSelection);
      if (masterSelection != null) {
        masterViewer.setSelection(new StructuredSelection(masterSelection));
      }
      // restore master expanded
      Object[] masterExpanded = pathsToObjects(masterProvider, masterInput, page.masterExpanded);
      if (masterExpanded.length > 0) {
        masterViewer.collapseAll();
        masterViewer.setExpandedElements(masterExpanded);
      }
      // prepare properties objects
      TreeViewer propertiesViewer = observeElementsComposite.getPropertiesViewer();
      ITreeContentProvider propertiesProvider =
          (ITreeContentProvider) propertiesViewer.getContentProvider();
      Object[] propertiesInput = propertiesProvider.getElements(propertiesViewer.getInput());
      // restore properties selection
      Object propertiesSelection =
          pathToObject(propertiesProvider, propertiesInput, page.propertiesSelection);
      if (propertiesSelection != null) {
        propertiesViewer.setSelection(new StructuredSelection(propertiesSelection));
      }
      // restore properties expanded
      Object[] propertiesExpanded =
          pathsToObjects(propertiesProvider, propertiesInput, page.propertiesExpanded);
      if (propertiesExpanded.length > 0) {
        propertiesViewer.collapseAll();
        propertiesViewer.setExpandedElements(propertiesExpanded);
      }
    } finally {
      observeElementsComposite.setRedraw(true);
    }
    //
    return true;
  }

  /**
   * @return the array of objects for given array of paths.
   */
  private static Object[] pathsToObjects(ITreeContentProvider provider,
      Object[] input,
      int[][] paths) {
    List<Object> objects = Lists.newArrayList();
    for (int[] path : paths) {
      Object object = pathToObject(provider, input, path);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects.toArray();
  }

  /**
   * @return the object of elements tree for given path.
   */
  private static Object pathToObject(ITreeContentProvider provider, Object[] input, int[] path) {
    Object[] elements = input;
    Object object = null;
    for (int index : path) {
      if (index < 0 || index >= elements.length) {
        return null;
      }
      object = elements[index];
      elements = provider.getChildren(object);
    }
    return object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Page
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class PageSelection {
    public int pageIndex = -1;
    public int[] masterSelection;
    public int[][] masterExpanded;
    public int[] propertiesSelection;
    public int[][] propertiesExpanded;
  }
}