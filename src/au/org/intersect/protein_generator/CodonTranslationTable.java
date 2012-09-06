package au.org.intersect.protein_generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class CodonTranslationTable
{
    private Map<String, String> codonMap;
    private Map<String, String> startCodonMap;
    private Set<String> stopCodons;

    private CodonTranslationTable(){}

    public static CodonTranslationTable parseTableFile(File f)
        throws IOException, FileNotFoundException
    {
        CodonTranslationTable codonTable = new CodonTranslationTable();
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(f));

        String[] aminoAcids;
        String[] startAminoAcids;
        String[] base1, base2, base3;
        
        String line = null;
        line = reader.readLine();

        while (!line.startsWith("\\s*AAs"))
        {
            line = reader.readLine();
        }

        aminoAcids = parseLine(StringUtils.chomp(reader.readLine()));
        startAminoAcids = parseLine(StringUtils.chomp(reader.readLine()));
        base1 = parseLine(StringUtils.chomp(reader.readLine()));
        base2 = parseLine(StringUtils.chomp(reader.readLine()));
        base3 = parseLine(StringUtils.chomp(reader.readLine()));

        codonTable.codonMap = new HashMap<String, String>();
        codonTable.startCodonMap = new HashMap<String, String>();
        codonTable.stopCodons = new HashSet<String>();

        int length = aminoAcids.length;
        for (int i=0; i < length; i++)
        {
            String aminoAcid = aminoAcids[i].toUpperCase();
            String codon = base1[i].toUpperCase()
                         + base2[i].toUpperCase()
                         + base3[i].toUpperCase();
            if (aminoAcid.equals("*"))
            {
                codonTable.stopCodons.add(codon);
                continue;
            }
            String startAminoAcid = startAminoAcids[i];

            codonTable.codonMap.put(codon, aminoAcid);
            if (startAminoAcid.matches("^[A-Z]$"))
            {
                codonTable.startCodonMap.put(codon, startAminoAcid);
            }
        }

        return codonTable;
    }

    public String toAminoAcid(String codon)
    {
        return null;
    }

    public String toStartAminoAcid(String codon)
    {
        return null;
    }

    public String proteinToAminoAcidSequence(String nucleotideSequence)
    {
        return null;
    }

    private static String[] parseLine(String line)
    {
        String[] parts = line.split("\\s*=\\s*");
        return parts[1].split("");
    }
}
