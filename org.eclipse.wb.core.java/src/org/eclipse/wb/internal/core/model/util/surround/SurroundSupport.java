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
package org.eclipse.wb.internal.core.model.util.surround;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.ui.IActionSingleton;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;
import java.util.Set;

/**
 * Helper for surrounding {@link AbstractComponentInfo}'s with some container.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class SurroundSupport<C extends AbstractComponentInfo, T extends AbstractComponentInfo> {
  private static final String SURROUND_POINT = "org.eclipse.wb.core.surroundWith";
  private final C m_sourceContainer;
  private final Class<T> m_componentClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SurroundSupport(C sourceContainer, Class<T> componentClass) {
    m_sourceContainer = sourceContainer;
    m_componentClass = componentClass;
    m_sourceContainer.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (objects != null && object != null) {
          boolean shouldExecute = objects.indexOf(object) == 0;
          addSurroundMenu(objects, shouldExecute, manager);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares components to surround, appends <code>"Surround"</code> {@link IMenuManager} with
   * separate actions for each possible target surround container.
   */
  @SuppressWarnings("unchecked")
  private void addSurroundMenu(List<? extends ObjectInfo> objects,
      boolean shouldExecute,
      IMenuManager manager) throws Exception {
    if (objects.isEmpty()) {
      return;
    }
    if (!isActive()) {
      return;
    }
    // prepare selected components
    Set<T> selectedComponents = Sets.newHashSet();
    for (ObjectInfo selectedObject : objects) {
      // only components of source container
      if (selectedObject.getParent() != m_sourceContainer) {
        return;
      }
      // only components of valid type
      if (!m_componentClass.isAssignableFrom(selectedObject.getClass())) {
        return;
      }
      // OK, add this component
      T component = (T) selectedObject;
      selectedComponents.add(component);
    }
    // sort components to be in same order, as in source container
    List<T> sortedComponents = Lists.newArrayList();
    for (ObjectInfo component : m_sourceContainer.getChildren()) {
      if (selectedComponents.contains(component)) {
        sortedComponents.add((T) component);
      }
    }
    // validate
    if (!validateComponents(sortedComponents)) {
      return;
    }
    // add "Surround with" sub-menu
    MenuManagerEx surroundManager;
    {
      surroundManager = new MenuManagerEx(ModelMessages.SurroundSupport_surorundManager);
      surroundManager.setImage(DesignerPlugin.getImage("actions/surround/surround.png"));
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, surroundManager);
    }
    // add actions
    addSurroundActions(surroundManager, sortedComponents);
  }

  /**
   * Adds separate actions for each possible target surround container.
   */
  private void addSurroundActions(MenuManagerEx surroundManager, List<T> components)
      throws Exception {
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(SURROUND_POINT, "target");
    String sourceToolkitID = m_sourceContainer.getDescription().getToolkit().getId();
    for (IConfigurationElement element : elements) {
      if (ExternalFactoriesHelper.getRequiredAttribute(element, "toolkit").equals(sourceToolkitID)) {
        ISurroundTarget<C, T> target =
            ExternalFactoriesHelper.createExecutableExtension(element, "class");
        if (target.validate(components)) {
          surroundManager.add(new SurroundAction(components, target));
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Action for single {@link ISurroundTarget}.
   */
  private class SurroundAction extends ObjectInfoAction implements IActionSingleton {
    private final List<T> m_components;
    private final ISurroundTarget<C, T> m_target;
    private final AstEditor m_editor;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SurroundAction(List<T> components, ISurroundTarget<C, T> target) throws Exception {
      super(m_sourceContainer);
      m_components = components;
      m_target = target;
      m_editor = m_sourceContainer.getEditor();
      // presentation
      setText(m_target.getText(m_editor));
      setIcon(m_target.getIcon(m_editor));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void runEx() throws Exception {
      // prepare container
      final C container;
      {
        container = m_target.createContainer(m_editor);
        addContainer(container, m_components);
        m_target.afterContainerAdd(container, m_components);
      }
      // do refresh
      {
        ObjectInfo rootObject = container.getRoot();
        // disable refreshed() to avoid conflict with UndoManager
        rootObject.putArbitraryValue(ObjectInfo.KEY_NO_REFRESHED_BROADCAST, Boolean.FALSE);
        try {
          rootObject.refresh();
        } finally {
          rootObject.removeArbitraryValue(ObjectInfo.KEY_NO_REFRESHED_BROADCAST);
        }
      }
      // do move
      moveComponents(container);
      moveDone(container, m_components);
      ExecutionUtils.runLogLater(new RunnableEx() {
        public void run() throws Exception {
          container.getBroadcastObject().select(ImmutableList.of(container));
        }
      });
    }

    private void moveComponents(C container) throws Exception {
      // try to move using ISurroundProcessor
      for (ISurroundProcessor<C, T> processor : getSurroundProcessors()) {
        if (processor.filter(m_sourceContainer, container)) {
          processor.move(m_sourceContainer, container, m_components);
          return;
        }
      }
      // move components using default ISurroundTarget behavior
      m_target.beforeComponentsMove(container, m_components);
      for (T component : m_components) {
        moveComponent(m_target, container, component);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link SurroundSupport} is active, for example layout manager
   *         should check if it is still active on its container.
   */
  protected boolean isActive() {
    return true;
  }

  /**
   * Performs validation for selected/sorted components.
   *
   * @return <code>true</code> if components are valid and can be used to extract from source
   *         container.
   */
  protected boolean validateComponents(List<T> components) throws Exception {
    return true;
  }

  /**
   * Adds newly created container into appropriate location (usually source container).
   */
  protected abstract void addContainer(C container, List<T> components) throws Exception;

  /**
   * Moves given component into new container.
   */
  protected void moveComponent(ISurroundTarget<C, T> target, C container, T component)
      throws Exception {
    target.move(container, component);
  }

  /**
   * Notifies that all components were moved.
   */
  protected void moveDone(C container, List<T> components) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ISurroundProcessor}'s registered for source toolkit.
   */
  private List<ISurroundProcessor<C, T>> getSurroundProcessors() {
    List<ISurroundProcessor<C, T>> typedProcessors = Lists.newArrayList();
    //
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(SURROUND_POINT, "processor");
    String sourceToolkitID = m_sourceContainer.getDescription().getToolkit().getId();
    for (IConfigurationElement element : elements) {
      if (ExternalFactoriesHelper.getRequiredAttribute(element, "toolkit").equals(sourceToolkitID)) {
        ISurroundProcessor<C, T> processor =
            ExternalFactoriesHelper.createExecutableExtension(element, "class");
        typedProcessors.add(processor);
      }
    }
    //
    return typedProcessors;
  }
}
