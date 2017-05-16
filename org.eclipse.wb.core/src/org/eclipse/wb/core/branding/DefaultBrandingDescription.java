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
package org.eclipse.wb.core.branding;

/**
 * The {@link IBrandingDescription} used if there are no branding extensions.
 *
 * @see BrandingUtils
 *
 * @author Jaime Wren
 * @coverage core.util
 */
final class DefaultBrandingDescription extends AbstractBrandingDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Support info
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final IBrandingSupportInfo SUPPORT_INFO = new IBrandingSupportInfo() {
    public String getBugtrackingUrl() {
      return "https://bugs.eclipse.org/bugs/enter_bug.cgi?product=WindowBuilder";
    }

    public String getForumUrl() {
      return "http://eclipse.org/forums/index.php?t=thread&frm_id=214";
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static IBrandingDescription INSTANCE = new DefaultBrandingDescription();

  private DefaultBrandingDescription() {
    super("WindowBuilder", SUPPORT_INFO);
  }
}
