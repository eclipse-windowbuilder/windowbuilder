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
package org.eclipse.wb.internal.core.xml.editor.actions;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.editor.actions.ActionUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMementoTransfer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Action} for {@link ActionFactory#COPY}.
 * 
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public class CopyAction extends Action {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CopyAction(IEditPartViewer viewer) {
    m_viewer = viewer;
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        firePropertyChange(ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
      }
    });
    // copy presentation
    ActionUtils.copyPresentation(this, ActionFactory.COPY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<XmlObjectMemento> m_mementos;

  @Override
  public void run() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        List<EditPart> editParts = m_viewer.getSelectedEditParts();
        m_mementos = getMementos(editParts);
        doCopy(m_mementos);
      }
    });
  }

  @Override
  public boolean isEnabled() {
    List<EditPart> editParts = m_viewer.getSelectedEditParts();
    return hasMementos(editParts);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Memento
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copies {@link XmlObjectMemento}'s into {@link Clipboard}.
   */
  static void doCopy(List<XmlObjectMemento> mementos) {
    if (mementos != null) {
      Clipboard clipboard = new Clipboard(Display.getCurrent());
      try {
        clipboard.setContents(
            new Object[]{mementos},
            new Transfer[]{XmlObjectMementoTransfer.getInstance()});
      } finally {
        clipboard.dispose();
      }
    }
  }

  /**
   * @return <code>true</code> if given {@link XmlObjectInfo}'s can be copy/pasted.
   */
  static boolean hasMementos(final List<EditPart> editParts) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        // selection required
        if (editParts.isEmpty()) {
          return false;
        }
        // check that JavaInfoMemento's can be created
        for (EditPart editPart : editParts) {
          // prepare model
          XmlObjectInfo object;
          {
            Object model = editPart.getModel();
            if (model instanceof XmlObjectInfo) {
              object = (XmlObjectInfo) model;
            } else {
              return false;
            }
          }
          // check for memento
          if (!XmlObjectMemento.hasMemento(object)) {
            return false;
          }
        }
        // OK
        return true;
      }
    }, false);
  }

  /**
   * @return the {@link XmlObjectMemento}'s with copy/paste information for given
   *         {@link XmlObjectInfo}'s.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static List<XmlObjectMemento> getMementos(final List<EditPart> editParts) {
    return (List<XmlObjectMemento>) ExecutionUtils.runObjectLog(new RunnableObjectEx() {
      public Object runObject() throws Exception {
        return getMementoEx(editParts);
      }
    }, null);
  }

  /**
   * Implementation of {@link #getMementos(List)}.
   */
  private static List<XmlObjectMemento> getMementoEx(List<EditPart> editParts) throws Exception {
    // prepare objects
    List<XmlObjectInfo> objects = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      Object model = editPart.getModel();
      if (model instanceof XmlObjectInfo) {
        XmlObjectInfo object = (XmlObjectInfo) model;
        objects.add(object);
      }
    }
    // don't copy child, if we copy its parent
    for (Iterator<XmlObjectInfo> I = objects.iterator(); I.hasNext();) {
      XmlObjectInfo object = I.next();
      if (object.getParent(objects) != null) {
        I.remove();
      }
    }
    // prepare mementos
    List<XmlObjectMemento> mementos = Lists.newArrayList();
    for (XmlObjectInfo object : objects) {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(object);
      mementos.add(memento);
    }
    // OK, final result
    return mementos;
  }
}
