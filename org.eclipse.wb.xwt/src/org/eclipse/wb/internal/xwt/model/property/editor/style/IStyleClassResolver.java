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
package org.eclipse.wb.internal.xwt.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Generator of reference on class with style constants.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public interface IStyleClassResolver {
	/**
	 * @param property
	 *          the {@link Property} to generate source for.
	 * @param className
	 *          the name of {@link Class} with style constants.
	 * @return the prefix before name of style constant, may be empty, but not <code>null</code>.
	 */
	String resolve(Property property, String className);
}