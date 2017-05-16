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
package org.eclipse.wb.core.editor.structure.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;

import java.util.List;

/**
 * Provider for {@link PropertyCategoryProvider}.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public interface PropertyCategoryProviderProvider {
  /**
   * @return the {@link PropertyCategoryProvider} for properties of given {@link ObjectInfo}-s, may
   *         be <code>null</code> so default provider will be used.
   */
  PropertyCategoryProvider get(List<ObjectInfo> objects);
}
