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
package org.eclipse.wb.swt.widgets.baseline;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class BaselineActivator extends AbstractUIPlugin {
	private static BaselineActivator plugin;

	/* $if eclipse.version < 3.0 $
  public BaselineActivator(org.eclipse.core.runtime.IPluginDescriptor descriptor) {
  	super(descriptor);
  }
  $endif $ */
	/* $if eclipse.version >= 3.0 $ */
	@Override
	public void start(org.osgi.framework.BundleContext context) throws Exception {
		super.start(context);
		/* $else $
    public void startup() throws org.eclipse.core.runtime.CoreException {
    	super.startup();
    $endif $ */
		plugin = this;
	}

	/* $if eclipse.version >= 3.0 $ */
	@Override
	public void stop(org.osgi.framework.BundleContext context) throws Exception {
		/* $else $
    public void shutdown() throws org.eclipse.core.runtime.CoreException {
    $endif $ *//* $if eclipse.version >= 3.0 $ */
		plugin = null;
		/* $if eclipse.version >= 3.0 $ */
		super.stop(context);
		/* $else $
    super.shutdown();
    $endif $ */
	}

	public static BaselineActivator getDefault() {
		return plugin;
	}
}
