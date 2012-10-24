package au.org.intersect.protein_generator.domain;

public class GffOutputterGenerator implements ProteinLocationBasedOutputterGenerator
{
    private String genomeFilename;

    public GffOutputterGenerator(String genomeFilename)
    {
        this.genomeFilename = genomeFilename;
    }

    @Override
    public GffOutputter getOutputterFor(ProteinLocation proteinLocation)
    {
        return new GffOutputter(proteinLocation, genomeFilename);
    }
}
