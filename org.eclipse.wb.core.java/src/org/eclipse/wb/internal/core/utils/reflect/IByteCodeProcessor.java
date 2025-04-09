/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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