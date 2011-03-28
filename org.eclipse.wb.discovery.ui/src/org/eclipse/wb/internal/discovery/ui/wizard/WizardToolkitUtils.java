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

package org.eclipse.wb.internal.discovery.ui.wizard;

import org.eclipse.wb.internal.discovery.core.WBToolkit;

class WizardToolkitUtils {

  private WizardToolkitUtils() {
    
  }

  
  public static String getTitle(WBToolkit toolkit) {
    return toolkit.getName() + "  User Interface";
  }


  public static String getDescription(WBToolkit toolkit) {
    return "Create a new " + toolkit.getName()
      + " user interface. Requires loading additional WindowBuilder toolkits.";
  }

}
