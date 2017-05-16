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
package org.eclipse.wb.internal.core.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;

/**
 * Factory for creating configurable {@link Property}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public interface IConfigurablePropertyFactory {
  /**
   * @return the {@link Property} created/configured according the given
   *         {@link ConfigurablePropertyDescription} .
   */
  Property create(JavaInfo javaInfo, ConfigurablePropertyDescription description) throws Exception;
}
