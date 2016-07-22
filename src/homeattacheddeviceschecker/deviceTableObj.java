package homeattacheddeviceschecker;

/**
 *
 * @author NardusG
 */
class deviceTableObj {

    public String[] number;
    public String[] name;
    public String[] ip;
    public int len;
    
    public deviceTableObj(int length) {
        
        number = new String[length];
        name = new String[length];
        ip = new String[length];
        
        len = length;
        
    }

    
    
}
