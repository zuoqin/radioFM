package fm100.co.il.models;


public class ScheduleItem {
    private String program = "" ;
    private String ProgramName = "" ;
    private String ProgramDesc = "" ;
    private String ProgramAutor = "" ;
    private String ProgramDay = "" ;
    private String ProgramStartHoure = "" ;

    public ScheduleItem() {
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProgramDay() {
        return ProgramDay;
    }

    public void setProgramDay(String programDay) {
        ProgramDay = programDay;
    }

    public String getProgramDesc() {
        return ProgramDesc;
    }

    public void setProgramDesc(String programDes) {
        ProgramDesc = programDes;
    }

    public String getProgramName() {
        return ProgramName;
    }

    public void setProgramName(String programName) {
        ProgramName = programName;
    }

    public String getProgramAutor() {
        return ProgramAutor;
    }

    public void setProgramAutor(String ProgramAuto) {
        ProgramAutor = ProgramAuto;
    }

    public String getProgramStartHoure() {
        return ProgramStartHoure;
    }

    public void setProgramStartHoure(String programStartHoure) {
        ProgramStartHoure = programStartHoure;
    }
}
