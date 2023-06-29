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
