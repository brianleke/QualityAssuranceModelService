package com.qamam;

import java.util.ArrayList;

public class Assessments {

    private String dateAssessed;
    private String portfolio;
    private ArrayList<Assessment> assessments;

    public Assessments(String dateAssessed, String portfolio, ArrayList<Assessment> assessments) {
        this.dateAssessed = dateAssessed;
        this.portfolio = portfolio;
        this.assessments = assessments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assessments that = (Assessments) o;

        if (dateAssessed != null ? !dateAssessed.equals(that.dateAssessed) : that.dateAssessed != null) return false;
        if (portfolio != null ? !portfolio.equals(that.portfolio) : that.portfolio != null) return false;
        return assessments != null ? assessments.equals(that.assessments) : that.assessments == null;
    }

    @Override
    public int hashCode() {
        int result = dateAssessed != null ? dateAssessed.hashCode() : 0;
        result = 31 * result + (portfolio != null ? portfolio.hashCode() : 0);
        result = 31 * result + (assessments != null ? assessments.hashCode() : 0);
        return result;
    }
}
