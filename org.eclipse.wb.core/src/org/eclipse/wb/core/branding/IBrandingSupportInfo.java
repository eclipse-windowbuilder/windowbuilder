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
 * Interface providing info about bug-tracking system and discussion forum.
 *
 * @author mitin_aa
 * @coverage core.util
 */
public interface IBrandingSupportInfo {
  /**
   * @return the url to bug-tracking system to search and/or submit the issue.
   */
  String getBugtrackingUrl();

  /**
   * @return the url to discussion forum, mailing list or any other discussion platform.
   */
  String getForumUrl();
}
