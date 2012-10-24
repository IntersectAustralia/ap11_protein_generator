package au.org.intersect.protein_generator.domain;

public interface Outputter
{
    public String getOutput() throws OutputException;
}
