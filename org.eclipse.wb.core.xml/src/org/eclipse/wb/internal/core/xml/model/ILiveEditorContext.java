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
package org.eclipse.wb.internal.core.xml.model;

/**
 * {@link EditorContext} should provide implementation of this interface to support "live" parsing.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface ILiveEditorContext {
	/**
	 * Parses given source.
	 */
	XmlObjectInfo parse(String[] sourceLines) throws Exception;

	/**
	 * Performs clean up of {@link EditorContext} back to the state which was before
	 * {@link #parse(String[])}.
	 */
	void dispose() throws Exception;
}
