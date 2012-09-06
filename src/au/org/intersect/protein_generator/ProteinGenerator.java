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
//import java.io.Writer;

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
    // Initialised with double brace initialisation
    // See: http://www.c2.com/cgi/wiki?DoubleBraceInitialization
    public static final Map<String, String> CODONS = Collections.unmodifiableMap(
        new HashMap<String, String>() {{
            // Isoleucine
            put("ATT", "I");
            put("ATC", "I");
            put("ATA", "I");

            // Leucine
            put("CTT", "L");
            put("CTC", "L");
            put("CTA", "L");
            put("CTG", "L");
            put("TTA", "L");
            put("TTG", "L");

            // Valine
            put("GTT", "V");
            put("GTC", "V");
            put("GTA", "V");
            put("GTG", "V");

            // Phenylalanine
            put("TTT", "F");
            put("TTC", "F");

            // Methionine
            put("ATG", "M"); // Also the start codon

            // Cysteine
            put("TGT", "C");
            put("TGC", "C");

            // Arginine
            put("GCT", "A");
            put("GCC", "A");
            put("GCA", "A");
            put("GCG", "A");

            // Proline
            put("CCT", "P");
            put("CCC", "P");
            put("CCA", "P");
            put("CCG", "P");

            // Threonine
            put("ACT", "T");
            put("ACC", "T");
            put("ACA", "T");
            put("ACG", "T");

            // Serine
            put("TCT", "S");
            put("TCC", "S");
            put("TCA", "S");
            put("TCG", "S");
            put("AGT", "S");
            put("AGC", "S");

            // Tyrosine
            put("TAT", "Y");
            put("TAC", "Y");

            // Tryptophan
            put("TGG", "W");

            // Glutamine
            put("CAA", "Q");
            put("CAG", "Q");

            // Asparagine
            put("AAT", "N");
            put("AAC", "N");

            // Histidine
            put("CAT", "H");
            put("CAC", "H");

            // Glutamic acid
            put("GAA", "E");
            put("GAG", "E");

            // Aspartic acid
            put("GAT", "D");
            put("GAC", "D");

            // Lysine
            put("AAA", "K");
            put("AAG", "K");

            // Arginine
            put("CGT", "R");
            put("CGC", "R");
            put("CGA", "R");
            put("CGG", "R");
            put("AGA", "R");
            put("AGG", "R");
        }}
    );

    public static final String START_CODON = "ATG";

    public static final Set<String> STOP_CODONS = Collections.unmodifiableSet(
        new HashSet<String>() {{
            add("TAA");
            add("TAG");
            add("TGA");
        }}
    );

    private ProteinGenerator(){}

    /**
     *
     */
    public static String nucleotideToAminoAcidSequence(String nucleotideSequence)
        throws UnknownCodonException
    {
        int length = nucleotideSequence.length();
        StringBuilder aminoAcidSequence = new StringBuilder();
        String codon;
        int codonCount = 0;
        for (int i=0; i < length; i+=3)
        {
            codon = nucleotideSequence.substring(i, i+3).toUpperCase();
            codonCount++;
            if (STOP_CODONS.contains(codon))
            {
                continue;
            }
            if (!CODONS.containsKey(codon))
            {
                //throw new UnknownCodonException(codon + " is not a known codon (at codon "+codonCount+")");
System.out.println("Junk codon "+codon+" ("+codonCount+")");
                continue;
            }
            aminoAcidSequence.append(CODONS.get(codon));
        }
        return aminoAcidSequence.toString();
    }

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

    public static void generateProteinsFile(File genomeFile, List<ProteinLocation> locations, String outFilename)
        throws IOException, FileNotFoundException, UnknownCodonException
    {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        Collections.sort(locations, new ProteinLocationComparator());
        try {
            reader = new BufferedReader(new FileReader(genomeFile));
            writer = new BufferedWriter(new FileWriter(outFilename));

            // Skip header
            reader.readLine();
            String line = null;
            int readCursor = 0;
            for (ProteinLocation location : locations)
            {
System.err.println("SMC location start="+location.getStartIndex()+", length="+location.getLength()+", direction="+location.getDirection());
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

System.out.println(sequence);
                if (location.getDirection().equals(ProteinLocation.BACKWARD))
                {
                    System.out.println("BACKWARD:");
                    System.out.println(nucleotideToAminoAcidSequence(sequence.reverse().toString()));
                }
                else
                {
                    System.out.println("FORWARD:");
                    System.out.println(nucleotideToAminoAcidSequence(sequence.toString()));
                }
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

    public static void main(String[] args)
    {
        Option splitIntervalOpt =
            OptionBuilder.withArgName("splitInterval")
                         .hasArg()
                         .withDescription("Size of the intervals into which the genome will be split. Can't be used with the -g option.")
                         .create("i");
        Option glimmerFileOpt =
            OptionBuilder.withArgName("glimmerFile")
                         .hasArg()
                         .withDescription("Glimmer txt file. Can't be used with the -i option.")
                         .create("g");
        Option genomeFileOpt =
            OptionBuilder.withArgName("genomeFile")
                         .hasArg()
                         .withDescription("Genome file in FASTA format")
                         .isRequired()
                         .create("f");
        Option databaseNameOpt =
            OptionBuilder.withArgName("databaseName")
                         .hasArg()
                         .withDescription("Database name")
                         .isRequired()
                         .create("d");
        Option outputFileOpt =
            OptionBuilder.withArgName("outputFile")
                         .hasArg()
                         .withDescription("Filename to write the FASTA format file to")
                         .isRequired()
                         .create("o");
        Options options = new Options();
        options.addOption(splitIntervalOpt);
        options.addOption(glimmerFileOpt);
        options.addOption(genomeFileOpt);
        options.addOption(databaseNameOpt);
        options.addOption(outputFileOpt);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse( options, args );
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
        }
        catch (ParseException pe)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("samifier", options, true);
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}

