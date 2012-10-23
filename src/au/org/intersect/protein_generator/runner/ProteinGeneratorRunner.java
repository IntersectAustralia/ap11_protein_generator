package au.org.intersect.protein_generator.runner;

import au.org.intersect.protein_generator.domain.CodonTranslationTable;
import au.org.intersect.protein_generator.domain.ProteinLocation;
import au.org.intersect.protein_generator.domain.UnknownCodonException;
import au.org.intersect.protein_generator.generator.CodonsPerIntervalLocationGenerator;
import au.org.intersect.protein_generator.generator.GlimmerFileLocationGenerator;
import au.org.intersect.protein_generator.generator.LocationGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

public class ProteinGeneratorRunner
{

    public static final int BASES_PER_CODON = 3;
    public static final int FASTA_LINE_LENGTH = 60;

    private String glimmerFilePath;
    private File genomeFile;
    private String interval;
    private String databaseName;
    private Writer outputWriter;
    private File translationTableFile;

    public ProteinGeneratorRunner(String glimmerFilePath, File genomeFile, String interval, String databaseName, Writer outputWriter, File translationTableFile)
    {
        this.glimmerFilePath = glimmerFilePath;
        this.genomeFile = genomeFile;
        this.interval = interval;
        this.databaseName = databaseName;
        this.outputWriter = outputWriter;
        this.translationTableFile = translationTableFile;
    }

    public void run() throws Exception
    {
        LocationGenerator locationGenerator = null;
        if (glimmerFilePath != null)
        {
            locationGenerator = new GlimmerFileLocationGenerator(glimmerFilePath);
        }
        else
        {
            locationGenerator = new CodonsPerIntervalLocationGenerator(interval, genomeFile);
        }

        List<ProteinLocation> locations = locationGenerator.generateLocations();
        generateProteinsFile(databaseName, genomeFile, locations, CodonTranslationTable.parseTableFile(translationTableFile), outputWriter);

    }


    public void generateProteinsFile(String databaseName, File genomeFile, List<ProteinLocation> locations, CodonTranslationTable table, Writer output)
            throws IOException, FileNotFoundException, UnknownCodonException
    {
        BufferedWriter writer = null;
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

    private StringBuilder readGenomeFile(File genomeFile)
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

    public static String invertNucleotideSequence(String sequence)
    {
        return StringUtils.replaceChars(sequence, "ACGT", "TGCA");
    }


    private static String fastaHeader(String db, String name)
    {
        return ">gn1|"+db+"|"+name;
    }

}
