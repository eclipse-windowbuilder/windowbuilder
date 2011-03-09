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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ButtonGroupInfo}, direct child of root {@link JavaInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ButtonGroupContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing or new {@link ButtonGroupContainerInfo} for given root.
   */
  public static ButtonGroupContainerInfo get(JavaInfo root) throws Exception {
    // try to find existing container
    ButtonGroupContainerInfo container = findContainer(root);
    if (container != null) {
      return container;
    }
    // add new container
    container = new ButtonGroupContainerInfo();
    root.addChild(container);
    return container;
  }

  /**
   * @return all {@link ButtonGroupInfo}'s for given root.
   */
  public static List<ButtonGroupInfo> getButtonGroups(JavaInfo root) {
    ButtonGroupContainerInfo container = findContainer(root);
    if (container != null) {
      return container.getChildren(ButtonGroupInfo.class);
    }
    return Collections.emptyList();
  }

  /**
   * Creates new instance of {@link ButtonGroupInfo} and adds it to the
   * {@link ButtonGroupContainerInfo}.
   * 
   * @return the new added {@link ButtonGroupInfo}.
   */
  public static ButtonGroupInfo add(JavaInfo root, String className) throws Exception {
    // create ButtonGroupInfo
    ButtonGroupInfo buttonGroup;
    {
      AstEditor editor = root.getEditor();
      buttonGroup =
          (ButtonGroupInfo) JavaInfoUtils.createJavaInfo(
              editor,
              className,
              new ConstructorCreationSupport());
    }
    // add ButtonGroupInfo to ButtonGroupContainerInfo
    {
      JavaInfoUtils.add(
          buttonGroup,
          new FieldInitializerVariableSupport(buttonGroup),
          PureFlatStatementGenerator.INSTANCE,
          AssociationObjects.empty(),
          root,
          null);
      root.removeChild(buttonGroup);
      ButtonGroupContainerInfo.get(root).addChild(buttonGroup);
    }
    //
    return buttonGroup;
  }

  /**
   * @return find the existing {@link ButtonGroupContainerInfo} for given root.
   */
  private static ButtonGroupContainerInfo findContainer(JavaInfo root) {
    for (ObjectInfo child : root.getChildren()) {
      if (child instanceof ButtonGroupContainerInfo) {
        return (ButtonGroupContainerInfo) child;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{org.eclipse.wb.internal.swing.model.bean.ButtonGroupContainerInfo}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new DefaultObjectPresentation(this) {
      public String getText() throws Exception {
        return "(button groups)";
      }

      @Override
      public Image getIcon() throws Exception {
        return Activator.getImage("info/ButtonGroup/ButtonGroup_container.gif");
      }
    };
  }
}