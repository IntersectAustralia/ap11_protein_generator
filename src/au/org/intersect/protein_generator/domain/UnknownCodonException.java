package au.org.intersect.protein_generator.domain;

public class UnknownCodonException extends OutputException
{
    public UnknownCodonException(String message)
    {
        super(message);
    }
}
