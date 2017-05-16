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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Utils for using in {@link VariableSupport} implementations.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class VariableUtils {
  private final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariableUtils(JavaInfo javaInfo) {
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Prefixes/suffixes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the unique name of field by given local variable name.
   */
  public String getUniqueFieldName(String localName, VariableDeclaration excludedVariable) {
    return convertName(
        -1,
        localName,
        JavaCore.CODEASSIST_LOCAL_PREFIXES,
        JavaCore.CODEASSIST_LOCAL_SUFFIXES,
        JavaCore.CODEASSIST_FIELD_PREFIXES,
        JavaCore.CODEASSIST_FIELD_SUFFIXES,
        excludedVariable);
  }

  /**
   * Generates unique variable name by converting from local/field to field/local.
   */
  public String convertName(int position,
      String name,
      String keyPrefixes_source,
      String keySuffixes_source,
      String keyPrefixes_target,
      String keySuffixes_target,
      VariableDeclaration excludedVariable) {
    // remove possible _NNN from base name
    {
      int index = name.lastIndexOf('_');
      if (index != -1) {
        String possibleNumber = name.substring(index + 1);
        if (StringUtils.isNumeric(possibleNumber)) {
          name = name.substring(0, index);
        }
      }
    }
    // remove source prefix/suffix
    name = stripPrefixSuffix(name, keyPrefixes_source, keySuffixes_source);
    // add target prefix/suffix
    name = addPrefixSuffix(name, keyPrefixes_target, keySuffixes_target);
    // generate unique name
    return m_javaInfo.getEditor().getUniqueVariableName(position, name, excludedVariable);
  }

  /**
   * @return the name with added prefix/suffix.
   *
   * @param keyPrefixes
   *          the key of prefixes in {@link IJavaProject} options.
   * @param keySuffixes
   *          the key of suffixes in {@link IJavaProject} options.
   */
  public String addPrefixSuffix(String name, String keyPrefixes, String keySuffixes) {
    // add prefix
    {
      String[] prefixes = getVariablesPrefixSuffixOptions(keyPrefixes);
      if (prefixes.length != 0) {
        String prefix = prefixes[0];
        if (!name.startsWith(prefix)) {
          name = prefix + name;
        }
      }
    }
    // add suffix
    {
      String[] suffixes = getVariablesPrefixSuffixOptions(keySuffixes);
      if (suffixes.length != 0) {
        String suffix = suffixes[0];
        if (!name.endsWith(suffix)) {
          name = name + suffix;
        }
      }
    }
    // return result
    return name;
  }

  /**
   * @return the name with removed prefix/suffix.
   *
   * @param keyPrefixes
   *          the key of prefixes in {@link IJavaProject} options.
   * @param keySuffixes
   *          the key of suffixes in {@link IJavaProject} options.
   */
  public String stripPrefixSuffix(String name, String keyPrefixes, String keySuffixes) {
    Assert.isNotNull(name);
    Assert.isNotNull(keyPrefixes);
    Assert.isNotNull(keySuffixes);
    // remove prefix
    {
      String[] prefixes = getVariablesPrefixSuffixOptions(keyPrefixes);
      for (String prefix : prefixes) {
        if (name.startsWith(prefix)) {
          name = name.substring(prefix.length());
          break;
        }
      }
    }
    // remove suffix
    {
      String[] suffixes = getVariablesPrefixSuffixOptions(keySuffixes);
      for (String suffix : suffixes) {
        if (name.endsWith(suffix)) {
          name = name.substring(0, name.length() - suffix.length());
          break;
        }
      }
    }
    // return result
    return name;
  }

  /**
   * @return the array of prefixes or suffixes for given key.
   */
  private String[] getVariablesPrefixSuffixOptions(String key) {
    IJavaProject javaProject = m_javaInfo.getEditor().getJavaProject();
    Map<String, String> javaOptions = ProjectUtils.getOptions(javaProject);
    return StringUtils.split(javaOptions.get(key), ",");
  }
}
