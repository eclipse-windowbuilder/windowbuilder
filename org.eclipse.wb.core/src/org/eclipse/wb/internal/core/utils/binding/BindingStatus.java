/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.binding;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import java.util.Arrays;

/**
 * Copy from JFace DB.
 * <p>
 * A <code>MultiStatus</code> implementation that copies that state of the added status to this
 * instance if it is >= the current severity.
 *
 * @since 1.0
 */
public class BindingStatus extends MultiStatus {
	private static final String PLUGIN_ID = "org.eclipse.wb";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a new instance.
	 *
	 * @param pluginId
	 * @param code
	 * @param message
	 * @param exception
	 */
	public BindingStatus(String pluginId, int code, String message, Throwable exception) {
		super(pluginId, code, message, exception);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds the status to the multi status. The details of the status will be copied to the multi
	 * status if the severity is >= the current severity.
	 *
	 * @see org.eclipse.core.runtime.MultiStatus#add(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void add(IStatus status) {
		if (status.getSeverity() >= getSeverity()) {
			setMessage(status.getMessage() != null ? status.getMessage() : ""); //$NON-NLS-1$
			setException(status.getException());
			setPlugin(status.getPlugin());
			setCode(status.getCode());
		}
		super.add(status);
	}

	/**
	 * Instance initialized with the following values:
	 * <ul>
	 * <li>plugin = Policy.JFACE_DATABINDING</li>
	 * <li>severity = 0</li>
	 * <li>code = 0</li>
	 * <li>message = ""</li>
	 * <li>exception = null</li>
	 * </ul>
	 *
	 * @return status
	 */
	public static BindingStatus ok() {
		return new BindingStatus(PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static int hashCode(Object[] array) {
		final int prime = 31;
		if (array == null) {
			return 0;
		}
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result + (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + BindingStatus.hashCode(getChildren());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BindingStatus other = (BindingStatus) obj;
		if (!Arrays.equals(getChildren(), other.getChildren())) {
			return false;
		}
		return true;
	}
}
