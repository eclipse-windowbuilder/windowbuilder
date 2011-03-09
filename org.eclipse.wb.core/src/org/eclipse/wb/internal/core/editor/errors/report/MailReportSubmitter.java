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
/**
 * Opens mailing program and asks the user to attach saved report data to prepared mail message.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
package org.eclipse.wb.internal.core.editor.errors.report;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * Submit report by email: copy report file path into clipboard, creates "mailto:" url, opens it in
 * browser and opens system default "file explorer".
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
final class MailReportSubmitter implements IReportSubmitter {
  public void submit(String filePath, IProgressMonitor monitor) throws Exception {
    // copy file name into clipboard
    Clipboard clipboard = new Clipboard(DesignerPlugin.getStandardDisplay());
    clipboard.setContents(new String[]{filePath}, new Transfer[]{TextTransfer.getInstance()});
    clipboard.dispose();
    // ask the user
    MessageDialog.openInformation(
        DesignerPlugin.getShell(),
        "Submit report manually via email or forum",
        "Please send the following file to product support via email (if you\n"
            + "have a support contract) or post it to the product support forum.\n\""
            + filePath
            + "\"\n"
            + "(this file path has been copied to the clipboard).");
    // prepare mail
    //		ISupport support = DesignerSupport.getInstance();
    //		IProduct product = support.getProduct();
    //		URL url = new URL(URLUtilities.encodeURL(
    //				"mailto:"
    //					+ product.getSupportEmailAddress()
    //					+ "?subject="
    //					+ "Support request for "
    //					+ product.getName()
    //					+ " "
    //					+ product.getVersion()
    //					+ "&body="
    //					+ "Please attach the support report file: "
    //					+ filePath));
    //		// open browser with mailto request
    //		DesignerExceptionUtils.openBrowser(url.toExternalForm());
    //		Program.launch(new Path(filePath).removeLastSegments(1).toOSString());
  }
}