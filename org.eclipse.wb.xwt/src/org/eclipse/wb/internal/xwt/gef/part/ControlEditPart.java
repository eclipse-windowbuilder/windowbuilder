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
package org.eclipse.wb.internal.xwt.gef.part;

import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

/**
 * {@link GraphicalEditPart} for {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.gef
 */
public class ControlEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlEditPart(ControlInfo control) {
		super(control);
	}
}