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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.palette.DialogButtonEntryInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Model for {@link Dialog}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public class DialogInfo extends WindowInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // move properties "id", "text" and "default" for Button on top level
    addBroadcastListener(new JavaInfoAddProperties() {
      @Override
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        configureButtonBarProperties(javaInfo, properties);
      }
    });
    // add Button to palette
    addBroadcastListener(new PaletteEventListener() {
      @Override
      public void entries(CategoryInfo category, List<EntryInfo> entries) throws Exception {
        if (category.getId().equals("org.eclipse.wb.rcp.jface")) {
          entries.add(new DialogButtonEntryInfo());
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button bar support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "button bar" {@link CompositeInfo}, or <code>null</code> if not found.
   */
  public CompositeInfo getButtonBar() {
    final CompositeInfo[] buttonBar = new CompositeInfo[1];
    accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (buttonBar[0] == null && objectInfo instanceof CompositeInfo) {
          CompositeInfo composite = (CompositeInfo) objectInfo;
          if (isButtonBar(composite)) {
            buttonBar[0] = composite;
          }
        }
      }
    });
    return buttonBar[0];
  }

  /**
   * Moves "id/title/default" {@link Property}'s of "button" on "button bar".
   */
  private void configureButtonBarProperties(JavaInfo javaInfo, List<Property> properties)
      throws Exception {
    if (javaInfo instanceof ControlInfo
        && javaInfo.getDescription().getComponentClass().getName().equals(
            "org.eclipse.swt.widgets.Button")
        && javaInfo.getParent() == getButtonBar()) {
      for (Property property : properties) {
        if (property.getTitle().equals("Factory")) {
          IComplexPropertyEditor factoryEditor = (IComplexPropertyEditor) property.getEditor();
          Property[] factoryProperties = factoryEditor.getProperties(property);
          for (Property factoryProperty : factoryProperties) {
            if (factoryProperty.getTitle().equals("id")) {
              GenericPropertyImpl idProperty = getCopy(factoryProperty, "ID", -3);
              properties.add(0, idProperty);
            }
            if (factoryProperty.getTitle().equals("text")) {
              GenericPropertyImpl titleProperty = getCopy(factoryProperty, "Text", -2);
              properties.add(1, titleProperty);
            }
            if (factoryProperty.getTitle().equals("default")) {
              GenericPropertyImpl defaultProperty = getCopy(factoryProperty, "Default", -1);
              properties.add(2, defaultProperty);
            }
          }
          // OK, we've changes properties
          break;
        }
      }
    }
  }

  /**
   * @return the copy of given {@link GenericPropertyImpl} with new title/category.
   */
  private GenericPropertyImpl getCopy(Property property, String newTitle, int newCategory)
      throws Exception {
    GenericPropertyImpl newProperty = (GenericPropertyImpl) getArbitraryValue(property);
    if (newProperty == null) {
      newProperty = new GenericPropertyImpl((GenericPropertyImpl) property, newTitle);
      newProperty.setCategory(PropertyCategory.system(newCategory));
      putArbitraryValue(property, newProperty);
    }
    return newProperty;
  }

  /**
   * @return <code>true</code> if given {@link CompositeInfo} if "parent" in
   *         {@link Dialog#createButtonsForButtonBar(Composite)}.
   */
  public static boolean isButtonBar(CompositeInfo composite) {
    if (composite.getRoot() instanceof DialogInfo
        && composite.getCreationSupport() instanceof MethodParameterCreationSupport) {
      SingleVariableDeclaration parameter =
          (SingleVariableDeclaration) composite.getCreationSupport().getNode();
      MethodDeclaration methodDeclaration = (MethodDeclaration) parameter.getParent();
      String methodSignature = AstNodeUtils.getMethodSignature(methodDeclaration);
      return methodSignature.equals("createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)");
    }
    return false;
  }

  /**
   * Adds new "button" on "button bar".
   *
   * @return the added button {@link ControlInfo}.
   */
  public static ControlInfo createButtonOnButtonBar(CompositeInfo buttonBar, ControlInfo nextButton)
      throws Exception {
    AstEditor editor = buttonBar.getEditor();
    // prepare CreationSupport
    CreationSupport creationSupport;
    {
      DialogInfo dialog = (DialogInfo) buttonBar.getRootJava();
      String signature =
          "createButton(org.eclipse.swt.widgets.Composite,int,java.lang.String,boolean)";
      String source = "createButton(%parent%, 0, \"New button\", false)";
      creationSupport = new ImplicitFactoryCreationSupport(dialog, signature, source);
    }
    // do add
    ControlInfo button =
        (ControlInfo) JavaInfoUtils.createJavaInfo(
            editor,
            "org.eclipse.swt.widgets.Button",
            creationSupport);
    JavaInfoUtils.add(
        button,
        new EmptyPureVariableSupport(button),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.factoryParent(),
        buttonBar,
        nextButton);
    return button;
  }

  /**
   * Moves "button" on "buttonBar".
   */
  public static void moveButtonOnButtonBar(ControlInfo button, ControlInfo nextButton)
      throws Exception {
    JavaInfoUtils.move(button, null, button.getParentJava(), nextButton);
  }
}
