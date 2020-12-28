package client;

import com.beust.jcommander.Parameter;
import com.google.gson.JsonElement;

public class MainCLIParameters {
    // -in
    // -t is the type of the request
    // -i is the index of the cell.
    // -m is the value to save in the database:
    // you only need it in case of a set request.

    @Parameter(names = {"-in"}, description = "-in filename with request")
    private String fileWithRequest;

    @Parameter(names = {"-t"}, description = "-t is the type of the request")
    private String requestType;

    @Parameter(names = {"-k"}, description = "-k is the key")
    private String key;

    @Parameter(names = {"-v"}, description = "-v is the value to save in the database", variableArity = true)
    private String value;

    @Parameter(names = {"-h", "--help"}, description = "Displays help information", help = true)
    private boolean help;

    public boolean isHelp() {
        return help;
    }

    public String getFileWithRequest() {
        return fileWithRequest == null ? "" : fileWithRequest;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("C File with request: \"%s\", request Type: \"%s\", key: \"%s\", cellValue: \"%s\"",
                getFileWithRequest(), getRequestType(), getKey(), getValue());
    }
}
