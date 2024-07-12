/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
