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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Allows subscribers to handle request to open some event listener.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfoEventOpen {
	void invoke(JavaInfo javaInfo, String spec) throws Exception;
}