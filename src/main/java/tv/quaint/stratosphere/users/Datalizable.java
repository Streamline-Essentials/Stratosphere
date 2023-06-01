package tv.quaint.stratosphere.users;

public interface Datalizable {
    public String toData();

    public static String interpret(Object rawData) {
        return rawData.toString();
    }

    public static String interpret(Datalizable datalizable) {
        return datalizable.toData();
    }
}
