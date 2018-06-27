package utils.exceptions;

import java.io.FileNotFoundException;

public class SmaliNotFoundException extends FileNotFoundException {
    public SmaliNotFoundException(String message){
        super(message);
    }
}
