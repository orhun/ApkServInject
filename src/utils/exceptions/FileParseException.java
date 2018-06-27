package utils.exceptions;

import java.text.ParseException;

public class FileParseException extends ParseException {
    public FileParseException(String message){
        super(message, 0);
    }
}
