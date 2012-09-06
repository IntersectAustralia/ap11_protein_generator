package au.org.intersect.protein_generator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import java.io.File;
//import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

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
        assertEquals("Should have 10 protein locations", 10, proteins.size());
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
            File f = new File(getClass().getResource("/test_glimmer.txt").getFile());
            List<ProteinLocation> locations = ProteinGenerator.parseGlimmerFile(f);
            File genomeFile = new File(getClass().getResource("/test_genome.faa").getFile());
            ProteinGenerator.generateProteinsFile(genomeFile, locations, "out.fa");
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
