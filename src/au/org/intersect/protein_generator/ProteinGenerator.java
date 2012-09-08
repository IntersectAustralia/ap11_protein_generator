package au.org.intersect.protein_generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.Writer;

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

    public static String invertNucleotideSequence(String sequence)
    {
        return StringUtils.replaceChars(sequence, "ACGT", "TGCA");
    }

    public static List<ProteinLocation> parseGlimmerFile(File glimmerFile)
        throws GlimmerFileParsingException, IOException
    {
        int lineCount = 0;
        List<ProteinLocation> proteinLocations = new ArrayList<ProteinLocation>();
        for(String line : FileUtils.readLines(glimmerFile))
        {
            lineCount++;
            if (line.startsWith(">"))
            {
                continue;
            }
            String[] columns = line.split("\\s+");
            if (columns.length < 5)
            {
                throw new GlimmerFileParsingException("Expecting 5 columns at line: "+lineCount);
            }

            String name = columns[0];
            int firstIndex = Integer.parseInt(columns[1]);
            int secondIndex = Integer.parseInt(columns[2]);
            String direction = columns[3];

            if (direction.startsWith(ProteinLocation.FORWARD))
            {
                proteinLocations.add(new ProteinLocation(name, firstIndex, secondIndex - firstIndex, ProteinLocation.FORWARD));
            }
            else if (direction.startsWith(ProteinLocation.BACKWARD))
            {
                proteinLocations.add(new ProteinLocation(name, secondIndex, firstIndex - secondIndex, ProteinLocation.BACKWARD));
            }
            else
            {
                throw new GlimmerFileParsingException("Unexpected value for direction (4th column) at line: "+lineCount);
            }
        }
        return proteinLocations;
    }

    public static void generateProteinsFile(String databaseName, File genomeFile, List<ProteinLocation> locations, CodonTranslationTable table, Writer output)
        throws IOException, FileNotFoundException, UnknownCodonException
    {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        Collections.sort(locations, new ProteinLocationComparator());
        try {
            reader = new BufferedReader(new FileReader(genomeFile));
            writer = new BufferedWriter(output);

            // Skip header
            reader.readLine();
            String line = null;
            int readCursor = 0;
            for (ProteinLocation location : locations)
            {
                StringBuilder sequence = new StringBuilder();
                int startIndex = location.getStartIndex();
                int stopIndex = startIndex + location.getLength() - 1;

                // Read forward to startIndex
                while (readCursor < startIndex)
                {
                    line = StringUtils.chomp(reader.readLine());
                    readCursor += line.length();
                }

                int readStart = (startIndex - 1) % line.length();
                int readStop  = line.length();

                while (readCursor < stopIndex)
                {
                    sequence.append(line.substring(readStart, readStop));
                    readStart = 0;
                    line = StringUtils.chomp(reader.readLine());
                    readCursor += line.length();
                }

                readStop = stopIndex % line.length();
                sequence.append(line.substring(readStart, readStop + 1));

                writer.write(fastaHeader(databaseName, location.getName()));
                writer.newLine();
                if (location.getDirection().equals(ProteinLocation.BACKWARD))
                {
                    StringBuilder invertedReversedSequence = new StringBuilder(invertNucleotideSequence(sequence.toString())).reverse();
                    writer.write(table.proteinToAminoAcidSequence(invertedReversedSequence.toString()));
                }
                else
                {
                    writer.write(table.proteinToAminoAcidSequence(sequence.toString()));
                }
                writer.newLine();
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
            if (writer != null)
            {
                writer.close();
            }
        }
    }

    private static String fastaHeader(String db, String name)
    {
        return ">gn1|"+db+"|"+name;
    }

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
                         .withDescription("Size of the intervals into which the genome will be split. Can't be used with the -g option.")
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
            File glimmerFile = new File(line.getOptionValue("g"));
            String interval = line.getOptionValue("i");
            String databaseName = line.getOptionValue("d");
            File outfile = new File(line.getOptionValue("o"));

            if ((glimmerFile == null && interval == null) ||
                (glimmerFile != null && interval != null))
            {
                throw new ParseException("Only one of -i or -g permitted");
            }
            List<ProteinLocation> locations = null;
            if (glimmerFile != null)
            {
                locations = ProteinGenerator.parseGlimmerFile(glimmerFile);
            }
            else
            {
                // TODO: generate locations from split interval
            }
            Writer output = new FileWriter(outfile);
            ProteinGenerator.generateProteinsFile(databaseName, genomeFile, locations, CodonTranslationTable.parseTableFile(translationTableFile), output);
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

