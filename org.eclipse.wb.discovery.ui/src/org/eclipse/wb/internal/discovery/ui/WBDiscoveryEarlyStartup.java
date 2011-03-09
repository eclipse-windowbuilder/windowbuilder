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

package org.eclipse.wb.internal.discovery.ui;

import org.eclipse.ui.IStartup;

/**
 * This early startup class ensures that the bundle activator gets initialized during
 * Eclipse's startup process. We can do things like check to see if new toolkits are 
 * available and dynamically contribute wizard entries.
 */
public class WBDiscoveryEarlyStartup implements IStartup {

  /**
   * Create a new instance of WBDiscoveryEarlyStartup.
   */
  public WBDiscoveryEarlyStartup() {
    
  }
  
  public void earlyStartup() {
    
  }

}
