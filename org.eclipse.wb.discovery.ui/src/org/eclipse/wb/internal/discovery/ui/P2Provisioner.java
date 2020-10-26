/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 * 
 * Portions of this file were taken from the Mylyn Connector Discovery UI plugin
 * (org.eclipse.mylyn.discovery.ui/PrepareInstallProfileJob_e_3_6.java),
 * and are Copyright (c) 2009, 2010 Tasktop Technologies and others.
 *******************************************************************************/
package org.eclipse.wb.internal.discovery.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.internal.discovery.core.WBDiscoveryCorePlugin;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.core.WBToolkitFeature;
import org.osgi.framework.ServiceReference;

/**
 * A utility class to help manage P2.
 */
class P2Provisioner {
  private List<WBToolkit> toolkits;
  private ProvisioningUI provisioningUI;
  private LoadMetadataRepositoryJob repositoryLoadingJob;

  /**
   * Create a new instance of the P2Provisioner class to act on the given list of toolkits.
   * 
   * @param toolkits
   *          the toolkits to act on
   */
  public P2Provisioner(List<WBToolkit> toolkits) {
    this.toolkits = toolkits;
  }

  /**
   * Install a set of toolkits.
   * 
   * @param progressMonitor
   *          a progress monitor
   * @throws ProvisionException
   *           a P2 exception
   * @throws OperationCanceledException
   *           thrown if the user canceled
   */
  public void installToolkits(final IProgressMonitor progressMonitor) throws ProvisionException,
      OperationCanceledException {
    provisioningUI = ProvisioningUI.getDefaultUI();
    repositoryLoadingJob = new LoadMetadataRepositoryJob(provisioningUI);
    final Collection<IInstallableUnit> installableUnits = collectInstallableUnits(progressMonitor);
    if (installableUnits.size() > 0) {
      final InstallOperation installOperation =
          provisioningUI.getInstallOperation(installableUnits, getRepositories());
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          try {
            provisioningUI.openInstallWizard(
                installableUnits,
                installOperation,
                repositoryLoadingJob);
          } finally {
            progressMonitor.done();
          }
        }
      });
    } else {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          progressMonitor.done();
          MessageDialog.openError(
              Display.getDefault().getActiveShell(),
              Messages.P2Provisioner_unableInstallTitle,
              Messages.P2Provisioner_unableInstallMessage);
        }
      });
    }
  }

  /**
   * Uninstall a set of toolkits.
   * 
   * @param progressMonitor
   *          a progress monitor
   * @throws ProvisionException
   *           a P2 exception
   * @throws OperationCanceledException
   *           thrown if the user canceled
   */
  public void uninstallToolkits(final IProgressMonitor progressMonitor) throws ProvisionException,
      OperationCanceledException {
    provisioningUI = ProvisioningUI.getDefaultUI();
    repositoryLoadingJob = new LoadMetadataRepositoryJob(provisioningUI);
    final Collection<IInstallableUnit> units = collectUninstallableUnits(progressMonitor);
    if (units.size() > 0) {
      final UninstallOperation installOperation =
          provisioningUI.getUninstallOperation(units, getRepositories());
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          try {
            provisioningUI.openUninstallWizard(units, installOperation, repositoryLoadingJob);
          } finally {
            progressMonitor.done();
          }
        }
      });
    }
  }

  private URI[] getRepositories() {
    List<URI> uris = new ArrayList<URI>();
    
    for (WBToolkit toolkit : toolkits) {
      URI uri = toolkit.getUpdateSiteURI();
      
      if (uri != null) {
      	uris.add(uri);
      }
      
      uri = toolkit.getAuxiliaryUpdateSiteURI();
      
      if (uri != null) {
      	uris.add(uri);
      }
    }
    
    return uris.toArray(new URI[uris.size()]);
  }

  private Collection<IInstallableUnit> collectInstallableUnits(IProgressMonitor progressMonitor)
      throws ProvisionException, OperationCanceledException {
    String statusText = Messages.P2Provisioner_statusInstalling;
    SubMonitor monitor = SubMonitor.convert(progressMonitor, statusText, 100 * toolkits.size());
    Collection<IInstallableUnit> units = new ArrayList<IInstallableUnit>();
    for (WBToolkit toolkit : toolkits) {
      ProvisioningSession session = provisioningUI.getSession();
      URI updateSiteURI = toolkit.getUpdateSiteURI();
      if (updateSiteURI == null) {
        continue;
      }
      IProvisioningAgent agent = session.getProvisioningAgent();
      // Get the repository managers and define our repositories.
      IMetadataRepositoryManager manager =
          (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
      IArtifactRepositoryManager artifactManager =
          (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
      
      URI auxiliaryUpdateUrl = toolkit.getAuxiliaryUpdateSiteURI();
      
      if (auxiliaryUpdateUrl != null) {
        manager.addRepository(auxiliaryUpdateUrl);
        artifactManager.addRepository(auxiliaryUpdateUrl);
	      
        // Load and query the metadata.
        manager.loadRepository(auxiliaryUpdateUrl, monitor.newChild(25));
      }
      
      manager.addRepository(updateSiteURI);
      artifactManager.addRepository(updateSiteURI);
      
      IMetadataRepository metadataRepo =
          manager.loadRepository(updateSiteURI, monitor.newChild(25));
      
      for (WBToolkitFeature feature : toolkit.getFeatures()) {
        // ??? necessary magic
        String featureId = feature.getFeatureId() + ".feature.group";
        Collection<IInstallableUnit> featureResults =
            metadataRepo.query(
                QueryUtil.createLatestQuery(QueryUtil.createIUQuery(featureId)),
                monitor.newChild(50)).toUnmodifiableSet();
        units.addAll(featureResults);
      }
    }
    
    return units;
  }

  private Collection<IInstallableUnit> collectUninstallableUnits(IProgressMonitor progressMonitor) {
    List<IInstallableUnit> units = new ArrayList<IInstallableUnit>();
    IProfile profile = getCurrentProfile();
    if (profile != null) {
      for (WBToolkit toolkit : toolkits) {
        for (WBToolkitFeature feature : toolkit.getFeatures()) {
          IQueryResult<IInstallableUnit> results =
              profile.available(
                  QueryUtil.createIUQuery(feature.getFeatureId() + ".feature.group"),
                  new NullProgressMonitor());
          units.addAll(results.toSet());
        }
      }
    }
    return units;
  }

  private IProfile getCurrentProfile() {
    // get the agent
    ServiceReference<IProvisioningAgentProvider> sr =
        WBDiscoveryCorePlugin.getBundleContext().getServiceReference(
            IProvisioningAgentProvider.class);
    if (sr == null) {
      return null;
    }
    IProvisioningAgentProvider agentProvider =
        WBDiscoveryCorePlugin.getBundleContext().getService(sr);
    try {
      // null == the current Eclipse installation
      IProvisioningAgent agent = agentProvider.createAgent(null);
      IProfileRegistry profileRegistry =
          (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
      return profileRegistry.getProfile(IProfileRegistry.SELF);
    } catch (ProvisionException e) {
      return null;
    }
  }
}
