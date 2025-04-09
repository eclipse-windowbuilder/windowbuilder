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
package org.eclipse.wb.internal.rcp.databinding.model.beans;

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * This provider used to obtain the appropriate master observable. Using AST reference during detail
 * observable parsing its possible to get the master observable. But while creating master's
 * ObservableInfo its not know that it is the master observable (master should refer to certain
 * special detail's property). Thats why all of the possible masters should implement this interface
 * to be able to create by request it's own copy with detail property reference.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public interface IMasterDetailProvider {
	/**
	 * @return the master {@link ObservableInfo}.
	 */
	ObservableInfo getMasterObservable() throws Exception;
}