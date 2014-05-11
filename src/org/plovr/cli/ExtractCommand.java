package org.plovr.cli;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.plovr.CompilationException;
import org.plovr.Config;
import org.plovr.ConfigParser;
import org.plovr.JsInput;
import org.plovr.LanguageConfig.Language;
import org.plovr.Manifest;

import sk.kuzmisin.xtbgenerator.XtbGenerator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.SourceFile;

public class ExtractCommand extends
		AbstractCommandRunner<ExtractCommandOptions> {

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
		List<String> arguments = options.getArguments();
		if (arguments.size() != 1) {
			printUsage();
			return 1;
		}
		
		for (String configFile: arguments) {
			Config config = ConfigParser.parseFile(new File(configFile));
			
			// TODO support for modules... --> getModules()->getInputs...
			
			// get input files
			Manifest manifest = config.getManifest();
			List<JsInput> inputs;
			try {
				inputs = manifest.getInputsInCompilationOrder();
			} catch (CompilationException e) {
				System.err.println(e.getMessage());
				return 1;
			}
			// convert input files
			Collection<SourceFile> inputFiles = Lists.transform(inputs, new Function<JsInput, SourceFile>() {
				@Override
				public SourceFile apply(JsInput input) {
					return SourceFile.fromGenerator(input.getName(), input);
				}
			});
			
			final String projectId = config.getId();

			for(Language lang : config.getLanguageConfig().getLanguages()){
				File existingTranslation = null;
				
				// take existing translation file as input
				if(lang.translationFile.exists()){
					existingTranslation = lang.translationFile;
				}
				
				XtbGenerator.process(lang.langKey, projectId, inputFiles, existingTranslation, lang.translationFile);
			}
		}
		
		return 0;
	}
}
