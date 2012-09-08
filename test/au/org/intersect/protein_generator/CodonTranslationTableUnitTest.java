package au.org.intersect.protein_generator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import java.io.File;

public class CodonTranslationTableUnitTest
{

    @Test
    public void testParseTableFile()
    {
        try {
            File f = new File(getClass().getResource("/standard_code_translation_table.txt").getFile());
            CodonTranslationTable table = CodonTranslationTable.parseTableFile(f);
            assertEquals("ATT codes for Isoleucine", "I", table.toAminoAcid("ATT"));
            assertEquals("Should have 61 codons", 61, table.getCodons().length);
            assertEquals("Should have 3 start codons", 3, table.getStartCodons().length);
            assertEquals("Should have 3 stop codons", 3, table.getStopCodons().length);
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
/*
    @Test
    public void testNucleotideToAminoAcidSequence()
    {
        try {
            // Isoleucine
            assertEquals("3 codons code for Isoleucine", "III", ProteinGenerator.nucleotideToAminoAcidSequence("ATTATCATA"));

            // Leucine
            assertEquals("6 codons code for Leucine", "LLLLLL", ProteinGenerator.nucleotideToAminoAcidSequence("CTTCTCCTACTGTTATTG"));

            // Valine
            assertEquals("4 codons code for Valine", "VVVV", ProteinGenerator.nucleotideToAminoAcidSequence("GTTGTCGTAGTG"));

            // Phenylalanine
            assertEquals("2 codons code for Phenylalanine", "FF", ProteinGenerator.nucleotideToAminoAcidSequence("TTTTTC"));

            // Methionine
            assertEquals("1 codon codes for Methionine", "M", ProteinGenerator.nucleotideToAminoAcidSequence("ATG"));

            // Cysteine
            assertEquals("2 codons code for Cysteine", "CC", ProteinGenerator.nucleotideToAminoAcidSequence("TGTTGC"));

            // Arginine
            assertEquals("4 codons code for Arginine", "AAAA", ProteinGenerator.nucleotideToAminoAcidSequence("GCTGCCGCAGCG"));

            // Proline
            assertEquals("4 codons code for Proline", "PPPP", ProteinGenerator.nucleotideToAminoAcidSequence("CCTCCCCCACCG"));

            // Threonine
            assertEquals("4 codons code for Threonine", "TTTT", ProteinGenerator.nucleotideToAminoAcidSequence("ACTACCACAACG"));

            // Serine
            assertEquals("6 codons code for Serine", "SSSSSS", ProteinGenerator.nucleotideToAminoAcidSequence("TCTTCCTCATCGAGTAGC"));

            // Tyrosine
            assertEquals("2 codons code for Tyrosine", "YY", ProteinGenerator.nucleotideToAminoAcidSequence("TATTAC"));

            // Tryptophan
            assertEquals("1 codon codes for Tryptophan", "W", ProteinGenerator.nucleotideToAminoAcidSequence("TGG"));

            // Glutamine
            assertEquals("2 codons code for Glutamine", "QQ", ProteinGenerator.nucleotideToAminoAcidSequence("CAACAG"));

            // Asparagine
            assertEquals("2 codons code for Asparagine", "NN", ProteinGenerator.nucleotideToAminoAcidSequence("AATAAC"));

            // Histidine
            assertEquals("2 codons code for Histidine", "HH", ProteinGenerator.nucleotideToAminoAcidSequence("CATCAC"));

            // Glutamic acid
            assertEquals("2 codons code for Glutamic acid", "EE", ProteinGenerator.nucleotideToAminoAcidSequence("GAAGAG"));

            // Aspartic acid 
            assertEquals("2 codons code for Aspartic acid", "DD", ProteinGenerator.nucleotideToAminoAcidSequence("GATGAC"));

            // Lysine
            assertEquals("2 codons code for Lysine", "KK", ProteinGenerator.nucleotideToAminoAcidSequence("AAAAAG"));

            // Arginine
            assertEquals("6 codons code for Arginine", "RRRRRR", ProteinGenerator.nucleotideToAminoAcidSequence("CGTCGCCGACGGAGAAGG"));
        }
        catch(Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/
}
