package net.pedroricardo.commander.commands;

public class CommandParameterTypes {
    public static final CommanderCommandParameterType FLOAT_COORDINATES = new FloatCoordinatesParameterType();
    public static final CommanderCommandParameterType INTEGER_COORDINATES = new IntegerCoordinatesParameterType();
//    INTEGER("int", 1),
//    FLOAT("float", 1),
//    DOUBLE("double", 1),
//    INTEGER_COORDINATES("int_coordinates", 3),
//    FLOAT_COORDINATES("float_coordinates", 3),
//    DOUBLE_COORDINATES("double_coordinates", 3),
//    STRING("string", 1),
//    ENTITY("entity", 1),
//    PLAYER("player", 1),
//    JSON("json", 1);

    private final String id;
    private final int numberOfExpectedParameters;

    CommandParameterTypes(String id, int numberOfExpectedParameters) {
        this.id = id;
        this.numberOfExpectedParameters = numberOfExpectedParameters;
    }

    public String getId() {
        return this.id;
    }

    public int getNumberOfExpectedParameters() {
        return this.numberOfExpectedParameters;
    }
}
