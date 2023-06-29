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
package org.eclipse.wb.internal.core.xml.model.property;

import org.eclipse.wb.internal.core.xml.model.EditorContext;

import java.util.Map;

/**
 * Extends object with ability to be configured.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public interface IConfigurablePropertyObject {
	/**
	 * Configures object with given {@link Map} of parameters.
	 */
	void configure(EditorContext context, Map<String, Object> parameters) throws Exception;
}
