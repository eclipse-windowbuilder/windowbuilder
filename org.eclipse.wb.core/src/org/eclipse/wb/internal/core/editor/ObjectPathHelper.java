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

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.viewers.ITreeContentProvider;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Helper for remembering path and dump for objects.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class ObjectPathHelper {
  private final ITreeContentProvider m_componentsProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectPathHelper(ITreeContentProvider componentsProvider) {
    m_componentsProvider = componentsProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of paths for given array of objects.
   */
  public int[][] getObjectsPaths(Object[] objects) {
    final int[][] paths = new int[objects.length][];
    for (int objectIndex = 0; objectIndex < objects.length; objectIndex++) {
      Object object = objects[objectIndex];
      paths[objectIndex] = getObjectPath(object);
    }
    return paths;
  }

  /**
   * @return the path for given object in components tree.
   */
  private int[] getObjectPath(Object object) {
    ArrayIntList path = new ArrayIntList();
    while (true) {
      Object parent = m_componentsProvider.getParent(object);
      if (parent == null) {
        break;
      }
      // add index
      int index = ArrayUtils.indexOf(m_componentsProvider.getChildren(parent), object);
      path.add(index);
      // go to parent
      object = parent;
    }
    // convert to array
    int[] finalPath = path.toArray();
    ArrayUtils.reverse(finalPath);
    return finalPath;
  }

  /**
   * @return the array of objects for given array of paths.
   */
  public Object[] getObjectsForPaths(int[][] paths) {
    Object[] objects = new Object[paths.length];
    for (int i = 0; i < paths.length; i++) {
      int[] path = paths[i];
      Object object = getObjectForPath(path);
      // add object to selection
      if (object != null) {
        objects[i] = object;
      }
    }
    return objects;
  }

  /**
   * @return the object of components tree for given path.
   */
  private Object getObjectForPath(int[] path) {
    try {
      Object object = m_componentsProvider.getElements(null)[0];
      for (int index : path) {
        object = m_componentsProvider.getChildren(object)[index];
      }
      return object;
    } catch (Throwable e) {
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dump
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text dump of given {@link ObjectInfo} and its children.
   */
  public static String getObjectsDump(ObjectInfo objectInfo, int level) {
    StringBuilder result = new StringBuilder();
    result.append(StringUtils.repeat(" ", level));
    // add this object
    result.append(objectInfo.getClass().getName());
    result.append("\n");
    // add children
    for (ObjectInfo child : objectInfo.getChildren()) {
      result.append(getObjectsDump(child, level + 1));
    }
    //
    return result.toString();
  }
}
