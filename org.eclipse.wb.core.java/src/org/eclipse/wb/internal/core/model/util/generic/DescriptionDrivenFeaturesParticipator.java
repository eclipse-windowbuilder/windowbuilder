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
package org.eclipse.wb.internal.core.model.util.generic;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;

/**
 * Helper to configure any {@link JavaInfo} to support configuration based features (from
 * <code>parameter</code> tags).
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class DescriptionDrivenFeaturesParticipator
implements
IJavaInfoInitializationParticipator {
	////////////////////////////////////////////////////////////////////////////
	//
	// IJavaInfoInitializationParticipator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(JavaInfo javaInfo) throws Exception {
		CopyPropertyTopSupport.install(javaInfo);
		CopyPropertyTopChildSupport.install(javaInfo);
		ModelMethodPropertySupport.install(javaInfo, "modelMethodProperty ");
		ModelMethodPropertyChildSupport.install(javaInfo, "modelMethodChildProperty ");
	}
}
