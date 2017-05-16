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
package org.eclipse.wb.internal.core.utils.exception;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.core.editor.errors.IExceptionRewriter;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.errors.ErrorEntryInfo;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.osgi.framework.Bundle;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Helper for {@link DesignerException} manipulations.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.util
 */
public final class DesignerExceptionUtils {
  /**
   * @return the HTML for displaying given {@link Throwable}.
   *
   * @param message
   *          the optional message (can be <code>null</code>).
   * @param e
   *          the {@link Throwable} to display.
   */
  public static String getExceptionHTML0(String message, Throwable e) {
    String html = "";
    if (message != null) {
      html += "<H2>" + message + "</H2>";
    }
    // prepare root exception
    Throwable rootException = getDesignerCause(e);
    // prepare HTML text
    if (rootException != null) {
      // for DesignerException get description
      if (rootException instanceof DesignerException) {
        DesignerException designerException = (DesignerException) rootException;
        String description = getErrorEntry(designerException).getDescription();
        html += StringEscapeUtils.escapeHtml(description);
      }
      // add stack trace
      {
        html += Messages.DesignerExceptionUtils_stackTraceLabel;
        html += "<pre>";
        html += StringEscapeUtils.escapeHtml(ExceptionUtils.getStackTrace(rootException));
        html += "</pre>";
      }
      // add full stack trace
      {
        html += Messages.DesignerExceptionUtils_fullStackTraceLabel;
        html += "<pre>";
        html += StringEscapeUtils.escapeHtml(ExceptionUtils.getStackTrace(e));
        html += "</pre>";
      }
    } else {
      html += "<pre>";
      html += Messages.DesignerExceptionUtils_noExceptionLabel;
      html += "</pre>";
    }
    return html;
  }

  /**
   * @return the HTML for displaying given {@link Throwable} based on template html file.
   *
   * @param e
   *          the {@link Throwable} to display.
   */
  public static String getExceptionHTML(Throwable e) {
    ErrorEntryInfo errorDescription = getErrorEntry(e);
    if (!BrowserComposite.browserAvailable(DesignerPlugin.getShell())) {
      return getPlainTextDescription(errorDescription) + "\n" + ExceptionUtils.getStackTrace(e);
    }
    // prepare HTML template
    String html;
    InputStream htmlStream = null;
    try {
      htmlStream = DesignerPlugin.getFile("icons/actions/errors/errorTemplate.html");
      html = IOUtils2.readString(htmlStream);
    } catch (Throwable _e) {
      DesignerPlugin.log(_e);
      return getPlainTextDescription(errorDescription);
    } finally {
      IOUtils.closeQuietly(htmlStream);
    }
    // prepare color
    Color color = IColorConstants.button;
    String colorString = getColorWebString(color);
    html = StringUtils.replace(html, "%bg_color%", colorString);
    // prepare HTML
    html = StringUtils.replace(html, "%error_message%", errorDescription.getTitle());
    html = StringUtils.replace(html, "%error_description%", errorDescription.getDescription());
    html = includeStackTrace(html, "%stack_trace_short%", getDesignerCause(e));
    html = includeStackTrace(html, "%stack_trace_full%", e);
    return html;
  }

  private static String includeStackTrace(String html, String searchString, Throwable e) {
    String stackTrace = ExceptionUtils.getStackTrace(e);
    String stackTraceEscaped = StringEscapeUtils.escapeHtml(stackTrace);
    return StringUtils.replace(html, searchString, stackTraceEscaped);
  }

  /**
   * @return the HTML for displaying given warning based on template HTML file.
   */
  public static String getWarningHTML(ErrorEntryInfo entry) {
    if (!BrowserComposite.browserAvailable(DesignerPlugin.getShell())) {
      return getPlainTextDescription(entry);
    }
    String html;
    InputStream htmlStream = null;
    try {
      htmlStream = DesignerPlugin.getFile("icons/actions/errors/warningTemplate.html");
      html = IOUtils2.readString(htmlStream);
    } catch (Throwable _e) {
      DesignerPlugin.log(_e);
      return getPlainTextDescription(entry);
    } finally {
      IOUtils.closeQuietly(htmlStream);
    }
    // prepare color
    Color color = IColorConstants.button;
    String colorString = getColorWebString(color);
    html = StringUtils.replace(html, "%bg_color%", colorString);
    // apply entry
    html = StringUtils.replace(html, "%error_description%", entry.getDescription());
    return html;
  }

  /**
   * Checks for alternative plain-text description and returns it. For other case strips html tags
   * and returns plain-text from html-based description.
   */
  private static String getPlainTextDescription(ErrorEntryInfo errorEntry) {
    if (!StringUtils.isEmpty(errorEntry.getAltDescription())) {
      return errorEntry.getAltDescription();
    } else {
      // remove html tags if any
      String description = errorEntry.getDescription();
      description = StringUtilities.stripHtml(description);
      // 20110509.Kosta: keep all characters for now
      //description = StringUtils.replaceChars(description, "~`@#$%^&*()-=+[]\\{}|;':\",./<>?!", "");
      return description;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<Throwable, Integer> m_exceptionPositions = new MapMaker().weakKeys().makeMap();

  /**
   * See {@link ExceptionUtils#getRootCause(Throwable)}, but returns {@link Throwable} itself if no
   * causes.
   */
  @SuppressWarnings("unchecked")
  public static Throwable getRootCause(Throwable throwable) {
    List<Throwable> list = ExceptionUtils.getThrowableList(throwable);
    return GenericsUtils.getLastOrNull(list);
  }

  /**
   * @return the {@link Throwable} may be rewritten by {@link IExceptionRewriter}.
   */
  public static Throwable rewriteException(Throwable throwable) {
    List<IExceptionRewriter> rewriters =
        ExternalFactoriesHelper.getElementsInstances(
            IExceptionRewriter.class,
            "org.eclipse.wb.core.exceptions",
            "rewriter");
    for (IExceptionRewriter rewriter : rewriters) {
      throwable = rewriter.rewrite(throwable);
    }
    return throwable;
  }

  /**
   * @return the first {@link DesignerException} in causes list, excluding wrappers. If no
   *         {@link DesignerException}, then initial/root {@link Throwable} returned.
   */
  public static Throwable getDesignerCause(Throwable throwable) {
    // try to find DesignerException
    for (Throwable e = throwable; e != null; e = e.getCause()) {
      if (e instanceof DesignerException) {
        return e;
      }
    }
    // no DesignerException, use root cause
    return getRootCause(throwable);
  }

  /**
   * @return the first {@link DesignerException} in causes list, excluding wrappers. Always. If no
   *         {@link DesignerException}, then {@link ClassCastException} will happen.
   */
  public static DesignerException getDesignerException(Throwable throwable) {
    return (DesignerException) getDesignerCause(throwable);
  }

  /**
   * Remembers the source position, associated with given {@link Throwable}.
   */
  public static void setSourcePosition(Throwable throwable, int position) {
    m_exceptionPositions.put(throwable, position);
  }

  /**
   * @return the source position, associated with given {@link Throwable}, may be -1, if no
   *         position.
   */
  public static int getSourcePosition(Throwable throwable) {
    Integer position = m_exceptionPositions.get(throwable);
    if (position != null) {
      return position;
    }
    return -1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<Integer, ErrorEntryInfo> m_codeToDescription;

  public static boolean isWarning(Throwable throwable) {
    Throwable rootException = getDesignerCause(throwable);
    if (rootException instanceof DesignerException) {
      DesignerException designerException = (DesignerException) rootException;
      int code = designerException.getCode();
      ErrorEntryInfo entry = getErrorEntry0(code);
      return entry != null && entry.isWarning();
    }
    return false;
  }

  public static boolean isFatal(Throwable throwable) {
    while (throwable != null) {
      if (throwable instanceof FatalDesignerException) {
        return true;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  /**
   * @return <code>true</code> if given {@link Throwable} has given sequence on class/method names.
   */
  public static boolean hasTraceElementsSequence(Throwable e, String[][] expected) {
    StackTraceElement[] elements = e.getStackTrace();
    traceElements : for (int i = 0; i < elements.length; i++) {
      for (int j = 0; j < expected.length; j++) {
        if (i + j < elements.length) {
          StackTraceElement element = elements[i + j];
          String expectedClass = expected[j][0];
          String expectedMethod = expected[j][1];
          if (element.getClassName().equals(expectedClass)
              && element.getMethodName().equals(expectedMethod)) {
            continue;
          }
        }
        continue traceElements;
      }
      return true;
    }
    return false;
  }

  /**
   * @return the title that corresponds to the given exception code, may be some default, but not
   *         <code>null</code>.
   */
  public static String getExceptionTitle(int exceptionCode) {
    ErrorEntryInfo entry = getErrorEntry0(exceptionCode);
    if (entry == null) {
      return Messages.DesignerExceptionUtils_noDescriptionTitle2;
    }
    return entry.getTitle();
  }

  /**
   * @return the error description entry for given {@link Throwable}.
   */
  public static ErrorEntryInfo getErrorEntry(Throwable throwable) {
    Throwable rootException = getDesignerCause(throwable);
    if (rootException instanceof DesignerException) {
      DesignerException designerException = (DesignerException) rootException;
      int code = designerException.getCode();
      String[] parameters = designerException.getParameters();
      return getErrorEntry(code, parameters);
    } else {
      return getUnexpectedErrorEntryInfo(throwable);
    }
  }

  /**
   * Create error entry for other exceptions which are not instance of {@link DesignerException}.
   *
   * @return the {@link ErrorEntryInfo} instance.
   */
  private static ErrorEntryInfo getUnexpectedErrorEntryInfo(Throwable throwable) {
    String message = throwable.getMessage();
    ErrorEntryInfo e = getErrorEntry(ICoreExceptionConstants.UNEXPECTED);
    // append exception message
    String desc = e.getDescription();
    desc += message != null ? "<p>" + throwable.getClass().getName() + ": " + message + "</p>" : "";
    // return updated
    return new ErrorEntryInfo(e.getCode(), e.isWarning(), e.getTitle(), desc, e.getAltDescription());
  }

  public static ErrorEntryInfo getErrorEntry(int exceptionCode, String... parameters) {
    // try to find existing entry
    {
      ErrorEntryInfo entry = getErrorEntry0(exceptionCode, parameters);
      if (entry != null) {
        return entry;
      }
    }
    // not found, use default
    {
      String description =
          MessageFormat.format(Messages.DesignerExceptionUtils_noDescriptionMessage, exceptionCode);
      return new ErrorEntryInfo(exceptionCode,
          false,
          Messages.DesignerExceptionUtils_noDescriptionTitle,
          description);
    }
  }

  private static ErrorEntryInfo getErrorEntry0(int exceptionCode, String... parameters) {
    // get error entry if any and prepare it.
    ErrorEntryInfo errorEntryInfo = getErrorEntry0(exceptionCode);
    if (errorEntryInfo != null) {
      String description = errorEntryInfo.getDescription();
      if (description != null) {
        // replace {0}, {1}, etc with parameters
        for (int i = 0; i < parameters.length; i++) {
          String template = "{" + i + "}";
          int index = description.indexOf(template);
          if (index != -1) {
            description = includeExceptionParameter(description, template, parameters[i]);
          }
        }
        // prepared
        return new ErrorEntryInfo(errorEntryInfo.getCode(),
            errorEntryInfo.isWarning(),
            errorEntryInfo.getTitle(),
            description,
            errorEntryInfo.getAltDescription());
      }
    }
    // no description
    return null;
  }

  private static ErrorEntryInfo getErrorEntry0(int exceptionCode) {
    if (m_codeToDescription == null || EnvironmentUtils.DEVELOPER_HOST) {
      fillErrorEntries();
    }
    return m_codeToDescription.get(exceptionCode);
  }

  private static String includeExceptionParameter(String html, String searchString, String message) {
    String messageEscaped = StringEscapeUtils.escapeHtml(message);
    return StringUtils.replace(html, searchString, messageEscaped);
  }

  /**
   * Reads exception definitions (using extensions) and creates code->entry map for error entries.
   */
  private static void fillErrorEntries() {
    m_codeToDescription = Maps.newTreeMap();
    try {
      List<IConfigurationElement> fileElements =
          ExternalFactoriesHelper.getElements("org.eclipse.wb.core.exceptions", "file");
      for (IConfigurationElement fileElement : fileElements) {
        // parse single exceptions file
        try {
          Bundle fileBundle = ExternalFactoriesHelper.getExtensionBundle(fileElement);
          String filePath = fileElement.getAttribute("path");
          URL fileURL = fileBundle.getEntry(filePath);
          //
          final String xmlText = IOUtils2.readString(fileURL.openStream());
          QParser.parse(new StringReader(xmlText), new QHandlerAdapter() {
            private ErrorEntryInfo m_errorEntry;
            private int m_code;
            private boolean m_isWarning;
            private String m_description;
            private String m_altDescription;
            private int m_descriptionStart;

            @Override
            public void startElement(int offset,
                int length,
                String tag,
                Map<String, String> attributes,
                List<QAttribute> attrList,
                boolean closed) throws Exception {
              if ("exception".equals(tag)) {
                m_code = Integer.parseInt(attributes.get("id"));
                m_isWarning = "true".equals(attributes.get("warning"));
                m_description = attributes.get("title");
                m_altDescription = applyBranding(attributes.get("alt"));
                m_descriptionStart = xmlText.indexOf('>', offset) + 1;
              }
            }

            @Override
            public void endElement(int offset, int endOffset, String tag) throws Exception {
              if ("exception".equals(tag)) {
                // extract description
                String description = xmlText.substring(m_descriptionStart, offset);
                description = applyBranding(description);
                // add entry
                m_errorEntry =
                    new ErrorEntryInfo(m_code,
                        m_isWarning,
                        m_description,
                        description,
                        m_altDescription);
                m_codeToDescription.put(m_errorEntry.getCode(), m_errorEntry);
              }
            }

            private String applyBranding(String description) {
              if (description == null) {
                return null;
              }
              String productNameTemplate = "{product_name}";
              int index = description.indexOf(productNameTemplate);
              if (index != -1) {
                description =
                    includeExceptionParameter(
                        description,
                        productNameTemplate,
                        BrandingUtils.getBranding().getProductName());
              }
              return description;
            }
          });
        } catch (Throwable ex) {
        }
      }
    } catch (Throwable ex) {
      DesignerPlugin.log(ex);
    }
  }

  /**
   * Clears cache for "org.eclipse.wb.core.exceptions" descriptions.
   */
  public static void flushErrorEntriesCache() {
    m_codeToDescription = null;
  }

  /**
   * Returns a string representation of {@link Color} suitable for web pages.
   *
   * @param color
   *          the {@link Color} instance, not <code>null</code>.
   * @return a string representation of {@link Color} suitable for web pages.
   */
  public static String getColorWebString(final Color color) {
    String colorString = "#" + Integer.toHexString(color.getRed());
    colorString += Integer.toHexString(color.getGreen());
    colorString += Integer.toHexString(color.getBlue());
    return colorString;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot of problem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates screen shot of entire Eclipse shell.
   *
   * @return an image of entire Eclipse.
   */
  public static Image makeScreenshot() {
    Shell shell = DesignerPlugin.getShell();
    shell.redraw();
    shell.update();
    final Rectangle shellBounds = shell.getBounds();
    final Display standardDisplay = DesignerPlugin.getStandardDisplay();
    standardDisplay.update();
    Image image = new Image(standardDisplay, shellBounds.width, shellBounds.height);
    GC gc = new GC(standardDisplay);
    gc.copyArea(image, shellBounds.x, shellBounds.y);
    gc.dispose();
    return image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // External browser
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Open given url in external browser.
   *
   * @param url
   *          the url string to open in external browser.
   */
  public static void openBrowser(String url) {
    try {
      IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
      IWebBrowser browserSupport = support.createBrowser("wbp.browser");
      browserSupport.openURL(new URL(url));
    } catch (Throwable e) {
      // for every error copy url into clipboard and show message box.
      Clipboard clipboard = new Clipboard(DesignerPlugin.getStandardDisplay());
      clipboard.setContents(new String[]{url}, new Transfer[]{TextTransfer.getInstance()});
      clipboard.dispose();
      MessageDialog.openInformation(
          DesignerPlugin.getShell(),
          Messages.DesignerExceptionUtils_openUrlTitle,
          MessageFormat.format(Messages.DesignerExceptionUtils_openUrlMessage, url));
    }
  }
}
