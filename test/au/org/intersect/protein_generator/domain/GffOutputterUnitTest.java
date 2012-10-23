package au.org.intersect.protein_generator.domain;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: diego
 * Date: 24/10/12
 * Time: 8:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class GffOutputterUnitTest
{
    public GffOutputterUnitTest()
    {
    }

    @Test
    public void testGffOutputWithGenomeFileWithoutExtension()
    {
        ProteinLocation proteinLocation = new ProteinLocation("a",1,2,"+");
        GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test");
        String lineFeed = System.getProperty("line.separator");
        String firstLine = gffOutputter.toString().split(lineFeed)[0];
        String firstToken = firstLine.split(" ")[0];
        assertEquals("test", firstToken);
    }

    @Test
    public void testGffOutputWithGenomeFileWithExtension()
    {
        ProteinLocation proteinLocation = new ProteinLocation("a",1,2,"+");
        GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test.txt");
        String lineFeed = System.getProperty("line.separator");
        String firstLine = gffOutputter.toString().split(lineFeed)[0];
        String firstToken = firstLine.split(" ")[0];
        assertEquals("test", firstToken);
    }

    @Test
    public void testGffWholeOutput()
    {
        ProteinLocation proteinLocation = new ProteinLocation("glimmer_name",1,2,"+");
        GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test");
        String lineFeed = System.getProperty("line.separator");

        StringBuffer expectedOutput = new StringBuffer();
        expectedOutput.append("test Glimmer gene  1 3  + 1 ID=glimmer_name;Name=glimmer_name;Note=");
        expectedOutput.append(lineFeed);
        expectedOutput.append("test Glimmer CDS  1 3  + 1 ID=glimmer_name;Name=glimmer_name;Note=");
        expectedOutput.append(lineFeed);
        assertEquals(expectedOutput.toString(), gffOutputter.toString());
    }


}
