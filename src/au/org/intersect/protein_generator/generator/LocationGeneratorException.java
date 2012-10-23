package au.org.intersect.protein_generator.generator;

/**
 * Exception thrown when error generating protein locations
 */
public class LocationGeneratorException extends Exception
{
    public LocationGeneratorException(String message, Exception e)
    {
        super(message, e);
    }

    public LocationGeneratorException(String message)
    {
        super(message);
    }

}
