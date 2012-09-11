package au.org.intersect.protein_generator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * * Tests {@link Samifier}
 * */
public final class ProteinGeneratorUnitTest
{

    @Test
    public void testParsingGlimmerFile()
    {
        File f = new File(getClass().getResource("/test_glimmer.txt").getFile());
        List<ProteinLocation> proteins = null;
        try {
            proteins = ProteinGenerator.parseGlimmerFile(f);
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
        assertEquals("Should have 13 protein locations", 13, proteins.size());
        ProteinLocation location1 = proteins.get(0);
    }

    @Test
    public void testInvertNucleotideSequence()
    {
        assertEquals("Should convert a nucleotide sequence to its complement nucleotides", "GATTACA", ProteinGenerator.invertNucleotideSequence("CTAATGT"));
    }

    @Test
    public void testGenerateProteinsFile()
    {
        try {
            File genomeFile = new File(getClass().getResource("/test_genome.faa").getFile());
            File glimmerFile = new File(getClass().getResource("/test_glimmer.txt").getFile());
            File tableFile = new File(getClass().getResource("/bacterial_translation_table.txt").getFile());
            List<ProteinLocation> locations = ProteinGenerator.parseGlimmerFile(glimmerFile);
            CodonTranslationTable translationTable = CodonTranslationTable.parseTableFile(tableFile);
            StringWriter out = new StringWriter();
            ProteinGenerator.generateProteinsFile("testdb", genomeFile, locations, translationTable, out);
            List<String> expectedLines = FileUtils.readLines(new File(getClass().getResource("/test_protein_file.fa").getFile()));

            assertEquals("Should produce a FASTA file of amino acid sequences", expectedLines.toArray(new String[0]), out.toString().split(System.getProperty("line.separator")));
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO
    public void testCreateLocations()
    {
        try {
            File genomeFile = new File(getClass().getResource("/test_genome_short.faa").getFile());
            List<ProteinLocation> locations = ProteinGenerator.createLocations(genomeFile, 20);
            for (ProteinLocation location : locations)
            {
                System.out.println(location);
            }
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
