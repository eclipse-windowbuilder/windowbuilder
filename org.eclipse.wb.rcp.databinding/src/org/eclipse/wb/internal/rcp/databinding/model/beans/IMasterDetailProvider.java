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