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
package org.eclipse.wb.core.editor.structure.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.List;

/**
 * Processor for {@link Property}-s, used directly before displaying them.
 * <p>
 * Processor can for example reorder properties, or even wrap them into groups.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public interface PropertyListProcessor {
	/**
	 * Processes given {@link Property}-s or {@link ObjectInfo}-s.
	 */
	void process(List<ObjectInfo> objects, List<Property> properties);
}
