package au.org.intersect.protein_generator;

public class ProteinLocation
{
    public static final String FORWARD = "+";
    public static final String REVERSE = "-";

    private String name;
    private int startIndex;
    private int length;
    private String direction;

    public ProteinLocation(String name, int startIndex, int length, String direction)
    {
        this.name = name;
        this.startIndex = startIndex;
        this.length = length;
        this.direction = direction;
    }

    public String getName()
    {
        return name;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getLength()
    {
        return length;
    }

    public String getDirection()
    {
        return direction;
    }

    public String toString()
    {
        return name+", startIndex="+startIndex+", length="+length+", direction="+direction;
    }

}
