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
package org.eclipse.wb.core.gefTree.part;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.gef.tree.TreeViewer;
import org.eclipse.wb.internal.gef.tree.policies.AutoExpandEditPolicy;
import org.eclipse.wb.internal.gef.tree.policies.SelectionEditPolicy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;

import java.util.Collections;
import java.util.List;

/**
 * {@link TreeEditPart} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree
 */
public class ObjectEditPart extends TreeEditPart {
  private final ObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectEditPart(ObjectInfo object) {
    m_object = object;
    setModel(m_object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private Listener m_updatePresentationListener;

  @Override
  public void activate() {
    super.activate();
    if (m_object.isRoot()) {
      final TreeViewer viewer = (TreeViewer) getViewer();
      final Tree tree = viewer.getTree();
      // update presentation only when EditPart become visible
      {
        m_updatePresentationListener = new Listener() {
          public void handleEvent(Event event) {
            if (event.item.getData() instanceof ObjectEditPart) {
              ObjectEditPart editPart = (ObjectEditPart) event.item.getData();
              editPart.update();
            }
          }
        };
        tree.addListener(SWT.PaintItem, m_updatePresentationListener);
      }
      // refresh hierarchy
      m_object.addBroadcastListener(new ObjectEventListener() {
        private List<ObjectInfo> m_delayedSelectionObjects;

        @Override
        public void refreshed() throws Exception {
          // do in setRedraw(false) to avoid flashing after component moving
          tree.setRedraw(false);
          try {
            refresh();
            {
              setSelectionIfAllEditParts(m_delayedSelectionObjects);
              m_delayedSelectionObjects = null;
            }
            viewer.setSelectionToTreeWidget();
          } finally {
            tree.setRedraw(true);
          }
        }

        @Override
        public void select(List<? extends ObjectInfo> objects) throws Exception {
          m_delayedSelectionObjects = null;
          // set selection now, or delay
          if (!setSelectionIfAllEditParts(objects)) {
            m_delayedSelectionObjects = Lists.newArrayList(objects);
          }
        }

        private boolean setSelectionIfAllEditParts(List<? extends ObjectInfo> objects) {
          List<EditPart> editParts = getAllEditParts(objects);
          if (editParts == null) {
            return false;
          }
          viewer.setSelection(editParts);
          return true;
        }

        /**
         * @return {@link EditPart} for each given model, or <code>null</code> if some model has no
         *         {@link EditPart}.
         */
        private List<EditPart> getAllEditParts(List<? extends ObjectInfo> objects) {
          if (objects == null) {
            return null;
          }
          List<EditPart> editParts = Lists.newArrayList();
          for (ObjectInfo object : objects) {
            EditPart editPart = viewer.getEditPartByModel(object);
            if (editPart == null) {
              return null;
            }
            editParts.add(editPart);
          }
          return editParts;
        }
      });
    }
  }

  @Override
  public void deactivate() {
    if (m_updatePresentationListener != null) {
      TreeViewer viewer = (TreeViewer) getViewer();
      Tree tree = viewer.getTree();
      tree.removeListener(SWT.PaintItem, m_updatePresentationListener);
    }
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visual
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_updateRequired;

  @Override
  protected final void refreshVisuals() {
    m_updateRequired = true;
  }

  private void update() {
    if (m_updateRequired) {
      m_updateRequired = false;
      ExecutionUtils.runLogUI(new RunnableEx() {
        public void run() throws Exception {
          update0();
        }
      });
    }
  }

  private void update0() {
    Image image = ObjectsLabelProvider.INSTANCE.getImage(m_object);
    String text = ObjectsLabelProvider.INSTANCE.getText(m_object);
    if (image != null && !image.isDisposed()) {
      getWidget().setImage(image);
    }
    getWidget().setText(text);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new SelectionEditPolicy());
    installEditPolicy(new AutoExpandEditPolicy());
    refreshEditPolicies();
  }

  /**
   * Installs {@link EditPolicy}'s after model refresh.
   */
  protected void refreshEditPolicies() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<List<?>>() {
      public List<?> runObject() throws Exception {
        return m_object.getPresentation().getChildrenTree();
      }
    }, Collections.emptyList());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh() {
    super.refresh();
    refreshEditPolicies();
  }
}
