package utils.exceptions;

public class EpNotFoundException extends NoSuchMethodException {
    public EpNotFoundException(String message){
        super(message);
    }
}
