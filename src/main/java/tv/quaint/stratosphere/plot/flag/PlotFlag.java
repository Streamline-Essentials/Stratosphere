package tv.quaint.stratosphere.plot.flag;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class PlotFlag implements Comparable<PlotFlag> {
    public static final String CLASSIFIER_SEPARATOR = "!";

    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private String value;
    @Getter @Setter
    private String classifier;

    public <T> PlotFlag(String identifier, T value) {
        this.identifier = identifier;
        Class<T> valueClass = (Class<T>) value.getClass();
        if (valueClass == String.class) {
            this.value = (String) value;
            this.classifier = "S";
        } else if (valueClass == Integer.class) {
            this.value = String.valueOf(value);
            this.classifier = "I";
        } else if (valueClass == Double.class) {
            this.value = String.valueOf(value);
            this.classifier = "D";
        } else if (valueClass == Boolean.class) {
            this.value = String.valueOf(value);
            this.classifier = "B";
        } else if (valueClass == Float.class) {
            this.value = String.valueOf(value);
            this.classifier = "F";
        } else if (valueClass == Long.class) {
            this.value = String.valueOf(value);
            this.classifier = "L";
        } else {
            this.value = value.toString();
            this.classifier = "O";
        }
    }

    public <T> PlotFlag(String identifier, List<T> value) {
        this.identifier = identifier;

        StringBuilder stringBuilder = new StringBuilder();
        for (T t : value) {
            stringBuilder.append(t.toString()).append(",");
        }
        this.value = stringBuilder.toString();
        String classifierNow = "l";

        try {
            T t = (T) "test";
            classifierNow += "S";
        } catch (Exception e) {
            try {
                T t = (T) (Integer) 1;
                classifierNow += "I";
            } catch (Exception e1) {
                try {
                    T t = (T) (Double) 1.0;
                    classifierNow += "D";
                } catch (Exception e2) {
                    try {
                        T t = (T) (Boolean) true;
                        classifierNow += "B";
                    } catch (Exception e3) {
                        try {
                            T t = (T) (Float) 1.0f;
                            classifierNow += "F";
                        } catch (Exception e4) {
                            try {
                                T t = (T) (Long) 1L;
                                classifierNow += "L";
                            } catch (Exception e5) {
                                classifierNow += "O";
                            }
                        }
                    }
                }
            }
        }
    }

    public <T> PlotFlag(String identifier, Set<T> value) {
        this.identifier = identifier;

        StringBuilder stringBuilder = new StringBuilder();
        for (T t : value) {
            stringBuilder.append(t.toString()).append(",");
        }
        this.value = stringBuilder.toString();
        String classifierNow = "l";

        try {
            T t = (T) "test";
            classifierNow += "S";
        } catch (Exception e) {
            try {
                T t = (T) (Integer) 1;
                classifierNow += "I";
            } catch (Exception e1) {
                try {
                    T t = (T) (Double) 1.0;
                    classifierNow += "D";
                } catch (Exception e2) {
                    try {
                        T t = (T) (Boolean) true;
                        classifierNow += "B";
                    } catch (Exception e3) {
                        try {
                            T t = (T) (Float) 1.0f;
                            classifierNow += "F";
                        } catch (Exception e4) {
                            try {
                                T t = (T) (Long) 1L;
                                classifierNow += "L";
                            } catch (Exception e5) {
                                classifierNow += "O";
                            }
                        }
                    }
                }
            }
        }
    }

    public String getFullValue() {
        return this.classifier + CLASSIFIER_SEPARATOR + this.value;
    }

    @Override
    public int compareTo(@NotNull PlotFlag o) {
        return this.identifier.compareTo(o.identifier);
    }
}
