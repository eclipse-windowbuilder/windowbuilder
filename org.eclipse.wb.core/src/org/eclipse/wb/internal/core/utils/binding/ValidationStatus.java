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
package org.eclipse.wb.internal.core.utils.binding;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Convenience class for creating status objects.
 * <p>
 * Copy from JFace DB.
 */
public final class ValidationStatus extends Status {
	private static final String PLUGIN_ID = "org.eclipse.wb";
	public static final IStatus STATUS_OK = new Status(OK, PLUGIN_ID, OK, "OK", null);

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new validation status with the given severity, message, and exception.
	 *
	 * @param severity
	 * @param message
	 * @param exception
	 */
	private ValidationStatus(int severity, String message, Throwable exception) {
		super(severity, PLUGIN_ID, IStatus.OK, message, exception);
	}

	/**
	 * Creates a new validation status with the given severity and message.
	 *
	 * @param severity
	 * @param message
	 */
	private ValidationStatus(int severity, String message) {
		super(severity, PLUGIN_ID, IStatus.OK, message, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new validation error status with the given message.
	 *
	 * @param message
	 * @return a new error status with the given message
	 */
	public static IStatus error(String message) {
		return new ValidationStatus(IStatus.ERROR, message);
	}

	/**
	 * Creates a new validation cancel status with the given message.
	 *
	 * @param message
	 * @return a new cancel status with the given message
	 */
	public static IStatus cancel(String message) {
		return new ValidationStatus(0x08/*IStatus.CANCEL*/, message);
	}

	/**
	 * Creates a new validation error status with the given message and exception.
	 *
	 * @param message
	 * @param exception
	 * @return a new error status with the given message and exception
	 */
	public static IStatus error(String message, Throwable exception) {
		return new ValidationStatus(IStatus.ERROR, message, exception);
	}

	/**
	 * Creates a new validation warning status with the given message.
	 *
	 * @param message
	 * @return a new warning status with the given message
	 */
	public static IStatus warning(String message) {
		return new ValidationStatus(IStatus.WARNING, message);
	}

	/**
	 * Creates a new validation info status with the given message.
	 *
	 * @param message
	 * @return a new info status with the given message
	 */
	public static IStatus info(String message) {
		return new ValidationStatus(IStatus.INFO, message);
	}

	/**
	 * Returns an OK status.
	 *
	 * @return an OK status
	 */
	public static IStatus ok() {
		return STATUS_OK;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String message = getMessage();
		int severity = getSeverity();
		Throwable throwable = getException();
		result = prime * result + (message == null ? 0 : message.hashCode());
		result = prime * result + severity;
		result = prime * result + (throwable == null ? 0 : throwable.hashCode());
		return result;
	}

	/**
	 * Equality is based upon instance equality rather than identity.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		final ValidationStatus other = (ValidationStatus) obj;
		if (getSeverity() != other.getSeverity()) {
			return false;
		}
		if (getMessage() == null) {
			if (other.getMessage() != null) {
				return false;
			}
		} else if (!getMessage().equals(other.getMessage())) {
			return false;
		}
		if (getException() == null) {
			if (other.getException() != null) {
				return false;
			}
		} else if (!getException().equals(other.getException())) {
			return false;
		}
		return true;
	}
}
