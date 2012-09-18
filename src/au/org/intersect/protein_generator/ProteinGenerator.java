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

    private static final int BASES_PER_CODON = 3;
    private static final int FASTA_LINE_LENGTH = 60;

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
                proteinLocations.add(new ProteinLocation(name, firstIndex, secondIndex - firstIndex + 1, ProteinLocation.FORWARD));
            }
            else if (direction.startsWith(ProteinLocation.REVERSE))
            {
                proteinLocations.add(new ProteinLocation(name, secondIndex, firstIndex - secondIndex + 1, ProteinLocation.REVERSE));
            }
            else
            {
                throw new GlimmerFileParsingException("Unexpected value for direction (4th column) at line: "+lineCount);
            }
        }
        return proteinLocations;
    }

    public static List<ProteinLocation> createLocations(File genomeFile, int codonsPerInterval)
        throws IOException, FileNotFoundException
    {
        BufferedReader reader = new BufferedReader(new FileReader(genomeFile));
        int baseCount = 0;
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            if (line.matches("^>.*$"))
            {
                continue;
            }

            baseCount += StringUtils.chomp(line).length();
        }

        int basesPerInterval = codonsPerInterval * BASES_PER_CODON;
        if (basesPerInterval >= baseCount)
        {
            // TODO: log this to error file
            return null;
        }

        List<ProteinLocation> locations = new ArrayList<ProteinLocation>();
        int nameIndex = 0;
        int halfIntervalSize = basesPerInterval / 2;
        int lastCodonStartPosition = baseCount - BASES_PER_CODON;

        // Forward locations
        for (int i=1; i <= baseCount; i += basesPerInterval)
        {
            addLocations(locations, i, nameIndex, basesPerInterval, baseCount, true, false);
            int halfIntervalStart = i + halfIntervalSize;
            if (halfIntervalStart <= lastCodonStartPosition)
            {
                addLocations(locations, halfIntervalStart, nameIndex, basesPerInterval, baseCount, true, true);
            }
            nameIndex++;
        }

        // Reverse locations
        for (int i=baseCount; i > 0; i -= basesPerInterval)
        {
            int start = i - basesPerInterval;
            addLocations(locations, start, nameIndex, basesPerInterval, baseCount, false, false);
            int halfIntervalStart = start - halfIntervalSize;
            int halfIntervalEnd = halfIntervalStart + basesPerInterval;
            if (halfIntervalEnd > 0)
            {
                addLocations(locations, halfIntervalStart, nameIndex, basesPerInterval, baseCount, false, true);
            }
            nameIndex++;
        }
        return locations;
    }

    private static void addLocations(List<ProteinLocation> locations, int start, int nameIndex, int basesPerInterval, int baseCount, boolean isForward, boolean isHalfInterval)
      throws IOException
    {
        // 3 frame translation (see http://en.wikipedia.org/wiki/Reading_frame)
        for (int subIndex=0; subIndex < 3; subIndex++)
        {
            int startIndex = start;
            int endIndex = startIndex + basesPerInterval - 1;
            if (isForward)
            {
                startIndex += subIndex;
                endIndex   += subIndex;
            }
            else
            {
                startIndex -= subIndex;
                endIndex   -= subIndex;
            }

            // Ensure the start and end positions are a multiple of 3.
            // i.e. a full codon
            if (startIndex <= 0)
            {
                int shiftFactor = endIndex % BASES_PER_CODON;
                startIndex = 1 + shiftFactor;
                //endIndex += shiftFactor;
            }
            if (endIndex > baseCount)
            {
                int leftOverBases = (baseCount - startIndex + 1) % BASES_PER_CODON;
                endIndex = baseCount - leftOverBases;
            }

            if (startIndex >= endIndex)
            {
                continue;
            }

            String name = "p" +
                          nameIndex +
                          (isHalfInterval ? "b" : "") +
                          "." +
                          subIndex +
                          (isForward ? "W" : "C");
            int length = endIndex - startIndex + 1;
            locations.add(new ProteinLocation(name, startIndex, length, isForward ? ProteinLocation.FORWARD : ProteinLocation.REVERSE));
        }
    }

    private static StringBuilder readGenomeFile(File genomeFile)
        throws IOException, FileNotFoundException
    {
          BufferedReader reader = new BufferedReader(new FileReader(genomeFile));
          StringBuilder sequence = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null)
          {
              if (line.matches("^>.*$"))
              {
                  continue;
              }
              sequence.append(line);
          }
          return sequence;
    }

    public static void generateProteinsFile(String databaseName, File genomeFile, List<ProteinLocation> locations, CodonTranslationTable table, Writer output)
        throws IOException, FileNotFoundException, UnknownCodonException
    {
        BufferedWriter writer = null;
        Collections.sort(locations, new ProteinLocationComparator());
        StringBuilder genomeString = null;
        try {
            genomeString = readGenomeFile(genomeFile);
            writer = new BufferedWriter(output);

            // Skip header
            for (ProteinLocation location : locations)
            {
                int startIndex = location.getStartIndex() - 1;
                int stopIndex = startIndex + location.getLength();
                String sequence = genomeString.substring(startIndex, stopIndex); 
                writer.write(fastaHeader(databaseName, location.getName()));
                writer.newLine();
                String aminoAcidSequence = null;
                if (location.getDirection().equals(ProteinLocation.REVERSE))
                {
                    StringBuilder invertedReversedSequence = new StringBuilder(invertNucleotideSequence(sequence.toString())).reverse();
                    aminoAcidSequence = table.proteinToAminoAcidSequence(invertedReversedSequence.toString());
                }
                else
                {
                    aminoAcidSequence = table.proteinToAminoAcidSequence(sequence.toString());
                }
                int sequenceLength = aminoAcidSequence.length();
                int wholeParts = sequenceLength / FASTA_LINE_LENGTH;
                int sequenceCursor = 0;
                for (int i=0; i < wholeParts; i++)
                {
                    writer.write(aminoAcidSequence.substring(sequenceCursor, sequenceCursor + FASTA_LINE_LENGTH));
                    writer.newLine();
                    sequenceCursor += FASTA_LINE_LENGTH;
                }
                if (sequenceCursor < sequenceLength)
                {
                    writer.write(aminoAcidSequence.substring(sequenceCursor, sequenceLength));
                    writer.newLine();
                }
            }
        }
        finally
        {
            genomeString = null;
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
                int codonsPerInterval = Integer.parseInt(interval);
                locations = ProteinGenerator.createLocations(genomeFile, codonsPerInterval);
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

