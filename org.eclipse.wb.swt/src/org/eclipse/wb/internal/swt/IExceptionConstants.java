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
package org.eclipse.wb.internal.swt;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Constants for SWT {@link DesignerException}'s.
 *
 * @author scheglov_ke
 * @coverage swt
 */
public interface IExceptionConstants {
	int NO_LAYOUT_EXPECTED = 2001;
	int NO_LAYOUT_DATA_EXPECTED = 2002;
	int INCOMPATIBLE_LAYOUT_DATA = 2003;
	int DOUBLE_SET_LAYOUT = 2004;
	int NULL_PARENT = 2005;
}
