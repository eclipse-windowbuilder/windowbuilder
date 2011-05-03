/*******************************************************************************
 * Copyright (c) 2011 Google, Inc. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.discovery.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This job checks for updates to the toolkits.xml file. It runs 30 minutes
 * after Eclipse starts, and every 24 hours after that. It pulls the latest
 * information directly from the svn repository using the web interface.
 */
class WBToolkitRegistryUpdateJob extends Job {
  private static final long MILLIS_PER_MINUTE = 60 * 1000;
  private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

  private static final String UPDATE_URL = "http://dev.eclipse.org/svnroot/tools/org.eclipse.windowbuilder/"
    + "trunk/org.eclipse.wb.discovery.core/resources/toolkits.xml"; //$NON-NLS-1$

  public WBToolkitRegistryUpdateJob() {
    super(Messages.WBToolkitRegistryUpdateJob_updateJobTitle);

    setSystem(true);
  }

  public void startJob() {
    schedule(30 * MILLIS_PER_MINUTE);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      doUpdate(monitor);
    } catch (RuntimeException re) {
      WBDiscoveryCorePlugin.logError(re);
    }

    schedule(24 * MILLIS_PER_HOUR);

    return Status.OK_STATUS;
  }

  private void doUpdate(IProgressMonitor monitor) {
    try {
      URL url = getURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("HEAD"); //$NON-NLS-1$

      int responseCode = connection.getResponseCode();
      long lastModified = connection.getHeaderFieldDate("Last-Modified", 0); //$NON-NLS-1$

      connection.disconnect();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        long lastCachedModified = WBToolkitRegistry.getRegistry().getLastCachedModified();

        if (lastModified > lastCachedModified) {
          WBToolkitRegistry.getRegistry().updateCacheFrom(url);
        }
      }
    } catch (IOException ioe) {
      if (WBDiscoveryCorePlugin.DEBUG) {
        WBDiscoveryCorePlugin.logError("Error polling toolkits.xml", ioe); //$NON-NLS-1$
      }
    }
  }

  private URL getURL() {
    try {
      return new URL(UPDATE_URL);
    } catch (MalformedURLException e) {
      return null;
    }
  }

}
