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
package org.eclipse.wb.internal.core.model.description.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.List;

/**
 * {@link IDescriptionVersionsProvider} that returns versions from {@link List}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class FromListDescriptionVersionsProvider implements IDescriptionVersionsProvider {
  private final List<String> m_versions = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FromListDescriptionVersionsProvider(List<String> allVersions, String currentVersion) {
    int currentVersionIndex = allVersions.indexOf(currentVersion);
    Assert.isTrue(
        currentVersionIndex != -1,
        "Version %s is not present in %s",
        currentVersion,
        allVersions);
    // add versions from current to earlier versions
    for (String version : allVersions) {
      m_versions.add(0, version);
      if (version.equals(currentVersion)) {
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<String> getVersions(Class<?> componentClass) throws Exception {
    if (validate(componentClass)) {
      return m_versions;
    }
    return ImmutableList.of();
  }

  protected abstract boolean validate(Class<?> componentClass) throws Exception;
}