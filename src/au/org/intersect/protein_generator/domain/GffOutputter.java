package au.org.intersect.protein_generator.domain;

public class GffOutputter
{
    public String genomeFileName;
    public Integer start;
    public Integer end;
    private String glimmerScore;
    private String directionFlag;
    private String frame;
    private String glimmerName;

    public GffOutputter(ProteinLocation location, String genomeFileNameWithExtension)
    {
        this.genomeFileName =  genomeFileNameNoExtension(genomeFileNameWithExtension);
        this.start = location.getStartIndex();
        this.end = location.getStop();
        if (location.getConfidenceScore() != null)
        {
            this.glimmerScore = location.getConfidenceScore().toString();
        }
        else
        {
            this.glimmerScore = "";
        }
        this.directionFlag = location.getDirection();
        this.frame = location.getFrame();
        this.glimmerName = location.getName();
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();

        output.append(genomeFileName);
        output.append(" Glimmer gene ");
        output.append(" " + start);
        output.append(" " + end);
        output.append(" " + glimmerScore);
        output.append(" " + directionFlag);
        output.append(" " + frame);
        output.append(" ID=" + glimmerName + ";Name=" + glimmerName + ";Note=");
        output.append(System.getProperty("line.separator"));

        output.append(genomeFileName);
        output.append(" Glimmer CDS ");
        output.append(" " + start);
        output.append(" " + end);
        output.append(" " + glimmerScore);
        output.append(" " + directionFlag);
        output.append(" " + frame);
        output.append(" ID=" + glimmerName + ";Name=" + glimmerName + ";Note=");
        output.append(System.getProperty("line.separator"));

        return output.toString();

    }

    private String genomeFileNameNoExtension(String genomeFileName)
    {
        int indexOfDot = genomeFileName.lastIndexOf('.');
        if (indexOfDot > 0)
        {
            return genomeFileName.substring(0, indexOfDot);
        }
        return genomeFileName;
    }
}
