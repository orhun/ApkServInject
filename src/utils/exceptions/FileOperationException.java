package utils.exceptions;

import java.io.IOException;

public class FileOperationException extends IOException {
    public FileOperationException(String message){
        super(message);
    }
}
