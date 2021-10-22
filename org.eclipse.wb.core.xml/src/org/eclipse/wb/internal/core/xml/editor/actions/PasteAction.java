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

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.PasteTool;
import org.eclipse.wb.internal.core.editor.actions.ActionUtils;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMementoTransfer;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.ActionFactory;

import java.util.List;

/**
 * Implementation of {@link Action} for {@link ActionFactory#PASTE}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public class PasteAction extends Action {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PasteAction(IEditPartViewer viewer) {
    m_viewer = viewer;
    // copy presentation
    ActionUtils.copyPresentation(this, ActionFactory.PASTE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    // check for SWT paste
    {
      Control focusControl = Display.getCurrent().getFocusControl();
      if (focusControl instanceof Text) {
        ((Text) focusControl).paste();
        return;
      }
    }
    // do JavaInfo paste
    List<XmlObjectMemento> mementos = getMementos();
    if (mementos != null) {
      m_viewer.getEditDomain().setActiveTool(new PasteTool(mementos));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link XmlObjectMemento} in {@link Clipboard}.
   */
  @SuppressWarnings("unchecked")
  private static List<XmlObjectMemento> getMementos() {
    Clipboard clipboard = new Clipboard(Display.getCurrent());
    try {
      return (List<XmlObjectMemento>) clipboard.getContents(XmlObjectMementoTransfer.getInstance());
    } finally {
      clipboard.dispose();
    }
  }
}
