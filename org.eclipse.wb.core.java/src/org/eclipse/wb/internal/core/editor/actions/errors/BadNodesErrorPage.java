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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import java.io.StringWriter;

/**
 * Implementation of {@link IErrorPage} for displaying {@link BadNodesCollection} from
 * {@link EditorState}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public abstract class BadNodesErrorPage implements IErrorPage {
  private AstEditor m_editor;
  private BadNodesCollection m_collection;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IErrorPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setRoot(ObjectInfo rootObject) {
    if (rootObject instanceof JavaInfo) {
      JavaInfo javaInfo = (JavaInfo) rootObject;
      m_editor = javaInfo.getEditor();
      EditorState editorState = EditorState.get(m_editor);
      m_collection = getCollection(editorState);
    } else {
      m_collection = null;
    }
  }

  public final boolean hasErrors() {
    return m_collection != null && !m_collection.isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private List m_nodesList;
  private Browser m_browser;

  public final Control create(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container);
    // create List with bad nodes
    {
      Group group = new Group(container, SWT.NONE);
      GridDataFactory.create(group).grabH().fill();
      GridLayoutFactory.create(group);
      group.setText(Messages.BadNodesErrorPage_nodesGroup);
      //
      m_nodesList = new List(group, SWT.BORDER | SWT.V_SCROLL);
      GridDataFactory.create(m_nodesList).hintC(100, 10).grab().fill();
      // fill items
      if (m_collection != null) {
        for (BadNodeInformation badNode : m_collection.nodes()) {
          try {
            m_nodesList.add(m_editor.getSource(badNode.getNode()));
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      }
      // add selection listener
      m_nodesList.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          showException();
        }
      });
    }
    // create Text for displaying selected bad node
    {
      Group group = new Group(container, SWT.NONE);
      GridDataFactory.create(group).grab().fill();
      GridLayoutFactory.create(group);
      group.setText(Messages.BadNodesErrorPage_nodeGroup);
      //
      m_browser = new Browser(group, SWT.BORDER);
      GridDataFactory.create(m_browser).hintC(100, 15).grab().fill();
    }
    // show first node
    if (m_nodesList.getItemCount() != 0) {
      m_nodesList.select(0);
      showException();
    }
    //
    return container;
  }

  /**
   * Shows given {@link BadNodeInformation}.
   */
  private void showException() {
    try {
      int index = m_nodesList.getSelectionIndex();
      BadNodeInformation badNode = m_collection.nodes().get(index);
      //
      StringWriter stringWriter = new StringWriter();
      {
        // node source
        stringWriter.write("<pre>");
        stringWriter.write(m_editor.getSource(badNode.getNode()));
        stringWriter.write("</pre>");
        stringWriter.write("<p>");
        // exception
        {
          Throwable exception = badNode.getException();
          exception = DesignerExceptionUtils.rewriteException(exception);
          String text = DesignerExceptionUtils.getExceptionHTML(exception);
          stringWriter.write(text);
        }
      }
      // set text
      m_browser.setText(stringWriter.toString());
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link BadNodesCollection} to display for given {@link EditorState}.
   */
  protected abstract BadNodesCollection getCollection(EditorState editorState);
}
