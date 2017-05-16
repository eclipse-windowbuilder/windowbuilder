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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetVariable;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Map;

/**
 * Support for sync managing name of child {@link JavaInfo}, so that it corresponds to the name of
 * its parent {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.layout
 */
public abstract class SyncParentChildVariableNameSupport<T extends JavaInfo> {
  protected final T m_childInfo;
  public static final String TEMPLATE_FOR_DEFAULT = "${defaultName}";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SyncParentChildVariableNameSupport(T layoutData) {
    m_childInfo = layoutData;
    // materialization of child JavaInfo
    m_childInfo.addBroadcastListener(new JavaInfoSetVariable() {
      public void invoke(JavaInfo javaInfo, VariableSupport oldVariable, VariableSupport newVariable)
          throws Exception {
        if (javaInfo == m_childInfo
            && oldVariable instanceof EmptyVariableSupport
            && newVariable instanceof AbstractNamedVariableSupport) {
          JavaInfo parent = m_childInfo.getParentJava();
          if (!GlobalState.isParsing()
              && parent != null
              && parent.getVariableSupport() instanceof AbstractNamedVariableSupport) {
            setNewName();
          }
        }
      }
    });
    // parent JavaInfo rename
    m_childInfo.addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_setName(AbstractNamedVariableSupport variableSupport,
          String oldName,
          String newName) throws Exception {
        JavaInfo parent = m_childInfo.getParentJava();
        if (variableSupport.getJavaInfo() == parent
            && m_childInfo.getVariableSupport() instanceof AbstractNamedVariableSupport
            && parent.getVariableSupport() instanceof AbstractNamedVariableSupport) {
          setNewName();
        }
      }
    });
  }

  private void setNewName() throws Exception {
    String newName = generateName();
    if (newName != null) {
      m_childInfo.getVariableSupport().setName(newName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static boolean isValidTemplate(String templates[], String template) {
    return ArrayUtils.contains(templates, template);
  }

  /**
   * @return the name of child {@link JavaInfo} that corresponds to the name of parent
   *         {@link JavaInfo}.
   */
  protected String generateName() {
    // prepare template
    String template = getTemplate();
    if (template.equals(getTemplateForDefault()) || StringUtils.isEmpty(template)) {
      return null;
    }
    // use template
    return StrSubstitutor.replace(template, getValueMap());
  }

  /**
   * @return template applied as default variable name (old name style).
   */
  protected String getTemplateForDefault() {
    return TEMPLATE_FOR_DEFAULT;
  }

  /**
   * @return template for applied variable name.
   */
  protected abstract String getTemplate();

  /**
   * @return template values map.
   */
  protected abstract Map<String, String> getValueMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClassName() {
    return CodeUtils.getShortClass(m_childInfo.getDescription().getComponentClass().getName());
  }

  public String getAcronym() {
    return StringUtilities.extractCamelCaps(getClassName()).toLowerCase();
  }

  public String getParentName() {
    return m_childInfo.getParentJava().getVariableSupport().getComponentName();
  }

  public String getParentNameCap() {
    return StringUtils.capitalize(getParentName());
  }
}