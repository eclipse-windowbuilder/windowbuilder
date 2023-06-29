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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.MethodDescription;

/**
 * Before associating new {@link JavaInfo} child using some method.
 *
 * We use it on parse to handle "setContentPane(myCustomPane)".
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfoMethodAssociationOnParse {
	void invoke(JavaInfo parent, JavaInfo child, MethodDescription methodDescription)
			throws Exception;
}