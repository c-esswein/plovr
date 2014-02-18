package org.plovr.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.plovr.CompilationException;
import org.plovr.Config;
import org.plovr.ConfigParser;
import org.plovr.JsInput;
import org.plovr.Manifest;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.javascript.jscomp.GoogleJsMessageIdGenerator;
import com.google.javascript.jscomp.JsMessage;
import com.google.javascript.jscomp.JsMessageExtractor;
import com.google.javascript.jscomp.SourceFile;

public class ExtractCommand extends AbstractCommandRunner<ExtractCommandOptions> {

  @Override
  ExtractCommandOptions createOptions() {
    return new ExtractCommandOptions();
  }

  @Override
  String getUsageIntro() {
    return "Specify a config file with the messages to extract.";
  }

  @Override
  int runCommandWithOptions(ExtractCommandOptions options) throws IOException {
    // Exit if the user did not supply a single config file.
    List<String> arguments = options.getArguments();
    if (arguments.size() != 1) {
      printUsage();
      return 1;
    }

    // Use the config file to get the list of inputs, in order.
    String configFile = arguments.get(0);
    Config config = ConfigParser.parseFile(new File(configFile));
    Manifest manifest = config.getManifest();
    List<JsInput> inputs;
    try {
      inputs = manifest.getInputsInCompilationOrder();
    } catch (CompilationException e) {
      System.err.println(e.getMessage());
      return 1;
    }

    JsMessageExtractor extractor =
      new JsMessageExtractor(
        new GoogleJsMessageIdGenerator(null), JsMessage.Style.CLOSURE);
    System.out.println("<translationbundle lang=\"REPLACE_ME\">");
    for (JsMessage message : extractor.extractMessages(
           Iterables.transform(inputs, new Function<JsInput, SourceFile>() {
               @Override public SourceFile apply(JsInput input) {
                 return SourceFile.fromGenerator(input.getName(), input);
               }
             }))) {
      System.out.println(
        "<translation id=\"" + message.getId() + "\">" +
        formatMessage(message) +
        "</translation>");
    }
    System.out.println("</translationbundle>");
    return 0;
  }

  private String formatMessage(JsMessage message) {
    StringBuilder out = new StringBuilder();
    // if (message.getHidden()) {
    //   out.append("<hidden/>\n");
    // }
    // if (message.getDesc() != null) {
    //   out.append("<desc>" + message.getDesc() + "</desc>\n");
    // }
    // if (message.getMeaning() != null) {
    //   out.append("<meaning>" + message.getMeaning() + "</meaning>\n");
    // }
    for (CharSequence part : message.parts()) {
      // TODO: XML-escape
      if (part instanceof JsMessage.PlaceholderReference) {
        // Placeholder References need to be stored in
        // UPPER_UNDERSCORE format, with some exceptions. See
        // JsMessageVisitor.toLowerCamelCaseWithNumericSuffixes for
        // details.
        String phName = toUpperUnderscoreWithNumbericSuffixes(
            ((JsMessage.PlaceholderReference)part).getName());
        out.append("<ph name=\"" + phName + "\"/>");
      } else {
        out.append(part);
      }
    }
    return out.toString();
  }

  /**
   * Converts the given string from lower-camel case to
   * upper-underscore case, preserving numeric suffixes. For example,
   * "name" -> "NAME", "A4_LETTER" -> "a4Letter", "START_SPAN_1_23" ->
   * "startSpan_1_23". This is done to counteract the logic that
   * happens when the XTB bundle is read in.
   */
  static String toUpperUnderscoreWithNumbericSuffixes(String input) {
    // Copied from JsMessageVisitor.toLowerCamelCaseWithNumericSuffixes
    // Determine where the numeric suffixes begin
    int suffixStart = input.length();
    while (suffixStart > 0) {
      char ch = '\0';
      int numberStart = suffixStart;
      while (numberStart > 0) {
        ch = input.charAt(numberStart - 1);
        if (Character.isDigit(ch)) {
          numberStart--;
        } else {
          break;
        }
      }
      if ((numberStart > 0) && (numberStart < suffixStart) && (ch == '_')) {
        suffixStart = numberStart - 1;
      } else {
        break;
      }
    }

    if (suffixStart == input.length()) {
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, input);
    } else {
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE,
          input.substring(0, suffixStart)) +
          input.substring(suffixStart);
    }
  }
}
