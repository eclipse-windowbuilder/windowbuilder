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
package org.eclipse.wb.core.model;

/**
 * Participator of {@link JavaInfo} initialization, used from {@link JavaInfo#initialize()}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IJavaInfoInitializationParticipator {
	/**
	 * Participates in given {@link JavaInfo} initialization.
	 */
	void process(JavaInfo javaInfo) throws Exception;
}
