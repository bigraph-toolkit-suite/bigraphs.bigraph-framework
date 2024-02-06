package GCF.original;

/**
 * Interface to define the create-and close-methods and the printData-method for 
 * the standard values.
 */
public interface GXLStandardValueMethods {
    
    public Object createLocator();
    public Object createSeq();
    public Object createSet();
    public Object createBag();
    public Object createTup();
    public Object createBool();
    public Object createString();
    public Object createInt();
    public Object createFloat();
    public Object createEnum();
    
    public void closeLocator();
    public void closeSeq();
    public void closeSet();
    public void closeBag();
    public void closeTup();
    public void closeBool();
    public void closeString();
    public void closeInt();
    public void closeFloat();
    public void closeEnum();
}

