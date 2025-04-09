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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.List;

/**
 * Listener for {@link ObjectInfo} events.
 * <p>
 * Used to get notified while preparing {@link Property} list for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoAddProperties {
	/**
	 * During preparing {@link Property} list for {@link ObjectInfo}.
	 */
	void invoke(ObjectInfo target, List<Property> properties) throws Exception;
}