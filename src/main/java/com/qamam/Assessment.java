package com.qamam;

public class Assessment {

    private String teamName;
    private String testing;
    private String testMetrics;
    private String qualityAlignment;
    private String practiceInnovation;
    private String toolsArtefacts;
    private String rawData;
    private String recommendedCapabilities;
    private String capabilitiesToStop;

    public Assessment() {
    }

    public void setToolsArtefacts(String toolsArtefacts) {
        this.toolsArtefacts = toolsArtefacts;
    }

    public void setPracticeInnovation(String practiceInnovation) {
        this.practiceInnovation = practiceInnovation;
    }

    public void setQualityAlignment(String qualityAlignment) {
        this.qualityAlignment = qualityAlignment;
    }

    public void setTestMetrics(String testMetrics) {
        this.testMetrics = testMetrics;
    }

    public void setTesting(String testing) {
        this.testing = testing;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setRecommendedCapabilities(String recommendedCapabilities){ this.recommendedCapabilities = recommendedCapabilities;}

    public void setCapabilitiesToStop(String capabilitiesToStop){this.capabilitiesToStop = capabilitiesToStop;}

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public double removeValueFromAssessment(String attribute, double value){
        if(attribute.equals("Testing")){
            return value - Double.parseDouble(this.testing);
        }

        if(attribute.equals("TestMetrics")){
            return value - Double.parseDouble(this.testMetrics);
        }

        if(attribute.equals("QualityAlignment")){
            return value - Double.parseDouble(this.qualityAlignment);
        }

        if(attribute.equals("PracticeInnovation")){
            return value - Double.parseDouble(this.practiceInnovation);
        }

        if(attribute.equals("ToolsArtefacts")){
            return value - Double.parseDouble(this.toolsArtefacts);
        }

        return 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assessment that = (Assessment) o;

        if (teamName != null ? !teamName.equals(that.teamName) : that.teamName != null) return false;
        if (testing != null ? !testing.equals(that.testing) : that.testing != null) return false;
        if (testMetrics != null ? !testMetrics.equals(that.testMetrics) : that.testMetrics != null) return false;
        if (qualityAlignment != null ? !qualityAlignment.equals(that.qualityAlignment) : that.qualityAlignment != null) return false;
        if (practiceInnovation != null ? !practiceInnovation.equals(that.practiceInnovation) : that.practiceInnovation != null) return false;
        if (toolsArtefacts != null ? !toolsArtefacts.equals(that.toolsArtefacts) : that.toolsArtefacts != null) return false;
        return rawData != null ? rawData.equals(that.rawData) : that.rawData == null;
    }

    @Override
    public int hashCode() {
        int result = teamName != null ? teamName.hashCode() : 0;
        result = 31 * result + (testing != null ? testing.hashCode() : 0);
        result = 31 * result + (testMetrics != null ? testMetrics.hashCode() : 0);
        result = 31 * result + (qualityAlignment != null ? qualityAlignment.hashCode() : 0);
        result = 31 * result + (practiceInnovation != null ? practiceInnovation.hashCode() : 0);
        result = 31 * result + (toolsArtefacts != null ? toolsArtefacts.hashCode() : 0);
        result = 31 * result + (rawData != null ? rawData.hashCode() : 0);
        return result;
    }
}
