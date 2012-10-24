package au.org.intersect.protein_generator.runner;

import au.org.intersect.protein_generator.domain.*;
import au.org.intersect.protein_generator.generator.CodonsPerIntervalLocationGenerator;
import au.org.intersect.protein_generator.generator.GlimmerFileLocationGenerator;
import au.org.intersect.protein_generator.generator.LocationGenerator;
import au.org.intersect.protein_generator.util.ProteingLocationFileGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

public class ProteinGeneratorRunner
{

    public static final int BASES_PER_CODON = 3;

    private String glimmerFilePath;
    private File genomeFile;
    private String interval;
    private String databaseName;
    private Writer outputWriter;
    private File translationTableFile;
    private Writer gffWriter;

    public ProteinGeneratorRunner(String glimmerFilePath, File genomeFile, String interval, String databaseName, Writer outputWriter, File translationTableFile, Writer gffWriter)
    {
        this.glimmerFilePath = glimmerFilePath;
        this.genomeFile = genomeFile;
        this.interval = interval;
        this.databaseName = databaseName;
        this.outputWriter = outputWriter;
        this.translationTableFile = translationTableFile;
        this.gffWriter = gffWriter;
    }

    public void run() throws Exception
    {
        LocationGenerator locationGenerator = createLocationGenerator();
        List<ProteinLocation> locations = locationGenerator.generateLocations();
        generateProteinsFile(databaseName, genomeFile, locations, CodonTranslationTable.parseTableFile(translationTableFile), outputWriter);
        generateGffFile(locations);
    }

    private LocationGenerator createLocationGenerator()
    {
        LocationGenerator locationGenerator;
        if (glimmerFilePath != null)
        {
            locationGenerator = new GlimmerFileLocationGenerator(glimmerFilePath);
        }
        else
        {
            locationGenerator = new CodonsPerIntervalLocationGenerator(interval, genomeFile);
        }
        return locationGenerator;
    }

    private void generateGffFile(List<ProteinLocation> locations) throws IOException
    {
        String genomeFileName = genomeFile.getName();
        GffOutputterGenerator outputterGenerator = new GffOutputterGenerator(genomeFileName);
        ProteingLocationFileGenerator.generateFile(locations, gffWriter, outputterGenerator);
    }

    public void generateProteinsFile(String databaseName, File genomeFile, List<ProteinLocation> locations, CodonTranslationTable table, Writer output)
            throws IOException, UnknownCodonException
    {
        StringBuilder genomeString = readGenomeFile(genomeFile);
        ProteinOutputterGenerator outputterGenerator = new ProteinOutputterGenerator(databaseName, genomeString, table);
        ProteingLocationFileGenerator.generateFile(locations, outputWriter, outputterGenerator);
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



}
