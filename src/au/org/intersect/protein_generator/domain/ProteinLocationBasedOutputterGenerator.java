package au.org.intersect.protein_generator.domain;


public interface ProteinLocationBasedOutputterGenerator
{
    public Outputter getOutputterFor(ProteinLocation proteinLocation);
}
