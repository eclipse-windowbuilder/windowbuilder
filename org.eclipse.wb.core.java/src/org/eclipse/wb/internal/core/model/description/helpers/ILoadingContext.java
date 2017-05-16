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
package org.eclipse.wb.internal.core.model.description.helpers;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;

import java.net.URL;
import java.util.List;

/**
 * Context for loading descriptions.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface ILoadingContext {
  String getToolkitId();

  URL getResource(String name) throws Exception;

  Object getGlobalValue(String key);

  void putGlobalValue(String key, Object value);

  List<IDescriptionVersionsProvider> getDescriptionVersionsProviders();
}
