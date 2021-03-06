package se.sogeti.umea.cvconverter.application.impl.cvparser;

import java.io.IOException;

import javax.inject.Inject;
import javax.swing.text.DefaultStyledDocument;

import se.sogeti.umea.configuration.InjectedConfiguration;
import se.sogeti.umea.cvconverter.application.ContentLanguage;
import se.sogeti.umea.cvconverter.application.CurriculumVitae;

public class Parser {

	RtfParser rtfParser;
	DocumentParser documentParser;
	StringParser stringParser;
	XmlGenerator xmlGenerator;

	// Sets Parser to include or exclude portrait url from RTF document
	@Inject
	@InjectedConfiguration(key = "parser.portrait.mode", defaultValue = "INCLUDE")
	private String mode;

	public Parser() {
		// CDI
	}

	@Inject
	public Parser(RtfParser rtfParser, DocumentParser documentParser,
			StringParser stringParser, XmlGenerator xmlGenerator) {
		this.rtfParser = rtfParser;
		this.documentParser = documentParser;
		this.stringParser = stringParser;
		this.xmlGenerator = xmlGenerator;
	}

	public CurriculumVitae parseToModel(String rtf, String encoding)
			throws IllegalArgumentException, IOException {

		ContentLanguage language = rtfParser.parseLanguage(rtf);
		if (language == null) {
			throw new IllegalArgumentException(
					"Unable to parse the content language in CV!");
		}

		String printDate = rtfParser.parsePrintDate(rtf);

		String portraitUrl = null;
		if ("INCLUDE".equals(mode)) {
			portraitUrl = rtfParser.parsePortraitUrl(rtf);
		}

		DefaultStyledDocument doc = rtfParser.parseRtf(rtf);

		StringBuilder sb = documentParser.parseDocument(doc);

		return stringParser.parseString(language, printDate, portraitUrl, sb);

	}

}