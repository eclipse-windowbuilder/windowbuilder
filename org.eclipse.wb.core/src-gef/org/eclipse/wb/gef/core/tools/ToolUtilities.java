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
package org.eclipse.wb.gef.core.tools;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;

import java.util.Collections;
import java.util.List;

/**
 * Utilities for {@link Tool}'s.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class ToolUtilities {
  /**
   * Returns a list containing the top level selected {@link EditPart}'s based on the viewer's
   * selection. If selection parents of edit parts is differed returns empty list.
   */
  public static List<EditPart> getSelectionWithoutDependants(IEditPartViewer viewer) {
    List<EditPart> operationSet = Lists.newArrayList();
    // add selected EditPart's only if their parent is not added yet
    {
      List<EditPart> selectedParts = viewer.getSelectedEditParts();
      for (EditPart part : selectedParts) {
        if (!isAncestorContainedIn(selectedParts, part)) {
          operationSet.add(part);
        }
      }
    }
    // check that all EditPart's have same parent
    {
      EditPart commonParent = null;
      for (EditPart editPart : operationSet) {
        if (commonParent == null) {
          commonParent = editPart.getParent();
        } else if (editPart.getParent() != commonParent) {
          return Collections.emptyList();
        }
      }
    }
    // OK, we have valid set
    return operationSet;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if <code>containers</code> contains parent of given {@link EditPart}.
   */
  private static boolean isAncestorContainedIn(List<EditPart> container, EditPart part) {
    EditPart parent = part.getParent();
    while (parent != null) {
      if (container.contains(parent)) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }
}