package utils.exceptions;

import java.io.FileNotFoundException;

public class ManifestNotFoundException extends FileNotFoundException {
    public ManifestNotFoundException(String message){
        super(message);
    }
}
