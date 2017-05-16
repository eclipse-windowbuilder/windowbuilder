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
package org.eclipse.wb.internal.core.model.util.factory;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * Helper for assisting user in operations with factories:
 * <ul>
 * <li>creating static factory method based on currently existing component, with existing
 * properties and method invocations;</li>
 * <li>applying factory method on existing components (with compatible types).</li>
 * </ul>
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class FactoryActionsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes actions for factory operations.
   *
   * @param component
   *          the {@link JavaInfo} the target component.
   * @param manager
   *          the {@link IContributionManager} to add actions to.
   */
  public static void contribute(JavaInfo component, IContributionManager manager) throws Exception {
    // only constructor creation is supported, no "factory-on-factory-on-factory" please :-)
    if (!(component.getCreationSupport() instanceof ConstructorCreationSupport)) {
      return;
    }
    // add "Factory" sub-menu
    MenuManagerEx factoryManager;
    {
      factoryManager = new MenuManagerEx(ModelMessages.FactoryActionsSupport_factoryManager);
      factoryManager.setImage(DesignerPlugin.getImage("actions/factory/factory.png"));
      manager.appendToGroup(IContextMenuConstants.GROUP_INHERITANCE, factoryManager);
    }
    // add "factory" actions
    new FactoryActionsSupport(component).contribute(factoryManager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final JavaInfo m_component;
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private FactoryActionsSupport(JavaInfo component) {
    m_component = component;
    m_editor = m_component.getEditor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes "factory" actions for this component.
   *
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  private void contribute(IContributionManager manager) throws Exception {
    // factories from same package
    {
      IPackageFragment currentPackage = (IPackageFragment) m_editor.getModelUnit().getParent();
      // check all methods in all factories
      List<ICompilationUnit> factoryUnits =
          FactoryDescriptionHelper.getFactoryUnits(m_editor, currentPackage);
      for (ICompilationUnit unit : factoryUnits) {
        String typeName = unit.findPrimaryType().getFullyQualifiedName();
        addApplyActions(manager, typeName);
      }
    }
    // previous factories
    manager.add(new Separator());
    {
      String[] previousTypeNames = getPreviousTypeNames(m_component);
      for (String typeName : previousTypeNames) {
        if (m_editor.getJavaProject().findType(typeName) != null) {
          addApplyActions(manager, typeName);
        }
      }
    }
    manager.add(new FactorySelectAction(m_component));
    // create factory
    manager.add(new Separator());
    manager.add(new FactoryCreateAction(m_component));
  }

  /**
   * Adds {@link FactoryApplyAction}'s from given factory type.
   */
  private void addApplyActions(IContributionManager manager, String typeName) throws Exception {
    ClassLoader classLoader = EditorState.get(m_editor).getEditorLoader();
    Class<?> factoryClass = classLoader.loadClass(typeName);
    Collection<FactoryMethodDescription> descriptions =
        FactoryDescriptionHelper.getDescriptionsMap(m_editor, factoryClass, true).values();
    for (FactoryMethodDescription description : descriptions) {
      Class<?> componentClass = m_component.getDescription().getComponentClass();
      if (componentClass.isAssignableFrom(description.getReturnClass())) {
        manager.add(new FactoryApplyAction(m_component, description));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Previous factory types
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_PREVIOUS_FACTORIES =
      new QualifiedName(DesignerPlugin.PLUGIN_ID, "previousFactoryTypes");
  private static final int MAX_PREVIOUS_FACTORIES = 5;

  /**
   * Clears the factory types history.
   */
  static void clearPreviousTypeNames(JavaInfo component) throws CoreException {
    IProject project = component.getEditor().getJavaProject().getProject();
    project.setPersistentProperty(KEY_PREVIOUS_FACTORIES, null);
  }

  /**
   * @return the names of factory types that were selected by user in past, may be empty array, but
   *         not <code>null</code>.
   */
  static String[] getPreviousTypeNames(JavaInfo component) throws CoreException {
    IProject project = component.getEditor().getJavaProject().getProject();
    String possiblePreviousTypeNames = project.getPersistentProperty(KEY_PREVIOUS_FACTORIES);
    if (possiblePreviousTypeNames != null) {
      return StringUtils.split(possiblePreviousTypeNames, ',');
    }
    // no previous types
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * Adds new factory type into history, but limit history to some size.
   */
  static void addPreviousTypeName(JavaInfo component, String typeName) throws CoreException {
    String[] typeNames = getPreviousTypeNames(component);
    // move element into head
    typeNames = (String[]) ArrayUtils.removeElement(typeNames, typeName);
    typeNames = (String[]) ArrayUtils.add(typeNames, 0, typeName);
    // limit history size
    if (typeNames.length > MAX_PREVIOUS_FACTORIES) {
      typeNames = (String[]) ArrayUtils.remove(typeNames, typeNames.length - 1);
    }
    // set new type names
    IProject project = component.getEditor().getJavaProject().getProject();
    project.setPersistentProperty(KEY_PREVIOUS_FACTORIES, StringUtils.join(typeNames, ','));
  }
}
