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
package org.eclipse.wb.internal.core.databinding.model;

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * Code generation utils.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public class CodeGenerationSupport {
  private final boolean m_useGenerics;
  private final Set<String> m_variables = Sets.newHashSet();
  private final Set<AstObjectInfo> m_models = Sets.newHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CodeGenerationSupport(boolean useGenerics) {
    m_useGenerics = useGenerics;
  }

  public CodeGenerationSupport(boolean useGenerics, AstObjectInfo rootModelObject) throws Exception {
    m_useGenerics = useGenerics;
    // store all exist variables
    rootModelObject.accept(new AstObjectInfoVisitor() {
      @Override
      public void visit(AstObjectInfo object) throws Exception {
        // store variable
        String variable = object.getVariableIdentifier();
        if (variable != null) {
          m_variables.add(variable);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@code true} if current project support Java 1.5 generic types.
   */
  public boolean useGenerics() {
    return m_useGenerics;
  }

  /**
   * Add source code from given {@link AstObjectInfo} to <code>lines</code> over invoke
   * {@link AstObjectInfo#addSourceCode(List, ICodeGenerationSupport)}. This method may be called
   * several times for the same object, but the code is added only one time.
   */
  public void addSourceCode(AstObjectInfo modelObject, List<String> lines) throws Exception {
    if (!m_models.contains(modelObject)) {
      m_models.add(modelObject);
      modelObject.addSourceCode(lines, this);
      Assert.isNotNull(modelObject.getVariableIdentifier());
    }
  }

  /**
   * Add model.
   */
  public boolean addModel(AstObjectInfo modelObject) {
    return m_models.add(modelObject);
  }

  /**
   * @return unique variable name consist of given parts <code>names</code>.
   */
  public String generateLocalName(String... names) throws Exception {
    // create base name
    String baseVariable = createName(names);
    String variable = baseVariable;
    int variableIndex = 1;
    // ensure unique
    while (m_variables.contains(variable)) {
      variable = baseVariable + "_" + Integer.toString(variableIndex++);
    }
    m_variables.add(variable);
    //
    return variable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String createName(String[] names) {
    Assert.isTrue(names.length > 0);
    for (int i = 0; i < names.length; i++) {
      names[i] = convertName(names[i]);
    }
    return StringUtils.uncapitalize(StringUtils.join(names));
  }

  private static String convertName(String name) {
    if (name.startsWith("m_")) {
      name = name.substring(2);
    }
    name = StringUtils.remove(name, '.');
    name = StringUtils.remove(name, '"');
    name = StringUtils.remove(name, '(');
    name = StringUtils.remove(name, ')');
    return StringUtils.capitalize(name);
  }
}