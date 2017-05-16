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
package org.eclipse.wb.internal.core.utils.reflect;

/**
 * This interface allows modifying classes byte code during loading it by {@link ProjectClassLoader}
 * .
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface IByteCodeProcessor {
  /**
   * This method invoked during add this processor to class loader.
   */
  void initialize(ProjectClassLoader classLoader);

  /**
   * @return the possibly modified bytes for given class.
   */
  byte[] process(String className, byte[] bytes);
}