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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.model.generation.GenerationSettings;

/**
 * {@link ToolkitDescription} for Java.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class ToolkitDescriptionJava extends ToolkitDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link GenerationSettings}.
	 */
	public abstract GenerationSettings getGenerationSettings();
}
