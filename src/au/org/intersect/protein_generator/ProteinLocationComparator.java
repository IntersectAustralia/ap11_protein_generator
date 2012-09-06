package au.org.intersect.protein_generator;

import java.util.Comparator;

public class ProteinLocationComparator implements Comparator<ProteinLocation>
{
    public int compare(ProteinLocation a, ProteinLocation b)
    {
        if (a.getStartIndex() > b.getStartIndex())
        {
            return 1;
        }
        else if (a.getStartIndex() < b.getStartIndex())
        {
            return -1;
        }
        return 0;
    }
}
