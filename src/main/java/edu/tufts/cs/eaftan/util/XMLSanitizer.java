/*
 * Copyright 2014 Edward Aftandilian. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.tufts.cs.eaftan.util;

/**
 * This class provides a facility to sanitize characters for XML output.
 * It will replace some characters with escape sequences, and scrub
 * invalid characters from output. 
 */

public class XMLSanitizer {
  
  /**
   * Given an input string, return the same string with characters that 
   * are supposed to be escaped, escaped.
   * 
   * @param input The string to be escaped
   * @return The same string with characters escaped
   */
  public static String escape(String input) {
    StringBuilder output = new StringBuilder();
    for (int i=0; i<input.length(); i++) {
      if (input.charAt(i) == '\"') 
        output.append("&quot;");
      else if (input.charAt(i) == '\'') 
        output.append("&apos;");
      else if (input.charAt(i) == '<')
        output.append("&lt;");
      else if (input.charAt(i) == '>')
        output.append("&gt;");
      else if (input.charAt(i) == '&')
        output.append("&amp;");
      else 
        output.append(input.charAt(i));
    }
    return output.toString(); 
  }
  
  /**
   * Sanitize an input string by replacing any instance of a invalid
   * XML character with a placeholder character. 
   */
  public static String sanitize(String input) {   
    StringBuilder output = new StringBuilder();
    for (int i=0; i<input.length(); i++) {
      if (isValid(input.charAt(i)))
        output.append(input.charAt(i));
      else
        output.append(0xfffd);
    }
    return output.toString();
  }
  
  /**
   * Is this a valid XML character?
   * Valid characters from here:
   * http://www.w3.org/TR/REC-xml/#charsets
   * 
   * @param c The character to validated
   * @return True if the character is valid for XML
   */
  private static boolean isValid(char c) {
    if (c == 0x9 || c == 0xa || c == 0xd || 
        (c >= 0x20 && c <= 0xd7ff) || 
        (c >= 0xe000 && c <= 0xfffd))
      return true;
    else
      return false;
  }

}
