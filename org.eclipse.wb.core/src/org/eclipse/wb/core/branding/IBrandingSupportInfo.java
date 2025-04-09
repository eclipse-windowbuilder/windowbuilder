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
