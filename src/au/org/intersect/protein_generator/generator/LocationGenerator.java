package au.org.intersect.protein_generator.generator;

import au.org.intersect.protein_generator.domain.ProteinLocation;

import java.util.List;

public interface LocationGenerator
{
    /**
     * returns a list of the protein locations
     * @return a list of the protein locations
     */
    public List<ProteinLocation> generateLocations() throws LocationGeneratorException;
}
