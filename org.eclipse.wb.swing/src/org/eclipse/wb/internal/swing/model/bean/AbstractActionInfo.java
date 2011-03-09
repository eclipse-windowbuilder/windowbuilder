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

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.property.editor.DisplayedMnemonicKeyPropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.accelerator.KeyStrokePropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.icon.IconPropertyEditor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.SystemUtils;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * Model for {@link AbstractAction}.
 * 
 * @author sablin_aa
 * @coverage swing.model
 */
public class AbstractActionInfo extends ActionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractActionInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // add "putValue()" properties
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo == AbstractActionInfo.this) {
          addProperties(properties);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds additional properties accessed via
   * {@link javax.swing.AbstractAction#putValue(String, Object)} if can.
   */
  private List<Property> m_properties = null;

  private void addProperties(List<Property> properties) throws Exception {
    if (m_properties == null) {
      m_properties = createProperties();
    }
    if (!m_properties.isEmpty()) {
      properties.addAll(m_properties);
    }
  }

  private List<Property> createProperties() throws Exception {
    CreationSupport creationSupport = getCreationSupport();
    // no additional properties available
    if (!(creationSupport instanceof IActionSupport)) {
      return Lists.newArrayList();
    }
    // create properties
    List<Property> properties = Lists.newArrayList();
    properties.add(createStringProperty("name", "NAME"));
    properties.add(createStringProperty("short description", "SHORT_DESCRIPTION"));
    properties.add(createStringProperty("long description", "LONG_DESCRIPTION"));
    properties.add(createIconProperty("small icon", "SMALL_ICON"));
    properties.add(createStringProperty("action command", "ACTION_COMMAND_KEY"));
    properties.add(createProperty(
        "accelerator",
        "ACCELERATOR_KEY",
        null,
        KeyStrokePropertyEditor.INSTANCE));
    properties.add(createProperty(
        "mnemonic",
        "MNEMONIC_KEY",
        null,
        DisplayedMnemonicKeyPropertyEditor.INSTANCE));
    if (SystemUtils.JAVA_VERSION_FLOAT >= 1.6) {
      properties.add(createProperty(
          "displayed mnemonic index",
          "DISPLAYED_MNEMONIC_INDEX_KEY",
          IntegerConverter.INSTANCE,
          IntegerPropertyEditor.INSTANCE));
      properties.add(createIconProperty("large icon", "LARGE_ICON_KEY"));
    }
    // remove null-s
    Iterables.removeIf(properties, Predicates.isNull());
    return properties;
  }

  /**
   * @return the new {@link Action} property for {@link String}.
   */
  private Property createStringProperty(String title, String keyName) throws Exception {
    return createProperty(title, keyName, StringConverter.INSTANCE, StringPropertyEditor.INSTANCE);
  }

  /**
   * @return the new {@link Action} property for {@link Icon}.
   */
  private Property createIconProperty(String title, String keyName) throws Exception {
    return createProperty(title, keyName, null, IconPropertyEditor.INSTANCE);
  }

  /**
   * @return the new {@link Action} property.
   */
  private Property createProperty(String title,
      String keyName,
      ExpressionConverter converter,
      PropertyEditor editor) throws Exception {
    List<ExpressionAccessor> accessors = getAccessors(keyName);
    // may be no accessors
    if (accessors.isEmpty()) {
      return null;
    }
    // create property
    return new GenericPropertyImpl(this, title, Iterables.toArray(
        accessors,
        ExpressionAccessor.class), Property.UNKNOWN_VALUE, converter, editor);
  }

  private List<ExpressionAccessor> getAccessors(String keyName) throws Exception {
    IActionSupport creationInfo = (IActionSupport) getCreationSupport();
    List<ExpressionAccessor> accessors = Lists.newArrayList();
    // <init>()
    {
      ExpressionAccessor constructorAccessor =
          createConstructorArgumentAccessor(creationInfo, keyName);
      if (constructorAccessor != null) {
        accessors.add(constructorAccessor);
      }
    }
    // putValue()
    if (!creationInfo.getInitializationBlocks().isEmpty()) {
      ExpressionAccessor actionExpressionAccessor =
          new ActionExpressionAccessor(creationInfo, keyName);
      accessors.add(actionExpressionAccessor);
    }
    // done
    return accessors;
  }

  private static ExpressionAccessor createConstructorArgumentAccessor(final IActionSupport creationInfo,
      String keyName) throws Exception {
    String keyValue = (String) ReflectionUtils.getFieldObject(Action.class, keyName);
    ConstructorDescription constructorDescription = creationInfo.getConstructorDescription();
    if (constructorDescription != null) {
      for (ParameterDescription parameter : constructorDescription.getParameters()) {
        if (keyValue.equals(parameter.getTag("actionKey"))) {
          final int index = parameter.getIndex();
          return new ExpressionAccessor() {
            @Override
            public Expression getExpression(JavaInfo javaInfo) throws Exception {
              ASTNode creationNode = creationInfo.getCreation();
              return DomGenerics.arguments(creationNode).get(index);
            }

            @Override
            public boolean setExpression(JavaInfo javaInfo, String source) throws Exception {
              // prepare source
              final String newSource;
              if (source != null) {
                newSource = source;
              } else {
                newSource = "null";
              }
              // replace source
              final AstEditor editor = javaInfo.getEditor();
              final Expression oldExpression = getExpression(javaInfo);
              if (!editor.getSource(oldExpression).equals(source)) {
                ExecutionUtils.run(javaInfo, new RunnableEx() {
                  public void run() throws Exception {
                    editor.replaceExpression(oldExpression, newSource);
                  }
                });
                return true;
              }
              // no changes
              return false;
            }
          };
        }
      }
    }
    return null;
  }
}
