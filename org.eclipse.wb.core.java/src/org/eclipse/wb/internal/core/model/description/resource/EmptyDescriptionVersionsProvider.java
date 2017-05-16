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

import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * {@link IDescriptionVersionsProvider} that does not return any version. It can be used for example
 * when {@link IJavaProject} does not include supported toolkit/library.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class EmptyDescriptionVersionsProvider implements IDescriptionVersionsProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IDescriptionVersionsProvider INSTANCE =
      new EmptyDescriptionVersionsProvider();

  private EmptyDescriptionVersionsProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<String> getVersions(Class<?> componentClass) throws Exception {
    return ImmutableList.of();
  }
}