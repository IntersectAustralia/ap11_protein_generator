package au.org.intersect.protein_generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.Writer;

import au.org.intersect.protein_generator.runner.ProteinGeneratorRunner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.StringUtils;

public class ProteinGenerator {

    private ProteinGenerator(){}



    public static void main(String[] args)
    {
        Option translationTableOpt =
            OptionBuilder.withArgName("Translation Table File")
                         .hasArg()
                         .withDescription("File containing a mapping of codons to amino acids, in the format used by NCBI.")
                         .create("t");
        Option splitIntervalOpt =
            OptionBuilder.withArgName("Split Interval")
                         .hasArg()
                         .withType(Number.class)
                         .withDescription("Size of the intervals (number of codons) into which the genome will be split. Can't be used with the -g option.")
                         .create("i");
        Option glimmerFileOpt =
            OptionBuilder.withArgName("Glimmer File")
                         .hasArg()
                         .withDescription("Glimmer txt file. Can't be used with the -i option.")
                         .create("g");
        Option genomeFileOpt =
            OptionBuilder.withArgName("Genome File")
                         .hasArg()
                         .withDescription("Genome file in FASTA format")
                         .isRequired()
                         .create("f");
        Option databaseNameOpt =
            OptionBuilder.withArgName("Database Name")
                         .hasArg()
                         .withDescription("Database name")
                         .isRequired()
                         .create("d");
        Option outputFileOpt =
            OptionBuilder.withArgName("Output File")
                         .hasArg()
                         .withDescription("Filename to write the FASTA format file to")
                         .isRequired()
                         .create("o");
        Options options = new Options();
        options.addOption(translationTableOpt);
        options.addOption(splitIntervalOpt);
        options.addOption(glimmerFileOpt);
        options.addOption(genomeFileOpt);
        options.addOption(databaseNameOpt);
        options.addOption(outputFileOpt);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse( options, args );
            File translationTableFile = new File(line.getOptionValue("t"));
            File genomeFile = new File(line.getOptionValue("f"));
            String glimmerFilePath = line.getOptionValue("g");
            String interval = line.getOptionValue("i");
            String databaseName = line.getOptionValue("d");
            File outfile = new File(line.getOptionValue("o"));
            Writer outputWriter = new FileWriter(outfile);

            if ((glimmerFilePath == null && interval == null) ||
                (glimmerFilePath != null && interval != null))
            {
                throw new ParseException("Only one of -i or -g permitted");
            }
            ProteinGeneratorRunner runner = new ProteinGeneratorRunner(glimmerFilePath, genomeFile, interval, databaseName, outputWriter, translationTableFile);
            runner.run();
        }
        catch (ParseException pe)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("protein_generator", options, true);
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}

