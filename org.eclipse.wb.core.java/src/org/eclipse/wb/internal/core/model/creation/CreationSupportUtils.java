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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SuperConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.util.List;

/**
 * Utils for {@link CreationSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class CreationSupportUtils {
  private final JavaInfo m_javaInfo;
  private final CreationSupport m_creationSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationSupportUtils(JavaInfo javaInfo) {
    m_javaInfo = javaInfo;
    m_creationSupport = javaInfo.getCreationSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ExpressionAccessor} for accessing {@link Expression}.
   */
  private ExpressionAccessor createAccessor(ParameterDescription parameter) {
    int index = parameter.getIndex();
    String defaultSource = parameter.getDefaultSource();
    if (m_creationSupport instanceof ConstructorCreationSupport) {
      return new ConstructorAccessor(index, defaultSource);
    } else if (m_creationSupport instanceof ThisCreationSupport) {
      SuperConstructorInvocation invocation =
          ((ThisCreationSupport) m_creationSupport).getInvocation();
      return new SuperConstructorAccessor(invocation, index, defaultSource);
    } else {
      Assert.instanceOf(AbstractFactoryCreationSupport.class, m_creationSupport);
      return new FactoryAccessor(index, defaultSource);
    }
  }

  private Expression getArgument(ParameterDescription parameter) {
    int index = parameter.getIndex();
    return getArguments().get(index);
  }

  private List<Expression> getArguments() {
    if (m_creationSupport instanceof ConstructorCreationSupport) {
      return DomGenerics.arguments(((ConstructorCreationSupport) m_creationSupport).getCreation());
    } else if (m_creationSupport instanceof ThisCreationSupport) {
      SuperConstructorInvocation invocation =
          ((ThisCreationSupport) m_creationSupport).getInvocation();
      return DomGenerics.arguments(invocation);
    } else {
      Assert.instanceOf(AbstractFactoryCreationSupport.class, m_creationSupport);
      return DomGenerics.arguments(((AbstractFactoryCreationSupport) m_creationSupport).getInvocation());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link GenericPropertyImpl} for given {@link ParameterDescription} of constructor.
   */
  public Property createProperty(ParameterDescription parameter) throws Exception {
    // may be disabled
    if (parameter.hasTrueTag("property.no")) {
      return null;
    }
    // try to find JavaInfo passed as parameter
    if (parameter.isParent()) {
      Expression argument = getArgument(parameter);
      JavaInfo argumentJavaInfo = getArgumentJavaInfo(argument);
      if (argumentJavaInfo != null) {
        ComplexProperty complexProperty = new ComplexProperty(parameter.getName(), "(properties)");
        complexProperty.setProperties(argumentJavaInfo.getProperties());
        return complexProperty;
      }
    }
    // prepare PropertyEditor
    PropertyEditor editor = parameter.getEditor();
    // if no special PropertyEditor, use Object editor
    if (editor == null && shouldUseObjectPropertyEditor(parameter)) {
      editor = ObjectPropertyEditor.INSTANCE;
    }
    // create property
    if (editor != null) {
      ExpressionAccessor accessor = createAccessor(parameter);
      ExpressionConverter converter = parameter.getConverter();
      GenericPropertyImpl property =
          new GenericPropertyImpl(m_javaInfo,
              parameter.getName(),
              new ExpressionAccessor[]{accessor},
              Property.UNKNOWN_VALUE,
              converter,
              editor);
      property.setType(parameter.getType());
      return property;
    }
    // no property
    return null;
  }

  /**
   * @return the {@link JavaInfo} in same hierarchy which corresponds to the given
   *         {@link Expression}.
   */
  private JavaInfo getArgumentJavaInfo(Expression argument) {
    JavaInfo argumentJavaInfo = m_javaInfo.getRootJava().getChildRepresentedBy(argument);
    // may be not bound to hierarchy
    if (argumentJavaInfo != null && argumentJavaInfo.getRoot() != m_javaInfo.getRoot()) {
      return null;
    }
    // done
    return argumentJavaInfo;
  }

  private static boolean shouldUseObjectPropertyEditor(ParameterDescription parameter) {
    Class<?> type = parameter.getType();
    return type != null && !type.isPrimitive();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source for arguments.
   */
  public String getClipboardArguments(List<ParameterDescription> parameters) throws Exception {
    StringBuilder argumentsSource = new StringBuilder();
    List<Expression> arguments = getArguments();
    for (ParameterDescription parameter : parameters) {
      // append separator
      if (argumentsSource.length() != 0) {
        argumentsSource.append(", ");
      }
      // try to use broadcast
      {
        String[] source = new String[1];
        Expression argument = arguments.get(parameter.getIndex());
        m_javaInfo.getBroadcastJava().clipboardCopy_Argument(
            m_javaInfo,
            parameter,
            argument,
            source);
        if (source[0] != null) {
          argumentsSource.append(source[0]);
          continue;
        }
      }
      // append argument
      if (parameter.isParent()) {
        argumentsSource.append("%parent%");
      } else {
        String argumentSource = getClipboardArgument(parameter);
        argumentsSource.append(argumentSource);
      }
    }
    return argumentsSource.toString();
  }

  private String getClipboardArgument(ParameterDescription parameter) throws Exception {
    String argumentSource = null;
    // try GenericPropertyImpl
    {
      Property argumentProperty = createProperty(parameter);
      if (argumentProperty instanceof GenericPropertyImpl) {
        argumentSource = ((GenericPropertyImpl) argumentProperty).getClipboardSource();
      }
    }
    // default source
    if (argumentSource == null) {
      argumentSource = parameter.getDefaultSource();
    }
    // ensure or fail
    Assert.isNotNull(argumentSource, "No source for "
        + parameter.getIndex()
        + "-th argument of "
        + m_creationSupport.getNode());
    return argumentSource;
  }
}
