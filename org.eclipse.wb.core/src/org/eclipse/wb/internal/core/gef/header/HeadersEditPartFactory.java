/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef.header;

import org.eclipse.wb.gef.core.IEditPartFactory;

import org.eclipse.gef.EditPart;

/**
 * Implementation of {@link IEditPartFactory} for headers.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class HeadersEditPartFactory implements IEditPartFactory {
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		return (org.eclipse.wb.gef.core.EditPart) model;
	}
}
