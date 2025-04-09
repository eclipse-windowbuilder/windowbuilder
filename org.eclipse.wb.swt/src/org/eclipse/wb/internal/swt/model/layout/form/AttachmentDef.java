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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

/**
 * Definition of the attachment.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public class AttachmentDef {
	public IAbstractComponentInfo source;
	public IAbstractComponentInfo target;
	public int sourceSide;
	public int targetSide;
}
