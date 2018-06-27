package utils.exceptions;

import java.io.FileNotFoundException;

public class ApkNotFoundException extends FileNotFoundException {
    public ApkNotFoundException(String message){
        super(message);
    }
}
