package au.org.intersect.protein_generator.generator;

import au.org.intersect.protein_generator.domain.ProteinLocation;
import au.org.intersect.protein_generator.domain.ProteinLocationComparator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlimmerFileLocationGenerator implements LocationGenerator
{

    private String glimmerFilePath;

    public GlimmerFileLocationGenerator(String glimmerFilePath)
    {
        this.glimmerFilePath = glimmerFilePath;
    }

    @Override
    public List<ProteinLocation> generateLocations() throws LocationGeneratorException
    {
        File glimmerFile = new File(glimmerFilePath);
        List<ProteinLocation> locations = null;
        try
        {
            locations = parseGlimmerFile(glimmerFile);
        }
        catch (IOException e)
        {
            throw new LocationGeneratorException("Error with file " + glimmerFilePath, e);
        }

        Collections.sort(locations, new ProteinLocationComparator());
        return locations;
    }

    public List<ProteinLocation> parseGlimmerFile(File glimmerFile)
            throws LocationGeneratorException, IOException
    {
        int lineCount = 0;
        List<ProteinLocation> proteinLocations = new ArrayList<ProteinLocation>();
        for(String line : FileUtils.readLines(glimmerFile))
        {
            lineCount++;
            if (line.startsWith(">"))
            {
                continue;
            }
            String[] columns = line.split("\\s+");
            if (columns.length < 5)
            {
                throw new LocationGeneratorException("Expecting 5 columns at line: "+lineCount);
            }

            String name = columns[0];
            int firstIndex = Integer.parseInt(columns[1]);
            int secondIndex = Integer.parseInt(columns[2]);
            String direction = columns[3];

            if (direction.startsWith(ProteinLocation.FORWARD))
            {
                proteinLocations.add(new ProteinLocation(name, firstIndex, secondIndex - firstIndex + 1, ProteinLocation.FORWARD));
            }
            else if (direction.startsWith(ProteinLocation.REVERSE))
            {
                proteinLocations.add(new ProteinLocation(name, secondIndex, firstIndex - secondIndex + 1, ProteinLocation.REVERSE));
            }
            else
            {
                throw new LocationGeneratorException("Unexpected value for direction (4th column) at line: "+lineCount);
            }
        }
        return proteinLocations;
    }


}
