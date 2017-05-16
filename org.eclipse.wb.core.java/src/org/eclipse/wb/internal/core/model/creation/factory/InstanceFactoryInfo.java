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
package org.eclipse.wb.internal.core.model.creation.factory;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Special {@link JavaInfo} for instance factories.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class InstanceFactoryInfo extends JavaInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InstanceFactoryInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ComponentDescription} for {@link Object}, cached, initialized only one time.
   */
  private static ComponentDescription OBJECT_DESCRIPTION;

  /**
   * @return <code>true</code> if given class is the instance factory class.
   */
  public static boolean isFactory(AstEditor editor, Class<?> clazz) throws Exception {
    return !FactoryDescriptionHelper.getDescriptionsMap(editor, clazz, false).isEmpty();
  }

  /**
   * Creates new instance of {@link InstanceFactoryInfo}. It is expected that
   * {@link #isFactory(AstEditor, Class)} returned <code>true</code> for given {@link Class}, i.e.
   * that it is really instance factory.
   *
   * @return new instance {@link InstanceFactoryInfo}.
   */
  public static InstanceFactoryInfo createFactory(AstEditor editor,
      Class<?> factoryClass,
      CreationSupport creationSupport) throws Exception {
    ComponentDescription componentDescription;
    {
      componentDescription = ComponentDescriptionHelper.getDescription(editor, factoryClass);
      // use InstanceFactoryInfo as model
      componentDescription.setModelClass(InstanceFactoryInfo.class);
      // set factory icon
      {
        // prepare description for java.lang.Object
        if (OBJECT_DESCRIPTION == null) {
          OBJECT_DESCRIPTION = ComponentDescriptionHelper.getDescription(editor, Object.class);
        }
        // if icon is java.lang.Object, use default instance factory icon
        if (UiUtils.equals(componentDescription.getIcon(), OBJECT_DESCRIPTION.getIcon())) {
          Image defaultFactoryIcon = DesignerPlugin.getImage("components/factory.gif");
          componentDescription.setIcon(defaultFactoryIcon);
        }
      }
    }
    // OK, create InstanceFactoryInfo
    return (InstanceFactoryInfo) JavaInfoUtils.createJavaInfo(
        editor,
        componentDescription,
        creationSupport);
  }

  /**
   * @return the {@link List} of {@link InstanceFactoryInfo} with given {@link Class}.
   */
  public static List<InstanceFactoryInfo> getFactories(JavaInfo rootJavaInfo, Class<?> factoryClass)
      throws Exception {
    List<InstanceFactoryInfo> factories = Lists.newArrayList();
    //
    InstanceFactoryContainerInfo container = InstanceFactoryContainerInfo.get(rootJavaInfo);
    for (InstanceFactoryInfo factory : container.getChildrenFactory()) {
      if (factoryClass.isAssignableFrom(factory.getDescription().getComponentClass())) {
        factories.add(factory);
      }
    }
    //
    return factories;
  }

  /**
   * Creates new instance of {@link InstanceFactoryInfo} of given class and adds it to the
   * {@link InstanceFactoryContainerInfo}.
   *
   * @return the new added {@link InstanceFactoryInfo}.
   */
  public static InstanceFactoryInfo add(JavaInfo rootJavaInfo, Class<?> factoryClass)
      throws Exception {
    // create model
    InstanceFactoryInfo factoryInfo;
    {
      AstEditor editor = rootJavaInfo.getEditor();
      factoryInfo = createFactory(editor, factoryClass, new ConstructorCreationSupport());
    }
    // add model
    {
      JavaInfoUtils.add(
          factoryInfo,
          new FieldInitializerVariableSupport(factoryInfo),
          PureFlatStatementGenerator.INSTANCE,
          AssociationObjects.empty(),
          rootJavaInfo,
          null);
      // move to InstanceFactoryContainerInfo
      rootJavaInfo.removeChild(factoryInfo);
      InstanceFactoryContainerInfo.get(rootJavaInfo).addChild(factoryInfo);
    }
    //
    return factoryInfo;
  }
}
