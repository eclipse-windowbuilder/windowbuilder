/**
   Copyright 2000  The Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.eclipse.wb.internal.css.parser.scanner;

/**
 * This class encapsulates a general parse error or warning.
 * 
 * <p>
 * This class can contain basic error or warning information from either the parser or the
 * application.
 * 
 * <p>
 * If the application needs to pass through other types of exceptions, it must wrap those exceptions
 * in a ParseException.
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: ParseException.java,v 1.1 2006-10-30 08:51:05 rkosta Exp $
 */
public class ParseException extends RuntimeException {
  private static final long serialVersionUID = 0L;
  /**
   * @serial The embedded exception if tunnelling, or null.
   */
  protected Exception exception;
  /**
   * @serial The offset.
   */
  protected int offset;

  /**
   * Creates a new ParseException.
   * 
   * @param message
   *          The error or warning message.
   * @param line
   *          The line of the last parsed character.
   * @param column
   *          The column of the last parsed character.
   */
  public ParseException(String message, int offset) {
    super(message);
    exception = null;
    this.offset = offset;
  }

  /**
   * Creates a new ParseException wrapping an existing exception.
   * 
   * <p>
   * The existing exception will be embedded in the new one, and its message will become the default
   * message for the ParseException.
   * 
   * @param e
   *          The exception to be wrapped in a ParseException.
   */
  public ParseException(Exception e) {
    exception = e;
    offset = -1;
  }

  /**
   * Creates a new ParseException from an existing exception.
   * 
   * <p>
   * The existing exception will be embedded in the new one, but the new exception will have its own
   * message.
   * 
   * @param message
   *          The detail message.
   * @param e
   *          The exception to be wrapped in a SAXException.
   */
  public ParseException(String message, Exception e) {
    super(message);
    exception = e;
  }

  /**
   * Return a detail message for this exception.
   * 
   * <p>
   * If there is a embedded exception, and if the ParseException has no detail message of its own,
   * this method will return the detail message from the embedded exception.
   * 
   * @return The error or warning message.
   */
  @Override
  public String getMessage() {
    String message = super.getMessage();
    if (message == null && exception != null) {
      return exception.getMessage();
    } else {
      return message;
    }
  }

  /**
   * Return the embedded exception, if any.
   * 
   * @return The embedded exception, or null if there is none.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Returns the offset of the last parsed character.
   */
  public int getOffset() {
    return offset;
  }
}
