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
 * Helper for creating {@link AssociationObjectFactory}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class AssociationObjectFactories {
	public static AssociationObjectFactory no() {
		return new AssociationObjectFactory() {
			@Override
			public String toString() {
				return "NO";
			}

			@Override
			public AssociationObject create() {
				return AssociationObjects.no();
			}
		};
	}

	public static AssociationObjectFactory invocationChild(final String source, final boolean required) {
		return new AssociationObjectFactory() {
			@Override
			public String toString() {
				return source;
			}

			@Override
			public AssociationObject create() {
				return new AssociationObject(source, new InvocationChildAssociation(source), required);
			}
		};
	}
}
