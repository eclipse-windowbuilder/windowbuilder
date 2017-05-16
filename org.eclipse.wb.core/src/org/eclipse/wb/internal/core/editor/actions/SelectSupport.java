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
package org.eclipse.wb.internal.core.editor.actions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IDescriptionHelper;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * Support for "Select" sub-menu in context menu, plus hot keys for same actions.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public final class SelectSupport {
  private final ObjectInfo m_rootObject;
  private final IEditPartViewer m_graphicalViewer;
  private final IEditPartViewer m_treeViewer;
  private final Set<ObjectInfo> m_selectedObjects = Sets.newHashSet();
  private final Set<ObjectInfo> m_selectingSet = Sets.newLinkedHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectSupport(ObjectInfo rootObject,
      IEditPartViewer graphicalViewer,
      IEditPartViewer treeViewer) {
    m_rootObject = rootObject;
    m_graphicalViewer = graphicalViewer;
    m_treeViewer = treeViewer;
    addKeyDownListener(m_graphicalViewer);
    addKeyDownListener(m_treeViewer);
    rootObject.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void dispose() throws Exception {
        removeKeyDownListener(m_graphicalViewer);
        removeKeyDownListener(m_treeViewer);
      }

      @Override
      public void addContextMenu(List<? extends ObjectInfo> selectedObjects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        contributeActions(manager);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Listener m_keyListener = new Listener() {
    public void handleEvent(Event event) {
      int cmdModifierValue = getCommandModifierValue();
      if (event.keyCode == 'a' && event.stateMask == cmdModifierValue) {
        selectAll();
      }
      if (event.keyCode == 'a' && event.stateMask == (cmdModifierValue | SWT.SHIFT)) {
        selectSameType();
      }
      if (event.keyCode == 'a' && event.stateMask == (cmdModifierValue | SWT.ALT)) {
        selectSameParent();
      }
    }
  };

  /**
   * Adds {@link SWT#KeyDown} listener for {@link Control} of viewer.
   */
  private void addKeyDownListener(IEditPartViewer viewer) {
    Control control = viewer.getControl();
    control.addListener(SWT.KeyDown, m_keyListener);
  }

  /**
   * Removes {@link SWT#KeyDown} listener from {@link Control} of viewer.
   */
  private void removeKeyDownListener(IEditPartViewer viewer) {
    Control control = viewer.getControl();
    if (!control.isDisposed()) {
      control.removeListener(SWT.KeyDown, m_keyListener);
    }
  }

  /**
   * @return the value of M1 modifier - "Cmd" or "Ctrl".
   */
  private static int getCommandModifierValue() {
    return EnvironmentUtils.IS_MAC ? SWT.COMMAND : SWT.CTRL;
  }

  /**
   * @return the name of M1 modifier - "Cmd" or "Ctrl".
   */
  private static String getCommandModifierName() {
    return EnvironmentUtils.IS_MAC ? "Cmd" : "Ctrl";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void selectAll() {
    doBeforeSelect();
    m_rootObject.accept(new ObjectInfoVisitor() {
      @Override
      public boolean visit(ObjectInfo object) throws Exception {
        m_selectingSet.add(object);
        return true;
      }
    });
    selectByModels();
  }

  private void selectSameType() {
    doBeforeSelect();
    final IDescriptionHelper descriptionHelper = GlobalState.getDescriptionHelper();
    for (ObjectInfo selectedObject : m_selectedObjects) {
      IComponentDescription selectedDescription = descriptionHelper.getDescription(selectedObject);
      if (selectedDescription != null) {
        final Class<?> selectedClass = selectedDescription.getComponentClass();
        m_rootObject.accept(new ObjectInfoVisitor() {
          @Override
          public boolean visit(ObjectInfo object) throws Exception {
            IComponentDescription description = descriptionHelper.getDescription(object);
            if (description != null && description.getComponentClass() == selectedClass) {
              m_selectingSet.add(object);
            }
            return true;
          }
        });
      }
    }
    selectByModels();
  }

  private void selectSameParent() {
    doBeforeSelect();
    for (ObjectInfo selectedObject : m_selectedObjects) {
      ObjectInfo selectedParent = selectedObject.getParent();
      if (selectedParent != null) {
        m_selectingSet.addAll(selectedParent.getChildren());
      }
    }
    selectByModels();
  }

  /**
   * Method which prepares information for "select" implementation.
   */
  private void doBeforeSelect() {
    m_selectingSet.clear();
    m_selectedObjects.clear();
    List<EditPart> selectedEditParts = m_graphicalViewer.getSelectedEditParts();
    for (EditPart editPart : selectedEditParts) {
      Object model = editPart.getModel();
      if (model instanceof ObjectInfo) {
        m_selectedObjects.add((ObjectInfo) model);
      }
    }
  }

  /**
   * Sets selection in {@link #m_graphicalViewer} using prepared {@link #m_selectingSet} models.
   */
  private void selectByModels() {
    List<EditPart> editParts = Lists.newArrayList();
    for (ObjectInfo object : m_selectingSet) {
      EditPart editPart = m_graphicalViewer.getEditPartByModel(object);
      if (editPart != null) {
        editParts.add(editPart);
      }
    }
    m_graphicalViewer.setSelection(editParts);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void contributeActions(IMenuManager manager) {
    IMenuManager selectMenuManager = new MenuManager("Select");
    manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, selectMenuManager);
    // add separate actions
    String cmdModifierName = getCommandModifierName();
    {
      String text = MessageFormat.format("All\t{0}+A", cmdModifierName);
      selectMenuManager.add(new SelectAction(text, "all.png") {
        @Override
        protected void runEx() throws Exception {
          selectAll();
        }
      });
    }
    {
      String text = MessageFormat.format("All of Same Type\t{0}+Shift+A", cmdModifierName);
      selectMenuManager.add(new SelectAction(text, "sameType.png") {
        @Override
        protected void runEx() throws Exception {
          selectSameType();
        }
      });
    }
    {
      String text = MessageFormat.format("All on Same Parent\t{0}+Alt+A", cmdModifierName);
      selectMenuManager.add(new SelectAction(text, "sameParent.png") {
        @Override
        protected void runEx() throws Exception {
          selectSameParent();
        }
      });
    }
  }

  /**
   * Abstract super class for selecting actions.
   */
  private abstract class SelectAction extends ObjectInfoAction {
    public SelectAction(String text, String imageName) {
      super(m_rootObject);
      setText(text);
      setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/select/" + imageName));
    }
  }
}
