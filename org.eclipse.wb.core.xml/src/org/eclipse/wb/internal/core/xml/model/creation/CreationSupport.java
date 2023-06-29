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
package org.eclipse.wb.internal.core.xml.model.creation;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardCreationSupport;

import org.apache.commons.lang.NotImplementedException;

/**
 * Provides support for different patterns of {@link XmlObjectInfo} creation.
 *
 * @author scheglov_ke
 * @coverage XML.model.creation
 */
public abstract class CreationSupport {
	protected XmlObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link DocumentElement} in XML model.
	 */
	public DocumentElement getElement() {
		throw new NotImplementedException();
	}

	/**
	 * @return the existing {@link DocumentElement} in XML model, or <code>null</code> if
	 *         corresponding {@link XmlObjectInfo} is virtual, not existing yet.
	 */
	public DocumentElement getExistingElement() {
		return getElement();
	}

	/**
	 * @return the {@link DocumentElement} which should be moved, usually same as
	 *         {@link #getElement()}.
	 */
	public DocumentElement getElementMove() {
		return getElement();
	}

	/**
	 * @return the title to display to user, usually name of tag.
	 */
	public String getTitle() {
		throw new NotImplementedException();
	}

	/**
	 * Sets the {@link XmlObjectInfo} that has this {@link CreationSupport}.
	 */
	public void setObject(XmlObjectInfo object) throws Exception {
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link XmlObjectInfo} can be deleted.
	 */
	public boolean canDelete() {
		return true;
	}

	/**
	 * Deletes this {@link XmlObjectInfo}.
	 */
	public void delete() throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new {@link DocumentElement} for creating this {@link XmlObjectInfo}.
	 */
	public void addElement(DocumentElement parent, int index) throws Exception {
		throw new NotImplementedException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IClipboardCreationSupport} that can be used during paste to create
	 *         {@link CreationSupport} for pasted {@link XmlObjectInfo}. Can return <code>null</code>
	 *         , if copy/paste is not available.
	 */
	public IClipboardCreationSupport getClipboard() {
		return null;
	}
}
