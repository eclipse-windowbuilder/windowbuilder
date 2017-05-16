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
package org.eclipse.wb.internal.core.utils.state;

import java.util.Map;

/**
 * Provider for parameters of component, from description or instance specific.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IParametersProvider {
  /**
   * @return the read only {@link Map} of parameters from model or description.
   */
  Map<String, String> getParameters(Object object);

  /**
   * @return the parameter value from model or description.
   */
  String getParameter(Object object, String name);

  /**
   * Checks if object has parameter with value <code>"true"</code>.
   */
  boolean hasTrueParameter(Object object, String name);
}
