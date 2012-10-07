package au.org.intersect.protein_generator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * * Tests {@link ProteinGenerator}
 * */
public final class ProteinGeneratorUnitTest
{

    @Test
    public void testParsingGlimmerFile()
    {
        File f = new File("test/resources/test_glimmer.txt");
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
    public void testGenerateProteinsFileFromGlimmerInput()
    {
        try {
            File genomeFile = new File("test/resources/test_genome.faa");
            File glimmerFile = new File("test/resources/test_glimmer.txt");
            File tableFile = new File("test/resources/bacterial_translation_table.txt");
            List<ProteinLocation> locations = ProteinGenerator.parseGlimmerFile(glimmerFile);
            CodonTranslationTable translationTable = CodonTranslationTable.parseTableFile(tableFile);
            StringWriter out = new StringWriter();
            ProteinGenerator.generateProteinsFile("testdb", genomeFile, locations, translationTable, out);
            List<String> expectedLines = FileUtils.readLines(new File("test/resources/test_protein_file.fa"));

            assertEquals("Should produce a FASTA file of amino acid sequences", expectedLines.toArray(new String[0]), out.toString().split(System.getProperty("line.separator")));
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateLocations()
    {
        try {
            File genomeFile = new File("test/resources/test_genome_short.faa");
            List<ProteinLocation> locations = ProteinGenerator.createLocations(genomeFile, 20);
            for (ProteinLocation loc : locations)
            {
                System.out.println(loc);
            }
            assertEquals("Should generate 30 locations", 30, locations.size());
            int forward = 0;
            int forwardHalf = 0;
            int reverse = 0;
            int reverseHalf = 0;
            for (ProteinLocation loc : locations)
            {
                if (loc.getDirection().equals(ProteinLocation.FORWARD))
                {
                    forward++;
                    if (loc.getName().matches("p\\d+b\\.\\d+W"))
                    {
                        forwardHalf++;
                    }
                }
                else
                {
                    reverse++;
                    if (loc.getName().matches("p\\d+b\\.\\d+C"))
                    {
                        reverseHalf++;
                    }
                }
            }
            assertEquals("Should have 15 forward locations", 15, forward);
            assertEquals("Should have 6 forward half interval locations", 6, forwardHalf);
            assertEquals("Should have 15 reverse locations", 15, reverse);
            assertEquals("Should have 6 reverse half interval locations", 6, reverseHalf);
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateVirtualProteins()
    {
        try {
            File genomeFile = new File("test/resources/test_genome_short.faa");
            File tableFile = new File("test/resources/bacterial_translation_table.txt");
            CodonTranslationTable translationTable = CodonTranslationTable.parseTableFile(tableFile);
            StringWriter out = new StringWriter();
            //java.io.FileWriter out = new java.io.FileWriter(new File("/tmp/virtual_proteins.fa"));
            List<ProteinLocation> locations = ProteinGenerator.createLocations(genomeFile, 20);
            ProteinGenerator.generateProteinsFile("testdb", genomeFile, locations, translationTable, out);


            List<String> expectedLines = FileUtils.readLines(new File("test/resources/test_virtual_protein_file.fa"));
            String [] outputAsArray = out.toString().split(System.getProperty("line.separator"));
            assertEquals("Should produce a FASTA file of amino acid sequences", expectedLines.toArray(new String[0]), outputAsArray);
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
