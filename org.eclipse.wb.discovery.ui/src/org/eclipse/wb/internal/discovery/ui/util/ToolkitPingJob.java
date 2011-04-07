package org.eclipse.wb.internal.discovery.ui.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wb.internal.discovery.core.WBToolkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Ping the given toolkits; this gives the toolkit providers a way to track how many installs
 * they received from WindowBuilder Discovery.
 */
public class ToolkitPingJob extends Job {
  private static final String USER_AGENT = "WindowBuilder-Toolkit-Discovery";
  
  private List<WBToolkit> toolkits;
  
  public ToolkitPingJob(List<WBToolkit> toolkits) {
    super("WindowBuilder Discovery toolkit ping");
    
    this.toolkits = new ArrayList<WBToolkit>(toolkits);
    
    setSystem(true);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    for (WBToolkit toolkit : toolkits) {
      try {
        String updateSite = toolkit.getUpdateSite();
        
        if (!updateSite.endsWith("/")) {
          updateSite += "/";
        }
        
        updateSite += "site.xml";
        
        URL url = new URL(updateSite);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        @SuppressWarnings("unused")
        int responseCode = connection.getResponseCode();

        connection.disconnect();
      } catch (IOException ioe) {
        //ignore
        
      }
    }
    
    return Status.OK_STATUS;
  }

}
