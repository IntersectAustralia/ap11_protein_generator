package au.org.intersect.protein_generator.domain;

public class UnknownCodonException extends Exception
{
    public UnknownCodonException(String message)
    {
        super(message);
    }
}
