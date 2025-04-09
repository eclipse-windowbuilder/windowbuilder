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
package org.eclipse.wb.core.model.association;

/**
 * Factory for creating {@link AssociationObject}'s.
 * <p>
 * We need it for example for generic containers, where we should create new instances of
 * {@link AssociationObject} for each new component.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public interface AssociationObjectFactory {
	AssociationObject create();
}
